package com.x10.photo.maker.ui.screen.editor.preview

import com.x10.photo.maker.data.realm_model.WallpaperDownloaded

interface SaveImageProgressListener {
    fun onSuccess(objectImageVideoFragment: WallpaperDownloaded)
    fun onProgress()
    fun onFailure(error : String)
}