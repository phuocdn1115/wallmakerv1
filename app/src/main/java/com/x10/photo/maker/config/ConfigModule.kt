package com.x10.photo.maker.config

import android.content.Context
import com.x10.photo.maker.delegate.AppLifecycle

interface ConfigModule {

    fun applyOptions(context: Context, builder: GlobalConfigModule.Builder)

    fun injectAppLifecycle(context: Context, lifecycles: MutableList<AppLifecycle>)
}