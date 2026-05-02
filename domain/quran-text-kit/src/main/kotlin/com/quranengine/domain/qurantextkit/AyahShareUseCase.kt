package com.quranengine.domain.qurantextkit

import android.content.Context
import android.content.Intent
import androidx.core.app.ShareCompat
import com.quranengine.core.localization.Localizer
import com.quranengine.domain.translationservice.SelectedTranslationsPreferences
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurantext.TranslationText
import com.quranengine.model.qurantext.VerseText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AyahShareUseCase(
    private val context: Context,
    private val quranTextDataService: QuranTextDataService,
    private val selectedTranslationsPreferences: SelectedTranslationsPreferences,
    private val localizer: Localizer,
) {
    suspend fun share(ayah: AyahNumber) {
        val text = shareText(ayah)
        withContext(Dispatchers.Main) {
            val intent = ShareCompat.IntentBuilder(context)
                .setType("text/plain")
                .setText(text)
                .setChooserTitle("Share ayah")
                .createChooserIntent()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    suspend fun shareText(ayah: AyahNumber): String {
        val verseText = verseText(ayah)
        return "${verseText.fullArabicText()} — ${ayah.sura.englishName()} [${ayah.sura.suraNumber}:${ayah.ayah}]"
    }

    suspend fun clipboardText(ayah: AyahNumber): String {
        val selectedTranslations = selectedTranslations()
        val verseText = quranTextDataService.textForVerses(listOf(ayah), selectedTranslations).getValue(ayah)
        val components = mutableListOf(shareText(ayah))

        selectedTranslations.forEachIndexed { index, translation ->
            components.add("")
            components.add("${translation.translationName}:")
            components.add(verseText.translations[index].displayText())
        }

        return components.joinToString(separator = "\n")
    }

    private suspend fun verseText(ayah: AyahNumber): VerseText =
        quranTextDataService.textForVerses(listOf(ayah), emptyList()).getValue(ayah)

    private suspend fun selectedTranslations() =
        selectedTranslationsPreferences.selectedTranslations(
            quranTextDataService.localTranslationRetriever.getLocalTranslations()
        )

    private fun TranslationText.displayText(): String =
        when (this) {
            is TranslationText.Reference -> localizer.lFormat(
                "translation.text.see-referenced-verse",
                arguments = arrayOf(ayah.ayah),
            )
            is TranslationText.StringText -> value.text
        }

    private fun VerseText.fullArabicText(): String =
        buildList {
            addAll(arabicPrefix)
            add(arabicText)
            addAll(arabicSuffix)
        }.filter { it.isNotBlank() }
            .joinToString(separator = " ")
}
