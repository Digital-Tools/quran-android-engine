package com.quranengine.core.localization

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.text.NumberFormat
import java.util.Locale

class NumberFormattingTest {

    @Test
    fun `fixedLocaleNumbers removes latin numbers suffix`() {
        val locale = Locale("ar", "SA", "@numbers=latn")

        val result = locale.fixedLocaleNumbers()

        assertThat(result.toLanguageTag()).isEqualTo("ar-SA")
    }

    @Test
    fun `fixedLocaleNumbers leaves other locales unchanged`() {
        val locale = Locale.US

        val result = locale.fixedLocaleNumbers()

        assertThat(result).isEqualTo(locale)
    }

    @Test
    fun `fixedCurrentLocaleNumbers uses default locale`() {
        val previousLocale = Locale.getDefault()
        val locale = Locale("ar", "SA", "@numbers=latn")
        Locale.setDefault(locale)
        try {
            assertThat(fixedCurrentLocaleNumbers.toLanguageTag()).isEqualTo("ar-SA")
        } finally {
            Locale.setDefault(previousLocale)
        }
    }

    @Test
    fun `int format delegates to long overload`() {
        val formatter = NumberFormat.getIntegerInstance(Locale.US)

        val result = formatter.format(1234)

        assertThat(result).isEqualTo("1,234")
    }

    @Test
    fun `float format delegates to double overload`() {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = 1

        val result = formatter.format(12.25f)

        assertThat(result).isEqualTo("12.2")
    }
}
