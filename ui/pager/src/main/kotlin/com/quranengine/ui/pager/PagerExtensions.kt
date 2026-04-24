package com.quranengine.ui.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
fun PagerState.animateToPage(scope: CoroutineScope, page: Int) {
    scope.launch {
        animateScrollToPage(page)
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun PagerState.scrollToPageImmediate(scope: CoroutineScope, page: Int) {
    scope.launch {
        scrollToPage(page)
    }
}
