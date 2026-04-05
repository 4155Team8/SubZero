package com.example.subzero.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // 10.0.2.2 is just what the emulator needs to reach the node server
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // allows tests to create a mock ApiService
    private var testInstance: ApiService? = null

    val instance: ApiService
        get() = testInstance ?: Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

    // ONLY CALL FROM TESTS PLS
    fun setInstanceForTesting(mock: ApiService) {
        testInstance = mock
    }

    // resetting from tests
    fun clearTestInstance() {
        testInstance = null
    }
}