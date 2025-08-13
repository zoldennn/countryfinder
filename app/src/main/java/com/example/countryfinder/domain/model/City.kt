package com.example.countryfinder.domain.model

import com.google.gson.annotations.SerializedName

data class City(
    val country: String,
    val name: String,
    @SerializedName("_id")
    val id: Long,
    @SerializedName("coord")
    val coordinates: CityCoordinates
)