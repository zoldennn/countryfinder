package com.example.countryfinder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.countryfinder.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.countryfinder.domain.usecase.ToggleFavoriteCityUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CityDetailViewModel(
    private val toggleFavorite: ToggleFavoriteCityUseCase,
    observeFavoriteIds: ObserveFavoriteIdsUseCase
) : ViewModel() {

    private val cityId = MutableStateFlow<Long?>(null)

    val isFavorite: StateFlow<Boolean> =
        combine(cityId, observeFavoriteIds()) { id, favs ->
            id?.let { it.toInt() in favs } ?: false
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setCity(id: Long) { cityId.value = id }

    fun onFavoriteClicked() = viewModelScope.launch {
        cityId.value?.let { toggleFavorite(it.toInt()) }
    }
}
