package com.example.countryfinder.data.repository

import com.example.countryfinder.domain.model.City
import com.example.countryfinder.data.services.CityApiService
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class CityRepository @Inject constructor(
    private val apiService: CityApiService
) {
    suspend fun getCities(): List<City> = apiService.getCities()
}
