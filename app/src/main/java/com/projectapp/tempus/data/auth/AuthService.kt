package com.projectapp.tempus.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
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
     * Gửi email khôi phục mật khẩu (Reset Password)
     * Thư viện sẽ tự gọi endpoint auth/v1/recover của Supabase
     */
    suspend fun resetPassword(email: String) {
        // Hàm này sẽ gửi một email chứa link/OTP về cho người dùng
        supabaseClient.auth.resetPasswordForEmail(email)
    }

    /**
     * Đăng xuất và xóa Session trong máy
     */
    suspend fun logout() {
        supabaseClient.auth.signOut()
    }
}