package com.quranengine.core.localization

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class LocalizationsTest {

    @Test
    fun `l returns matching value for requested language`() {
        val localizer = MapLocalizer(
            strings = mapOf(
                (Table.LOCALIZABLE to Language.ARABIC) to mapOf("greeting" to "marhaban"),
            ),
        )

        val result = localizer.l("greeting", language = Language.ARABIC)

        assertThat(result).isEqualTo("marhaban")
    }

    @Test
    fun `l falls back to english when requested language is missing`() {
        val localizer = MapLocalizer(
            strings = mapOf(
                (Table.LOCALIZABLE to Language.ENGLISH) to mapOf("greeting" to "hello"),
            ),
        )

        val result = localizer.l("greeting", language = Language.ARABIC)

        assertThat(result).isEqualTo("hello")
    }

    @Test
    fun `l falls back to key when no localization exists`() {
        val localizer = MapLocalizer()

        val result = localizer.l("missing")

        assertThat(result).isEqualTo("missing")
    }

    @Test
    fun `lAndroid reads from android table`() {
        val localizer = MapLocalizer(
            strings = mapOf(
                (Table.ANDROID to Language.ENGLISH) to mapOf("quran_page" to "Page"),
            ),
        )

        val result = localizer.lAndroid("quran_page")

        assertThat(result).isEqualTo("Page")
    }

    @Test
    fun `lFormat uses locale aware formatting`() {
        val previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
        try {
            val localizer = MapLocalizer(
                strings = mapOf(
                    (Table.LOCALIZABLE to Language.ENGLISH) to mapOf("greeting" to "Hello, %s"),
                ),
            )

            val result = localizer.lFormat("greeting", arguments = arrayOf("world"))

            assertThat(result).isEqualTo("Hello, world")
        } finally {
            Locale.setDefault(previousLocale)
        }
    }
}
