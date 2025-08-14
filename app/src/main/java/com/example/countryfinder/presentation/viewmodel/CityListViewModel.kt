package com.example.countryfinder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.repository.CityRepository
import com.example.countryfinder.domain.search.CitySearchIndex
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CityListViewModel(
    private val repository: CityRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    private val _favorites = MutableStateFlow<Set<Long>>(emptySet())
    val favorites: StateFlow<Set<Long>> = _favorites.asStateFlow()

    private lateinit var searchIndex: CitySearchIndex
    private var fullList: List<City> = emptyList()

    fun onQueryChange(newQuery: String) { _query.value = newQuery }

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // TODO: Si se agregan más scopes, considerar unificarlos con combine de ser necesario
    init {
        load()

        viewModelScope.launch {
            _query
                .debounce(200)
                .distinctUntilChanged()
                .collect { recomputeCityList() }
        }

        // Recompute list when "only favorites" is checked
        viewModelScope.launch {
            _onlyFavorites
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

    fun toggleOnlyFavorites() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    /**
     * Connect favorites flow to ViewModel
     * For now, we receive ids as Long
     * TODO: Convertir valores futuros a Long para que no explote
     */
    fun attachFavorites(favoritesFlow: Flow<Set<String>>) {
        viewModelScope.launch {
            favoritesFlow
                .map { set -> set.mapNotNull { it.toLongOrNull() }.toSet() }
                .collect { ids ->
                    _favorites.value = ids
                    recomputeCityList()
                }
        }
    }

    /**
     * We receive the search query value
     * If blank, we must show entire city list
     * If not blank we filter by prefix and show a city list containing that prefix
     * If only favorites is toggle, we only display cities with favorites
     */
    private fun recomputeCityList() {
        if (!::searchIndex.isInitialized) {
            _cities.value = emptyList()
            return
        }
        val queryPrefix = _query.value
        val cityList = if (queryPrefix.isBlank()) fullList else searchIndex.findByPrefix(queryPrefix)
        _cities.value = if (_onlyFavorites.value) {
            cityList.filter { it.id in _favorites.value }
        } else {
            cityList
        }
    }
}
