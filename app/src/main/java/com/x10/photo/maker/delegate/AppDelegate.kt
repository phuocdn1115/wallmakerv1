package com.x10.photo.maker.delegate

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.x10.photo.maker.config.ConfigModule
import com.x10.photo.maker.config.GlobalConfigModule
import com.x10.photo.maker.integration.ManifestParser

class AppDelegate(context: Context) : AppLifecycle {
    private val TAG = AppDelegate::class.simpleName

    private var appLifecycleList: MutableList<AppLifecycle>? = mutableListOf()

    private var globalConfigModule: GlobalConfigModule? = null

    private val configModules: List<ConfigModule> = ManifestParser(context).parse()

    init {
        //Parse the config module from AndroidManifest.xml

        for (module in configModules) {
            appLifecycleList?.let { module.injectAppLifecycle(context, it) }
        }
    }

    override fun attachBaseContext(context: Context) {
        appLifecycleList?.forEach {
            it.attachBaseContext(context)
        }
    }

    override fun onCreate(application: Application) {
        globalConfigModule = getGlobalConfig(application, configModules)

        appLifecycleList?.forEach {
            it.onCreate(application)
        }

        Log.i(TAG, "Delegate::ThanhVC")

    }

    private fun getGlobalConfig(
        context: Context,
        modules: List<ConfigModule>?
    ): GlobalConfigModule {
        //add configuration here
        val builder = GlobalConfigModule.builder()
        modules?.forEach {
            it.applyOptions(context, builder)
        }
        return builder.build()
    }

    override fun onTerminate(application: Application) {
        appLifecycleList?.forEach {
            it.onTerminate(application)
        }
        this.appLifecycleList = null
    }

    override fun onConfigurationChanged(configuration: Configuration) {
    }

    override fun onLowMemory() {
        appLifecycleList?.forEach {
            it.onLowMemory()
        }
    }

    override fun onTrimMemory(level: Int) {
        appLifecycleList?.forEach {
            it.onTrimMemory(level)
        }
    }
}