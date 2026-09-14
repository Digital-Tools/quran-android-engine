package com.quranengine.ui.quran

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.quranengine.ui.theme.QuranColors

data class WordHighlight(
    val rect: RectF,
    val color: Color = QuranColors.wordHighlight,
)

/**
 * Merges word frames into one highlight bar per text line, instead of one
 * box per word — matches how a text-selection highlight reads (a continuous
 * bar across the line), rather than choppy per-word boxes.
 */
fun List<com.quranengine.model.qurangeometry.WordFrame>.toLineHighlights(
    color: Color,
): List<WordHighlight> =
    groupBy { it.line }.values.map { lineFrames ->
        WordHighlight(
            rect = RectF(
                lineFrames.minOf { it.minX }.toFloat(),
                lineFrames.minOf { it.minY }.toFloat(),
                lineFrames.maxOf { it.maxX }.toFloat(),
                lineFrames.maxOf { it.maxY }.toFloat(),
            ),
            color = color,
        )
    }

data class ImageDecorations(
    val imageSize: Size = Size.Zero,
    val wordHighlights: List<WordHighlight> = emptyList(),
    val suraHeaderRects: List<RectF> = emptyList(),
    val ayahNumberRects: List<RectF> = emptyList(),
)

@Composable
fun ImageDecorationsView(
    decorations: ImageDecorations,
    modifier: Modifier = Modifier,
    viewSize: Size = Size.Zero,
) {
    if (decorations.imageSize == Size.Zero || viewSize == Size.Zero) return

    val scaleX = viewSize.width / decorations.imageSize.width
    val scaleY = viewSize.height / decorations.imageSize.height

    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw word highlights
        for (highlight in decorations.wordHighlights) {
            drawHighlightRect(highlight.rect, highlight.color, scaleX, scaleY)
        }
    }
}

private fun DrawScope.drawHighlightRect(
    rect: RectF,
    color: Color,
    scaleX: Float,
    scaleY: Float,
) {
    drawRect(
        color = color,
        topLeft = Offset(rect.left * scaleX, rect.top * scaleY),
        size = Size(rect.width() * scaleX, rect.height() * scaleY),
    )
}
