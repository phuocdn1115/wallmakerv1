package com.x10.photo.maker.viewholder.videohome

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.x10.photo.maker.aplication.ApplicationContext
import com.x10.photo.maker.base.handler.GlideHandler
import com.x10.photo.maker.data.model.Template
import com.x10.photo.maker.databinding.ItemTemplateVideoViewHolderBinding
import com.x10.photo.maker.utils.CommonUtils
import com.x10.photo.maker.utils.CommonUtils.getColor
import com.x10.photo.maker.utils.setSafeOnClickListener

class TemplateVideoViewHolder(
    private val binding: ItemTemplateVideoViewHolderBinding,
    private var onClickTemplate: (Int?, Template) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {
    init {
        setupUIItemImgVideoHome(itemView)
        binding.viewParent.setCardBackgroundColor(CommonUtils.randomColor())
    }

    fun bind(data : Template, position: Int){
        binding.tvNameTemplate.text = data.name
        binding.viewParent.setCardBackgroundColor(data.colorCode?.getColor() ?: CommonUtils.randomColor())
        binding.viewParent.post{
            GlideHandler.setImageFormUrlWithCallBack(binding.imgVideoTemplate, "${ApplicationContext.getNetworkContext().videoURL}${data.thumbUrlImageString()}"){
                binding.imgVideoTemplate.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }
        binding.root.setSafeOnClickListener {
            onClickTemplate.invoke(position, data)
        }
    }

}