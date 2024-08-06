package com.x10.photo.maker.ui.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseDialog
import com.x10.photo.maker.databinding.LayoutChooseTypeSettingBottomSheetBinding
import com.x10.photo.maker.enums.WallpaperTypeSetting
import com.x10.photo.maker.utils.BlurViewUtils
import com.x10.photo.maker.utils.setSafeOnClickListener

class DialogChooseTypeSettingBottomSheet(
    private val onSettingImage: (WallpaperTypeSetting) -> Unit
) : BaseDialog<LayoutChooseTypeSettingBottomSheetBinding>() {
    override fun getLayoutResource(): Int {
        return R.layout.layout_choose_type_setting_bottom_sheet
    }

    override fun init(saveInstanceState: Bundle?, view: View?) {
    }

    override fun setUp(view: View?) {
        binding.btnHomeScreen.setSafeOnClickListener {
            dismiss()
            onSettingImage.invoke(WallpaperTypeSetting.HOME)
        }
        binding.btnLockScreen.setSafeOnClickListener {
            dismiss()
            onSettingImage.invoke(WallpaperTypeSetting.LOCK)
        }
        binding.btnAll.setSafeOnClickListener {
            dismiss()
            onSettingImage.invoke(WallpaperTypeSetting.BOTH)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun getGravityForDialog(): Int {
        return Gravity.BOTTOM
    }

    override fun onStart() {
        super.onStart()
        setSizeFullForDialog()
    }
}