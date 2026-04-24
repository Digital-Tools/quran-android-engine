package com.quranengine.domain.qurantextkit

import com.quranengine.core.localization.Localizer
import com.quranengine.core.localization.Table
import com.quranengine.data.versetext.TranslationTextPersistenceModel
import com.quranengine.data.versetext.TranslationVerseTextPersistence
import com.quranengine.data.versetext.VerseTextPersistence
import com.quranengine.domain.translationservice.LocalTranslationsRetriever
import com.quranengine.domain.translationservice.SelectedTranslationsPreferences
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurantext.Translation
import com.quranengine.model.qurantext.TranslatedVerses
import com.quranengine.model.qurantext.TranslationString
import com.quranengine.model.qurantext.TranslationText
import com.quranengine.model.qurantext.VerseText
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.util.regex.Pattern

class QuranTextDataService(
    val localTranslationRetriever: LocalTranslationsRetriever,
    val arabicPersistence: VerseTextPersistence,
    val translationsPersistenceBuilder: (Translation) -> TranslationVerseTextPersistence,
    val selectedTranslationsPreferences: SelectedTranslationsPreferences,
    private val localizer: Localizer,
) {
    suspend fun textForVerses(
        verses: List<AyahNumber>,
        translations: List<Translation>,
    ): Map<AyahNumber, VerseText> {
        val translatedVerses = textForVersesInternal(verses) { translations }
        return verses.indices.associate { i -> verses[i] to translatedVerses.verses[i] }
    }

    internal suspend fun textForVersesInternal(
        verses: List<AyahNumber>,
        translations: suspend () -> List<Translation>,
    ): TranslatedVerses = coroutineScope {
        val asyncArabicText = async { retrieveArabicText(verses) }
        val asyncTranslationsText = async {
            val localTranslations = translations()
            fetchTranslationsText(verses, localTranslations)
        }

        val arabicText = asyncArabicText.await()
        val translationsText = asyncTranslationsText.await()
        TranslatedVerses(
            translations = translationsText.map { it.first },
            verses = merge(verses, translationsText, arabicText),
        )
    }

    private fun merge(
        verses: List<AyahNumber>,
        translations: List<Pair<Translation, List<TranslationText>>>,
        arabic: List<String>,
    ): List<VerseText> {
        return verses.indices.map { i ->
            val verse = verses[i]
            val arabicText = arabic[i]
            val ayahTranslations = translations.map { (_, ayahs) -> ayahs[i] }
            val prefix = if (verse == verse.sura.firstVerse && verse.sura.startsWithBesmAllah) {
                listOf(verse.quran.arabicBesmAllah)
            } else {
                emptyList()
            }
            VerseText(
                arabicText = arabicText,
                translations = ayahTranslations,
                arabicPrefix = prefix,
                arabicSuffix = emptyList(),
            )
        }
    }

    private suspend fun retrieveArabicText(verses: List<AyahNumber>): List<String> {
        val versesText = arabicPersistence.textForVerses(verses)
        return verses.map { versesText[it]!! }
    }

    private suspend fun fetchTranslationsText(
        verses: List<AyahNumber>,
        translations: List<Translation>,
    ): List<Pair<Translation, List<TranslationText>>> = coroutineScope {
        val results = translations.map { translation ->
            async { fetchTranslation(verses, translation) }
        }.awaitAll()

        // Preserve original ordering
        val byId = results.associateBy { it.first.id }
        translations.mapNotNull { byId[it.id] }
    }

    private suspend fun fetchTranslation(
        verses: List<AyahNumber>,
        translation: Translation,
    ): Pair<Translation, List<TranslationText>> {
        val translationPersistence = translationsPersistenceBuilder(translation)

        val verseTextList = mutableListOf<TranslationText>()
        try {
            val versesText = translationPersistence.textForVerses(verses)
            for (verse in verses) {
                val text = versesText[verse]
                    ?: TranslationTextPersistenceModel.Text(
                        localizer.l("error.translation.text-not-available")
                    )
                verseTextList.add(translationText(text))
            }
        } catch (e: Exception) {
            Timber.e(e, "Issue getting verse $verses, translation: ${translation.id}")
            val errorText = localizer.l("error.translation.text-retrieval")
            for (verse in verses) {
                verseTextList.add(
                    TranslationText.StringText(
                        TranslationString(
                            text = errorText,
                            quranRanges = emptyList(),
                            footnoteRanges = emptyList(),
                            footnotes = emptyList(),
                        )
                    )
                )
            }
        }
        return translation to verseTextList
    }

    private fun translationText(from: TranslationTextPersistenceModel): TranslationText {
        return when (from) {
            is TranslationTextPersistenceModel.Text -> TranslationText.StringText(translationString(from.text))
            is TranslationTextPersistenceModel.Reference -> TranslationText.Reference(from.verse)
        }
    }

    private fun translationString(originalString: String): TranslationString {
        val footnoteMatcher = FOOTNOTES_REGEX.matcher(originalString)
        val footnoteTextRanges = mutableListOf<IntRange>()
        while (footnoteMatcher.find()) {
            footnoteTextRanges.add(footnoteMatcher.start()..footnoteMatcher.end() - 1)
        }
        val footnotes = footnoteTextRanges.map { originalString.substring(it) }

        // Replace footnote markers with numbered footnotes
        val sb = StringBuilder()
        val footnoteRanges = mutableListOf<IntRange>()
        var lastEnd = 0
        for ((index, range) in footnoteTextRanges.withIndex()) {
            sb.append(originalString, lastEnd, range.first)
            val replacement = localizer.lFormat("translation.text.footnote-number", arguments = arrayOf(index + 1))
            val start = sb.length
            sb.append(replacement)
            footnoteRanges.add(start..sb.length - 1)
            lastEnd = range.last + 1
        }
        sb.append(originalString, lastEnd, originalString.length)
        val resultString = sb.toString()

        val quranMatcher = QURAN_REGEX.matcher(resultString)
        val quranRanges = mutableListOf<IntRange>()
        while (quranMatcher.find()) {
            quranRanges.add(quranMatcher.start()..quranMatcher.end() - 1)
        }

        return TranslationString(
            text = resultString,
            quranRanges = quranRanges,
            footnoteRanges = footnoteRanges,
            footnotes = footnotes,
        )
    }

    companion object {
        /** Regex to detect quran text in translation text. */
        val QURAN_REGEX: Pattern = Pattern.compile("([«{﴿][\\s\\S]*?[﴾}»])")

        /** Regex to detect footnotes in translation text. */
        val FOOTNOTES_REGEX: Pattern = Pattern.compile("\\[\\[[\\s\\S]*?]]")
    }
}
