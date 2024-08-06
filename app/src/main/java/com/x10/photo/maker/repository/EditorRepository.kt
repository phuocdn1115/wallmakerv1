package com.x10.photo.maker.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.x10.photo.maker.PreferencesKey.URL_WALLPAPER_LIVE_IF_PREVIEW
import com.x10.photo.maker.PreferencesKey.URL_WALLPAPER_LIVE_IF_SET
import com.x10.photo.maker.PreferencesManager
import com.x10.photo.maker.RealmManager
import com.x10.photo.maker.base.Result
import com.x10.photo.maker.base.handler.WallpaperHandler
import com.x10.photo.maker.data.realm_model.WallpaperDownloaded
import com.x10.photo.maker.download.DownloadWallpaperManager
import com.x10.photo.maker.enums.WallpaperTypeSetting
import com.x10.photo.maker.ui.screen.editor.set_wallpaper.SetWallpaperFragment
import com.x10.photo.maker.utils.ToastUtil
import com.x10.photo.maker.utils.extension.decodeBitmap
import io.realm.Realm
import javax.inject.Inject

class EditorRepository @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val downloadWallpaperManager: DownloadWallpaperManager,
    private val realmManager: RealmManager
) {
    fun saveWallpaperVideoUrl(wallpaperVideoUrl: String?){
        preferencesManager.save(URL_WALLPAPER_LIVE_IF_PREVIEW,wallpaperVideoUrl)
    }

    fun saveURLWallpaperLiveSet(urlWallpaperLiveSet: String) = preferencesManager.save(
        URL_WALLPAPER_LIVE_IF_SET, urlWallpaperLiveSet)

    fun renameImageVideoUserCreated(wallpaperDownloaded: WallpaperDownloaded?){
        Realm.getDefaultInstance().beginTransaction()
        Realm.getDefaultInstance().commitTransaction()
        realmManager.save(wallpaperDownloaded)
    }
}