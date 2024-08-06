package com.x10.photo.maker.ui.screen.editor.set_wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.x10.photo.maker.R
import com.x10.photo.maker.WallpaperMakerApp
import com.x10.photo.maker.enums.RequestCode
import com.x10.photo.maker.enums.WallpaperTypeSetting
import com.x10.photo.maker.live.LiveWallpaperService
import com.x10.photo.maker.utils.FileUtils
import com.x10.photo.maker.utils.ToastUtil
import java.io.File
import com.x10.photo.maker.base.Result
import com.x10.photo.maker.base.handler.WallpaperHandler
import com.x10.photo.maker.utils.extension.decodeBitmap

fun SetWallpaperFragment.setLiveWallpaper() {
    if (WallpaperMakerApp.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_LIVE_WALLPAPER)) {
        editorViewModel.saveWallpaperVideoUrl(absolutePathFile)
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(WallpaperMakerApp.instance, LiveWallpaperService::class.java)
        )
        startActivityForResult(intent, RequestCode.SET_WALLPAPER_LIVE.requestCode)
    } else ToastUtil.showToast(resources.getString(R.string.txt_set_wallpaper_error), requireContext())
}

fun SetWallpaperFragment.renameFile() {
    objectImageVideoCreated?.name = binding.tvNameWallpaper.text.toString().trim()
    editorViewModel.renameImageVideoUserCreated(objectImageVideoCreated)
}

fun SetWallpaperFragment.showDialogChooseTyeSettingBottomSheet() {
    chooseTypeSettingBottomSheet?.show(childFragmentManager, "chooseTypeSettingBottomSheet")
}

fun SetWallpaperFragment.showDialogSetVideoSuccess() {
    dialogSetVideoWallpaperSuccess?.show(childFragmentManager, "dialogSetVideoWallpaperSuccess")
}

fun SetWallpaperFragment.showDialogSetImageSuccess() {
    dialogSetImageWallpaperSuccess?.show(childFragmentManager, "dialogSetImageWallpaperSuccess")
}

fun SetWallpaperFragment.setWallpaper(path: String, typeSetting: WallpaperTypeSetting) {
    val setWallpaperLiveData = MutableLiveData<Result<Boolean>>()
    setWallpaperLiveData.postValue(Result.InProgress())
    val bitmap = decodeBitmap(path)
    var postValue = false
    if (bitmap != null) WallpaperHandler.setWallpaper(bitmap, typeSetting) { isSuccess ->
        if (isSuccess && !postValue) {
            setWallpaperLiveData.postValue(Result.Success(true))
            postValue = true
            showDialogSetImageSuccess()
        } else {
            ToastUtil.showToast(resources.getString(R.string.txt_set_wallpaper_error), requireContext())
            setWallpaperLiveData.postValue(Result.Failure(400, "Error, Try again!"))
        }
    }
}