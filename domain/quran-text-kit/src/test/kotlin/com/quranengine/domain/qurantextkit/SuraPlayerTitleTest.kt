package com.quranengine.domain.qurantextkit

import com.google.common.truth.Truth.assertThat
import com.quranengine.core.localization.Language
import com.quranengine.core.localization.MapLocalizer
import com.quranengine.core.localization.Table
import com.quranengine.model.qurankit.Quran
import org.junit.Test

class SuraPlayerTitleTest {
    private val taHa = Quran.hafsMadani1405.suras[19]

    @Test
    fun `without loaded sura names the player shows the English name, not a key`() {
        assertThat(taHa.playerTitle(MapLocalizer())).isEqualTo("Surah Ṭā-Hā")
        assertThat(Quran.hafsMadani1405.suras[0].playerTitle(MapLocalizer())).isEqualTo("Surah Al-Fātihah")
    }

    @Test
    fun `loaded sura names are used as they are`() {
        val localizer = MapLocalizer(
            strings = mapOf((Table.SURAS to Language.ENGLISH) to mapOf("sura_names[19]" to "Taha")),
        )
        assertThat(taHa.playerTitle(localizer)).isEqualTo("Taha")
    }
}
