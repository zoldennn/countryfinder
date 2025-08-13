package com.example.countryfinder

import com.example.countryfinder.domain.repository.CityRepository


interface AppContainer {
    val cityRepository: CityRepository
}