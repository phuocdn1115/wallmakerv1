package com.x10.photo.maker.ui.screen.editor.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.x10.photo.maker.repository.EditorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(editorRepository: EditorRepository): ViewModel() {
    private val progressing = MutableLiveData<List<Int>>()
    val myProgressing : LiveData<List<Int>> = progressing
    fun sendProgressSavingVideo(progress: Int, totalDuration: Int){
        val listData: ArrayList<Int> = arrayListOf()
        listData.apply {
            add(progress)
            add(totalDuration)
        }
        progressing.postValue(listData)
    }

    private val imageThumbSavingVideo = MutableLiveData<String?>()
    val currentImageThumbSavingVideo : LiveData<String?> = imageThumbSavingVideo
    fun setImageThumbSavingVideo(image: String?){
        imageThumbSavingVideo.postValue(image)
    }

}