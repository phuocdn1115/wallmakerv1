package com.x10.photo.maker.data.model

import com.x10.photo.maker.data.realm_model.WallpaperDownloaded
import com.x10.photo.maker.model.Data

class VideoMadeByUser(
    var id: String = "",
    var createTime: Long = 0,
    var name: String = "",
    var wallpaperType: String = "",
    var pathInStorage: String = "",
    var imageThumb: String? = "",
    var isTemplate: Boolean = false,
    var idTemplate: String = ""
) : Data() {
    fun convertToWallpaperDownloaded(): WallpaperDownloaded {
        var wallpaperDownloaded = WallpaperDownloaded()
        wallpaperDownloaded.name = this.name
        wallpaperDownloaded.id = this.id
        wallpaperDownloaded.createTime = this.createTime
        wallpaperDownloaded.wallpaperType = this.wallpaperType
        wallpaperDownloaded.pathInStorage = this.pathInStorage
        wallpaperDownloaded.imageThumb = this.imageThumb
        wallpaperDownloaded.isTemplate = this.isTemplate
        wallpaperDownloaded.idTemplate = this.idTemplate
        return wallpaperDownloaded
    }
}