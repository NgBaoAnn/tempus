package com.projectapp.tempus.core.gemini

import com.projectapp.tempus.data.ai.GeminiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton provider for Gemini API client
 * Similar pattern to SupabaseClientProvider
 */
object GeminiClientProvider {
    
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val TIMEOUT_SECONDS = 60L
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
    
    /**
     * Lazy-initialized Gemini service instance
     */
    val service: GeminiService by lazy {
        retrofit.create(GeminiService::class.java)
    }
}
