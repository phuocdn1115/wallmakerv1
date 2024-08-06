package com.x10.photo.maker.ui.adapter

import android.graphics.Color
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseRecyclerAdapter
import com.x10.photo.maker.base.handler.GlideHandler
import com.x10.photo.maker.data.model.TemplateVideo
import com.x10.photo.maker.databinding.ItemImageSelectedHorizontalBinding
import com.x10.photo.maker.databinding.LayoutItemTemplateBinding
import com.x10.photo.maker.databinding.LayoutTemplateBinding
import com.x10.photo.maker.utils.setSafeOnClickListener
import com.x10.photo.maker.viewholder.ViewHolderLifecycle

class TemplateVideoAdapter(
    dataSet: MutableList<TemplateVideo?>,
    private val onClickItemTemplate: (Int) -> Unit
) : BaseRecyclerAdapter<TemplateVideo, TemplateVideoAdapter.TemplateViewHolder>(dataSet) {

    override fun getLayoutResourceItem(): Int {
        return R.layout.layout_item_template
    }

    override fun setViewHolderLifeCircle(): ViewHolderLifecycle? {
        return null
    }

    override fun onCreateBasicViewHolder(binding: ViewDataBinding?): TemplateViewHolder {
        return TemplateViewHolder(binding as LayoutItemTemplateBinding, onClickItemTemplate)
    }

    override fun onBindBasicItemView(holder: TemplateViewHolder, position: Int) {
        holder.bind(position, getDataSet()?.get(position))
    }

    inner class TemplateViewHolder(
        private val binding: LayoutItemTemplateBinding,
        private val onClickItemTemplate: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, data: TemplateVideo?) {
            GlideHandler.setImageFormDrawableResource(binding.imgImageSelected, data?.image)
            if (data?.isSelected == true) {
                binding.viewTemplateSelected.visibility = View.VISIBLE
            } else {
                binding.viewTemplateSelected.visibility = View.GONE
            }
            binding.cardViewImageSelected.setSafeOnClickListener {
                onClickItemTemplate.invoke(position)
            }
        }
    }
}