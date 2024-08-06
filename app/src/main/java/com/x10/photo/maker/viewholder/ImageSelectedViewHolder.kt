package com.x10.photo.maker.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.x10.photo.maker.databinding.ItemImagePickerViewHolderBinding
import com.x10.photo.maker.databinding.ItemImageSelectedViewHolderBinding
import com.bumptech.glide.Glide

class ImageSelectedViewHolder(private val binding : ItemImageSelectedViewHolderBinding, private val onRemoveImageListener: (Int, String?) -> Unit) : RecyclerView.ViewHolder(binding.root){
    init {
        if(absoluteAdapterPosition == 0)
            setupUIItemImageFromGallery(itemView)
    }

    fun bind(data: String?, position: Int) {
        Glide.with(binding.root.context)
            .load(data)
            .into(binding.imgPicker)
        binding.icRemovePickedImage.setOnClickListener {
            onRemoveImageListener.invoke(position, data)
        }
    }
}