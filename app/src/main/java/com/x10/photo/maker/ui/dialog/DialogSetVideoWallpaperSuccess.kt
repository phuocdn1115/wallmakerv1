package com.x10.photo.maker.ui.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.view.isInvisible
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseDialog
import com.x10.photo.maker.databinding.FragmentRequestPermissionReadAndWriteStorageBinding
import com.x10.photo.maker.databinding.LayoutDialogSetWallpaperSuccessBinding
import com.x10.photo.maker.utils.extension.isHidden
import com.google.android.material.imageview.ShapeableImageView
import com.x10.photo.maker.ads.nativeads.NativeAdsSetSuccessManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DialogSetVideoWallpaperSuccess: BaseDialog<LayoutDialogSetWallpaperSuccessBinding>() {

    @Inject
    lateinit var nativeAdsSetSuccessManager: NativeAdsSetSuccessManager

    companion object {
        fun newInstance(): DialogSetVideoWallpaperSuccess {
            return DialogSetVideoWallpaperSuccess().apply {}
        }
    }

    override fun getGravityForDialog(): Int {
        return Gravity.BOTTOM
    }

    override fun getLayoutResource(): Int {
        return R.layout.layout_dialog_set_wallpaper_success
    }

    override fun init(saveInstanceState: Bundle?, view: View?) {
        binding.txtContent.text = getString(R.string.txt_set_video_success)
        val adView = binding.nativeAdView

        /** Set other ad assets. */
        val data  = nativeAdsSetSuccessManager.getNativeAd()?.nativeAd
        adView.mediaView = binding.adMedia
        adView.headlineView = binding.adHeadline
        adView.callToActionView = binding.adCallToAction
        adView.iconView = binding.adAppIcon
        adView.starRatingView = binding.adStars
        adView.advertiserView = binding.adAdvertiser

        if (data == null) {
            binding.rootView.visibility = View.GONE
        } else {
            binding.rootView.visibility = View.VISIBLE
            (adView.headlineView as? TextView)?.text = data.headline
            if (data.callToAction == null) {
                (adView.callToActionView as Button).isHidden = true
            } else {
                (adView.callToActionView as Button).isHidden = false
                (adView.callToActionView as Button).text = data.callToAction
            }
            if (data.icon == null) {
                (adView.iconView as ShapeableImageView).isHidden = true
            } else {
                (adView.iconView as? ImageView)?.setImageDrawable(data.icon?.drawable)
                (adView.iconView as ShapeableImageView).isHidden = false
            }
            if (data.starRating == null) {
                (adView.starRatingView as RatingBar).visibility = View.GONE
            } else {
                (adView.starRatingView as RatingBar).rating = data.starRating?.toFloat() ?: 0F
                (adView.starRatingView as RatingBar).visibility = View.VISIBLE
            }
            if (data.advertiser == null) {
                (adView.advertiserView as TextView).isInvisible = true
            } else {
                (adView.advertiserView as TextView).text = data.advertiser
                (adView.advertiserView as TextView).isInvisible = false
            }
            adView.setNativeAd(data)
        }

    }

    override fun setUp(view: View?) {
       binding.btnOk.setOnClickListener {
           dismiss()
       }
    }

    override fun onStart() {
        super.onStart()
        setFullScreenStatusBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.Theme_AppCompat_Light_NoActionBar_FullScreen)
    }
}