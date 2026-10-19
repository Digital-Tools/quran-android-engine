package com.quranengine.features.quranview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quranengine.domain.readingservice.ReadingResourcesService.ResourceStatus
import com.quranengine.ui.theme.QuranTheme

/**
 * Over the pages while the mushaf downloads (first launch, or a mushaf just
 * picked), and with a Retry button when the download failed. Nothing once
 * the pages are on the phone.
 */
@Composable
internal fun ReadingDownloadCard(
    status: ResourceStatus?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (status !is ResourceStatus.Downloading && status !is ResourceStatus.Error) return
    Surface(
        modifier = modifier.padding(24.dp).widthIn(max = 360.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (status) {
                is ResourceStatus.Downloading -> {
                    val percent = (status.progress.coerceIn(0.0, 1.0) * 100).toInt()
                    Text(
                        text = "Downloading the Quran pages… $percent%",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                    )
                    LinearProgressIndicator(
                        progress = { status.progress.coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = QuranTheme.mizanGold,
                    )
                    Text(
                        text = "Only once: after this the Quran works offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = QuranTheme.colors.secondaryText,
                        textAlign = TextAlign.Center,
                    )
                }
                is ResourceStatus.Error -> {
                    Text(
                        text = "The Quran pages could not be downloaded. Check your internet connection and try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = QuranTheme.mizanGold),
                    ) {
                        Text("Retry")
                    }
                }
                else -> Unit
            }
        }
    }
}
