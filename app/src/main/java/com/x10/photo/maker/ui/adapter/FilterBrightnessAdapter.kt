package com.x10.photo.maker.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseRecyclerAdapter
import com.x10.photo.maker.data.model.FilterBrightness
import com.x10.photo.maker.databinding.ItemFilterBrightnessBinding
import com.x10.photo.maker.utils.BlurViewUtils
import com.x10.photo.maker.utils.pushDownClickAnimation
import com.x10.photo.maker.utils.setSafeOnClickListener
import com.x10.photo.maker.viewholder.ViewHolderLifecycle

class FilterBrightnessAdapter(
    dataSet: MutableList<FilterBrightness?>,
    private val onClickItemFilter: (Int, FilterBrightness?) -> Unit,
    private val onClickSaveFilter: (Int, FilterBrightness?) -> Unit,
    private val activity: Activity ?= null
) : BaseRecyclerAdapter<FilterBrightness, FilterBrightnessAdapter.FilterBrightnessViewHolder>(dataSet) {
    internal var context: Context? = null

    override fun getLayoutResourceItem(): Int {
        return R.layout.item_filter_brightness
    }

    override fun setViewHolderLifeCircle(): ViewHolderLifecycle? {
        return null
    }

    override fun onCreateBasicViewHolder(binding: ViewDataBinding?): FilterBrightnessViewHolder {
        context = binding?.root?.context
        return FilterBrightnessViewHolder(binding as ItemFilterBrightnessBinding)
    }

    override fun onBindBasicItemView(holder: FilterBrightnessViewHolder, position: Int) {
        holder.bind(data = getDataSet()?.get(position), position)
    }

    inner class FilterBrightnessViewHolder(private val binding: ItemFilterBrightnessBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(data: FilterBrightness?, position: Int) {
            if (data?.isLoading == true) {
                binding.cardViewLoading.visibility = View.VISIBLE
                binding.cardViewImageSelected.visibility = View.GONE
                binding.root.rootView.isClickable = false
            }
            else {
                bindDataNoLoading(data)
            }
        }

        private fun bindDataNoLoading(data: FilterBrightness?) {
            binding.root.rootView.isClickable = true
            binding.imgImageFilter.setImageBitmap(data?.bitmapImageCropped)
            if (activity != null) {
                BlurViewUtils.setupBlurView(activity, binding.blurViewPreview, binding.imgImageFilter.rootView as ViewGroup)
            }
            if (data?.preview == true) {
                binding.cardViewImageSelected.setCardBackgroundColor(
                    binding.root.context.resources.getColor(
                        R.color.white,
                        null
                    )
                )
                if (!data.isOrigin) binding.blurViewPreview.visibility = View.VISIBLE
                else binding.blurViewPreview.visibility = View.GONE
            } else {
                binding.blurViewPreview.visibility = View.GONE
                binding.cardViewImageSelected.setCardBackgroundColor(Color.TRANSPARENT)
            }
            binding.imgImageFilter.colorFilter
            binding.root.setSafeOnClickListener {
                onClickItemFilter.invoke(bindingAdapterPosition, data)
            }
            binding.btnSave.setSafeOnClickListener {
                onClickSaveFilter.invoke(bindingAdapterPosition, data)
            }
            binding.cardViewLoading.visibility = View.GONE
            binding.cardViewImageSelected.visibility = View.VISIBLE
        }
    }

}