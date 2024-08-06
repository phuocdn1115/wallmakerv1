package com.x10.photo.maker

import android.app.Application
import android.content.Context
import com.x10.photo.maker.delegate.AppDelegate
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class WallpaperMakerApp: Application() {

    private val appDelegate by lazy { AppDelegate(this) }

    @Inject
    lateinit var okHttpClient : OkHttpClient

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        appDelegate.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        appDelegate.onCreate(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        appDelegate.onTerminate(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        appDelegate.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        appDelegate.onTrimMemory(level)
    }

    companion object {
        lateinit var instance: WallpaperMakerApp
        fun getContext(): Context = instance.applicationContext
        fun getOkHttpClient() = instance.okHttpClient
    }
}