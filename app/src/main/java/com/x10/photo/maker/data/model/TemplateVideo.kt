package com.x10.photo.maker.data.model

import com.x10.photo.maker.model.ImageSelected
import com.hw.photomovie.PhotoMovieFactory

class TemplateVideo(
    var template: PhotoMovieFactory.PhotoMovieType? = null,
    var isSelected: Boolean? = false,
    var image: Int? = null,
    var name: String?= null
) {

}