package com.quranengine.domain.annotationservice

import com.quranengine.data.annotation.persistence.LastPagePersistence
import com.quranengine.data.annotation.persistence.LastPagePersistenceModel
import com.quranengine.model.quranannotations.LastPage
import com.quranengine.model.qurankit.Page
import com.quranengine.model.qurankit.Quran
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class LastPageService(
    private val persistence: LastPagePersistence,
) {
    fun lastPages(quran: Quran): Flow<List<LastPage>> =
        persistence.lastPages().map { lastPages ->
            lastPages.mapNotNull { it.toLastPage(quran) }
        }

    suspend fun add(page: Page): LastPage {
        val model = persistence.add(page.pageNumber)
        return model.toLastPage(page.quran)!!
    }

    suspend fun update(page: Page, toPage: Page): LastPage {
        val model = persistence.update(page.pageNumber, toPage.pageNumber)
        return model.toLastPage(toPage.quran)!!
    }
}

private fun LastPagePersistenceModel.toLastPage(quran: Quran): LastPage? {
    val p = Page(quran, page) ?: return null
    return LastPage(
        page = p,
        createdOn = Instant.ofEpochMilli(createdOn),
        modifiedOn = Instant.ofEpochMilli(modifiedOn),
    )
}

// Page factory helper (mirroring iOS: Page(quran:pageNumber:))
private fun Page(quran: Quran, pageNumber: Int): Page? {
    return quran.pages.firstOrNull { it.pageNumber == pageNumber }
}
