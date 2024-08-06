package com.x10.photo.maker.ui.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseDialog
import com.x10.photo.maker.databinding.LayoutPopupConfirmDeleteVideoBinding
import com.x10.photo.maker.utils.setSafeOnClickListener

class DialogConfirmDeleteVideo (private val onClickDelete: () -> Unit) :
    BaseDialog<LayoutPopupConfirmDeleteVideoBinding>() {
    override fun getLayoutResource(): Int {
        return R.layout.layout_popup_confirm_delete_video
    }

    override fun init(saveInstanceState: Bundle?, view: View?) {
    }

    override fun setUp(view: View?) {
        binding.btnDelete.setSafeOnClickListener {
            onClickDelete.invoke()
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