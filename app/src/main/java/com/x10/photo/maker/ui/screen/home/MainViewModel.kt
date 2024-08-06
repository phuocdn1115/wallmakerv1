package com.x10.photo.maker.ui.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.x10.photo.maker.base.Result
import com.x10.photo.maker.base.SingleLiveEvent
import com.x10.photo.maker.data.response.DataHomeResponse
import com.x10.photo.maker.data.response.InstallationResponse
import com.x10.photo.maker.model.Data
import com.x10.photo.maker.repository.AdvertiseRepository
import com.x10.photo.maker.repository.MainRepository
import com.x10.photo.maker.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val advertiseRepository: AdvertiseRepository,
    private val systemRepository: SystemRepository
    ) : ViewModel() {

    fun loadAds() = advertiseRepository.loadAds()
    val dataHomeResult = SingleLiveEvent<Result<DataHomeResponse>>()
    fun getDataHomeScreen(ownerItemCount : Int ?= 0){
        /**
         * Get data offline mode
         */
        dataHomeResult.postValue(Result.InProgress())
        val responseOfflineData: DataHomeResponse? = mainRepository.getHomeDataInLocal(ownerItemCount)
        responseOfflineData?.let {
            dataHomeResult.postValue(Result.Success(it))
        }
    }

    val checkingInstallerIfNeedResult = SingleLiveEvent<Result<InstallationResponse>>()
    fun callApiCheckingInstallerIfNeed(utmSource: String?, utmCampaign: String?, utmContent: String?, utmMedium: String?, utmTerm: String?) {
        val request = systemRepository.callApiCheckingInstallerIfNeed(utmSource, utmCampaign, utmContent, utmMedium, utmTerm)
        checkingInstallerIfNeedResult.addSource(request){
            checkingInstallerIfNeedResult.postValue(it)
        }
    }

    private val listVideoUser = MutableLiveData<List<Data>>()
    val listVideoUserResult : LiveData<List<Data>> = listVideoUser
    fun getListVideoUser() {
        val listVideoUserData = mainRepository.getListVideoUser()
        listVideoUser.postValue(listVideoUserData)
        getDataHomeScreen(ownerItemCount = listVideoUserData.size)
    }

    fun checkIsFirstOpenApp(): Boolean = systemRepository.checkIsFirstOpenApp()
    fun saveFirstOpenApp() = systemRepository.saveFirstOpenApp()

    fun deleteVideoUser(data : Data) = systemRepository.deleteVideoUser(data)

}