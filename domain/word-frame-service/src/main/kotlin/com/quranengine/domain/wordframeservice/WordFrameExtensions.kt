package com.quranengine.domain.wordframeservice

import com.quranengine.model.qurangeometry.WordFrame

fun WordFrame.normalize(): WordFrame {
    var newMinX = minX
    var newMaxX = maxX
    var newMinY = minY
    var newMaxY = maxY
    if (newMinX > newMaxX) {
        val tmp = newMinX; newMinX = newMaxX; newMaxX = tmp
    }
    if (newMinY > newMaxY) {
        val tmp = newMinY; newMinY = newMaxY; newMaxY = tmp
    }
    return copy(minX = newMinX, maxX = newMaxX, minY = newMinY, maxY = newMaxY)
}

fun alignedVertically(list: List<WordFrame>): List<WordFrame> {
    val minY = list.minOfOrNull { it.minY } ?: 0
    val maxY = list.maxOfOrNull { it.maxY } ?: 0
    return list.map { it.copy(minY = minY, maxY = maxY) }
}

fun unionHorizontally(leftFrame: WordFrame, rightFrame: WordFrame): Pair<WordFrame, WordFrame> {
    return if (leftFrame.maxX < rightFrame.minX) {
        val middleX = (leftFrame.maxX + rightFrame.minX) / 2
        leftFrame.copy(maxX = middleX) to rightFrame.copy(minX = middleX)
    } else {
        leftFrame.copy(maxX = rightFrame.minX) to rightFrame
    }
}

/**
 * Adjusts the top and bottom lines to meet vertically with an equal gap,
 * but only if they belong to the same sura.
 */
fun unionVertically(
    top: List<WordFrame>,
    bottom: List<WordFrame>
): Pair<List<WordFrame>, List<WordFrame>> {
    // Early return if not continuous lines (different suras).
    if (top.lastOrNull()?.word?.verse?.sura != bottom.firstOrNull()?.word?.verse?.sura) {
        return top to bottom
    }

    val topMaxY = top.maxOfOrNull { it.maxY } ?: 0
    val bottomMinY = bottom.minOfOrNull { it.minY } ?: 0
    val middleY = (topMaxY + bottomMinY) / 2

    val newTop = top.map { it.copy(maxY = middleY) }
    val newBottom = bottom.map { it.copy(minY = middleY) }
    return newTop to newBottom
}

fun unionLeftEdge(list: List<WordFrame>): List<WordFrame> =
    unionEdge(list, getter = { it.minX }, copier = { frame, value -> frame.copy(minX = value) }) { it.min() }

fun unionRightEdge(list: List<WordFrame>): List<WordFrame> =
    unionEdge(list, getter = { it.maxX }, copier = { frame, value -> frame.copy(maxX = value) }) { it.max() }

private inline fun unionEdge(
    list: List<WordFrame>,
    getter: (WordFrame) -> Int,
    copier: (WordFrame, Int) -> WordFrame,
    reduce: (List<Int>) -> Int
): List<WordFrame> {
    val value = reduce(list.map(getter))
    return list.map { copier(it, value) }
}
