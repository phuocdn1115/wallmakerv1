package com.x10.photo.maker.data.model


import com.google.gson.annotations.SerializedName

data class Status(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("statusCode")
    val statusCode: Int? = null
)