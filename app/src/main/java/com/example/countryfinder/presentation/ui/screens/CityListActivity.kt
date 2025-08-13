package com.example.countryfinder.presentation.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.countryfinder.AppContainer
import com.example.countryfinder.presentation.viewmodel.CityListViewModel

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
        setContent { CityListScreen(viewModel) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListScreen(viewModel: CityListViewModel) {
    val cities by viewModel.cities.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CountryFinder", style = MaterialTheme.typography.titleLarge) })
        }
    ) { innerPadding ->
        when {
            loading -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            error != null -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { Text("Error: $error") }
            }
            else -> {
                LazyColumn(Modifier.padding(innerPadding)) {
                    items(cities) { city ->
                        Text(
                            "${city.name}, ${city.country} (${city.coordinates.lat}, ${city.coordinates.lon})",
                            modifier = Modifier.padding(12.dp)
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
