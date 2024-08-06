package com.x10.photo.maker.video.template

import com.x10.photo.maker.data.model.Template
import com.x10.photo.maker.enums.SegmentType
import com.x10.photo.maker.video.BaseTemplate
import com.x10.photo.maker.video.PhotoMovieFactoryUsingTemplate
import com.hw.photomovie.PhotoMovie
import com.hw.photomovie.model.PhotoData
import com.hw.photomovie.model.PhotoSource
import com.hw.photomovie.segment.MovieSegment

class TemplateOne(template: Template, private val photoList: MutableList<PhotoData>) : BaseTemplate(template, photoList) {

    override fun process(): PhotoMovie<*>? {
        val segmentList : MutableList<MovieSegment<Nothing>> = arrayListOf()
        val modifyPhotoList = clonePhotoList()

        template.script?.forEach { segmentData ->
            val movieSegment = PhotoMovieFactoryUsingTemplate.generateSegment(segmentData.segmentType, segmentData.duration)
            segmentList.add(movieSegment as MovieSegment<Nothing>)
            if(segmentData.segmentType == SegmentType.SCALE_SEGMENT.type && segmentData.index != 0) {
                cloneNextPhoto(segmentData, modifyPhotoList)
            }
            else if(segmentData.segmentType == SegmentType.THAW_SEGMENT.type || segmentData.segmentType == SegmentType.GRADIENT_TRANSFER_SEGMENT.type) {
                cloneCurrentPhoto(segmentData, modifyPhotoList)
            }
        }
        if(photoList.size == 6)
            modifyPhotoList.add(modifyPhotoList.size -1, photoList[photoList.size -2])
        val photoSource = PhotoSource(modifyPhotoList)
        return PhotoMovie(photoSource, segmentList)
    }



}