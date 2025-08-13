package com.example.countryfinder

import android.app.Application
import com.example.countryfinder.data.repository.CityRepositoryImpl
import com.example.countryfinder.data.services.CityApiService
import com.example.countryfinder.domain.repository.CityRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * App's Composition Root.
 * Create and expose primary dependencies (Retrofit, Api, Repository)
 */
// TODO: Revisar si se puede pulir esto, no me convence
class MainApplication : Application(), AppContainer {

    override val cityRepository: CityRepository by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(CityApiService::class.java)
        CityRepositoryImpl(api)
    }

    companion object {
        private const val BASE_URL = "https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/"
    }
}
