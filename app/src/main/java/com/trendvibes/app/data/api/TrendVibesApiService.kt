package com.trendvibes.app.data.api

import com.trendvibes.app.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface TrendVibesApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/vendas")
    suspend fun registrarVenda(@Body request: VendaRequest): Response<VendaResponse>

    @GET("api/usuarios")
    suspend fun getUsuarios(): Response<List<Map<String, Any>>>
}

object RetrofitClient {
    // 10.0.2.2 is Android Emulator's alias for host loopback (localhost:5208)
    private var baseUrl: String = "http://10.0.2.2:5208/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    var apiService: TrendVibesApiService = createService(baseUrl)
        private set

    fun updateBaseUrl(newUrl: String) {
        val sanitizedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        baseUrl = sanitizedUrl
        apiService = createService(sanitizedUrl)
    }

    fun getBaseUrl(): String = baseUrl

    private fun createService(url: String): TrendVibesApiService {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrendVibesApiService::class.java)
    }
}
