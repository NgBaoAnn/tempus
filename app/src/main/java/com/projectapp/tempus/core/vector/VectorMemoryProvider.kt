package com.projectapp.tempus.core.vector

import com.projectapp.tempus.BuildConfig
import com.projectapp.tempus.data.ai.vector.VectorMemoryApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Provider for Vector Memory API client
 * Configures Retrofit with appropriate timeouts for AI operations
 */
object VectorMemoryProvider {
    
    // TODO: Update with actual backend URL
    private const val BASE_URL = "http://localhost:8000/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)  // Long timeout for AI responses
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: VectorMemoryApi by lazy {
        retrofit.create(VectorMemoryApi::class.java)
    }
    
    /**
     * Create API instance with custom base URL
     */
    fun createApi(baseUrl: String): VectorMemoryApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VectorMemoryApi::class.java)
    }
}
