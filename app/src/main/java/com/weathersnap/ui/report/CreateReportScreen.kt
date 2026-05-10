package com.weathersnap.ui.report

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.weathersnap.domain.model.Weather
import com.weathersnap.ui.theme.AmberAccent
import com.weathersnap.ui.theme.AppBackground
import com.weathersnap.ui.theme.CardSurface
import com.weathersnap.ui.theme.CyanAccent
import com.weathersnap.ui.theme.DarkText
import com.weathersnap.ui.theme.LimeAccent
import com.weathersnap.ui.theme.TextPrimary
import com.weathersnap.ui.theme.TextSecondary
import com.weathersnap.ui.weather.ChipButton
import com.weathersnap.ui.weather.StatChip
import com.weathersnap.ui.weather.WeatherCard
import com.weathersnap.ui.weather.outlinedFieldColors

@Composable
fun CreateReportScreen(
    navController: NavHostController,
    weather: Weather,
    onBack: () -> Unit,
    onCapture: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CreateReportViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val savedStateHandle = requireNotNull(navController.currentBackStackEntry?.savedStateHandle)
    val photoPath by savedStateHandle.getStateFlow("photo_path", "").collectAsState()
    val originalSize by savedStateHandle.getStateFlow("original_size_kb", 0L).collectAsState()
    val compressedSize by savedStateHandle.getStateFlow("compressed_size_kb", 0L).collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppBackground, androidx.compose.ui.graphics.Color(0xFF182003))))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ReportTopBar(title = "Create Report", subtitle = "Capture, compress, annotate", button = "Back", onButtonClick = onBack)
        WeatherCard(weather = weather, showCreateButton = false)
        PhotoCard(photoPath = photoPath, originalSize = originalSize, compressedSize = compressedSize, onCapture = onCapture)

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("Field Notes", color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = viewModel::onNotesChange,
                    placeholder = { Text("Notes") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors()
                )
            }
        }

        Button(
            onClick = {
                viewModel.saveReport(
                    weather = weather,
                    imagePath = photoPath,
                    originalSizeKb = originalSize,
                    compressedSizeKb = compressedSize,
                    onSaved = onSaved
                )
            },
            enabled = !saving && photoPath.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = DarkText)
        ) {
            Text(if (saving) "Saving..." else "Save Report")
        }
    }
}

@Composable
fun ReportTopBar(title: String, subtitle: String, button: String, onButtonClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LimeAccent, RoundedCornerShape(8.dp))
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DarkText)
            Text(subtitle, color = DarkText.copy(alpha = 0.7f))
        }
        ChipButton(text = button, onClick = onButtonClick)
    }
}

@Composable
private fun PhotoCard(photoPath: String, originalSize: Long, compressedSize: Long, onCapture: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PhotoPreviewBox(photoPath = photoPath)
            AnimatedVisibility(visible = photoPath.isNotBlank(), enter = fadeIn()) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatChip("Original", "$originalSize KB", AmberAccent, Modifier.weight(1f))
                    StatChip("Compressed", "$compressedSize KB", CyanAccent, Modifier.weight(1f))
                }
            }
            Button(
                onClick = onCapture,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = DarkText)
            ) {
                Text("Capture Photo")
            }
        }
    }
}

@Composable
private fun PhotoPreviewBox(photoPath: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(androidx.compose.ui.graphics.Color(0xFF414538), androidx.compose.ui.graphics.Color(0xFF596700))
                ),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = photoPath.isNotBlank(),
            enter = scaleIn(initialScale = 0.85f) + fadeIn()
        ) {
            val bitmap = remember(photoPath) { BitmapFactory.decodeFile(photoPath) }
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Captured photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        if (photoPath.isBlank()) {
            Text("Photo preview", color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
