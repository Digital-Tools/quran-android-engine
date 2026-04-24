package com.quranengine.model.quranannotations

import com.quranengine.model.qurankit.Page
import java.time.Instant

data class PageBookmark(
    val page: Page,
    val creationDate: Instant
)
