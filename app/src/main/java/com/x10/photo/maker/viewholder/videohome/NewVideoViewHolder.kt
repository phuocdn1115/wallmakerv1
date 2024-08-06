package com.x10.photo.maker.viewholder.videohome

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.x10.photo.maker.databinding.ItemNewVideoHomeBinding

class NewVideoViewHolder(
    itemBinding: ItemNewVideoHomeBinding,
    onClickItemNewVideo : () -> Unit
) : RecyclerView.ViewHolder(itemBinding.root) {
    val binding: ItemNewVideoHomeBinding
    val onClickItemNewVideo: () -> Unit
    init {
        this.binding = itemBinding
        this.onClickItemNewVideo = onClickItemNewVideo
        this.binding.cardViewNewVideo.setOnClickListener {
            onClickItemNewVideo.invoke()
        }
    }
}