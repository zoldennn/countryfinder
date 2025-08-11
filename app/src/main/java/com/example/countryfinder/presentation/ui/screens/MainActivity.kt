package com.example.countryfinder.presentation.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.countryfinder.presentation.viewmodel.CityListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CityListScreen()
        }
    }

    @Composable
    fun CityListScreen(viewModel: CityListViewModel = hiltViewModel()) {
        val cities by viewModel.cities.collectAsState()
        val loading by viewModel.loading.collectAsState()
        val error by viewModel.error.collectAsState()

        when {
            loading -> {
                Box(
                    Modifier.Companion.fillMaxSize(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    Modifier.Companion.fillMaxSize(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text("Error: $error")
                }
            }
            else -> {
                LazyColumn {
                    items(cities) { city ->
                        Text(
                            "${city.name}, ${city.country} (${city.coord.lat}, ${city.coord.lon})",
                            modifier = Modifier.Companion.padding(8.dp)
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

}