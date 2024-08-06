package com.x10.photo.maker.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.x10.photo.maker.PreferencesKey
import com.x10.photo.maker.PreferencesKey.COUNTRY_CODE
import com.x10.photo.maker.PreferencesManager
import com.x10.photo.maker.RealmManager
import com.x10.photo.maker.WallpaperMakerApp
import com.x10.photo.maker.base.Result
import com.x10.photo.maker.data.model.Wallpaper
import com.x10.photo.maker.data.realm_model.WallpaperDownloaded
import com.x10.photo.maker.data.realm_model.WallpaperSuggestionDownloaded
import com.x10.photo.maker.data.response.DataHomeResponse
import com.x10.photo.maker.download.DownloadWallpaperManager
import com.x10.photo.maker.enums.DataType
import com.x10.photo.maker.enums.WallpaperType
import com.x10.photo.maker.model.Data
import com.x10.photo.maker.network.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.coroutines.*
import java.io.IOException
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val api: Api,
    private val realmManager: RealmManager,
    private val preferencesManager: PreferencesManager,
    private val downloadWallpaperManager: DownloadWallpaperManager
) {
    fun getDataHomeScreen(
        pageNumber: Int? = 1,
        ownerItemNumber: Int? = 0
    ): LiveData<Result<DataHomeResponse>> {
        val url: String =  generateAPIUrl(END_POINT_GET_DATA_HOME_SCREEN)
        val dataResponse = MutableLiveData<Result<DataHomeResponse>>()
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.i("MainRepository", "exception::getDataHomeScreen::Offline")
            val offlineResponse = getHomeDataInLocal()
            dataResponse.postValue(Result.Success(offlineResponse))
        }
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            dataResponse.postValue(Result.InProgress())
            val request = api.getDataHomeScreen(
                url = url,
                pageNumber = pageNumber,
                ownerItemNumber = ownerItemNumber
            )
            withContext(Dispatchers.Main) {
                if (request.isSuccessful) {
                    dataResponse.postValue(Result.Success(request.body() as DataHomeResponse))
                } else {
                    Log.i("MainRepository", "else::getDataHomeScreen::Offline")
                    val offlineResponse = getHomeDataInLocal()
                    dataResponse.postValue(Result.Success(offlineResponse))
                }
            }
        }
        return dataResponse
    }

    fun getHomeDataInLocal(ownerItemCount: Int? = 0): DataHomeResponse {
        val pathGetFileDefault = "json/maker_default_us"
        val pathGetFile = "json/maker_default_" + (preferencesManager.getString(COUNTRY_CODE)?.lowercase() ?: "us") + ".json"
        val jsonSpecialDataString = try {
            WallpaperMakerApp.instance.assets.open(pathGetFile).bufferedReader().use { it.readText() }
        } catch (io: IOException) {
            WallpaperMakerApp.instance.assets.open(pathGetFileDefault).bufferedReader().use { it.readText() }
        }
        val dataResponse = object : TypeToken<DataHomeResponse>() {}.type
        val result = Gson().fromJson<DataHomeResponse?>(jsonSpecialDataString, dataResponse)
        processDataToFillNativeADs(ownerItemCount!!, result)
        return result
    }

    fun getListVideoUser() : List<Data> {
        val imageVideoUser = realmManager.findAllSorted(
            WallpaperDownloaded::class.java, "createTime", Sort.DESCENDING
        ) as RealmResults<WallpaperDownloaded>
        val listVideoUser: ArrayList<Data> = arrayListOf()
        imageVideoUser.forEach {
            if (it.wallpaperType == WallpaperType.VIDEO_USER_TYPE.value) {
                listVideoUser.add(it.convertToVideoUser())
            } else if (it.wallpaperType == WallpaperType.IMAGE_USER_TYPE.value) {
                listVideoUser.add(it.convertToImageUser())
            }
        }
        return listVideoUser
    }

    private fun processDataToFillNativeADs(ownerItemCount: Int, dataResponse : DataHomeResponse){
        /**
         * Handle fill native ads, video template, video/image made by user
         */
        for(i in 0 until ownerItemCount){
            (dataResponse.data.data as MutableList).add(0, Wallpaper(type = DataType.VIDEO_MADE_BY_USER.type, name = "OwnerItem"))
        }
        val nativeADsCount = dataResponse.data.data.size / 4
        for(index in 1..nativeADsCount ){
            /**
             * Position of native ADs
             */
            val indexOfNativeADs = index * 4 +  index - 1
            (dataResponse.data.data as MutableList).add(indexOfNativeADs, Wallpaper(type = DataType.NATIVE_ADS.type, name = "NativeAdsItem"))
        }
    }

    @SuppressLint("Range")
    fun downloadVideo(dataModel: Wallpaper) : LiveData<Result<String>> {
        val videoFileLiveData = MutableLiveData<Result<String>>()
        downloadWallpaperManager.downloadFile(
            url = dataModel.originUrlString(),
            onStart = { videoFileLiveData.postValue(Result.InProgress()) },
            onSuccess = { pathFile ->
                val wallpaperSuggestionDownloaded = WallpaperSuggestionDownloaded()
                wallpaperSuggestionDownloaded.id = dataModel.id.toString()
                wallpaperSuggestionDownloaded.name = dataModel.name.toString()
                wallpaperSuggestionDownloaded.pathInStorage = pathFile
                videoFileLiveData.postValue(Result.Success(pathFile))
            }
        ) { videoFileLiveData.postValue(Result.Failure(400, "Error download")) }
        return videoFileLiveData
    }

    fun saveWallpaperVideoUrl(wallpaperVideoUrl: String?){
        preferencesManager.save(PreferencesKey.URL_WALLPAPER_LIVE_IF_PREVIEW,wallpaperVideoUrl)
    }

    fun saveURLWallpaperLiveSet(urlWallpaperLiveSet: String) = preferencesManager.save(
        PreferencesKey.URL_WALLPAPER_LIVE_IF_SET, urlWallpaperLiveSet)

}