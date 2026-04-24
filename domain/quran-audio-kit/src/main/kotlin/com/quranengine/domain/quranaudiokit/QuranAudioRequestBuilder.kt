package com.quranengine.domain.quranaudiokit

import com.quranengine.core.audioplayer.AudioRequest
import com.quranengine.core.audioplayer.PlayerItemInfo
import com.quranengine.core.audioplayer.Runs
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.qurankit.AyahNumber

interface QuranAudioRequest {
    fun getRequest(): AudioRequest
    fun getAyahNumberFrom(fileIndex: Int, frameIndex: Int): AyahNumber
    fun getPlayerInfo(fileIndex: Int): PlayerItemInfo
}

interface QuranAudioRequestBuilder {
    suspend fun buildRequest(
        reciter: Reciter,
        from: AyahNumber,
        to: AyahNumber,
        frameRuns: Runs,
        requestRuns: Runs,
    ): QuranAudioRequest
}
