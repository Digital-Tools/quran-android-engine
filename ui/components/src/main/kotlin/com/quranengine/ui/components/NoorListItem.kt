package com.quranengine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

sealed class NoorAccessory {
    data class TextAccessory(val text: String) : NoorAccessory()
    data object DisclosureIndicator : NoorAccessory()
    data class ImageAccessory(val painter: Painter) : NoorAccessory()
}

@Composable
fun NoorListItem(
    title: String,
    modifier: Modifier = Modifier,
    heading: String? = null,
    subheading: String? = null,
    subtitle: String? = null,
    rightPretitle: String? = null,
    rightSubtitle: String? = null,
    leadingEdgeColor: Color? = null,
    accessory: NoorAccessory? = null,
    image: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val clickMod = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Row(
        modifier = clickMod
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading edge color indicator
        if (leadingEdgeColor != null) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(leadingEdgeColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Optional image
        if (image != null) {
            image()
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Center content
        Column(modifier = Modifier.weight(1f)) {
            if (heading != null) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.labelSmall,
                    color = QuranTheme.colors.secondaryText,
                )
            }
            if (subheading != null) {
                Text(
                    text = subheading,
                    style = MaterialTheme.typography.bodySmall,
                    color = QuranTheme.colors.secondaryText,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = QuranTheme.colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = QuranTheme.colors.secondaryText,
                )
            }
        }

        // Right side content
        if (rightPretitle != null || rightSubtitle != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (rightPretitle != null) {
                    Text(
                        text = rightPretitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuranTheme.colors.secondaryText,
                    )
                }
                if (rightSubtitle != null) {
                    Text(
                        text = rightSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuranTheme.colors.secondaryText,
                    )
                }
            }
        }

        // Accessory
        if (accessory != null) {
            Spacer(modifier = Modifier.width(8.dp))
            when (accessory) {
                is NoorAccessory.TextAccessory -> {
                    Text(
                        text = accessory.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = QuranTheme.colors.secondaryText,
                    )
                }
                is NoorAccessory.DisclosureIndicator -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = QuranTheme.colors.secondaryText,
                        modifier = Modifier.size(24.dp),
                    )
                }
                is NoorAccessory.ImageAccessory -> {
                    Icon(
                        painter = accessory.painter,
                        contentDescription = null,
                        tint = QuranTheme.colors.secondaryText,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
