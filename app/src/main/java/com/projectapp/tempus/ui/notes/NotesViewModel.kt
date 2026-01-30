package com.projectapp.tempus.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.notes.NotesRepository
import com.projectapp.tempus.data.notes.entity.NoteEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedNoteId: String? = null,
    val error: String? = null
)


data class NoteEditorState(
    val noteId: String? = null,
    val title: String = "",
    val content: String = "",
    val isNew: Boolean = true,
    val isSaving: Boolean = false
)


@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = NotesRepository(application)
    
    
    private val _searchQuery = MutableStateFlow("")
    
    
    private val _uiState = MutableStateFlow(NotesUiState(isLoading = true))
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()
    
    
    private val _editorState = MutableStateFlow(NoteEditorState())
    val editorState: StateFlow<NoteEditorState> = _editorState.asStateFlow()
    
    init {
        
        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.getAllNotes()
                } else {
                    repository.searchNotes(query)
                }
            }.collect { notes ->
                _uiState.update { 
                    it.copy(notes = notes, isLoading = false) 
                }
            }
        }
    }
    
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    
    fun clearSearch() {
        onSearchQueryChange("")
    }
    
    
    fun startNewNote() {
        _editorState.value = NoteEditorState(
            noteId = null,
            title = "",
            content = "",
            isNew = true,
            isSaving = false
        )
    }
    
    
    fun startEditNote(noteId: String) {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            if (note != null) {
                _editorState.value = NoteEditorState(
                    noteId = note.id,
                    title = note.title,
                    content = note.content,
                    isNew = false,
                    isSaving = false
                )
            }
        }
    }
    
    
    fun onTitleChange(title: String) {
        _editorState.update { it.copy(title = title) }
    }
    
    
    fun onContentChange(content: String) {
        _editorState.update { it.copy(content = content) }
    }
    
    
    fun saveNote(onComplete: () -> Unit) {
        val state = _editorState.value
        
        
        if (state.title.isBlank() && state.content.isBlank()) {
            onComplete()
            return
        }
        
        _editorState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                if (state.isNew || state.noteId == null) {
                    repository.createNote(state.title, state.content)
                } else {
                    repository.updateNote(state.noteId, state.title, state.content)
                }
                onComplete()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _editorState.update { it.copy(isSaving = false) }
            }
        }
    }
    
    
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }
    
    
    fun togglePin(noteId: String) {
        viewModelScope.launch {
            repository.togglePin(noteId)
        }
    }
    
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
