package com.example.countryfinder.data.repository

import com.example.countryfinder.data.services.CityApiService
import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.repository.CityRepository

class CityRepositoryImpl(
    private val api: CityApiService
) : CityRepository {
    override suspend fun getCities(): List<City> = api.getCities()
}