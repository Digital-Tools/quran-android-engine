package com.quranengine.model.quranannotations

import com.quranengine.model.qurankit.Page
import java.time.Instant

data class LastPage(
    val page: Page,
    val createdOn: Instant,
    val modifiedOn: Instant
)
