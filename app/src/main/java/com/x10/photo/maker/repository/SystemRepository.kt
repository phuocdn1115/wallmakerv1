package com.x10.photo.maker.repository

import com.x10.photo.maker.PreferencesKey.IS_FIRST_TIME_OPEN_APP
import com.x10.photo.maker.PreferencesManager
import com.x10.photo.maker.RealmManager
import com.x10.photo.maker.data.model.ImageMadeByUser
import com.x10.photo.maker.data.model.VideoMadeByUser
import com.x10.photo.maker.data.realm_model.WallpaperDownloaded
import com.x10.photo.maker.model.Data
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.x10.photo.maker.base.Result
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.Gson
import com.x10.photo.maker.PreferencesKey.IS_CALL_API_TRACKING_INSTALLATION
import com.x10.photo.maker.PreferencesKey.TIME_GENERATE_VIDEO_PREVIEW
import com.x10.photo.maker.data.response.ErrorResponse
import com.x10.photo.maker.data.response.InstallationResponse
import com.x10.photo.maker.network.Api
import com.x10.photo.maker.network.END_POINT_CHECKING_INSTALLATION
import com.x10.photo.maker.network.generateUrlRetry
import kotlinx.coroutines.*
import javax.inject.Inject

class SystemRepository @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val realmManager: RealmManager,
    private val api: Api
) {

    fun checkIsFirstOpenApp(): Boolean = !preferencesManager.getBoolean(IS_FIRST_TIME_OPEN_APP)

    fun saveFirstOpenApp() {
        preferencesManager.save(IS_FIRST_TIME_OPEN_APP, true)
    }

    fun deleteVideoUser(data: Data) {
        var itemDelete : WallpaperDownloaded ?= null
        when(data){
            is VideoMadeByUser ->{
                itemDelete = getVideoOrImageUser(data.id)
            }
            is ImageMadeByUser ->{
                itemDelete = getVideoOrImageUser(data.id)
            }
        }
        if(itemDelete != null){
            realmManager.delete(itemDelete)
        }
    }

    fun getVideoOrImageUser(id: String) : WallpaperDownloaded? = realmManager.findFirst(WallpaperDownloaded::class.java, "id", id)

    fun callApiCheckingInstallerIfNeed(utmSource: String?, utmCampaign: String?, utmContent: String?, utmMedium: String?, utmTerm: String?) : LiveData<Result<InstallationResponse>> {
        val url: String = generateUrlRetry(END_POINT_CHECKING_INSTALLATION).nextUrl() ?: END_POINT_CHECKING_INSTALLATION
        val callApiCheckingInstallerIfNeedLiveData = MutableLiveData<Result<InstallationResponse>>()
        if (!preferencesManager.getBoolean(IS_CALL_API_TRACKING_INSTALLATION)) {
            val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
                callApiCheckingInstallerIfNeedLiveData.postValue(
                    Result.Failure(
                        400,
                        throwable.message
                    )
                )
            }
            CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
                val request =
                    api.checkingInstall(url, utmSource, utmCampaign, utmContent, utmMedium, utmTerm)
                withContext(Dispatchers.Main) {
                    if (request.isSuccessful) {
                        preferencesManager.save(IS_CALL_API_TRACKING_INSTALLATION, true)
                        callApiCheckingInstallerIfNeedLiveData.postValue(Result.Success(request.body() as InstallationResponse))
                    } else {
                        val strErr = request.errorBody()?.string()
                        val status = Gson().fromJson(strErr, ErrorResponse::class.java)
                        callApiCheckingInstallerIfNeedLiveData.postValue(
                            Result.Failures(
                                status,
                                request.code(),
                                request.message()
                            )
                        )
                    }
                }
            }
        }
        return callApiCheckingInstallerIfNeedLiveData
    }

    fun saveTimeGeneratePreviewVideo(time: Long) = preferencesManager.save(TIME_GENERATE_VIDEO_PREVIEW, time)

    fun getTimeGeneratePreviewVideo(): Long = preferencesManager.getLong(TIME_GENERATE_VIDEO_PREVIEW)
}