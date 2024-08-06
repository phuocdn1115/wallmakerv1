package com.x10.photo.maker.ui.screen.editor.preview

import com.x10.photo.maker.data.realm_model.WallpaperDownloaded

interface SaveVideoProgressListener {
    fun onSuccess(objectImageVideoFragment: WallpaperDownloaded)
    fun onProgress(progress : Int, total : Int)
    fun onFailure(nameFile : String)
}