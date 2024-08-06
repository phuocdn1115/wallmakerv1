package com.x10.photo.maker.ui.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseDialog
import com.x10.photo.maker.databinding.LayoutPopupCancelFromPreviewScreenBinding
import com.x10.photo.maker.utils.BlurViewUtils
import com.x10.photo.maker.utils.setSafeOnClickListener

class DialogCancelPreview (private val onClickExit: () -> Unit) :
    BaseDialog<LayoutPopupCancelFromPreviewScreenBinding>() {
    override fun getLayoutResource(): Int {
        return R.layout.layout_popup_cancel_from_preview_screen
    }

    override fun init(saveInstanceState: Bundle?, view: View?) {
    }

    override fun setUp(view: View?) {
        binding.btnExit.setSafeOnClickListener {
            onClickExit.invoke()
            dismiss()
        }
        binding.btnCancel.setSafeOnClickListener {
            dismiss()
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