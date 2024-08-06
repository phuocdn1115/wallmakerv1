package com.x10.photo.maker.base.handler

import com.x10.photo.maker.data.model.Wallpaper
import com.x10.photo.maker.enums.DataType
import com.x10.photo.maker.model.Data
import com.x10.photo.maker.model.NativeAds

object DataResponseHandler {
    fun process(dataResponse: List<Wallpaper>, listVideoData: ArrayList<Data>){
        for(position in dataResponse.indices){
            if(dataResponse[position].type == DataType.NATIVE_ADS.type)
                listVideoData.add(NativeAds())
            else if(dataResponse[position].type == DataType.VIDEO_TEMPLATE_TYPE.type){
                listVideoData.add(dataResponse[position].convertToTemplateObject())
            }
            else{
                val checkPoint = listVideoData.firstOrNull { data: Data -> data is Wallpaper && data.id == dataResponse[position].id}
                if(checkPoint != null)
                    continue
                listVideoData.add(dataResponse[position])
            }
        }
    }
}