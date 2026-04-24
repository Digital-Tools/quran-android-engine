package com.quranengine.domain.wordframeservice

import com.quranengine.model.qurangeometry.WordFrame
import com.quranengine.model.qurangeometry.WordFrameCollection
import com.quranengine.model.qurangeometry.WordFrameLine

class WordFrameProcessor {

    fun processWordFrames(frames: List<WordFrame>): WordFrameCollection {
        if (frames.isEmpty()) {
            return WordFrameCollection(emptyList())
        }

        // Group by line
        var lines: MutableList<List<WordFrame>> = frames
            .groupBy { it.line }
            .toSortedMap()
            .values
            .toMutableList()

        lines = normalize(lines)
        lines = alignFramesVerticallyInEachLine(lines)
        lines = unionLinesVertically(lines)
        lines = unionFramesHorizontallyInEachLine(lines)
        lines = alignLineEdges(lines)

        val allFrames = lines.flatMap { it }
        return WordFrameCollection(allFrames)
    }

    private fun normalize(lines: MutableList<List<WordFrame>>): MutableList<List<WordFrame>> {
        return lines.map { line -> line.map { it.normalize() } }.toMutableList()
    }

    private fun alignFramesVerticallyInEachLine(lines: MutableList<List<WordFrame>>): MutableList<List<WordFrame>> {
        return lines.map { alignedVertically(it) }.toMutableList()
    }

    private fun unionLinesVertically(lines: MutableList<List<WordFrame>>): MutableList<List<WordFrame>> {
        val result = lines.toMutableList()
        for (i in 0 until result.size - 1) {
            val (newTop, newBottom) = unionVertically(result[i], result[i + 1])
            result[i] = newTop
            result[i + 1] = newBottom
        }
        return result
    }

    private fun unionFramesHorizontallyInEachLine(lines: MutableList<List<WordFrame>>): MutableList<List<WordFrame>> {
        return lines.map { line ->
            // Sort frames by minX descending (right-to-left text)
            val sorted = line.sortedByDescending { it.minX }.toMutableList()
            for (frameIndex in 0 until sorted.size - 1) {
                val (newLeft, newRight) = unionHorizontally(
                    leftFrame = sorted[frameIndex + 1],
                    rightFrame = sorted[frameIndex]
                )
                sorted[frameIndex + 1] = newLeft
                sorted[frameIndex] = newRight
            }
            sorted
        }.toMutableList()
    }

    private fun alignLineEdges(lines: MutableList<List<WordFrame>>): MutableList<List<WordFrame>> {
        // Align the edges
        var rightEdge = lines.map { it.first() }
        var leftEdge = lines.map { it.last() }
        leftEdge = unionLeftEdge(leftEdge)
        rightEdge = unionRightEdge(rightEdge)

        return lines.mapIndexed { i, line ->
            val mutableLine = line.toMutableList()
            mutableLine[0] = rightEdge[i]
            mutableLine[mutableLine.size - 1] = leftEdge[i]
            mutableLine.toList()
        }.toMutableList()
    }
}
