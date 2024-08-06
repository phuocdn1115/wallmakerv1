package com.x10.photo.maker.ui.screen.editor.crop_image

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.RelativeLayout
import com.x10.ringo.ext.CoroutineExt
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseFragment
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.databinding.LayoutCropImageBinding
import com.x10.photo.maker.eventbus.HandleImageEvent
import com.x10.photo.maker.model.ImageSelected
import com.x10.photo.maker.utils.extension.displayMetrics
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.CropImageView
import com.yalantis.ucrop.view.GestureCropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.TransformImageView
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File

class CropImageFragment: BaseFragment<LayoutCropImageBinding>() {
    private var gestureCropImageView: GestureCropImageView?= null
    private var overlayView: OverlayView?= null
    private lateinit var blockingView: RelativeLayout
    private var imageSelected: ImageSelected?= null
    private val transformImageListener = object : TransformImageView.TransformImageListener {
        override fun onLoadComplete() {
            binding.uCropView.animate().alpha(1F).setDuration(300).interpolator = AccelerateInterpolator()
            binding.container.animate().alpha(1F).setDuration(500).interpolator = AccelerateInterpolator()
            blockingView.isClickable = false
        }

        override fun onLoadFailure(e: Exception) {

        }

        override fun onRotate(currentAngle: Float) {

        }

        override fun onScale(currentScale: Float) {

        }

    }
    companion object {
        fun newInstance(bundle: Bundle): CropImageFragment {
            val fragment = CropImageFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.layout_crop_image
    }

    override fun initView() {
        gestureCropImageView = binding.uCropView.cropImageView
        gestureCropImageView?.setTransformImageListener(transformImageListener)
        overlayView = binding.uCropView.overlayView
        gestureCropImageView?.setPadding(0, resources.getDimensionPixelSize(R.dimen.dp100), 0, resources.getDimensionPixelSize(R.dimen.dp100))
        overlayView?.setPadding(0, resources.getDimensionPixelSize(R.dimen.dp100), 0, resources.getDimensionPixelSize(R.dimen.dp100))
        processOptionsCropImage()
        addBlockingView()
        initData()
    }

    private fun initData() {
        imageSelected = arguments?.get(ImageSelected.KeyData.IMAGE_SELECTED) as ImageSelected
        try {
            gestureCropImageView?.setImageUri(
                Uri.fromFile(File(imageSelected?.uriInput ?: "")),
                Uri.fromFile(File(requireContext().cacheDir, imageSelected?.getChildNameCacheFile() ?: ""))
            )
        } catch (e: Exception) {}
    }

    override fun initListener() {

    }

    override fun observerLiveData() {

    }

    override fun getLayoutLoading(): BaseLoadingView? = null

    private fun processOptionsCropImage() {
        /** Crop image view option */
        gestureCropImageView?.apply {
            maxBitmapSize = CropImageView.DEFAULT_MAX_BITMAP_SIZE
            setMaxScaleMultiplier(CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER)
            setImageToWrapCropBoundsAnimDuration(CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION.toLong())
            targetAspectRatio = displayMetrics!!.widthPixels.toFloat() / displayMetrics!!.heightPixels
            setImageToWrapCropBounds()
            isRotateEnabled = false
        }

        /** Overlay view option */
        overlayView?.apply {
            freestyleCropMode = OverlayView.FREESTYLE_CROP_MODE_DISABLE
            setDimmedColor(resources.getColor(R.color.color_BF000000, null))
            setCircleDimmedLayer(false)
            setShowCropFrame(true)
            setCropFrameColor(resources.getColor(R.color.ucrop_color_default_crop_frame, null))
            setCropFrameStrokeWidth(resources.getDimensionPixelSize(R.dimen.dp2))
        }
    }

    fun cropAndSaveImage(isGotoFilterMode: Boolean ?= true, position: Int?= null) {
            val compressQuality = 90
            val compressFormat = Bitmap.CompressFormat.JPEG
            gestureCropImageView?.cropAndSaveImage(compressFormat, compressQuality, object : BitmapCropCallback {
                override fun onBitmapCropped(
                    resultUri: Uri,
                    offsetX: Int,
                    offsetY: Int,
                    imageWidth: Int,
                    imageHeight: Int
                ) {
                    CoroutineExt.runOnMain {
                        imageSelected?.uriResultCutImageInCache = resultUri.toString()
                        gestureCropImageView?.setImageUri(
                            Uri.fromFile(File(imageSelected?.uriInput ?: "")),
                            Uri.fromFile(File(requireContext().cacheDir, imageSelected?.getChildNameCacheFile() ?: "")))
                        EventBus.getDefault().post(HandleImageEvent(HandleImageEvent.UPDATE_IMAGE_LIST_EVENT, imageSelected, isGotoFilterMode, position))
                    }
                }

                override fun onCropFailure(t: Throwable) {
                    Log.d("PREVIEW_VIDEO_FRAGMENT", "FAILURE----------------------------------${t.message}-------------------------------------")
                    CoroutineExt.runOnMain {
                        EventBus.getDefault().post(HandleImageEvent(HandleImageEvent.RETRY_CROP_IMAGE_TO_PREVIEW_SCREEN))
                    }
                }
            })
    }

    private fun addBlockingView() {
        blockingView = RelativeLayout(requireContext())
        val layoutParam: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        blockingView.layoutParams = layoutParam
        blockingView.isClickable = true
        binding.container.addView(blockingView)
    }

    /** Khi vào onResume() (Tức là fragment này đang hiển thị) thì mới register các sự kiện xoay và cắt ảnh trên fragment này
     *  Khi vào onPause() (Tức là khi fragment này không hiển thị nữa) thì unregister các sự kiện xoay và cắt ảnh trên fragment này */
    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }
    
    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }
    @SuppressLint("RtlHardcoded")
    @Subscribe
    fun onMessageEvent(event: HandleImageEvent) {
        when (event.message) {
            HandleImageEvent.ROTATE_IMAGE_EVENT -> {
                gestureCropImageView?.postRotate(90F)
                gestureCropImageView?.setImageToWrapCropBounds()
            }
            HandleImageEvent.CROP_SAVE_IMAGE_EVENT -> {
                CoroutineExt.runOnIO {
                    cropAndSaveImage()
                }

            }
            HandleImageEvent.UPDATE_IMAGE_FILTER_BRIGHTNESS_IN_LIST_IMAGE_SELECTED ->{
                imageSelected = event.imageSelected
                try {
                    gestureCropImageView?.setImageUri(
                        Uri.parse(imageSelected?.uriResultCutImageInCache ?: ""),
                        Uri.fromFile(File(requireContext().cacheDir, imageSelected?.getChildNameCacheFile() ?: ""))
                    )
                } catch (e: Exception) {}
            }
        }
        EventBus.getDefault().removeStickyEvent(event)
    }
}