package com.x10.photo.maker.ui.screen.preview_from_home

import androidx.lifecycle.ViewModel
import com.x10.photo.maker.base.SingleLiveEvent
import com.x10.photo.maker.base.Result
import com.x10.photo.maker.data.model.Wallpaper
import com.x10.photo.maker.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PreviewActivityViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    val downloadWallpaperVideoResult = SingleLiveEvent<Result<String>>()
    fun downloadVideo(dataModel: Wallpaper) {
        val request = mainRepository.downloadVideo(dataModel)
        downloadWallpaperVideoResult.addSource(request) {
            downloadWallpaperVideoResult.postValue(it)
        }
    }
}