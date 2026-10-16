package com.quranengine.domain.qurantextkit

import com.google.common.truth.Truth.assertThat
import com.quranengine.model.qurankit.Quran
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SuraSearcherTest {
    private val quran = Quran.hafsMadani1405
    private val searcher = SuraSearcher { number, _, language ->
        val sura = quran.suras[number - 1]
        if (language == "ar") sura.plainArabicName() else sura.englishName()
    }

    private fun suraNumbers(query: String): List<Int> = runBlocking {
        searcher.search(SearchTerm(query)!!, quran)
            .flatMap { it.items }
            .map { it.ayah.sura.suraNumber }
    }

    @Test
    fun `Latin names find the sura without diacritics`() {
        assertThat(suraNumbers("kahf")).containsExactly(18)
        assertThat(suraNumbers("yasin")).containsExactly(36)
        assertThat(suraNumbers("fatiha")).containsExactly(1)
    }

    @Test
    fun `names typed without hyphens or ayn marks still match`() {
        assertThat(suraNumbers("al kahf")).containsExactly(18)
        assertThat(suraNumbers("alkahf")).containsExactly(18)
        assertThat(suraNumbers("ali imran")).containsExactly(3)
    }

    @Test
    fun `Arabic names find the sura`() {
        assertThat(suraNumbers("الكهف")).containsExactly(18)
    }

    @Test
    fun `a loose match shows the proper name`() {
        val item = runBlocking { searcher.search(SearchTerm("al kahf")!!, quran) }.single().items.single()
        assertThat(item.text).isEqualTo("Al-Kahf")
    }
}
