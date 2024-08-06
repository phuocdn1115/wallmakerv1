package com.x10.photo.maker.viewholder.videohome

import androidx.recyclerview.widget.RecyclerView
import com.x10.photo.maker.base.handler.GlideHandler
import com.x10.photo.maker.data.Item
import com.x10.photo.maker.data.model.VideoMadeByUser
import com.x10.photo.maker.databinding.ItemUserVideoHomeBinding
import com.x10.photo.maker.utils.CommonUtils
import com.x10.photo.maker.utils.setSafeOnClickListener

class UserVideoViewHolder(
    private val binding: ItemUserVideoHomeBinding,
    private var onClickItemUserVideo: (Int?, VideoMadeByUser) -> Unit,
    private val onClickDelete: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        setupUIItemImgVideoHome(itemView)
    }

    fun bind(data: VideoMadeByUser?, position: Int){
        GlideHandler.setImageFormUrl(binding.imgUserVideo, data?.imageThumb)
        binding.tvName.text = data?.name
        binding.tvCreated.text = data?.createTime?.let { CommonUtils.milliSecondsToDate(it) }
        binding.root.setSafeOnClickListener {
            if (data != null) {
                onClickItemUserVideo.invoke(position, data)
            }
        }
        binding.icDelete.setSafeOnClickListener {
            onClickDelete.invoke(position)
        }
    }
}