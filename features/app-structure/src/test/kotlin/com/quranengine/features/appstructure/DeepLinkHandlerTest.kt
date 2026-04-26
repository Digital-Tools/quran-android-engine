package com.quranengine.features.appstructure

import com.google.common.truth.Truth.assertThat
import com.quranengine.model.qurankit.Quran
import org.junit.Test

class DeepLinkHandlerTest {
    private val handler = DeepLinkHandler(Quran.hafsMadani1405)

    @Test
    fun `maps page deep link to quran route`() {
        val route = handler.handle("quranengine://page/2")

        assertThat(route).isEqualTo(AppRoute.QuranView(2))
    }

    @Test
    fun `maps sura deep link to first page of sura`() {
        val route = handler.handle("quranengine://sura/2")

        assertThat(route).isEqualTo(AppRoute.QuranView(2))
    }

    @Test
    fun `maps search deep link with query`() {
        val route = handler.handle("quranengine://search?q=mercy")

        assertThat(route).isEqualTo(AppRoute.Search("mercy"))
    }

    @Test
    fun `maps search deep link without query to empty search`() {
        val route = handler.handle("quranengine://search")

        assertThat(route).isEqualTo(AppRoute.Search())
    }

    @Test
    fun `rejects invalid page deep link`() {
        val route = handler.handle("quranengine://page/9999")

        assertThat(route).isNull()
    }

    @Test
    fun `rejects unsupported scheme`() {
        val route = handler.handle("https://quranengine/page/2")

        assertThat(route).isNull()
    }
}
