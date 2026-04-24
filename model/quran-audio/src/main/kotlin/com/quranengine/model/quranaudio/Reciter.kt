package com.quranengine.model.quranaudio

data class Reciter(
    val id: Int,
    val nameKey: String,
    val directory: String,
    val audioURL: String,
    val audioType: AudioType,
    val hasGaplessAlternative: Boolean,
    val category: Category
) {
    enum class Category(val value: String) {
        ARABIC("arabic"),
        ENGLISH("english"),
        ARABIC_ENGLISH("arabicEnglish");

        companion object {
            fun fromValue(value: String): Category =
                entries.first { it.value == value }
        }
    }

    companion object
}
