package com.quranengine.model.quranaudio

sealed class AudioType {
    data class Gapless(val databaseName: String) : AudioType()
    data object Gapped : AudioType()
}
