package com.quranengine.ui.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuranPager(
    pageCount: Int,
    initialPage: Int = 0,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    onPageChanged: ((Int) -> Unit)? = null,
    state: PagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount }),
    pageContent: @Composable (page: Int) -> Unit,
) {
    LaunchedEffect(state) {
        if (onPageChanged != null) {
            snapshotFlow { state.currentPage }
                .distinctUntilChanged()
                .collect { page ->
                    onPageChanged(page)
                }
        }
    }

    HorizontalPager(
        state = state,
        modifier = modifier,
        reverseLayout = reverseLayout,
        beyondViewportPageCount = 1,
        key = { it },
    ) { page ->
        pageContent(page)
    }
}
