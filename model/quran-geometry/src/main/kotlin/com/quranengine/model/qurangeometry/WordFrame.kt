package com.quranengine.model.qurangeometry

import android.graphics.RectF
import com.quranengine.model.qurankit.Word

data class WordFrame(
    val line: Int,
    val word: Word,
    val minX: Int,
    val maxX: Int,
    val minY: Int,
    val maxY: Int
) {
    val rect: RectF
        get() = RectF(
            minX.toFloat(),
            minY.toFloat(),
            maxX.toFloat(),
            maxY.toFloat()
        )
}
