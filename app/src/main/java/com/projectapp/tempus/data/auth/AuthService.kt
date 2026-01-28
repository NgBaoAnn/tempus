package com.projectapp.tempus.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.providers.Google
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthService(
    private val supabaseClient: SupabaseClient
) {

    /**
     * Đăng ký tài khoản mới
     */
    suspend fun register(email: String, password: String, fullName: String) {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            // Lưu họ tên vào metadata của User
            data = buildJsonObject {
                put("full_name", fullName)
            }
        }
    }

    /**
     * Đăng nhập và tự động lưu Session
     */
    suspend fun login(email: String, password: String) {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Đăng nhập với Google sử dụng ID Token
     * @param idToken Google ID Token nhận được từ Credential Manager
     */
    suspend fun signInWithGoogle(idToken: String) {
        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
        }
    }

    /**
     * Gửi email khôi phục mật khẩu (Reset Password)
     * Thư viện sẽ tự gọi endpoint auth/v1/recover của Supabase
     * @param email Email của người dùng
     * @param redirectUrl URL redirect sau khi xác thực (deep link của app)
     */
    suspend fun resetPassword(email: String, redirectUrl: String = "com.projectapp.tempus://reset-callback") {
        supabaseClient.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = redirectUrl
        )
    }

    /**
     * Đặt lại mật khẩu mới sau khi xác thực từ deep link
     * Gọi trực tiếp SDK modifyUser để tận dụng Session hiện tại
     * @param newPassword Mật khẩu mới
     */
    suspend fun updatePassword(newPassword: String) {
        val session = supabaseClient.auth.currentSessionOrNull()
        if (session == null) {
             throw IllegalStateException("No active session. OTP verification might have failed.")
        }

        supabaseClient.auth.modifyUser {
            password = newPassword
        }
    }

    /**
     * Xác thực mã OTP 8 số để khôi phục mật khẩu
     */
    suspend fun verifyRecoveryOtp(email: String, token: String) {
        supabaseClient.auth.verifyEmailOtp(
            type = io.github.jan.supabase.gotrue.OtpType.Email.RECOVERY,
            email = email,
            token = token
        )
    }

    /**
     * Đăng xuất và xóa Session trong máy
     * @param syncBeforeLogout Nếu true, sẽ push pending changes lên server trước khi logout
     * @param context Context để access SyncManager (cần cho auto-sync)
     */
    suspend fun logout(
        syncBeforeLogout: Boolean = true,
        context: android.content.Context? = null
    ) {
        // Auto-sync: Push pending changes before logout
        if (syncBeforeLogout && context != null) {
            // 1. Sync Schedule data
            try {
                val syncManager = com.projectapp.tempus.data.RepositoryProvider.getSyncManager(context)
                val result = syncManager.pushToServer()
                android.util.Log.d("AuthService", "Schedule sync before logout: ${result.getOrNull()?.summary() ?: "failed"}")
            } catch (e: Exception) {
                android.util.Log.e("AuthService", "Schedule sync failed, continuing with logout", e)
            }
            
            // 2. Sync Gamification data (points, trees)
            try {
                val gamificationSyncManager = com.projectapp.tempus.data.RepositoryProvider.getGamificationSyncManager(context)
                val result = gamificationSyncManager.pushToServer()
                android.util.Log.d("AuthService", "Gamification sync before logout: ${result.getOrNull()?.summary() ?: "failed"}")
            } catch (e: Exception) {
                android.util.Log.e("AuthService", "Gamification sync failed, continuing with logout", e)
            }
            
            // 3. Sync Notes data
            try {
                val notesSyncManager = com.projectapp.tempus.data.RepositoryProvider.getNotesSyncManager(context)
                val result = notesSyncManager.pushToServer()
                android.util.Log.d("AuthService", "Notes sync before logout: ${result.getOrNull()?.summary() ?: "failed"}")
            } catch (e: Exception) {
                android.util.Log.e("AuthService", "Notes sync failed, continuing with logout", e)
            }
            
            // 4. Clear local Room data để đảm bảo data isolation giữa các users
            android.util.Log.d("AuthService", "=== CLEARING LOCAL DATA START ===")
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    // Clear Schedule data
                    android.util.Log.d("AuthService", "Getting LocalRepository...")
                    val localRepo = com.projectapp.tempus.data.RepositoryProvider.getLocalRepository(context)
                    android.util.Log.d("AuthService", "Calling clearAllLocalData()...")
                    localRepo.clearAllLocalData()
                    android.util.Log.d("AuthService", "✓ Cleared Schedule data")
                    
                    // Clear Gamification data
                    android.util.Log.d("AuthService", "Getting GamificationDatabase...")
                    val gamificationDb = com.projectapp.tempus.data.gamification.GamificationDatabase.getDatabase(context)
                    android.util.Log.d("AuthService", "Calling clearAllTables()...")
                    gamificationDb.clearAllTables()
                    android.util.Log.d("AuthService", "✓ Cleared Gamification data")
                    
                    // Clear Notes data
                    android.util.Log.d("AuthService", "Getting NotesRepository...")
                    val notesRepo = com.projectapp.tempus.data.RepositoryProvider.getNotesRepository(context)
                    android.util.Log.d("AuthService", "Calling clearAllNotes()...")
                    notesRepo.clearAllNotes()
                    android.util.Log.d("AuthService", "✓ Cleared Notes data")
                }
                android.util.Log.d("AuthService", "=== CLEARING LOCAL DATA COMPLETE ===")
            } catch (e: Exception) {
                android.util.Log.e("AuthService", "=== CLEARING LOCAL DATA FAILED ===")
                android.util.Log.e("AuthService", "Error: ${e.message}")
                e.printStackTrace()
            }
        }
        
        // Clear local repository cache in memory
        com.projectapp.tempus.data.RepositoryProvider.clear()
        
        supabaseClient.auth.signOut()
    }
}