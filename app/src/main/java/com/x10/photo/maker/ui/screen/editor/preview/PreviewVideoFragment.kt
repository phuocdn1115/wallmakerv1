package com.x10.photo.maker.ui.screen.editor.preview

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.x10.ringo.ext.CoroutineExt
import com.x10.photo.maker.RealmManager
import com.x10.photo.maker.ads.rewarded.RewardedAdsManager
import com.x10.photo.maker.base.BaseFragment
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.base.handler.GlideHandler
import com.x10.photo.maker.data.model.DataTemplateVideo
import com.x10.photo.maker.data.model.Template
import com.x10.photo.maker.data.model.TemplateVideo
import com.x10.photo.maker.data.realm_model.WallpaperDownloaded
import com.x10.photo.maker.enums.EditorTabType
import com.x10.photo.maker.enums.WallpaperType
import com.x10.photo.maker.navigation.NavigationManager
import com.x10.photo.maker.ui.dialog.DialogCancelPreview
import com.x10.photo.maker.ui.dialog.RequestViewAdsAgainBottomSheet
import com.x10.photo.maker.ui.dialog.RequestViewAdsBottomSheet
import com.x10.photo.maker.ui.screen.editor.EditorViewModel
import com.x10.photo.maker.ui.screen.home.MainViewModel
import com.x10.photo.maker.utils.EXTRA_TEMPLATE
import com.x10.photo.maker.utils.StatusBarUtils
import com.x10.photo.maker.utils.ToastUtil
import com.x10.photo.maker.utils.setSafeOnClickListener
import com.x10.photo.maker.video.PhotoMovieFactoryUsingTemplate
import com.alo.ringo.tracking.DefaultEventDefinition.Companion.EVENT_EV2_G1_REWARD
import com.alo.ringo.tracking.base_event.AdsRewardType
import com.alo.ringo.tracking.base_event.StateDownloadType
import com.alo.ringo.tracking.base_event.StatusType
import com.x10.photo.maker.R
import com.x10.photo.maker.databinding.LayoutPreviewBinding
import com.alo.wall.maker.tracking.EventTrackingManager
import com.alo.wall.maker.tracking.MakerEventDefinition.Companion.EVENT_EV2_G2_SAVE_VIDEO
import com.alo.wall.maker.tracking.MakerEventDefinition.Companion.EVENT_EV2_G5_LOAD_PREVIEW
import com.alo.wall.maker.tracking.MakerEventDefinition.Companion.EVENT_EV2_G8_CLICK_BTN_CHANGE_ANIMATION
import com.hw.photomovie.PhotoMovie
import com.hw.photomovie.PhotoMovieFactory
import com.hw.photomovie.PhotoMoviePlayer
import com.hw.photomovie.model.PhotoData
import com.hw.photomovie.model.PhotoSource
import com.hw.photomovie.model.SimplePhotoData
import com.hw.photomovie.render.GLSurfaceMovieRenderer
import com.hw.photomovie.render.GLTextureMovieRender
import com.hw.photomovie.timer.IMovieTimer
import com.x10.photo.maker.ads.nativeads.NativeAdsInFrameSaving
import com.x10.photo.maker.aplication.ApplicationContext
import com.x10.photo.maker.aplication.ApplicationContext.getAdsContext
import com.x10.photo.maker.aplication.ApplicationContext.sessionContext
import com.x10.photo.maker.utils.extension.getDeviceId
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class
PreviewVideoFragment: BaseFragment<LayoutPreviewBinding>() {
    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var rewardedAdsManager: RewardedAdsManager

    @Inject
    lateinit var nativeAdsInFrameSaving: NativeAdsInFrameSaving

    @Inject
    lateinit var realmManager: RealmManager

    @Inject
    lateinit var eventTrackingManager: EventTrackingManager

    val editorViewModel: EditorViewModel by activityViewModels()
    val previewViewModel: PreviewViewModel by activityViewModels()
    val mainViewModel: MainViewModel by viewModels()

    var requestViewAdsBottomSheet: RequestViewAdsBottomSheet? = null
    var requestViewAdsAgainBottomSheet: RequestViewAdsAgainBottomSheet? = null
    lateinit var movieRender: GLSurfaceMovieRenderer
    var templateVideo: TemplateVideo?= null
    var dataTemplateVideo: DataTemplateVideo?= null
    var photoMoviePlayer: PhotoMoviePlayer? = null
    var photoSource: PhotoSource?= null
    var photoMovie: PhotoMovie<*>?= null
    var wallpaperType: WallpaperType? = null
    var uriImagePrepareToSaveIfWallpaperIsImage : String?= null
    var dialogCancelPreview: DialogCancelPreview ?= null
    var fragmentProgressSavingVideo: FragmentProgressSavingVideo ?= null
    var template: Template? = null
    var timeGeneratePreviewVideo = 0L

    val photoDataList = ArrayList<PhotoData>()

    private val onDismissRewardAdCallBack = object : () -> Unit {
        override fun invoke() {
            eventTrackingManager.sendRewardAdsEvent(
                eventName = EVENT_EV2_G1_REWARD,
                contentId = getAdsContext().adsRewardInPreviewId,
                inPopup = AdsRewardType.EMPTY.value,
                approve = StatusType.SUCCESS.value,
                status = StatusType.SUCCESS.value
            )
        }
    }

    val onSaveVideoProgressListener = object : SaveVideoProgressListener {
        override fun onSuccess( objectImageVideoFragment: WallpaperDownloaded) {
            editorViewModel.setSavedFilePath(objectImageVideoFragment)
            ToastUtil.showToast(getString(R.string.text_save_success), requireContext())
            CoroutineExt.runOnMainAfterDelay(500) {
                if(fragmentProgressSavingVideo?.dialog?.isShowing == true)
                    fragmentProgressSavingVideo?.dismiss()
                viewPagerEditor?.setCurrentItem(EditorTabType.SET_WALLPAPER_TAB.position, true)
            }
            if (template != null) {
                eventTrackingManager.sendContentEvent(
                    eventName = EVENT_EV2_G2_SAVE_VIDEO,
                    contentId = template!!.id.toString(),
                    contentType = "template",
                    status = StateDownloadType.OK.value
                )
            } else {
                eventTrackingManager.sendContentEvent(
                    eventName = EVENT_EV2_G2_SAVE_VIDEO,
                    contentId = "",
                    contentType = "create",
                    status = StateDownloadType.OK.value
                )
            }
        }

        override fun onProgress(progress: Int, total: Int) {
        }

        override fun onFailure(nameFile: String) {
            Log.d("ON_SAVE_VIDEO_FAILURE", "FAILURE")
            if(fragmentProgressSavingVideo?.dialog?.isShowing == true)
                fragmentProgressSavingVideo?.dismiss()
            if (template != null) {
                eventTrackingManager.sendContentEvent(
                    eventName = EVENT_EV2_G2_SAVE_VIDEO,
                    contentId = template!!.id.toString(),
                    contentType = "template",
                    status = StateDownloadType.NOK.value,
                    comment = "Failure"
                )
            } else {
                eventTrackingManager.sendContentEvent(
                    eventName = EVENT_EV2_G2_SAVE_VIDEO,
                    contentId = "",
                    contentType = "create",
                    status = StateDownloadType.NOK.value,
                    comment = "Failure"
                )
            }
        }
    }

    val onSaveImageProgressListener = object : SaveImageProgressListener {
        override fun onProgress() {

        }

        override fun onSuccess(objectImageVideoFragment: WallpaperDownloaded) {
            editorViewModel.setSavedFilePath(objectImageVideoFragment)
            ToastUtil.showToast(getString(R.string.text_save_success) ,requireContext())
            CoroutineExt.runOnMainAfterDelay(500) {
                viewPagerEditor?.setCurrentItem(EditorTabType.SET_WALLPAPER_TAB.position, true)
            }
        }

        override fun onFailure(error: String) {
            TODO("Not yet implemented")
        }
    }

    private val movieListener = object : IMovieTimer.MovieListener {
        override fun onMovieUpdate(elapsedTime: Int) {}

        override fun onMovieStarted() {}

        override fun onMoviedPaused() {}

        override fun onMovieResumed() {}

        override fun onMovieEnd() {}
    }

    private val preparePhotoMovieListener = object : PhotoMoviePlayer.OnPreparedListener {
        override fun onPreparing(moviePlayer: PhotoMoviePlayer?, progress: Float) {
            Log.d("CHECK_STATE", "onPreparing: run")
        }
        override fun onPrepared(moviePlayer: PhotoMoviePlayer?, prepared: Int, total: Int) {
            Log.d("CHECK_STATE", "onPrepared: run")
            activity?.runOnUiThread {
                photoMoviePlayer?.start()
            }
        }
        override fun onError(moviePlayer: PhotoMoviePlayer?) {
            Log.d("CHECK_STATE", "onError: run")
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewPagerEditor?.setCurrentItem(EditorTabType.HANDLE_IMAGE_TAB.position, true)
        }
    }
    /**
     * @param viewPagerEditor is view pager in editor activity
     * Function to navigate SCREENS in activity parent
     */
    private var viewPagerEditor: ViewPager2?= null
    companion object {
        fun newInstance(template: Template? = null): PreviewVideoFragment {
            val previewVideoFragment = PreviewVideoFragment()
            if (template != null) {
                val bundle = Bundle()
                bundle.putSerializable(EXTRA_TEMPLATE, template)
                previewVideoFragment.arguments = bundle
            }
            return previewVideoFragment
        }
    }

    fun setViewPagerEditor(viewPagerEditor: ViewPager2) {
        this.viewPagerEditor = viewPagerEditor
    }

    override fun getContentLayout(): Int {
        return R.layout.layout_preview
    }

    override fun initView() {
        initToolbar()
        initBottomSheet()
        fragmentProgressSavingVideo = FragmentProgressSavingVideo()
        movieRender = GLTextureMovieRender(binding.glTexture)
        photoMoviePlayer = PhotoMoviePlayer(requireContext())
        photoMoviePlayer?.apply {
            setMovieRenderer(movieRender)
            setMovieListener(movieListener)
            setLoop(true)
            setOnPreparedListener(preparePhotoMovieListener)
        }
        rewardedAdsManager.setOnAdDismissedFullScreenContentListener {
            onDismissRewardAdCallBack.invoke()
        }
    }

    private fun initBottomSheet() {
        requestViewAdsBottomSheet = RequestViewAdsBottomSheet(
            onClickViewAds = {
                showRewardedAds()
            },
            onDismiss = {
                showRequestViewAdsAgainBottomSheet()
            }
        )
        requestViewAdsAgainBottomSheet = RequestViewAdsAgainBottomSheet(
            onClickViewAds = {
                showRewardedAds()
            }
        )
    }

    override fun initListener() {
        binding.toolbar.btnBack.setSafeOnClickListener {
            activity?.onBackPressed()
        }
        binding.layoutPreviewImage.toolbar.btnBack.setSafeOnClickListener {
            activity?.onBackPressed()
        }
        binding.btnChangeTemplate.setSafeOnClickListener {
            eventTrackingManager.sendOtherEvent(EVENT_EV2_G8_CLICK_BTN_CHANGE_ANIMATION)
            editorViewModel.setCurrentDataTemplateVideo()
            viewPagerEditor?.setCurrentItem(EditorTabType.TEMPLATE_VIDEO_TAB.position, true)
        }
        binding.btnSave.setSafeOnClickListener {
            previewViewModel.setImageThumbSavingVideo(dataTemplateVideo?.listImageSelected?.first()?.uriResultCutImageInCache)
            showRequestViewAdsBottomSheet()
        }
        binding.layoutPreviewImage.btnSave.setSafeOnClickListener {
            showRequestViewAdsBottomSheet()
        }
        binding.toolbar.btnClose.setSafeOnClickListener {
            showDialogCancelPreview()
        }
        binding.layoutPreviewImage.toolbar.btnClose.setSafeOnClickListener {
            showDialogCancelPreview()
        }
    }

    override fun observerLiveData() {
        editorViewModel.apply {
            myImageThumbToPreview.observe(this@PreviewVideoFragment) { urlThumb ->
                GlideHandler.setImageFormUrl(binding.imgThumb, urlThumb)
            }
            myCurrentDataTemplateVideo.observe(this@PreviewVideoFragment) { data ->
                if (viewPagerEditor?.currentItem == EditorTabType.PREVIEW_VIDEO_TAB.position && sessionContext.isHandleGoToPreview) {
                    timeGeneratePreviewVideo = System.currentTimeMillis() - editorViewModel.getTimeGeneratePreviewVideo()
                    eventTrackingManager.sendConfigsEvent(
                        eventName = EVENT_EV2_G5_LOAD_PREVIEW,
                        loadConfigDone = StatusType.EMPTY,
                        keyRemoteConfig = "",
                        waitTime = timeGeneratePreviewVideo.toInt(),
                        status = StateDownloadType.OK.value,
                        mobileId = context?.getDeviceId().toString(),
                        country = ApplicationContext.getNetworkContext().countryKey
                    )
                    sessionContext.isHandleGoToPreview = false
                }

                binding.layoutLinearProgressIndicator.visibility = View.GONE
                Log.d("PREVIEW_VIDEO_FRAGMENT", "----------------------------------GONE-------------------------------------")
                dataTemplateVideo = data
                val listImageSelected = dataTemplateVideo?.listImageSelected
                templateVideo = data?.templateVideo
                photoDataList.clear()
                if (listImageSelected?.isNotEmpty() == true) {
                    for (img in listImageSelected) {
                        if (img?.uriResultCutImageInCache != null) photoDataList.add(SimplePhotoData(context, img.uriResultCutImageInCache, PhotoData.STATE_LOCAL))
                    }
                    if (listImageSelected.size > 1) {
                        wallpaperType = WallpaperType.VIDEO_USER_TYPE
                        binding.layoutPreviewImage.viewParentImagePreview.visibility = View.GONE
                        binding.viewParentVideoPreview.visibility = View.VISIBLE
                        binding.viewControl.visibility = View.VISIBLE
                        binding.imgThumb.visibility = View.GONE
                        photoMoviePlayer?.stop()
                        photoMoviePlayer?.setIsWindowMovie(templateVideo?.template)
                        photoDataList.add(photoDataList[0])
                        photoSource = PhotoSource(photoDataList)
                        photoMovie = if(template == null){
                            PhotoMovieFactory.generatePhotoMovie(photoSource, templateVideo?.template)
                        } else {
                            PhotoMovieFactoryUsingTemplate.generatePhotoMovies(template, photoDataList)
                        }
                        photoMoviePlayer?.setDataSource(photoMovie)
                        photoMoviePlayer?.prepare()
                    } else {
                        wallpaperType = WallpaperType.IMAGE_USER_TYPE
                        binding.viewParent.visibility = View.GONE
                        binding.layoutPreviewImage.linear.visibility = View.VISIBLE
                        binding.layoutPreviewImage.imgPreview.visibility = View.VISIBLE
                        binding.layoutPreviewImage.viewParentImagePreview.visibility = View.VISIBLE
                        GlideHandler.setImageFormUrl(binding.layoutPreviewImage.imgPreview, dataTemplateVideo?.listImageSelected?.first()?.uriResultCutImageInCache)
                        uriImagePrepareToSaveIfWallpaperIsImage = dataTemplateVideo?.listImageSelected?.first()?.uriResultCutImageInCache
                    }
                }
            }
            myPreviewMode.observe(this@PreviewVideoFragment){
                binding.layoutLinearProgressIndicator.visibility = View.VISIBLE
                Log.d("PREVIEW_VIDEO_FRAGMENT", "----------------------------------VISIBLE-------------------------------------")
                if (it > 1) {
                   binding.layoutPreviewImage.root.visibility = View.GONE
                    binding.viewParent.visibility = View.VISIBLE
                    binding.viewParentVideoPreview.visibility = View.INVISIBLE
                    binding.viewControl.visibility = View.INVISIBLE
                }
                else{
                    binding.layoutPreviewImage.imgPreview.visibility = View.INVISIBLE
                    binding.layoutPreviewImage.root.visibility = View.VISIBLE
                    binding.viewParent.visibility = View.GONE
                }
            }
            myCurrentExampleTemplate.observe(this@PreviewVideoFragment){
                template = it
                if (template != null) {
                    binding.layoutChangeTemplate.visibility = View.GONE
                    binding.spaceOne.visibility = View.VISIBLE
                    binding.spaceTwo.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getLayoutLoading(): BaseLoadingView? = null

    private fun initToolbar() {
        binding.toolbar.container.setPadding(0, StatusBarUtils.getStatusBarHeight(requireContext()), 0, resources.getDimensionPixelSize(R.dimen.dp20))
        binding.toolbar.tvTitle.text = getString(R.string.text_toolbar_status_1)
        binding.toolbar.btnBack.visibility = View.VISIBLE

        binding.layoutPreviewImage.toolbar.container.setPadding(0, StatusBarUtils.getStatusBarHeight(requireContext()), 0, resources.getDimensionPixelSize(R.dimen.dp20))
        binding.layoutPreviewImage.toolbar.tvTitle.text = getString(R.string.text_toolbar_status_preview_image)
        binding.layoutPreviewImage.toolbar.btnBack.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        /** Khi màn hình hiển thị thì set lại onBackPress callback để xử lí nghiệp vụ back trên màn hình này*/
        activity?.onBackPressedDispatcher?.addCallback(onBackPressedCallback)
        StatusBarUtils.makeStatusBarTransparentAndDark(activity)
        if(photoSource != null){
            binding.glTexture.onResume()
            photoMoviePlayer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.glTexture.onPause()
        photoMoviePlayer?.pause()
    }
}