package com.example.countryfinder.domain.repository

import com.example.countryfinder.domain.model.City

interface CityRepository {
    suspend fun getCities(): List<City>
}