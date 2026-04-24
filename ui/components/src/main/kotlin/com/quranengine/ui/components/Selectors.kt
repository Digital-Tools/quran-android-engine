package com.quranengine.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.*

@Composable
fun <T> ChoicesView(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(QuranTheme.colors.secondaryBackground),
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onItemSelected(item) }
                    .background(
                        if (isSelected) QuranTheme.appIdentity else Color.Transparent,
                        RoundedCornerShape(8.dp),
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color.White else QuranTheme.colors.text,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun AppearanceModeSelector(
    selectedMode: AppearanceMode,
    onModeSelected: (AppearanceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    ChoicesView(
        items = AppearanceMode.entries.toList(),
        selectedItem = selectedMode,
        onItemSelected = onModeSelected,
        modifier = modifier,
        label = { mode ->
            when (mode) {
                AppearanceMode.LIGHT -> "Light"
                AppearanceMode.DARK -> "Dark"
                AppearanceMode.AUTO -> "Auto"
            }
        },
    )
}

@Composable
fun ThemeStyleSelector(
    selectedStyle: ThemeStyle,
    onStyleSelected: (ThemeStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(ThemeStyle.styles) { style ->
            val isSelected = style == selectedStyle
            val colors = style.colors(isDark = false)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onStyleSelected(style) }
                    .padding(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.background)
                        .then(
                            if (isSelected) Modifier.border(
                                BorderStroke(2.dp, QuranTheme.appIdentity),
                                CircleShape,
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = colors.text,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = style.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = QuranTheme.colors.text,
                )
            }
        }
    }
}
