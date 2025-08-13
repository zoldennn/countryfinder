package com.example.countryfinder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.repository.CityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CityListViewModel(
    private val repository: CityRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { load() }

    /**
     * First tells the activity to show loading while fetching
     * Then, tells repository that we want the city list
     * If fails, we post error value in VM so the Activity can show the error
     * If success, tells the activity to show the list
     * For all cases, we remove the loading at the end
     */
    fun load() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = withContext(ioDispatcher) { repository.getCities() }
                _cities.value = result
                _error.value = null
            } catch (e: Exception) {
                _cities.value = emptyList()
                _error.value = e.message ?: "Error fetching cities"
            } finally {
                _loading.value = false
            }
        }
    }
}
