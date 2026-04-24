package com.quranengine.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

@Composable
fun NoorBasicSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = QuranTheme.colors.secondaryText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = QuranTheme.colors.secondaryBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Column(content = content)
        }

        if (footer != null) {
            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall,
                color = QuranTheme.colors.secondaryText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
fun <T> NoorSection(
    items: List<T>,
    modifier: Modifier = Modifier,
    title: String? = null,
    footer: String? = null,
    itemContent: @Composable (T) -> Unit,
) {
    NoorBasicSection(
        title = title,
        footer = footer,
        modifier = modifier,
    ) {
        items.forEachIndexed { index, item ->
            itemContent(item)
            if (index < items.lastIndex) {
                NoorDivider()
            }
        }
    }
}
