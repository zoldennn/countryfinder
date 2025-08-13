// app/src/main/java/com/example/countryfinder/MainApplication.kt
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
 * Composition Root de la app.
 * Crea y expone las dependencias principales (Retrofit, Api, Repository).
 */
class MainApplication : Application(), AppContainer {

    override val cityRepository: CityRepository by lazy {
        // Interceptor de logging (útil en debug)
        val logging = HttpLoggingInterceptor().apply {
            // Si preferís menos ruido: HttpLoggingInterceptor.Level.BASIC
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
        // URL del Gist del challenge (host base para Retrofit)
        private const val BASE_URL = "https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/"
    }
}
