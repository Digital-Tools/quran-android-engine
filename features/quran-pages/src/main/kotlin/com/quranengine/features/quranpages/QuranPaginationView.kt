package com.quranengine.features.quranpages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.quranengine.ui.pager.QuranPager
import com.quranengine.ui.quran.PageMiddleSeparator
import com.quranengine.ui.quran.PageSide
import com.quranengine.ui.quran.PageSideSeparator
import com.quranengine.ui.theme.themedBackground

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuranPaginationView(
    pagingStrategy: PagingStrategy,
    pages: List<Int>,
    selectedPages: List<Int>,
    onPagesChanged: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    pageContent: @Composable (page: Int) -> Unit,
) {
    // Force RTL layout for Quran (pages go right-to-left)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .themedBackground(),
        ) {
            when (pagingStrategy) {
                PagingStrategy.SINGLE_PAGE -> {
                    SinglePagePager(
                        pages = pages,
                        selectedPage = selectedPages.firstOrNull() ?: pages.first(),
                        onPageChanged = { onPagesChanged(listOf(it)) },
                        pageContent = pageContent,
                    )
                }
                PagingStrategy.DOUBLE_PAGE -> {
                    DoublePagePager(
                        pages = pages,
                        selectedPages = selectedPages,
                        onPagesChanged = onPagesChanged,
                        pageContent = pageContent,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SinglePagePager(
    pages: List<Int>,
    selectedPage: Int,
    onPageChanged: (Int) -> Unit,
    pageContent: @Composable (page: Int) -> Unit,
) {
    val initialIndex = pages.indexOf(selectedPage).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { pages.size })

    LaunchedEffect(selectedPage) {
        val targetIndex = pages.indexOf(selectedPage)
        if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val page = pages.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        onPageChanged(page)
    }

    QuranPager(
        pageCount = pages.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { index ->
        val page = pages[index]
        val isRightSide = page % 2 == 1

        Row(modifier = Modifier.fillMaxSize()) {
            if (isRightSide) {
                PageSideSeparator(side = PageSide.START)
                Box(modifier = Modifier.weight(1f)) {
                    pageContent(page)
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    pageContent(page)
                }
                PageSideSeparator(side = PageSide.END)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DoublePagePager(
    pages: List<Int>,
    selectedPages: List<Int>,
    onPagesChanged: (List<Int>) -> Unit,
    pageContent: @Composable (page: Int) -> Unit,
) {
    // Pair pages into double-page spreads
    val doublePages = remember(pages) {
        (0 until pages.size step 2).map { i ->
            pages[i] to pages.getOrElse(i + 1) { pages[i] }
        }
    }

    val selectedIndex = remember(selectedPages, doublePages) {
        val firstSelected = selectedPages.firstOrNull() ?: pages.first()
        val pageIndex = pages.indexOf(firstSelected).coerceAtLeast(0)
        pageIndex / 2
    }

    val pagerState = rememberPagerState(initialPage = selectedIndex, pageCount = { doublePages.size })

    LaunchedEffect(selectedPages) {
        val firstSelected = selectedPages.firstOrNull() ?: pages.first()
        val pageIndex = pages.indexOf(firstSelected).coerceAtLeast(0)
        val targetIndex = pageIndex / 2
        if (pagerState.currentPage != targetIndex) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val (first, second) = doublePages.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        onPagesChanged(listOf(first, second))
    }

    QuranPager(
        pageCount = doublePages.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { index ->
        val (firstPage, secondPage) = doublePages[index]
        Row(modifier = Modifier.fillMaxSize()) {
            PageSideSeparator(side = PageSide.START)
            Box(modifier = Modifier.weight(1f)) {
                pageContent(firstPage)
            }
            PageMiddleSeparator()
            Box(modifier = Modifier.weight(1f)) {
                pageContent(secondPage)
            }
            PageSideSeparator(side = PageSide.END)
        }
    }
}
