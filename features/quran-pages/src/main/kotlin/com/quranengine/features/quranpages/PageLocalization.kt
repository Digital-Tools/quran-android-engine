package com.quranengine.features.quranpages

import java.text.NumberFormat
import java.util.Locale

object PageLocalization {
    private val arabicNumberFormat = NumberFormat.getInstance(Locale("ar"))

    fun localizedPageNumber(page: Int): String {
        return arabicNumberFormat.format(page)
    }
}
