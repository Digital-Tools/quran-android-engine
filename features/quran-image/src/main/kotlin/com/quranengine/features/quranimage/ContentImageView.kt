package com.quranengine.features.quranimage

import android.graphics.RectF
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import com.quranengine.model.qurangeometry.WordFrame

@Composable
fun ContentImageView(
    state: ContentImageState,
    modifier: Modifier = Modifier,
    selectedAyahs: List<AyahNumber> = emptyList(),
    onTap: () -> Unit = {},
    onAyahSelectionStarted: (AyahNumber, Offset) -> Unit = { _, _ -> },
    onAyahSelectionChanged: (AyahNumber) -> Unit = {},
    onAyahSelectionEnded: () -> Unit = {},
) {
    if (state.isLoading) {
        LoadingView(modifier = modifier)
        return
    }

    val bitmap = state.bitmap ?: return

    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var imageCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val selectedHighlights = remember(selectedAyahs, state.wordFramesByAyah) {
        selectedAyahs.flatMap { ayah ->
            state.wordFramesByAyah[ayah].orEmpty().map { frame ->
                WordHighlight(rect = RectF(frame.rect), color = QuranColors.wordHighlight)
            }
        }
    }

    AdaptiveImageScrollView(
        modifier = modifier,
        header = {
            QuranPageHeader(
                quarterName = state.quarterName,
                suraNames = state.suraNames,
                decoratedSuraName = state.decoratedSuraName,
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
                // Tap gesture for toggling bars (runs first, non-blocking)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onTap() })
                }
                // Long-press-then-drag for multi-verse selection
                .pointerInput(viewSize, state.decorations.imageSize) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            val ayah = resolveAyahAtOffset(offset, viewSize.toSize(), state)
                            val root = imageCoords?.takeIf { it.isAttached }?.localToRoot(offset)
                            if (ayah != null && root != null) {
                                onAyahSelectionStarted(ayah, root)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val ayah = resolveAyahAtOffset(
                                change.position,
                                viewSize.toSize(),
                                state,
                            )
                            if (ayah != null) {
                                onAyahSelectionChanged(ayah)
                            }
                        },
                        onDragEnd = { onAyahSelectionEnded() },
                        onDragCancel = { onAyahSelectionEnded() },
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
    offset: Offset,
    viewSize: Size,
    state: ContentImageState
): AyahNumber? {
    val imageSize = state.decorations.imageSize
    if (viewSize == Size.Zero || imageSize == Size.Zero) return null

    val x = offset.x * (imageSize.width / viewSize.width)
    val y = offset.y * (imageSize.height / viewSize.height)

    val frames = state.wordFramesByAyah.values.flatten()
    if (frames.isEmpty()) return null

    frames.firstOrNull { it.rect.contains(x, y) }?.let { return it.word.verse }

    // Presses land between words far more often than inside one, so fall back to
    // the closest word on the touched line instead of reporting no verse.
    return frames
        .filter { y >= it.minY && y <= it.maxY }
        .minByOrNull { it.horizontalDistanceTo(x) }
        ?.word
        ?.verse
}

private fun WordFrame.horizontalDistanceTo(x: Float): Float = when {
    x < minX -> minX - x
    x > maxX -> x - maxX
    else -> 0f
}
