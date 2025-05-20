package com.flashmob.platonus.data.network

import android.content.Context
import com.flashmob.platonus.BuildConfig
import com.flashmob.platonus.data.storage.AuthManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, MicroIsoDateAdapter().nullSafe())
        .build()

    private lateinit var retrofit: Retrofit

    fun init(context: Context) {
        val auth = AuthManager(context)

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        val authInterceptor = Interceptor { chain ->
            val token = auth.token
            val req = chain.request()
            val newReq = if (token == null) req
            else req.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newReq)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()
    }

    val service: ApiService by lazy { retrofit.create(ApiService::class.java) }
}