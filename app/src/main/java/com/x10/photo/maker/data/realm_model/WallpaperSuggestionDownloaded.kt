package com.x10.photo.maker.data.realm_model

import com.x10.photo.maker.data.model.ImageMadeByUser
import com.x10.photo.maker.data.model.VideoMadeByUser
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class WallpaperSuggestionDownloaded : RealmObject() {
    @PrimaryKey
    var id: String = ""
    var createTime: Long = 0
    var name: String = ""
    var pathInStorage: String = ""
}