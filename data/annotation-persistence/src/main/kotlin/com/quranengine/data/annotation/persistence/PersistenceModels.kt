package com.quranengine.data.annotation.persistence

data class LastPagePersistenceModel(
    val page: Int,
    val createdOn: Long,
    val modifiedOn: Long,
)

data class PageBookmarkPersistenceModel(
    val page: Int,
    val creationDate: Long,
)

data class VersePersistenceModel(
    val sura: Int,
    val ayah: Int,
)

data class NotePersistenceModel(
    val verses: Set<VersePersistenceModel>,
    val modifiedDate: Long,
    val note: String?,
    val color: Int,
)
