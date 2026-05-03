package com.quranengine.ui.quran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize

@Composable
fun AdaptiveImageScrollView(
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable (Size) -> Unit,
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (header != null) {
                header()
            }

            if (containerSize != IntSize.Zero) {
                content(containerSize.toSize())
            }

            if (footer != null) {
                footer()
            }
        }
    }
}
