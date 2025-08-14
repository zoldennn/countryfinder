package com.example.countryfinder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.repository.CityRepository
import com.example.countryfinder.domain.search.CitySearchIndex
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CityListViewModel(
    private val repository: CityRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private lateinit var searchIndex: CitySearchIndex
    private var fullList: List<City> = emptyList()

    fun onQueryChange(newQuery: String) { _query.value = newQuery }

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        load()

        viewModelScope.launch {
            _query
                .debounce(200)
                .distinctUntilChanged()
                .collect { recomputeCityList() }
        }
    }

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
                val list = withContext(ioDispatcher) { repository.getCities() }
                // We order by: city asc with case-insensitive, then country asc
                fullList = list.sortedWith(compareBy(
                    { it.name.lowercase(Locale.ROOT) },
                    { it.country.lowercase(Locale.ROOT) }
                ))
                // Must create search index here for recomputing later
                searchIndex = CitySearchIndex(fullList)
                _error.value = null
                recomputeCityList() // Apply search query, if any
            } catch (e: Exception) {
                _cities.value = emptyList()
                _error.value = e.message ?: "Error fetching cities"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * We receive the search query value
     * If blank, we must show entire city list
     * If not blank we filter by prefix and show a city list containing that prefix
     */
    private fun recomputeCityList() {
        // TODO: Recomputar también favoritos acá?
        if (!::searchIndex.isInitialized) {
            _cities.value = emptyList()
            return
        }
        val queryPrefix = _query.value
        val cityList = if (queryPrefix.isBlank()) fullList else searchIndex.findByPrefix(queryPrefix)
        _cities.value = cityList
    }
}
