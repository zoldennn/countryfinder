package com.example.countryfinder.data.services

import com.example.countryfinder.domain.model.City
import retrofit2.http.GET

interface CityApiService {
    @GET("0996accf70cb0ca0e16f9a99e0ee185fafca7af1/cities.json")
    suspend fun getCities(): List<City>
}