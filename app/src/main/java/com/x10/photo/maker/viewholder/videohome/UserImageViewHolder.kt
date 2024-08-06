package com.x10.photo.maker.viewholder.videohome

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.x10.photo.maker.base.handler.GlideHandler
import com.x10.photo.maker.data.model.ImageMadeByUser
import com.x10.photo.maker.databinding.ItemUserVideoHomeBinding
import com.x10.photo.maker.utils.CommonUtils
import com.x10.photo.maker.utils.setSafeOnClickListener

class UserImageViewHolder(
    private val binding: ItemUserVideoHomeBinding,
    private var onClickItemUserImage: (Int?, ImageMadeByUser) -> Unit,
    private val onClickDelete: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        setupUIItemImgVideoHome(itemView)
    }

    fun bind(data: ImageMadeByUser?, position: Int){
        GlideHandler.setImageFormUrl(binding.imgUserVideo, data?.imageThumb)
        binding.tvName.text = data?.name
        binding.tvCreated.text = data?.createTime?.let { CommonUtils.milliSecondsToDate(it) }
        binding.root.setSafeOnClickListener {
            if (data != null) {
                onClickItemUserImage.invoke(position, data)
            }
        }
        binding.icDelete.setSafeOnClickListener {
            onClickDelete.invoke(position)
        }
    }
}