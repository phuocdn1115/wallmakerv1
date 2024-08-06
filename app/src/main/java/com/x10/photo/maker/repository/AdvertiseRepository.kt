package com.x10.photo.maker.repository

import com.x10.photo.maker.ads.nativeads.*
import com.x10.photo.maker.ads.openapp.OpenAppAdsManager
import com.x10.photo.maker.aplication.ApplicationContext
import com.x10.photo.maker.ads.rewarded.RewardedAdsManager
import com.google.android.gms.ads.nativead.NativeAdView
import javax.inject.Inject

class AdvertiseRepository @Inject constructor(
    private val nativeAdsInHomeManager: NativeAdsInHomeManager,
    private val nativeAdsSetSuccessManager: NativeAdsSetSuccessManager,
    private val nativeAdsInFrameSaving: NativeAdsInFrameSaving,
    private val rewardedAdsManager: RewardedAdsManager,
    private val openAppAdsManager: OpenAppAdsManager
) {
    fun loadAds() {
        openAppAdsManager.loadOpenAppAds(ApplicationContext.getAdsContext().retry)
        nativeAdsInHomeManager.loadNativeAd(ApplicationContext.getAdsContext().retry)
        nativeAdsSetSuccessManager.loadNativeAd(ApplicationContext.getAdsContext().retry)
        nativeAdsInFrameSaving.loadNativeAd(ApplicationContext.getAdsContext().retry)
        rewardedAdsManager.loadRewardedAds(ApplicationContext.getAdsContext().retry)
    }
}