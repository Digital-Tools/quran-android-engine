package com.quranengine.model.qurangeometry

import android.graphics.Bitmap
import com.quranengine.model.qurankit.Page

data class ImagePage(
    val page: Page,
    val image: Bitmap
)
