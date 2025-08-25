package com.example.countryfinder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.repository.CityRepository
import com.example.countryfinder.domain.search.CitySearchIndex
import com.example.countryfinder.domain.usecase.ObserveFavoriteIdsUseCase
import com.example.countryfinder.domain.usecase.ToggleFavoriteCityUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CityListViewModel(
    private val repository: CityRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    // Use Cases
    private val toggleFavoriteUC = ToggleFavoriteCityUseCase(repository)
    private val observeFavoriteIdsUC = ObserveFavoriteIdsUseCase(repository)

    // UI States
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Favorites from DataStore repo -> UC
    val favorites: StateFlow<Set<Long>> =
        observeFavoriteIdsUC()
            .map { ids -> ids.map { it.toLong() }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    // Show cities using always _cities, filtering by favorite IDs
    val displayedCities: StateFlow<List<City>> =
        combine(_query, _onlyFavorites, _cities, favorites) { q, onlyFav, baseList, favIds ->
            val base = if (onlyFav) baseList.filter { it.id in favIds } else baseList
            if (q.isBlank()) base
            else base.filter { it.name.contains(q, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private lateinit var searchIndex: CitySearchIndex
    private var fullList: List<City> = emptyList()

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

    fun onQueryChange(newQuery: String) { _query.value = newQuery }
    fun toggleOnlyFavorites() { _onlyFavorites.value = !_onlyFavorites.value }

    fun onToggleFavorite(cityId: Long) {
        viewModelScope.launch { toggleFavoriteUC(cityId.toInt()) }
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

    // Display query if any, otherwise show full city list
    private fun recomputeCityList() {
        if (!::searchIndex.isInitialized) { _cities.value = emptyList(); return }
        val searchQuery = _query.value
        _cities.value = if (searchQuery.isBlank()) fullList else searchIndex.findByPrefix(searchQuery)
    }
}
