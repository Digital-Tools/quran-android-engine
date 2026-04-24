package com.quranengine.domain.translationservice

import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.PreferenceTransformer
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.preferences.TransformedPreference
import com.quranengine.model.qurantext.Translation
import kotlinx.coroutines.flow.Flow

class SelectedTranslationsPreferences(preferences: Preferences) {

    private val delegate = TransformedPreference(
        key = SELECTED_TRANSLATIONS_KEY,
        preferences = preferences,
        transformer = idsTransformer,
    )
    private val preferencesRef = preferences

    var selectedTranslationIds: List<Int>
        get() = delegate.getValue(this, ::selectedTranslationIds)
        set(value) = delegate.setValue(this, ::selectedTranslationIds, value)

    val selectedTranslationIdsFlow: Flow<List<Int>>
        get() = delegate.flow

    fun remove(translationId: Int) {
        val ids = selectedTranslationIds.toMutableList()
        if (ids.remove(translationId)) {
            selectedTranslationIds = ids
        }
    }

    fun isSelected(translationId: Int): Boolean =
        selectedTranslationIds.contains(translationId)

    fun toggleSelection(translationId: Int) {
        val ids = selectedTranslationIds.toMutableList()
        val index = ids.indexOf(translationId)
        if (index >= 0) {
            ids.removeAt(index)
        } else {
            ids.add(translationId)
        }
        selectedTranslationIds = ids
    }

    fun select(id: Int) {
        if (!selectedTranslationIds.contains(id)) {
            selectedTranslationIds = selectedTranslationIds + id
        }
    }

    fun deselect(id: Int) {
        val ids = selectedTranslationIds.toMutableList()
        if (ids.remove(id)) {
            selectedTranslationIds = ids
        }
    }

    fun selectedTranslations(from: List<Translation>): List<Translation> {
        val selected = selectedTranslationIds
        val byId = from.associateBy { it.id }
        return selected.mapNotNull { byId[it] }
    }

    fun reset() {
        preferencesRef.removeValueForKey(SELECTED_TRANSLATIONS_KEY)
    }

    companion object {
        private val SELECTED_TRANSLATIONS_KEY = PreferenceKey("selectedTranslations", "")

        private val idsTransformer = PreferenceTransformer<String, List<Int>>(
            rawToValue = { raw ->
                if (raw.isBlank()) emptyList()
                else raw.split(",").mapNotNull { it.trim().toIntOrNull() }
            },
            valueToRaw = { ids -> ids.joinToString(",") },
        )
    }
}
