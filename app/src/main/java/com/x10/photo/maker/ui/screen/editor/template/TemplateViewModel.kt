package com.x10.photo.maker.ui.screen.editor.template

import androidx.lifecycle.ViewModel
import com.x10.photo.maker.repository.EditorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(editorRepository: EditorRepository) : ViewModel() {
}