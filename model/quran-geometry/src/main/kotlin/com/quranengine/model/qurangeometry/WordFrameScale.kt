package com.quranengine.model.qurangeometry

import android.graphics.RectF
import android.util.SizeF

data class WordFrameScale(
    val scale: Float,
    val xOffset: Float,
    val yOffset: Float
) {
    companion object {
        val ZERO = WordFrameScale(0f, 0f, 0f)

        fun scaling(imageSize: SizeF, viewSize: SizeF): WordFrameScale {
            if (imageSize.width == 0f || imageSize.height == 0f ||
                viewSize.width == 0f || viewSize.height == 0f
            ) {
                return ZERO
            }

            val imageAspectRatio = imageSize.width / imageSize.height
            val viewAspectRatio = viewSize.width / viewSize.height

            val scale = if (imageAspectRatio < viewAspectRatio) {
                viewSize.height / imageSize.height
            } else {
                viewSize.width / imageSize.width
            }

            val xOffset = (viewSize.width - (scale * imageSize.width)) / 2f
            val yOffset = (viewSize.height - (scale * imageSize.height)) / 2f
            return WordFrameScale(scale, xOffset, yOffset)
        }
    }
}

fun RectF.scaled(by: WordFrameScale): RectF = RectF(
    left * by.scale + by.xOffset,
    top * by.scale + by.yOffset,
    right * by.scale + by.xOffset,
    bottom * by.scale + by.yOffset
)
