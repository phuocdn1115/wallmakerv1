package com.x10.photo.maker.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.x10.photo.maker.R
import com.x10.photo.maker.ads.nativeads.NativeAdsInHomeManager
import com.x10.photo.maker.data.model.ImageMadeByUser
import com.x10.photo.maker.data.model.Template
import com.x10.photo.maker.data.model.VideoMadeByUser
import com.x10.photo.maker.databinding.*
import com.x10.photo.maker.enums.VideoHomeType
import com.x10.photo.maker.model.Data
import com.x10.photo.maker.model.NativeAds
import com.x10.photo.maker.viewholder.videohome.*

class VideosHomeAdapter(
    private var dataList: List<Data>,
    private var mContext : Context,
    private var onClickItemNewVideo: () -> Unit,
    private var onClickItemUserVideo: (Int?, VideoMadeByUser) -> Unit,
    private var onClickItemUserImage: (Int?, ImageMadeByUser) -> Unit,
    private var onClickTemplate: (Int?, Template) -> Unit,
    private val onClickDelete: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var nativeAdsInHomeManager: NativeAdsInHomeManager

    fun setNativeAdsManager(nativeAdsInHomeManager: NativeAdsInHomeManager) {
        this.nativeAdsInHomeManager = nativeAdsInHomeManager
    }

    override fun getItemViewType(position: Int): Int {
        val viewTypeInPosition = dataList[position]
        return when (viewTypeInPosition) {
            is VideoMadeByUser -> {
                VideoHomeType.USER_VIDEO.viewType
            }
            is ImageMadeByUser -> {
                VideoHomeType.IMAGE_USER.viewType
            }
            is Template ->{
                VideoHomeType.TEMPLATE.viewType
            }
            is NativeAds -> {
                VideoHomeType.NATIVE_ADS.viewType
            }
            else -> VideoHomeType.NEW_VIDEO.viewType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = LayoutInflater.from(parent.context)
        return when (viewType) {
            VideoHomeType.USER_VIDEO.viewType -> {
                val binding = DataBindingUtil.inflate(
                    inflate,
                    R.layout.item_user_video_home,
                    parent,
                    false
                ) as ItemUserVideoHomeBinding
                return UserVideoViewHolder(binding, onClickItemUserVideo, onClickDelete)
            }
            VideoHomeType.IMAGE_USER.viewType -> {
                val binding = DataBindingUtil.inflate(
                    inflate,
                    R.layout.item_user_video_home,
                    parent,
                    false
                ) as ItemUserVideoHomeBinding
                return UserImageViewHolder(binding, onClickItemUserImage, onClickDelete)
            }
            VideoHomeType.NATIVE_ADS.viewType -> {
                val binding = DataBindingUtil.inflate(
                    inflate,
                    R.layout.item_native_ads_video_home,
                    parent,
                    false
                ) as ItemNativeAdsVideoHomeBinding
                return NativeAdsViewHolder(binding,mContext)
            }
            VideoHomeType.TEMPLATE.viewType ->{
                val binding = DataBindingUtil.inflate(
                    inflate,
                    R.layout.item_template_video_view_holder,
                    parent,
                    false
                ) as ItemTemplateVideoViewHolderBinding
                return TemplateVideoViewHolder(binding, onClickTemplate)
            }
            else -> {
                val binding = DataBindingUtil.inflate(
                    inflate,
                    R.layout.item_new_video_home,
                    parent,
                    false
                ) as ItemNewVideoHomeBinding
                return NewVideoViewHolder(binding,onClickItemNewVideo)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.apply {
            when (holder) {
                is UserVideoViewHolder -> {
                    var dataInPosition = dataList[position]
                    when (dataInPosition) {
                        is VideoMadeByUser -> {
                            holder.bind(dataInPosition, position)
                        }
                    }
                }

                is UserImageViewHolder -> {
                    var dataInPosition = dataList[position]
                    when (dataInPosition) {
                        is ImageMadeByUser -> {
                            holder.bind(dataInPosition, position)
                        }
                    }
                }
                is NativeAdsViewHolder -> {
                    var dataInPosition = dataList[position]
                    when (dataInPosition) {
                        is NativeAds -> {
                            nativeAdsInHomeManager.getNativeAd()?.nativeAd?.let { holder.bind(it) }
                        }
                    }
                }
                is TemplateVideoViewHolder ->{
                    var dataInPosition = dataList[position]
                    if(dataInPosition is Template){
                        holder.bind(dataInPosition, position)
                    }
                }
                is NewVideoViewHolder -> {

                }

                else -> {

                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}