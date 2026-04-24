package com.quranengine.domain.qurantextkit

import com.quranengine.core.localization.Localizer
import com.quranengine.core.localization.NumberFormatters
import com.quranengine.data.versetext.VerseTextPersistence
import com.quranengine.domain.translationservice.LocalTranslationsRetriever
import com.quranengine.domain.translationservice.SelectedTranslationsPreferences
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurantext.QuranMode
import com.quranengine.model.qurantext.Translation
import com.quranengine.model.qurantext.TranslationText
import com.quranengine.model.qurantext.VerseText

class ShareableVerseTextRetriever(
    private val textService: QuranTextDataService,
    private val shareableVersePersistence: VerseTextPersistence,
    private val localTranslationsRetriever: LocalTranslationsRetriever,
    private val preferences: QuranContentStatePreferences,
    private val selectedTranslationsPreferences: SelectedTranslationsPreferences,
    private val localizer: Localizer,
    private val localizedAyahName: (AyahNumber) -> String,
) {
    suspend fun textForVerses(verses: List<AyahNumber>): List<String> {
        val arabicText = arabicScript(verses)
        val translationText = translations(verses)

        return arabicText + translationText + listOf("", versesSummary(verses))
    }

    private fun versesSummary(verses: List<AyahNumber>): String {
        return if (verses.size == 1) {
            localizedAyahName(verses[0])
        } else {
            "${localizedAyahName(verses[0])} - ${localizedAyahName(verses.last())}"
        }
    }

    private suspend fun arabicText(verse: AyahNumber): String {
        val verseNumber = NumberFormatters.arabic.format(verse.ayah.toLong())

        // Avoid the arabic text to be displayed in the wrong direction in LTR languages
        val rightToLeftMark = "\u202B"
        val endMark = "\u202C"

        val arabicVerse = shareableVersePersistence.textForVerse(verse) + "﴿ $verseNumber ﴾"
        return "$rightToLeftMark$arabicVerse$endMark"
    }

    private suspend fun arabicScript(verses: List<AyahNumber>): List<String> {
        val joined = verses.map { arabicText(it) }.joinToString(" ")
        return listOf(joined)
    }

    private suspend fun translations(verses: List<AyahNumber>): List<String> {
        if (preferences.quranMode != QuranMode.TRANSLATION) return emptyList()

        val translations = selectedTranslations()
        val verseTexts = textService.textForVerses(verses, translations)
        val orderedVerseTexts = verses.mapNotNull { verseTexts[it] }
        return versesTranslationsText(translations, orderedVerseTexts)
    }

    private fun versesTranslationsText(
        translations: List<Translation>,
        verseTexts: List<VerseText>,
    ): List<String> {
        val components = mutableListOf("")

        for ((index, translation) in translations.withIndex()) {
            // translator
            components.add("• ${translation.translationName}:")

            // translation text for all verses
            components.addAll(verseTexts.map { stringFromTranslationText(it.translations[index]) })

            // separate multiple translations
            components.add("")
        }

        return components.dropLast(1)
    }

    private suspend fun selectedTranslations(): List<Translation> {
        val localTranslations = localTranslationsRetriever.getLocalTranslations()
        return selectedTranslationsPreferences.selectedTranslations(localTranslations)
    }

    private fun stringFromTranslationText(text: TranslationText): String {
        return when (text) {
            is TranslationText.Reference -> localizer.lFormat(
                "translation.text.see-referenced-verse",
                arguments = arrayOf(text.ayah.ayah),
            )
            is TranslationText.StringText -> text.value.text
        }
    }
}
