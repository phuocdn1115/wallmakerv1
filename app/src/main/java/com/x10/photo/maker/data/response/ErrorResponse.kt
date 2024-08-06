package com.x10.photo.maker.data.response

import com.x10.photo.maker.data.model.Status
import com.google.gson.annotations.SerializedName

class ErrorResponse {
    @SerializedName("status")
    val status: Status? = null
}
