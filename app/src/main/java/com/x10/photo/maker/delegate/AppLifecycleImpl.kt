package com.x10.photo.maker.delegate

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import com.x10.photo.maker.di.GlideApp
import com.blankj.utilcode.util.Utils

class AppLifecycleImpl : AppLifecycle {

    override fun attachBaseContext(context: Context) {
    }


    override fun onCreate(application: Application) {

    }

    override fun onTerminate(application: Application) {

    }

    override fun onConfigurationChanged(configuration: Configuration) {

    }

    override fun onLowMemory() {
        GlideApp.get(Utils.getApp()).clearMemory()
    }

    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            GlideApp.get(Utils.getApp()).clearMemory()
        }

        GlideApp.get(Utils.getApp()).trimMemory(level)
    }


}