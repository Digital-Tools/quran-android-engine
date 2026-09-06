package com.quranengine.features.quranimage

import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.quranengine.ui.components.LoadingView
import com.quranengine.ui.quran.*
import com.quranengine.ui.theme.QuranColors
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurangeometry.WordFrameCollection

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentImageView(
    state: ContentImageState,
    modifier: Modifier = Modifier,
    selectedAyah: AyahNumber? = null,
    onAyahTapped: (AyahNumber?, Offset?) -> Unit = { _, _ -> },
) {
    if (state.isLoading) {
        LoadingView(modifier = modifier)
        return
    }

    val bitmap = state.bitmap ?: return

    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var imageCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val selectedHighlights = remember(selectedAyah, state.wordFramesByAyah) {
        val ayah = selectedAyah ?: return@remember emptyList()
        state.wordFramesByAyah[ayah].orEmpty().map { frame ->
            WordHighlight(rect = RectF(frame.rect), color = QuranColors.wordHighlight)
        }
    }

    AdaptiveImageScrollView(
        modifier = modifier,
        header = {
            QuranPageHeader(
                quarterName = state.quarterName,
                suraNames = state.suraNames,
            )
        },
        footer = {
            QuranPageFooter(pageNumber = state.pageNumber)
        },
    ) { containerSize ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { viewSize = it }
                .onGloballyPositioned { imageCoords = it }
                .pointerInput(viewSize, state.decorations.imageSize) {
                    detectTapGestures(
                        onTap = { offset ->
                            val ayah = resolveAyahAtOffset(offset, viewSize.toSize(), state)
                            val root = imageCoords?.takeIf { it.isAttached }?.localToRoot(offset)
                            onAyahTapped(ayah, root)
                        },
                        onLongPress = { offset ->
                            val ayah = resolveAyahAtOffset(offset, viewSize.toSize(), state)
                            val root = imageCoords?.takeIf { it.isAttached }?.localToRoot(offset)
                            onAyahTapped(ayah, root)
                        }
                    )
                },
        ) {
            QuranThemedImage(
                bitmap = bitmap,
                contentDescription = "Quran page ${state.pageNumber}",
                modifier = Modifier.fillMaxWidth(),
                renderMode = state.renderMode,
            )

            if (viewSize != IntSize.Zero) {
                ImageDecorationsView(
                    decorations = state.decorations.copy(
                        wordHighlights = state.decorations.wordHighlights + selectedHighlights,
                    ),
                    viewSize = viewSize.toSize(),
                )
            }
        }
    }
}

private fun resolveAyahAtOffset(
    offset: androidx.compose.ui.geometry.Offset,
    viewSize: Size,
    state: ContentImageState
): AyahNumber? {
    val imageSize = state.decorations.imageSize
    if (viewSize == Size.Zero || imageSize == Size.Zero) return null

    val scaleX = imageSize.width / viewSize.width
    val scaleY = imageSize.height / viewSize.height

    val pointInImage = PointF(offset.x * scaleX, offset.y * scaleY)
    val wordFrames = state.wordFramesByAyah.values.flatten()
    val collection = WordFrameCollection(wordFrames)
    
    return collection.wordAtLocation(pointInImage)?.verse
}
