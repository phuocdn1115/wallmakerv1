package com.x10.photo.maker.config

import android.content.Context
import com.x10.photo.maker.delegate.AppLifecycle
import com.x10.photo.maker.delegate.AppLifecycleImpl

class GlobalConfiguration : ConfigModule {
    override fun applyOptions(context: Context, builder: GlobalConfigModule.Builder) {
    }

    override fun injectAppLifecycle(context: Context, lifecycles: MutableList<AppLifecycle>) {
        lifecycles.add(AppLifecycleImpl())
    }

}