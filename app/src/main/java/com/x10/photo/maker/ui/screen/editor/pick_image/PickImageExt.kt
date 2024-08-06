package com.x10.photo.maker.ui.screen.editor.pick_image

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.x10.photo.maker.R
import com.x10.photo.maker.enums.RequestCode
import com.x10.photo.maker.ui.dialog.DialogRequestPermissionStorage
import com.x10.photo.maker.ui.screen.home.videos.VideosFragment
import com.x10.photo.maker.utils.ToastUtil

fun PickImageFragment.checkPermissionReadExternalStorage(): Boolean {
    return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun PickImageFragment.showDialogRequestPermissionStorage(){
    dialogRequestPermissionStorage = DialogRequestPermissionStorage(onClickButtonLater = {
        ToastUtil.showToast(resources.getString(R.string.txt_explain_permission_storage), requireContext())
        activity?.finish()
    }, onClickButtonRequestPermission = {
        requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            RequestCode.PERMISSION_READ_EXTERNAL_STORAGE.requestCode
        )
    })
    dialogRequestPermissionStorage?.show(childFragmentManager, "DIALOG_REQUEST_PERMISSION")
}