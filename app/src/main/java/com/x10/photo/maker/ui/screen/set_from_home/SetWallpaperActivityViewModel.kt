package com.x10.photo.maker.ui.screen.set_from_home

import androidx.lifecycle.ViewModel
import com.x10.photo.maker.data.realm_model.WallpaperDownloaded
import com.x10.photo.maker.repository.EditorRepository
import com.x10.photo.maker.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SetWallpaperActivityViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val editorRepository: EditorRepository
) : ViewModel() {

    fun saveWallpaperVideoUrl(wallpaperVideoUrl : String?){
        mainRepository.saveWallpaperVideoUrl(wallpaperVideoUrl)
    }

    fun saveURLWallpaperLiveSet(urlWallpaperLiveSet: String) = mainRepository.saveURLWallpaperLiveSet(urlWallpaperLiveSet)

    fun renameImageVideoUserCreated(wallpaperDownloaded: WallpaperDownloaded?) = editorRepository.renameImageVideoUserCreated(wallpaperDownloaded)
}