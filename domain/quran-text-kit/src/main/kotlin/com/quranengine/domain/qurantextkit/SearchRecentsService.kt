package com.quranengine.domain.qurantextkit

import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.PreferenceTransformer
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.preferences.TransformedPreference
import kotlinx.coroutines.flow.Flow

class SearchRecentsService(private val preferences: Preferences) {

    private val delegate = TransformedPreference(
        key = SEARCH_RECENT_ITEMS,
        preferences = preferences,
        transformer = SEARCH_RECENT_ITEMS_TRANSFORMER,
    )

    val popularTerms: List<String> = listOf(
        "الرحمن",
        "الحي القيوم",
        "يس",
        "7",
        "5:88",
        "تبارك",
        "عم",
        "أعوذ",
    )

    var recentSearchItems: List<String>
        get() = delegate.getValue(this, ::recentSearchItems)
        set(value) = delegate.setValue(this, ::recentSearchItems, value)

    val recentSearchItemsFlow: Flow<List<String>>
        get() = delegate.flow

    fun addToRecents(term: String) {
        val recents = recentSearchItems.toMutableList()
        recents.remove(term)
        recents.add(0, term)
        if (recents.size > MAX_COUNT) {
            recentSearchItems = recents.take(MAX_COUNT)
        } else {
            recentSearchItems = recents
        }
    }

    fun reset() {
        preferences.removeValueForKey(SEARCH_RECENT_ITEMS)
    }

    companion object {
        private const val MAX_COUNT = 5

        private val SEARCH_RECENT_ITEMS =
            PreferenceKey("com.quran.searchRecentItems", "")

        private val SEARCH_RECENT_ITEMS_TRANSFORMER = PreferenceTransformer<String, List<String>>(
            rawToValue = { raw ->
                if (raw.isBlank()) emptyList()
                else raw.split("\u001F").filter { it.isNotEmpty() }.orderedUnique()
            },
            valueToRaw = { items -> items.joinToString("\u001F") },
        )
    }
}
