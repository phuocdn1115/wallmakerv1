package com.x10.photo.maker.startup

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.x10.photo.maker.BuildConfig
import com.x10.photo.maker.aplication.ApplicationContext
import com.x10.photo.maker.data.response.RemoteConfigResponse
import com.alo.ringo.tracking.DefaultEventDefinition
import com.alo.ringo.tracking.DefaultEventDefinition.Companion.EVENT_EV2_G5_LOAD_CONFIG
import com.alo.ringo.tracking.base_event.StatusType
import com.x10.photo.maker.R
import com.alo.wall.maker.tracking.EventTrackingManager
import com.blankj.utilcode.util.NetworkUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.x10.photo.maker.utils.extension.getDeviceId
import org.greenrobot.eventbus.EventBus

class FirebaseConfigureInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        Log.i(TAG, "REMOTE_CONFIG::FirebaseConfigureInitializer")
        fetchRemoteConfig(context.getDeviceId(), ApplicationContext.getNetworkContext().countryKey, context)
    }

    companion object {
        private val TAG = FirebaseConfigureInitializer::class.simpleName
        private var nodeFetch = ""
        private var startTimeLoadConfig = 0
        fun fetchRemoteConfig(mobileId: String, country: String, context: Context) {
            if (!NetworkUtils.isConnected()) {
                ApplicationContext.getDeviceContext().initializerFirebaseRemoteConfig = false
                return
            }
            startTimeLoadConfig = System.currentTimeMillis().toInt()
            ApplicationContext.getDeviceContext().initializerFirebaseRemoteConfig = true
            val firebaseRemoteConfig = Firebase.remoteConfig
            firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                val waitTime = System.currentTimeMillis().toInt() - startTimeLoadConfig
                var statusLoadConfigDone: StatusType
                if (task.isSuccessful) {
                    nodeFetch = BuildConfig.PREFIX_FIREBASE_REMOTE_CONFIG + BuildConfig.VERSION_NAME.replace(".", "_")
                    Log.d(TAG, "REMOTE_CONFIG::Fetch from $nodeFetch")
                    val json = firebaseRemoteConfig.getString(nodeFetch)
                    val configType = object : TypeToken<RemoteConfigResponse>() {}.type
                    if (json.isNotEmpty()) {
                        statusLoadConfigDone = StatusType.SUCCESS
                        val configResult: RemoteConfigResponse = Gson().fromJson(json, configType)
                        Log.d(TAG, "REMOTE_CONFIG::Get remote config success!\n $configResult")
                        ApplicationContext.updateAds(configResult)
                        ApplicationContext.updateNetworkContext(configResult)
                        ApplicationContext.updateVideoUrl(configResult)
                    } else {
                        statusLoadConfigDone = StatusType.FAIL
                        Log.d(TAG, "REMOTE_CONFIG::Get remote config is null!")
                    }
                } else {
                    statusLoadConfigDone = StatusType.FAIL
                    Log.d(TAG, "REMOTE_CONFIG::Get remote config is null!")
                }
                EventTrackingManager(context).sendConfigsEvent(
                    eventName = EVENT_EV2_G5_LOAD_CONFIG,
                    loadConfigDone = statusLoadConfigDone,
                    keyRemoteConfig = nodeFetch,
                    waitTime = waitTime,
                    mobileId = mobileId,
                    country = country
                )
                firebaseRemoteConfig.reset()
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(CountryCodeInitializer::class.java)
    }
}