package com.x10.photo.maker.ui.screen.preview_from_home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.x10.photo.maker.utils.StatusBarUtils
import com.x10.photo.maker.ads.rewarded.RewardedAdsManager
import com.x10.photo.maker.base.*
import com.x10.photo.maker.base.handler.GlideHandler
import com.x10.photo.maker.data.model.Wallpaper
import com.x10.photo.maker.enums.RequestCode
import com.x10.photo.maker.enums.WallpaperType
import com.x10.photo.maker.navigation.NavigationManager
import com.x10.photo.maker.ui.dialog.DialogRequestPermissionStorage
import com.x10.photo.maker.ui.dialog.RequestViewAdsAgainBottomSheet
import com.x10.photo.maker.ui.dialog.RequestViewAdsBottomSheet
import com.x10.photo.maker.utils.EXTRA_DATA_MODEL
import com.x10.photo.maker.utils.ToastUtil
import com.x10.photo.maker.utils.setSafeOnClickListener
import com.alo.ringo.tracking.DefaultEventDefinition.Companion.EVENT_EV2_G1_REWARD
import com.alo.ringo.tracking.base_event.AdsRewardType
import com.alo.ringo.tracking.base_event.StatusType
import com.x10.photo.maker.databinding.ActivityPreviewBinding
import com.x10.photo.maker.R
import com.alo.wall.maker.tracking.EventTrackingManager
import com.blankj.utilcode.util.NetworkUtils
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.metadata.Metadata
import com.x10.photo.maker.ads.nativeads.NativeAdsInFrameSaving
import com.x10.photo.maker.aplication.ApplicationContext
import dagger.hilt.android.AndroidEntryPoint
import im.ene.toro.exoplayer.ExoPlayable
import im.ene.toro.exoplayer.Playable
import im.ene.toro.exoplayer.ToroExo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PreviewActivity : BaseActivity<ActivityPreviewBinding>() {

    @Inject
    lateinit var nativeAdsInFrameSaving: NativeAdsInFrameSaving

    @Inject
    lateinit var rewardedAdsManager: RewardedAdsManager

    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var firebaseManager: FirebaseManager

    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

    @Inject
    lateinit var eventTrackingManager: EventTrackingManager

    private val previewActivityViewModel: PreviewActivityViewModel by viewModels()
    var requestViewAdsBottomSheet: RequestViewAdsBottomSheet? = null
    var requestViewAdsAgainBottomSheet: RequestViewAdsAgainBottomSheet? = null
    var dialogRequestPermissionStorage: DialogRequestPermissionStorage? = null
    var fragmentProgressDownloadVideo: FragmentProgressDownloadVideo? = null
    var wallpaperType: WallpaperType? = null
    var dataModel: Wallpaper? = null
    private var playable: Playable? = null
    var urlThumb: String? = null
    private var isDownloadWallpaperSuccess = false

    private val onDismissRewardAdCallBack = object : () -> Unit {
        override fun invoke() {
            showProgressDownloadVideo()
            eventTrackingManager.sendRewardAdsEvent(
                eventName = EVENT_EV2_G1_REWARD,
                contentId = ApplicationContext.getAdsContext().adsRewardInPreviewId,
                inPopup = AdsRewardType.EMPTY.value,
                approve = StatusType.SUCCESS.value,
                status = StatusType.SUCCESS.value
            )
        }
    }

    var timeDelay = 300L
    private fun showProgressDownloadVideo() {
        fragmentProgressDownloadVideo?.show(supportFragmentManager, "fragmentProgressDownloadVideo")
        CoroutineScope(Dispatchers.Main).launch {
            var process = 0
            var isSetThumbSuccess = false
            while (true) {
                process++
                if (process < 100) {
                    if (process % 10 == 0 && dataModel?.url != null && isDownloadWallpaperSuccess) {
                        timeDelay = 50L
                    }
                    fragmentProgressDownloadVideo?.setPercent(process, 100)
                }
                if (urlThumb != null && !isSetThumbSuccess) {
                    fragmentProgressDownloadVideo?.setThumb(urlThumb!!)
                    isSetThumbSuccess = true
                }
                if (process > 99 && dataModel?.url != null && isDownloadWallpaperSuccess) {
                    fragmentProgressDownloadVideo?.setPercent(100, 100)
                    timeDelay = 300L
                    goToSetTab()
                    break
                }
                delay(timeDelay)
            }
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_preview
    }

    override fun initView() {
        setConnectLiveData(connectionLiveData)
        StatusBarUtils.makeStatusBarTransparentAndDark(this)
        initToolbar()
        getDataFromIntent()
        setupUI()
        initBottomSheet()
        rewardedAdsManager.setOnAdDismissedFullScreenContentListener {
            onDismissRewardAdCallBack.invoke()
        }
    }

    private fun getDataFromIntent() {
        dataModel = intent.getSerializableExtra(EXTRA_DATA_MODEL) as Wallpaper?
        urlThumb = dataModel?.minThumbURLString()
        if (dataModel != null) {
            firebaseManager.createShareLinkRingtone(dataModel!!, onSuccess = { dataModel?.urlShare = it }, onFailure = {})
        }
    }

    private fun setupUI() {
        configWallpaperLive(dataModel?.originUrlString())
        GlideHandler.setImageFormUrl(binding.imgThumbPreview, dataModel?.minThumbURLString())
    }

    private fun initBottomSheet() {
        fragmentProgressDownloadVideo = FragmentProgressDownloadVideo()
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
        dialogRequestPermissionStorage = DialogRequestPermissionStorage(onClickButtonLater = {
            ToastUtil.showToast(resources.getString(R.string.txt_explain_permission_storage), this)
            dialogRequestPermissionStorage?.dismiss()
        }, onClickButtonRequestPermission = {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RequestCode.PERMISSION_READ_EXTERNAL_STORAGE.requestCode
            )
        })
    }

    override fun initListener() {
        binding.btnDownloadVideo.setSafeOnClickListener {
            isDownloadWallpaperSuccess = false
            if (NetworkUtils.isConnected()) {
                checkPermissionToDownload()
            } else showSnackBarNoInternet()
        }
        binding.toolbar.btnBack.setSafeOnClickListener {
            onBackPressed()
        }
        binding.btnShare.setSafeOnClickListener {
            if (NetworkUtils.isConnected()) {
                dataModel?.urlShare?.let { urlShare -> startActivityShared(urlShare) }
            } else showSnackBarNoInternet()
        }
    }

    private fun startActivityShared(link: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, link)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun checkPermissionToDownload() {
        if (checkPermissionWriteExternalStorage()) {
            showRequestViewAdsBottomSheet()
        } else {
            showDialogRequestPermissionStorage()
        }
    }

    override fun observerLiveData() {
        previewActivityViewModel.apply {
            downloadWallpaperVideoResult.observe(this@PreviewActivity) {
                when (it) {
                    is Result.InProgress -> {
                        binding.btnDownloadVideo.isClickable = false
                    }
                    is Result.Success -> {
                        dataModel?.url = it.data
                        isDownloadWallpaperSuccess = true
                        binding.btnDownloadVideo.isClickable = true
                    }
                    is Result.Failure -> {
                    }
                }
            }
        }
    }

    override fun getLayoutLoading(): BaseLoadingView? = null

    private fun initToolbar() {
        binding.toolbar.container.setPadding(0, StatusBarUtils.getStatusBarHeight(this), 0, resources.getDimensionPixelSize(R.dimen.dp20))
        binding.toolbar.tvTitle.text = getString(R.string.text_toolbar_video)
        binding.toolbar.btnClose.visibility = View.INVISIBLE
        binding.toolbar.btnBack.visibility = View.VISIBLE
    }

    private fun goToSetTab() {
        ToastUtil.showToast(getString(R.string.txt_download_success),this)
        dataModel?.convertToWallpaperDownloaded()
            ?.let { navigationManager.navigationToSetWallpaperActivity(it) }
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestCode.PERMISSION_READ_EXTERNAL_STORAGE.requestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dialogRequestPermissionStorage?.dismiss()
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RequestCode.PERMISSION_WRITE_EXTERNAL_STORAGE.requestCode
            )
        } else {
            if (requestCode == RequestCode.PERMISSION_WRITE_EXTERNAL_STORAGE.requestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showRequestViewAdsBottomSheet()
                dialogRequestPermissionStorage?.dismiss()
            } else {
                dialogRequestPermissionStorage?.dismiss()
                ToastUtil.showToast(resources.getString(R.string.txt_explain_permission_storage),this)
            }
        }
    }

    private fun configWallpaperLive(urlVideo: String?) {
        val mediaUriWallpaperLive = Uri.parse(urlVideo)
        playable = ExoPlayable(ToroExo.with(this).defaultCreator, mediaUriWallpaperLive, null)
            .also {
                it.prepare(true)
                it.play()
            }
        playable?.playerView = binding.playerView
        binding.playerView.player?.repeatMode = Player.REPEAT_MODE_ONE
        playable?.addEventListener(object : Playable.EventListener {
            override fun onRenderedFirstFrame() {
                binding.imgThumbPreview.animate().alpha(0f).duration = 300
            }

            override fun onCues(cues: MutableList<Cue>) {}

            override fun onMetadata(metadata: Metadata) {}

            override fun onLoadingChanged(isLoading: Boolean) {}

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}

            override fun onRepeatModeChanged(repeatMode: Int) {}

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

            override fun onPositionDiscontinuity(reason: Int) {}

            override fun onSeekProcessed() {}

            override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {}
        })
    }


    private fun showRequestViewAdsBottomSheet() {
        requestViewAdsBottomSheet?.show(supportFragmentManager, "requestViewAdsBottomSheet")
    }

    private fun showRequestViewAdsAgainBottomSheet() {
        requestViewAdsAgainBottomSheet?.show(supportFragmentManager, "requestViewAdsAgainBottomSheet")
    }

    private fun showRewardedAds() {
        rewardedAdsManager.show(this) {
            if (it == null) {
                showProgressDownloadVideo()
            }
            dataModel?.let { it1 -> previewActivityViewModel.downloadVideo(it1)}
        }
    }

    private fun checkPermissionWriteExternalStorage(): Boolean{
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(baseContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun showDialogRequestPermissionStorage(){
        dialogRequestPermissionStorage?.show(supportFragmentManager, "DIALOG_REQUEST_PERMISSION")
    }

    override fun onDestroy() {
        super.onDestroy()
        playable?.playerView = null
        playable?.release()
        playable = null
    }
}