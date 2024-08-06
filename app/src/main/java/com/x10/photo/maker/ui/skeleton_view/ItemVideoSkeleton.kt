package com.x10.photo.maker.ui.skeleton_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.x10.photo.maker.R
import com.x10.photo.maker.databinding.LayoutSkeletonItemVideoBinding

class ItemVideoSkeleton : ConstraintLayout {
    private var binding: LayoutSkeletonItemVideoBinding
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        this.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.dp343))
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DataBindingUtil.inflate(inflater, R.layout.layout_skeleton_item_video, this, true)
    }
}