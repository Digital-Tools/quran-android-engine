package com.quranengine.features.bookmarks

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quranengine.model.quranannotations.PageBookmark
import com.quranengine.ui.components.DataUnavailableView
import com.quranengine.ui.components.NoorAccessory
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.components.NoorSection
import com.quranengine.ui.theme.QuranTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel,
    onNavigateToPage: (PageBookmark) -> Unit,
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it.localizedMessage ?: "An error occurred")
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bookmarks",
                        color = QuranTheme.colors.text,
                    )
                },
                actions = {
                    if (bookmarks.isNotEmpty()) {
                        IconButton(onClick = { viewModel.deleteAllBookmarks() }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Delete all bookmarks",
                                tint = QuranTheme.colors.secondaryText,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = QuranTheme.colors.background,
                ),
            )
        },
        containerColor = QuranTheme.colors.background,
    ) { padding ->
        if (bookmarks.isEmpty()) {
            DataUnavailableView(
                title = "No Bookmarks",
                message = "Pages you bookmark will appear here.",
                modifier = Modifier.padding(padding),
                image = {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = QuranTheme.colors.secondaryText,
                        modifier = Modifier.size(64.dp),
                    )
                },
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                NoorSection(
                    items = bookmarks,
                    title = "Saved Pages",
                ) { bookmark ->
                    SwipeToDeleteBookmarkItem(
                        bookmark = bookmark,
                        onDelete = { viewModel.deleteBookmark(bookmark) },
                        onClick = { onNavigateToPage(bookmark) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteBookmarkItem(
    bookmark: PageBookmark,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                },
                label = "swipe-bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = Modifier.fillMaxWidth(),
    ) {
        val dateFormatter = remember {
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withZone(ZoneId.systemDefault())
        }

        NoorListItem(
            title = "Page ${bookmark.page.pageNumber}",
            subtitle = bookmark.page.startSura.let { "Sura ${it.suraNumber}" },
            rightSubtitle = dateFormatter.format(bookmark.creationDate),
            accessory = NoorAccessory.DisclosureIndicator,
            onClick = onClick,
        )
    }
}
