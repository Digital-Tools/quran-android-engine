package com.quranengine.model.qurangeometry

import android.graphics.PointF
import android.graphics.RectF
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Word

class WordFrameCollection(
    val frames: List<WordFrame>
) {
    private val framesByVerse: Map<AyahNumber, List<WordFrame>> by lazy {
        frames.groupBy { it.word.verse }
    }

    fun wordFramesForVerse(verse: AyahNumber): List<WordFrame> =
        framesByVerse[verse] ?: emptyList()

    fun lineFramesForVerse(verse: AyahNumber): List<WordFrameLine> {
        val verseFrames = wordFramesForVerse(verse)
        return verseFrames
            .groupBy { it.line }
            .toSortedMap()
            .values
            .map { WordFrameLine(it) }
    }

    fun wordFrameForWord(word: Word): WordFrame? =
        framesByVerse[word.verse]?.firstOrNull { it.word == word }

    fun wordAtLocation(point: PointF): Word? {
        val frame = frames.firstOrNull { it.rect.contains(point.x, point.y) } ?: return null
        return frame.word
    }

    fun topPadding(): Float {
        if (frames.isEmpty()) return 0f
        return frames.minOf { it.minY }.toFloat()
    }

    fun boundsForVerse(verse: AyahNumber): RectF? {
        val verseFrames = wordFramesForVerse(verse)
        if (verseFrames.isEmpty()) return null
        return RectF(
            verseFrames.minOf { it.minX }.toFloat(),
            verseFrames.minOf { it.minY }.toFloat(),
            verseFrames.maxOf { it.maxX }.toFloat(),
            verseFrames.maxOf { it.maxY }.toFloat()
        )
    }
}
