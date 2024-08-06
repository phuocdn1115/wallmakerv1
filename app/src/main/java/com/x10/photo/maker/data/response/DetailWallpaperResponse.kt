package com.x10.photo.maker.data.response


import com.x10.photo.maker.data.model.Status
import com.x10.photo.maker.data.model.Wallpaper
import com.google.gson.annotations.SerializedName

data class DetailWallpaperResponse(
    @SerializedName("data")
    val wallpaper: Wallpaper,
    @SerializedName("status")
    val status: Status
)