package com.projectapp.tempus
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

data class SignUpRequest(
    val email: String,
    val password: String,
    val options: SignUpOptions
)

data class SignUpOptions(
    val data: UserMetadata
)

data class UserMetadata(
    val full_name: String
)


data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refresh_token: String
)

data class SignUpResponse(
    val id: String,
    val email: String,
    val confirmation_sent_at: String?
)


data class AuthResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Long,
    val token_type: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val email: String
)

class SupabaseAnonInterceptor(
    private val anonKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .addHeader("Content-Type", "application/json")
            .build()

        return chain.proceed(request)
    }
}


interface SupabaseAuthApi {

    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body body: SignUpRequest
    ): SignUpResponse   // ✅ KHÔNG PHẢI AuthResponse

    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Body body: LoginRequest
    ): AuthResponse

    @POST("auth/v1/token?grant_type=refresh_token")
    suspend fun refreshToken(
        @Body body: RefreshTokenRequest
    ): AuthResponse
}



interface SessionStore {

    fun saveSession(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long
    )

    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clear()
}

class AuthService(
    private val api: SupabaseAuthApi
) {

    suspend fun register(
        email: String,
        password: String,
        fullName: String
    ): SignUpResponse {

        return api.signUp(
            SignUpRequest(
                email = email,
                password = password,
                options = SignUpOptions(
                    data = UserMetadata(full_name = fullName)
                )
            )
        )
    }

    suspend fun login(
        email: String,
        password: String
    ): AuthResponse {
        return api.login(
            LoginRequest(
                email = email,
                password = password
            )
        )
    }
}
