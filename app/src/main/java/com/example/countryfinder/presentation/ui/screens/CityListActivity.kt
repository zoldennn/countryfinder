package com.example.countryfinder.presentation.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.countryfinder.AppContainer
import com.example.countryfinder.data.favorites.FavoritesDataStore
import com.example.countryfinder.domain.model.City
import com.example.countryfinder.presentation.viewmodel.CityListViewModel
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class CityListActivity : ComponentActivity() {

    private val viewModel by viewModels<CityListViewModel> {
        viewModelFactory {
            initializer {
                val repo = (application as AppContainer).cityRepository
                CityListViewModel(repo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val favoritesStore = FavoritesDataStore(applicationContext)
        viewModel.attachFavorites(favoritesStore.favoritesFlow)

        setContent { CityListScreen(
            viewModel,
            onToggleFavorite = { cityId ->
            lifecycleScope.launch { favoritesStore.toggle(cityId) }
        },
            // TODO: quedaron repetidos acá y en la otra activity, usar un CityHelper?
            onOpenMap = { openCityMap(it) },
            onOpenInfo = { openCityInfo(it) }) }
    }

    private fun openCityMap(city: City) {
        val uri =
            "geo:${city.coordinates.lat},${city.coordinates.lon}?q=${city.coordinates.lat},${city.coordinates.lon}(${city.name})".toUri()
        startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        })
    }

    private fun openCityInfo(city: City) {
        // TODO: Usar NavigationCompose tal vez
        val intent = Intent(this, CityDetailActivity::class.java).apply {
            putExtra("city_name", city.name)
            putExtra("country", city.country)
            putExtra("lat", city.coordinates.lat)
            putExtra("lon", city.coordinates.lon)
            putExtra("city_id", city.id)
        }
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListScreen(
    viewModel: CityListViewModel,
    onToggleFavorite: (Long) -> Unit,
    onOpenMap: (City) -> Unit,
    onOpenInfo: (City) -> Unit) {
    val cities by viewModel.cities.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.query.collectAsState()
    val onlyFav by viewModel.onlyFavorites.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CountryFinder", style = MaterialTheme.typography.titleLarge) })
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search your city…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
            // "Only favorites" filter
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Only favorites")
                Spacer(Modifier.width(8.dp))
                Switch(checked = onlyFav, onCheckedChange = { viewModel.toggleOnlyFavorites() })
            }
            // TODO: Revisar esto, tal vez se puede reducir el boilerplate
            when {
                loading -> { PerformLoading(innerPadding) }
                error != null -> { PerformError(innerPadding, error) }
                else -> { DisplayCities(cities, favorites, onToggleFavorite, onOpenMap, onOpenInfo) }
            }
        }
    }
}

@Composable
fun PerformLoading(innerPadding: PaddingValues) {
    Box(
        Modifier.fillMaxSize().padding(innerPadding),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

@Composable
fun PerformError(innerPadding: PaddingValues, error: String?) {
    Box(
        Modifier.fillMaxSize().padding(innerPadding),
        contentAlignment = Alignment.Center
    ) { Text("Error: $error") }
}

@Composable
fun DisplayCities(
    cities: List<City>,
    favorites: Set<Long>,
    onToggleFavorite: (Long) -> Unit,
    onOpenMap: (City) -> Unit,
    onOpenInfo: (City) -> Unit
) {
    LazyColumn {
        items(cities) { city ->
            val isFav = favorites.contains(city.id)
            CityRowWithButtons(
                city = city,
                isFavorite = isFav,
                onToggleFavorite = { onToggleFavorite(city.id) },
                onOpenMap = onOpenMap,
                onOpenInfo = onOpenInfo
            )
        }
    }
}

@Composable
private fun CityRowWithButtons(
    city: City,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenMap: (City) -> Unit,
    onOpenInfo: (City) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("${city.name}, ${city.country}", style = MaterialTheme.typography.titleMedium)
            Text("(${city.coordinates.lat}, ${city.coordinates.lon})", style = MaterialTheme.typography.bodyMedium)
        }
        IconToggleButton(checked = isFavorite, onCheckedChange = { onToggleFavorite() }) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Favorite"
            )
        }
        IconButton(onClick = { onOpenMap(city) }) {
            Icon(Icons.Default.LocationOn, contentDescription = "Abrir mapa")
        }
        IconButton(onClick = { onOpenInfo(city) }) {
            Icon(Icons.Default.Info, contentDescription = "Info")
        }
    }
    HorizontalDivider()
}
