package com.example.countryfinder.presentation.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.countryfinder.domain.model.City
import com.example.countryfinder.domain.model.CityCoordinates
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.countryfinder.presentation.viewmodel.CityDetailViewModel

class CityDetailActivity : ComponentActivity() {

    private val cityDetailViewModel: CityDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val city = readCityFromExtras(intent)

        cityDetailViewModel.setCity(city.id)

        setContent {
            MaterialTheme {
                // Check if this city is in favorites
                val isFavorite by cityDetailViewModel.isFavorite.collectAsStateWithLifecycle()

                CityDetailScreen(
                    city = city,
                    isFavorite = isFavorite,
                    onBack = { finish() },
                    onToggleFavorite = { cityDetailViewModel.onFavoriteClicked() },
                    onOpenMap = { openCityInMap(city) },
                    onOpenMoreInfo = { openCityInWikipedia(city) }
                )
            }
        }
    }

    /**
     * We get city info from extras sent in previous activity
     */
    private fun readCityFromExtras(intent: Intent): City {
        val name   = intent.getStringExtra("city_name") ?: ""
        val country = intent.getStringExtra("country") ?: ""
        val lat    = intent.getDoubleExtra("lat", 0.0)
        val lon    = intent.getDoubleExtra("lon", 0.0)
        val id     = intent.getLongExtra("city_id", -1L)

        return City(
            country = country,
            name = name,
            id = id,
            coordinates = CityCoordinates(lon = lon, lat = lat)
        )
    }

    private fun openCityInMap(city: City) {
        val uri =
            "geo:${city.coordinates.lat},${city.coordinates.lon}?q=${city.coordinates.lat},${city.coordinates.lon}(${city.name})".toUri()
        startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        })
    }

    // TODO: Migrar wikipedia a City API para más info de la ciudad
    private fun openCityInWikipedia(city: City) {
        val title = Uri.encode(city.name.replace(' ', '_'))
        val url = "https://en.wikipedia.org/wiki/$title"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityDetailScreen(
    city: City,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenMoreInfo: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = city.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Favorite"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Header
            Text(
                text = "${city.name}, ${city.country}",
                style = MaterialTheme.typography.headlineSmall
            )

            // Coordinates as subtitle
            Text(
                text = "Lat: ${city.coordinates.lat}, Lon: ${city.coordinates.lon}",
                style = MaterialTheme.typography.bodyLarge
            )

            // Actions Open Map & Info
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenMap,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Open map")
                }

                OutlinedButton(onClick = onOpenMoreInfo) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("More info")
                }
            }

            // Additional info section
            HorizontalDivider()
            Text("Aditional info", style = MaterialTheme.typography.titleMedium)

            // TODO: Consumir de City API y mostrar bandera u otra información detallada
            Text(
                "Intern ID from cityID: ${city.id}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${city.name} Its a beautiful city",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
