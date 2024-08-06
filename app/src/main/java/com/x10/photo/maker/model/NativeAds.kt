package com.x10.photo.maker.model

import com.google.android.gms.ads.nativead.NativeAd

class NativeAds : Data() {

    var viewed = 0
    var nativeAd: NativeAd?= null

    fun incrementView() = viewed++
}