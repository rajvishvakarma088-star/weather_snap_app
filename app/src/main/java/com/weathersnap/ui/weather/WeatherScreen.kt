package com.weathersnap.ui.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.Weather
import com.weathersnap.ui.theme.AmberAccent
import com.weathersnap.ui.theme.AppBackground
import com.weathersnap.ui.theme.AppSurface
import com.weathersnap.ui.theme.CardSurface
import com.weathersnap.ui.theme.CyanAccent
import com.weathersnap.ui.theme.DarkText
import com.weathersnap.ui.theme.LimeAccent
import com.weathersnap.ui.theme.TextPrimary
import com.weathersnap.ui.theme.TextSecondary

@Composable
fun WeatherScreen(
    onCreateReport: (Weather) -> Unit,
    onReports: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val loadingAlpha by animateFloatAsState(
        targetValue = if (state is WeatherUiState.LoadingSuggestions || state is WeatherUiState.LoadingWeather) 1f else 0f,
        label = "loadingAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppBackground, androidx.compose.ui.graphics.Color(0xFF101909))))
            .statusBarsPadding()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WeatherTopBar(onReports = onReports)

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    label = { Text("City") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Enter more than 2 letters to start city suggestions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Suggestions(state = state, onCityClick = viewModel::selectCity)
            }
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = LimeAccent,
                modifier = Modifier.alpha(loadingAlpha)
            )
        }

        when (val current = state) {
            is WeatherUiState.WeatherLoaded -> {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = fadeOut()
                ) {
                    WeatherCard(weather = current.weather, showCreateButton = true, onCreateReport = onCreateReport)
                }
            }
            is WeatherUiState.Error -> ErrorCard(message = current.message, onRetry = viewModel::retry)
            else -> Unit
        }
    }
}

@Composable
private fun WeatherTopBar(onReports: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LimeAccent, RoundedCornerShape(8.dp))
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("WeatherSnap", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DarkText)
            Text("Live weather reports with camera evidence", style = MaterialTheme.typography.bodySmall, color = DarkText.copy(alpha = 0.75f))
        }
        ChipButton(text = "Reports", onClick = onReports)
    }
}

@Composable
private fun Suggestions(state: WeatherUiState, onCityClick: (City) -> Unit) {
    val cities = (state as? WeatherUiState.SuggestionsLoaded)?.cities.orEmpty()
    AnimatedVisibility(
        visible = state is WeatherUiState.SuggestionsLoaded,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .background(CardSurface, RoundedCornerShape(12.dp))
        ) {
            if (cities.isEmpty()) {
                Text("No cities found", color = TextSecondary, modifier = Modifier.padding(14.dp))
            } else {
                cities.forEach { city ->
                    Text(
                        text = "${city.name}, ${city.country}",
                        color = TextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCityClick(city) }
                            .padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherCard(
    weather: Weather,
    showCreateButton: Boolean,
    onCreateReport: (Weather) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${weather.cityName}, ${weather.country}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(weather.condition, color = TextSecondary)
                }
                Text(
                    "${weather.temperature.toInt()}°C",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LimeAccent,
                    modifier = Modifier
                        .background(androidx.compose.ui.graphics.Color(0xFF596700), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatChip("Humidity", "${weather.humidity}%", CyanAccent, Modifier.weight(1f))
                StatChip("Wind", "${weather.windSpeed} m/s", androidx.compose.ui.graphics.Color(0xFF2E9BFF), Modifier.weight(1f))
                StatChip("Pressure", "${weather.pressure.toInt()} hPa", AmberAccent, Modifier.weight(1f))
            }
            if (showCreateButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(Modifier.size(8.dp).background(LimeAccent, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text("Report readiness", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "Camera and Room DB enabled",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    onClick = { onCreateReport(weather) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = DarkText)
                ) {
                    Text("Create Report")
                }
            }
        }
    }
}

@Composable
fun StatChip(label: String, value: String, accent: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(value, color = accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChipButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkText.copy(alpha = 0.88f),
            contentColor = LimeAccent
        )
    ) {
        Text(text)
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(CardSurface, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Text(message, color = TextPrimary)
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = DarkText)
        ) {
            Text("Retry")
        }
    }
}

@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = TextSecondary,
    unfocusedBorderColor = TextSecondary,
    focusedLabelColor = TextPrimary,
    unfocusedLabelColor = TextSecondary,
    cursorColor = LimeAccent
)
