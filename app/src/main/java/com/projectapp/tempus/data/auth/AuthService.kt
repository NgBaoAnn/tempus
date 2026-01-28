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
     */
    suspend fun logout() {
        supabaseClient.auth.signOut()
    }
}