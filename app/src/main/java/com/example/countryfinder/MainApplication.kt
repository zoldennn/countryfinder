package com.example.countryfinder

import android.app.Application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.countryfinder.data.favorites.FavoritesDataStore
import com.example.countryfinder.data.repository.CityRepositoryImpl
import com.example.countryfinder.data.services.CityApiService
import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.repository.CityRepository
import com.example.countryfinder.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.countryfinder.domain.usecase.ToggleFavoriteCityUseCase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue

/**
 * App's Composition Root.
 * Create and expose primary dependencies (Retrofit, Api, Repository)
 */
// TODO: Revisar si se puede pulir esto, no me convence
class MainApplication : Application(), AppContainer {

    companion object {
        private const val BASE_URL = "https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/"
    }

    private val dataStore by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = { this.preferencesDataStoreFile("countryfinder_prefs") }
        )
    }

    private val favoritesDataStore by lazy {
        FavoritesDataStore(dataStore)
    }

    private val cityCache: Map<Int, City> = emptyMap()

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
        CityRepositoryImpl(favoritesDataStore, api, cityCache)
    }

    override val toggleFavoriteCityUseCase: ToggleFavoriteCityUseCase by lazy {
        ToggleFavoriteCityUseCase(cityRepository)
    }

    override val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase by lazy {
        ObserveFavoriteIdsUseCase(cityRepository)
    }
}
