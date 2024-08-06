package com.x10.photo.maker.data.response

import com.google.gson.annotations.SerializedName
import com.x10.photo.maker.data.model.Status

data class InstallationResponse(
    @SerializedName("data")
    val `data`: String,
    @SerializedName("status")
    val status: Status
)