package com.x10.maker.video

import com.x10.photo.maker.WallpaperMakerApp
import com.x10.photo.maker.data.model.Template
import com.x10.photo.maker.data.model.Wallpaper
import com.x10.photo.maker.data.response.DataHomeResponse
import com.x10.photo.maker.video.PhotoMovieFactoryUsingTemplate
import com.blankj.utilcode.util.FileUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hw.photomovie.model.PhotoData
import org.junit.Assert.assertEquals
import org.junit.Test


class VideoProcessTest {
    @Test
    fun testPreProcessWithFull5Photos() {
        val specialDataInLocal = getSpecialDataInLocal()
        val list = specialDataInLocal?.data?.data;

        //full photo with size = 5
        val template0: Wallpaper? = list?.get(0) ?: null
        val photoList = listOf<PhotoData>(PhotoDataMock(), PhotoDataMock(), PhotoDataMock(), PhotoDataMock(), PhotoDataMock())


        val templateScript0 =  Template(
                id = template0?.id,
                name = template0?.name,
                script = template0?.script,
                type = template0?.type,
                url = template0?.url,
                thumbUrlImg = "",
                thumbUrlVideo = "",
                colorCode = template0?.colorCode
            )

        val preTemplate = PhotoMovieFactoryUsingTemplate.preProcess(templateScript0, photoList)
        val expectedValue = 8L
        assertEquals(expectedValue.toInt(), preTemplate.script?.size)
    }

    @Test
    fun testPreProcessWithFull4Photos() {
        val specialDataInLocal = getSpecialDataInLocal()
        val list = specialDataInLocal?.data?.data;

        //full photo with size = 5
        val template0: Wallpaper? = list?.get(0) ?: null
        val photoList = listOf<PhotoData>(PhotoDataMock(), PhotoDataMock(), PhotoDataMock(), PhotoDataMock())


        val templateScript0 =  Template(
            id = template0?.id,
            name = template0?.name,
            script = template0?.script,
            type = template0?.type,
            url = template0?.url,
            thumbUrlImg = "",
            thumbUrlVideo = "",
            colorCode = template0?.colorCode
        )

        val preTemplate = PhotoMovieFactoryUsingTemplate.preProcess(templateScript0, photoList)
        val expectedValue = 6L
        assertEquals(expectedValue.toInt(), preTemplate.script?.size)
    }

    @Test
    fun testPreProcessWithFull3Photos() {
        val specialDataInLocal = getSpecialDataInLocal()
        val list = specialDataInLocal?.data?.data;

        //full photo with size = 5
        val template0: Wallpaper? = list?.get(0) ?: null
        val photoList = listOf<PhotoData>(PhotoDataMock(), PhotoDataMock(), PhotoDataMock())


        val templateScript0 =  Template(
            id = template0?.id,
            name = template0?.name,
            script = template0?.script,
            type = template0?.type,
            url = template0?.url,
            thumbUrlImg = "",
            thumbUrlVideo = "",
            colorCode = template0?.colorCode
        )

        val preTemplate = PhotoMovieFactoryUsingTemplate.preProcess(templateScript0, photoList)
        val expectedValue = 4L
        assertEquals(expectedValue.toInt(), preTemplate.script?.size)
    }

    @Test
    fun testPreProcessWithFull2Photos() {
        val specialDataInLocal = getSpecialDataInLocal()
        val list = specialDataInLocal?.data?.data;

        //full photo with size = 5
        val template0: Wallpaper? = list?.get(0) ?: null
        val photoList = listOf<PhotoData>(PhotoDataMock(), PhotoDataMock())


        val templateScript0 =  Template(
            id = template0?.id,
            name = template0?.name,
            script = template0?.script,
            type = template0?.type,
            url = template0?.url,
            thumbUrlImg = "",
            thumbUrlVideo = "",
            colorCode = template0?.colorCode
        )

        val preTemplate = PhotoMovieFactoryUsingTemplate.preProcess(templateScript0, photoList)
        val expectedValue = 2L
        assertEquals(expectedValue.toInt(), preTemplate.script?.size)
    }






    private fun getSpecialDataInLocal(): DataHomeResponse? {
        val pathGetFileDefault = "/json/template.json"

        val jsonSpecialDataString = loadFileText(this, pathGetFileDefault)
        val specialDataType = object : TypeToken<DataHomeResponse>() {}.type
        val makerResponseInLocal: DataHomeResponse = Gson().fromJson(jsonSpecialDataString, specialDataType)
        return makerResponseInLocal
    }

    inline fun <reified T> loadFileText(
        caller: T,
        filePath: String
    ): String =
        T::class.java.getResource(filePath)?.readText() ?: throw IllegalArgumentException(
            "Could not find file $filePath. Make sure to put it in the correct resources folder for $caller's runtime."
        )
}