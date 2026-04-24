package com.quranengine.model.qurangeometry

import android.graphics.RectF
import com.quranengine.model.qurankit.Sura

data class SuraHeaderLocation(
    val sura: Sura,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    val rect: RectF
        get() = RectF(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat())
}
