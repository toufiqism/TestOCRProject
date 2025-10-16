package com.example.testocrproject

import android.content.Context
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageExtractorAPI {
    @Multipart
    @POST("/extract_text")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<OCRResponse>
}

data class OCRResponse(val text: String)

/**
 * Retrofit instance provider for OCR API
 * Uses dynamic base URL from PreferencesManager
 */
object RetrofitInstance {
    private var api: ImageExtractorAPI? = null
    
    /**
     * Gets or creates the API instance with the current base URL from preferences
     * @param context Application context to access preferences
     * @return ImageExtractorAPI instance configured with current base URL
     */
    fun getApi(baseUrl: String): ImageExtractorAPI {

        
        // Recreate API instance if base URL might have changed
        // For production, consider caching based on URL
        return createApi(baseUrl)
    }
    
    /**
     * Creates a new API instance with the specified base URL
     * @param baseUrl The base URL for the API server
     * @return ImageExtractorAPI instance
     */
    private fun createApi(baseUrl: String): ImageExtractorAPI {
        // Create logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Build OkHttp client
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ImageExtractorAPI::class.java)
    }
    
    /**
     * Forces recreation of the API instance (useful after settings change)
     */
    fun resetApi() {
        api = null
    }
}