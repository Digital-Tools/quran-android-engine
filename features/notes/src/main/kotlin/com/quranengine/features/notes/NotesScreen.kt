package com.quranengine.features.notes

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.StickyNote2
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
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.model.quranannotations.Note
import com.quranengine.model.qurankit.AyahNumber
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
fun NotesScreen(
    viewModel: NotesViewModel,
    onBack: () -> Unit,
    onNavigateToAyah: (AyahNumber) -> Unit,
) {
    val notes by viewModel.notes.collectAsState()
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
                        text = "Notes",
                        color = QuranTheme.colors.text,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = QuranTheme.colors.text,
                        )
                    }
                },
                actions = {
                    if (notes.isNotEmpty()) {
                        IconButton(onClick = { viewModel.deleteAllNotes() }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Delete all notes",
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
        if (notes.isEmpty()) {
            DataUnavailableView(
                title = "No Notes",
                message = "Notes you add to ayahs will appear here.",
                modifier = Modifier.padding(padding),
                image = {
                    Icon(
                        imageVector = Icons.Outlined.StickyNote2,
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
                    items = notes,
                    title = "Saved Notes",
                ) { note ->
                    SwipeToDeleteNoteItem(
                        note = note,
                        onDelete = { viewModel.deleteNote(note) },
                        onClick = { onNavigateToAyah(note.firstVerse) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteNoteItem(
    note: Note,
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
            title = note.note.orEmpty(),
            subtitle = note.firstVerse.referenceLabel(),
            rightSubtitle = dateFormatter.format(note.modifiedDate),
            leadingEdgeColor = note.color.uiColor(),
            accessory = NoorAccessory.DisclosureIndicator,
            onClick = onClick,
        )
    }
}

private fun AyahNumber.referenceLabel(): String =
    "${sura.englishName()} ${sura.suraNumber}:$ayah"

private fun Note.Color.uiColor(): Color =
    when (this) {
        Note.Color.RED -> Color(0xFFE57373)
        Note.Color.GREEN -> Color(0xFF81C784)
        Note.Color.BLUE -> Color(0xFF64B5F6)
        Note.Color.YELLOW -> Color(0xFFFFC107)
        Note.Color.PURPLE -> Color(0xFFBA68C8)
    }
