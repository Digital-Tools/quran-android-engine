package com.quranengine.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

@Composable
fun DataUnavailableView(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    image: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            if (image != null) {
                image()
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = QuranTheme.colors.text,
                textAlign = TextAlign.Center,
            )
            if (message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuranTheme.colors.secondaryText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
