package com.x10.photo.maker.ui.screen.splash.extension

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.x10.photo.maker.R
import com.x10.photo.maker.ui.dialog.DialogRequestPermissionStorage
import com.x10.photo.maker.ui.screen.splash.SplashActivity
import com.x10.photo.maker.utils.ToastUtil

fun SplashActivity.checkPermissionReadExternalStorage(): Boolean {
    return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun SplashActivity.showDialogRequestPermissionStorage(){
    dialogRequestPermission = DialogRequestPermissionStorage(onClickButtonLater = {
        ToastUtil.showToast(
            resources.getString(R.string.txt_explain_permission_storage),
            baseContext
        )
        dialogRequestPermission?.dismiss()
        navigationManager.navigationToHomeScreen()
    }, onClickButtonRequestPermission = {
        requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            com.x10.photo.maker.enums.RequestCode.PERMISSION_READ_EXTERNAL_STORAGE.requestCode
        )
    })
    dialogRequestPermission?.show(supportFragmentManager, "DIALOG_REQUEST_PERMISSION")
}

