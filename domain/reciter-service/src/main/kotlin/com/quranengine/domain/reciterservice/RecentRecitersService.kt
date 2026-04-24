package com.quranengine.domain.reciterservice

import com.quranengine.model.quranaudio.Reciter

class RecentRecitersService(private val preferences: ReciterPreferences) {

    fun recentReciters(allReciters: List<Reciter>): List<Reciter> {
        val recentIds = preferences.recentReciterIds
        return recentIds
            .mapNotNull { id -> allReciters.firstOrNull { it.id == id } }
            .reversed()
    }

    fun updateRecentRecitersList(reciter: Reciter) {
        val recentReciterIds = LinkedHashSet(preferences.recentReciterIds)

        // Remove so it can go to the front (recently selected)
        recentReciterIds.remove(reciter.id)

        // Remove the least recently selected reciter if at max capacity
        if (recentReciterIds.isNotEmpty() && recentReciterIds.size >= MAX_RECENT_RECITERS) {
            recentReciterIds.remove(recentReciterIds.first())
        }
        recentReciterIds.add(reciter.id)

        preferences.recentReciterIds = recentReciterIds
    }

    companion object {
        private const val MAX_RECENT_RECITERS = 3
    }
}
