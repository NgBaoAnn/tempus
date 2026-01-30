package com.projectapp.tempus.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.projectapp.tempus.ui.notes.compose.NoteEditorScreen
import com.projectapp.tempus.ui.notes.compose.NotesScreen
import com.projectapp.tempus.ui.theme.TempusTheme


class NotesFragment : Fragment() {
    
    private val viewModel: NotesViewModel by viewModels()
    
    
    private var isEditing by mutableStateOf(false)
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                TempusTheme {
                    val uiState by viewModel.uiState.collectAsState()
                    val editorState by viewModel.editorState.collectAsState()
                    
                    if (isEditing) {
                        NoteEditorScreen(
                            title = editorState.title,
                            content = editorState.content,
                            isNew = editorState.isNew,
                            isSaving = editorState.isSaving,
                            onTitleChange = viewModel::onTitleChange,
                            onContentChange = viewModel::onContentChange,
                            onSaveClick = {
                                viewModel.saveNote {
                                    isEditing = false
                                }
                            },
                            onDeleteClick = {
                                editorState.noteId?.let { viewModel.deleteNote(it) }
                                isEditing = false
                            },
                            onBackClick = {
                                isEditing = false
                            }
                        )
                    } else {
                        NotesScreen(
                            notes = uiState.notes,
                            searchQuery = uiState.searchQuery,
                            isLoading = uiState.isLoading,
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            onClearSearch = viewModel::clearSearch,
                            onNoteClick = { note ->
                                viewModel.startEditNote(note.id)
                                isEditing = true
                            },
                            onAddClick = {
                                viewModel.startNewNote()
                                isEditing = true
                            },
                            onPinClick = { note ->
                                viewModel.togglePin(note.id)
                            },
                            onDeleteClick = { note ->
                                viewModel.deleteNote(note.id)
                            },
                            onBackClick = {
                                findNavController().popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
