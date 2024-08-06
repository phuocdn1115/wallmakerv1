package com.x10.photo.maker.ui.screen.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.x10.photo.maker.data.model.DataTemplateVideo
import com.x10.photo.maker.data.model.TemplateVideo
import com.x10.photo.maker.model.ImageSelected
import com.x10.photo.maker.repository.EditorRepository
import com.hw.photomovie.PhotoMovieFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.x10.photo.maker.data.model.Template
import com.x10.photo.maker.data.realm_model.WallpaperDownloaded
import com.x10.photo.maker.repository.SystemRepository

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val editorRepository: EditorRepository,
    private val systemRepository: SystemRepository
) : ViewModel(){

    var isFromHandleImageScreenToPickImageScreen = false
    var imageSelectedList = ArrayList<ImageSelected?>()
    var listStringImageSelected = ArrayList<String?>()
    var currentTemplateSelected: TemplateVideo?= null
    private var currentDataTemplateSelected = DataTemplateVideo()

    private val currentImageSelected = MutableLiveData<List<ImageSelected?>>()
    val myCurrentImageSelected: LiveData<List<ImageSelected?>> = currentImageSelected

    fun setCurrentImageSelected(imageSelected: List<String?>) {
        listStringImageSelected = imageSelected as ArrayList<String?>
        currentTemplateSelected = TemplateVideo(PhotoMovieFactory.PhotoMovieType.HORIZONTAL_TRANS, isSelected = true)
        imageSelectedList.clear()
        for (i in imageSelected.indices) {
            val imageSelect = ImageSelected()
            imageSelect.uriInput = imageSelected[i]
            imageSelect.id = i
            if (i == 0) imageSelect.isSelected = true
            imageSelectedList.add(imageSelect)
        }
        currentImageSelected.postValue(imageSelectedList)
        currentDataTemplateSelected.listImageSelected = imageSelectedList
        currentDataTemplateSelected.templateVideo = currentTemplateSelected
    }

    fun updateImageSelectedAfterEdit(imageSelected: ImageSelected?, position: Int = 0){
        imageSelectedList[position] = imageSelected
    }

    private val currentDataTemplateVideo = MutableLiveData<DataTemplateVideo?>()
    val myCurrentDataTemplateVideo: LiveData<DataTemplateVideo?> = currentDataTemplateVideo
    fun setCurrentDataTemplateVideo(){
        currentDataTemplateSelected.templateVideo = currentTemplateSelected
        currentDataTemplateSelected.listImageSelected = imageSelectedList
        currentDataTemplateVideo.postValue(currentDataTemplateSelected)
    }

    private val previewMode = MutableLiveData<Int>()
    val myPreviewMode: LiveData<Int> = previewMode

    fun setUpUIPreviewMode(){
        previewMode.postValue(listStringImageSelected.size)
    }

    private val objectImageOrVideoCreated = MutableLiveData<WallpaperDownloaded?>()
    val myObjectImageOrVideoCreated: LiveData<WallpaperDownloaded?> = objectImageOrVideoCreated
    fun setSavedFilePath(wallpaperDownloaded: WallpaperDownloaded) {
        objectImageOrVideoCreated.postValue(wallpaperDownloaded)
    }

    private val imageThumbToPreview = MutableLiveData<String?>()
    val myImageThumbToPreview: LiveData<String?> = imageThumbToPreview
    fun setImageThumbToPreview(urlThumb: String) {
        imageThumbToPreview.postValue(urlThumb)
    }

    private val currentExampleTemplate = MutableLiveData<Template?>()
    val myCurrentExampleTemplate: LiveData<Template?> = currentExampleTemplate
    fun setExampleTemplate(template: Template?){
        currentExampleTemplate.postValue(template)
    }

    fun saveWallpaperVideoUrl(wallpaperVideoUrl : String?){
        editorRepository.saveWallpaperVideoUrl(wallpaperVideoUrl)
    }

    fun saveTimeGeneratePreviewVideo(time: Long) = systemRepository.saveTimeGeneratePreviewVideo(time)

    fun getTimeGeneratePreviewVideo(): Long = systemRepository.getTimeGeneratePreviewVideo()

    fun saveURLWallpaperLiveSet(urlWallpaperLiveSet: String) = editorRepository.saveURLWallpaperLiveSet(urlWallpaperLiveSet)

    fun renameImageVideoUserCreated(wallpaperDownloaded: WallpaperDownloaded?) = editorRepository.renameImageVideoUserCreated(wallpaperDownloaded)
}