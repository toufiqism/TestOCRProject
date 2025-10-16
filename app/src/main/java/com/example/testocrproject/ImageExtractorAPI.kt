package com.example.testocrproject

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import kotlin.getValue

interface ImageExtractorAPI {
    @Multipart
    @POST("/extract_text")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<OCRResponse>
}

data class OCRResponse(val text: String)

object RetrofitInstance {
    val api: ImageExtractorAPI by lazy {
        // 1️⃣ Create logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 2️⃣ Build OkHttp client
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl("http://192.168.103.82:5000/") // Emulator localhost
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ImageExtractorAPI::class.java)
    }
}