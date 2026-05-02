package com.quranengine.features.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.domain.annotationservice.NoteService
import com.quranengine.model.quranannotations.Note
import com.quranengine.model.qurankit.Quran
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val quran: Quran,
    private val noteService: NoteService,
) : ViewModel() {

    val notes: StateFlow<List<Note>> = noteService.notes(quran)
        .map { notes ->
            notes
                .filter { it.verses.isNotEmpty() && !it.note.isNullOrBlank() }
                .sortedByDescending { it.modifiedDate }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteService.removeNotes(note.verses.toList())
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }

    fun deleteAllNotes() {
        viewModelScope.launch {
            try {
                notes.value.forEach { note ->
                    noteService.removeNotes(note.verses.toList())
                }
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
