package com.x10.photo.maker.ui.screen.home.videos

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.x10.photo.maker.R
import com.x10.photo.maker.enums.RequestCode
import com.x10.photo.maker.ui.dialog.DialogConfirmDeleteVideo
import com.x10.photo.maker.ui.dialog.DialogRequestPermissionStorage
import com.x10.photo.maker.utils.ToastUtil

fun VideosFragment.checkPermissionReadExternalStorage(): Boolean {
    return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun VideosFragment.showDialogRequestPermissionStorage(){
    dialogRequestPermissionStorage = DialogRequestPermissionStorage(onClickButtonLater = {
        ToastUtil.showToast(resources.getString(R.string.txt_explain_permission_storage), requireContext())
    }, onClickButtonRequestPermission = {
        requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            RequestCode.PERMISSION_READ_EXTERNAL_STORAGE.requestCode
        )
    })
    dialogRequestPermissionStorage?.show(childFragmentManager, "DIALOG_REQUEST_PERMISSION")
}