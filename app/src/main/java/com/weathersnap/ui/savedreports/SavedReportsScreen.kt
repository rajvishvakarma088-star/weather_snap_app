package com.weathersnap.ui.savedreports

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weathersnap.domain.model.WeatherReport
import com.weathersnap.ui.report.ReportTopBar
import com.weathersnap.ui.theme.AmberAccent
import com.weathersnap.ui.theme.AppBackground
import com.weathersnap.ui.theme.CardSurface
import com.weathersnap.ui.theme.CyanAccent
import com.weathersnap.ui.theme.DarkText
import com.weathersnap.ui.theme.LimeAccent
import com.weathersnap.ui.theme.TextPrimary
import com.weathersnap.ui.theme.TextSecondary
import com.weathersnap.ui.weather.StatChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedReportsScreen(
    onBack: () -> Unit,
    onWeatherSearch: () -> Unit,
    viewModel: SavedReportsViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppBackground, androidx.compose.ui.graphics.Color(0xFF161D0B))))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        ReportTopBar(
            title = "Saved Reports",
            subtitle = "${reports.size} report(s) stored locally",
            button = "Back",
            onButtonClick = onBack
        )
        if (reports.isEmpty()) {
            EmptyReports(onWeatherSearch = onWeatherSearch, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(reports, key = { it.id }) { report ->
                    ReportCard(report = report)
                }
            }
        }
    }
}

@Composable
private fun ReportCard(report: WeatherReport) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(androidx.compose.ui.graphics.Color.Black, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, report.imagePath) {
                    value = withContext(Dispatchers.IO) {
                        decodeSampledBitmap(report.imagePath, reqWidth = 900, reqHeight = 500)
                    }
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Report photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${report.cityName}, ${report.country}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(report.condition, color = TextSecondary)
                    Text(formatTimestamp(report.timestamp), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Text(
                    "${report.temperature.toInt()}°C",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LimeAccent,
                    modifier = Modifier
                        .background(androidx.compose.ui.graphics.Color(0xFF596700), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatChip("Original", "${report.originalSizeKb} KB", AmberAccent, Modifier.weight(1f))
                StatChip("Compressed", "${report.compressedSizeKb} KB", CyanAccent, Modifier.weight(1f))
            }
            if (report.notes.isNotBlank()) {
                Text(
                    report.notes,
                    color = TextPrimary,
                    modifier = Modifier
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyReports(onWeatherSearch: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmptyReportIcon()
        Spacer(Modifier.height(14.dp))
        Text("No reports yet", color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onWeatherSearch,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = DarkText)
        ) {
            Text("Go to Weather Search")
        }
    }
}

@Composable
private fun EmptyReportIcon() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(CardSurface, RoundedCornerShape(16.dp))
            .border(1.dp, LimeAccent.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 34.dp, height = 24.dp)
                .border(2.dp, LimeAccent, RoundedCornerShape(5.dp))
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(LimeAccent, RoundedCornerShape(50))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 15.dp)
                .size(width = 14.dp, height = 5.dp)
                .background(LimeAccent, RoundedCornerShape(3.dp))
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(timestamp))
}

private fun decodeSampledBitmap(path: String, reqWidth: Int, reqHeight: Int): android.graphics.Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, reqWidth, reqHeight)
    }
    return BitmapFactory.decodeFile(path, options)
}

private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
    var inSampleSize = 1
    var halfWidth = width / 2
    var halfHeight = height / 2

    while (halfWidth / inSampleSize >= reqWidth && halfHeight / inSampleSize >= reqHeight) {
        inSampleSize *= 2
    }
    return inSampleSize
}
