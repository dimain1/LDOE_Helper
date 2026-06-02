package com.example.kotlinclient.di

import com.example.kotlinclient.api_client.ApiService
import com.example.kotlinclient.api_client.AuthInterceptor
import com.example.kotlinclient.api_client.NetworkConfig
import com.example.kotlinclient.api_client.TokenAuthenticator
import com.example.kotlinclient.api_client.TokenStorage
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// URL берём из NetworkConfig — единая точка конфигурации для всего приложения

val networkModule = module {

    single { TokenStorage(androidContext()) }

    single { AuthInterceptor(get()) }

    // ApiService через лямбду — разрывает циклическую зависимость
    single { TokenAuthenticator(get()) { get() } }

    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .authenticator(get<TokenAuthenticator>())
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create()

        Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    single<ApiService> {
        get<Retrofit>().create(ApiService::class.java)
    }
}
