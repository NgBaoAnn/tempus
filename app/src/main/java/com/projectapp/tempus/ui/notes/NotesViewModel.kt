package com.projectapp.tempus.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectapp.tempus.data.notes.NotesRepository
import com.projectapp.tempus.data.notes.entity.NoteEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State cho Notes screen
 */
data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedNoteId: String? = null,
    val error: String? = null
)

/**
 * UI State cho Note Editor
 */
data class NoteEditorState(
    val noteId: String? = null,
    val title: String = "",
    val content: String = "",
    val isNew: Boolean = true,
    val isSaving: Boolean = false
)

/**
 * ViewModel cho Notes feature
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = NotesRepository(application)
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    
    // Main UI state
    private val _uiState = MutableStateFlow(NotesUiState(isLoading = true))
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()
    
    // Editor state
    private val _editorState = MutableStateFlow(NoteEditorState())
    val editorState: StateFlow<NoteEditorState> = _editorState.asStateFlow()
    
    init {
        // Collect notes based on search query
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
    
    /**
     * Cập nhật search query
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    /**
     * Xóa search query
     */
    fun clearSearch() {
        onSearchQueryChange("")
    }
    
    /**
     * Bắt đầu tạo ghi chú mới
     */
    fun startNewNote() {
        _editorState.value = NoteEditorState(
            noteId = null,
            title = "",
            content = "",
            isNew = true,
            isSaving = false
        )
    }
    
    /**
     * Bắt đầu chỉnh sửa ghi chú
     */
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
    
    /**
     * Cập nhật title trong editor
     */
    fun onTitleChange(title: String) {
        _editorState.update { it.copy(title = title) }
    }
    
    /**
     * Cập nhật content trong editor
     */
    fun onContentChange(content: String) {
        _editorState.update { it.copy(content = content) }
    }
    
    /**
     * Lưu ghi chú (tạo mới hoặc cập nhật)
     */
    fun saveNote(onComplete: () -> Unit) {
        val state = _editorState.value
        
        // Không lưu nếu cả title và content đều rỗng
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
    
    /**
     * Xóa ghi chú
     */
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }
    
    /**
     * Toggle pin ghi chú
     */
    fun togglePin(noteId: String) {
        viewModelScope.launch {
            repository.togglePin(noteId)
        }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
