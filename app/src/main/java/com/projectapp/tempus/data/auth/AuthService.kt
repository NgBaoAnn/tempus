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

    
    suspend fun register(email: String, password: String, fullName: String) {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            
            data = buildJsonObject {
                put("full_name", fullName)
            }
        }
    }

    
    suspend fun login(email: String, password: String) {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    
    suspend fun signInWithGoogle(idToken: String) {
        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
        }
    }

    
    suspend fun resetPassword(email: String, redirectUrl: String = "com.projectapp.tempus://reset-callback") {
        supabaseClient.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = redirectUrl
        )
    }

    
    suspend fun updatePassword(newPassword: String) {
        val session = supabaseClient.auth.currentSessionOrNull()
        if (session == null) {
             throw IllegalStateException("No active session. OTP verification might have failed.")
        }

        supabaseClient.auth.modifyUser {
            password = newPassword
        }
    }

    
    suspend fun verifyRecoveryOtp(email: String, token: String) {
        supabaseClient.auth.verifyEmailOtp(
            type = io.github.jan.supabase.gotrue.OtpType.Email.RECOVERY,
            email = email,
            token = token
        )
    }

    
    suspend fun logout(
        syncBeforeLogout: Boolean = true,
        context: android.content.Context? = null
    ) {
        
        if (syncBeforeLogout && context != null) {
            
            try {
                val syncManager = com.projectapp.tempus.data.RepositoryProvider.getSyncManager(context)
                val result = syncManager.pushToServer()
                android.util.Log.d("AuthService", "Schedule sync before logout: ${result.getOrNull()?.summary() ?: "failed"}")
            } catch (e: Exception) {
                android.util.Log.e("AuthService", "Schedule sync failed, continuing with logout", e)
            }
            
            
            try {
                val gamificationSyncManager = com.projectapp.tempus.data.RepositoryProvider.getGamificationSyncManager(context)
                val result = gamificationSyncManager.pushToServer()
                android.util.Log.d("AuthService", "Gamification sync before logout: ${result.getOrNull()?.summary() ?: "failed"}")
            } catch (e: Exception) {
                android.util.Log.e("AuthService", "Gamification sync failed, continuing with logout", e)
            }
            
            
            try {
                val notesSyncManager = com.projectapp.tempus.data.RepositoryProvider.getNotesSyncManager(context)
                val result = notesSyncManager.pushToServer()
                android.util.Log.d("AuthService", "Notes sync before logout: ${result.getOrNull()?.summary() ?: "failed"}")
            } catch (e: Exception) {
                android.util.Log.e("AuthService", "Notes sync failed, continuing with logout", e)
            }
            
            
            android.util.Log.d("AuthService", "=== CLEARING LOCAL DATA START ===")
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    
                    android.util.Log.d("AuthService", "Getting LocalRepository...")
                    val localRepo = com.projectapp.tempus.data.RepositoryProvider.getLocalRepository(context)
                    android.util.Log.d("AuthService", "Calling clearAllLocalData()...")
                    localRepo.clearAllLocalData()
                    android.util.Log.d("AuthService", "✓ Cleared Schedule data")
                    
                    
                    android.util.Log.d("AuthService", "Getting GamificationDatabase...")
                    val gamificationDb = com.projectapp.tempus.data.gamification.GamificationDatabase.getDatabase(context)
                    android.util.Log.d("AuthService", "Calling clearAllTables()...")
                    gamificationDb.clearAllTables()
                    android.util.Log.d("AuthService", "✓ Cleared Gamification data")
                    
                    
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
        
        
        com.projectapp.tempus.data.RepositoryProvider.clear()
        
        supabaseClient.auth.signOut()
    }
}