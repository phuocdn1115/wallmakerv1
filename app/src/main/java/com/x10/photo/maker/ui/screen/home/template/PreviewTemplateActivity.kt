package com.x10.photo.maker.ui.screen.home.template

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import com.x10.photo.maker.R
import com.x10.photo.maker.aplication.ApplicationContext
import com.x10.photo.maker.base.BaseActivity
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.base.handler.GlideHandler
import com.x10.photo.maker.data.model.Template
import com.x10.photo.maker.databinding.ActivityPreviewTemplateBinding
import com.x10.photo.maker.enums.RequestCode
import com.x10.photo.maker.eventbus.MessageEvent
import com.x10.photo.maker.navigation.NavigationManager
import com.x10.photo.maker.ui.dialog.DialogRequestPermissionStorage
import com.x10.photo.maker.utils.EXTRA_TEMPLATE
import com.x10.photo.maker.utils.StatusBarUtils
import com.x10.photo.maker.utils.ToastUtil
import com.x10.photo.maker.utils.setSafeOnClickListener
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.text.Cue
import dagger.hilt.android.AndroidEntryPoint
import im.ene.toro.exoplayer.ExoPlayable
import im.ene.toro.exoplayer.Playable
import im.ene.toro.exoplayer.ToroExo
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@AndroidEntryPoint
class PreviewTemplateActivity : BaseActivity<ActivityPreviewTemplateBinding>() {
    private var template: Template? = null
    @Inject
    lateinit var navigationManager: NavigationManager
    private var playable: Playable? = null
    var dialogRequestPermissionStorage: DialogRequestPermissionStorage? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_preview_template
    }

    override fun initView() {
        initToolbar()
        getDataFromIntent()
        setupUI()
    }

    override fun initListener() {
        binding.tvUseTemplate.setSafeOnClickListener {
            if (checkPermissionReadExternalStorage()) {
                navigationManager.navigationToEditorScreen(template = template)
            } else {
                showDialogRequestPermissionStorage()
            }
        }

        binding.toolbar.btnBack.setSafeOnClickListener {
            onBackPressed()
        }
    }

    override fun observerLiveData() {}

    override fun getLayoutLoading(): BaseLoadingView? = null

    private fun initToolbar() {
        binding.toolbar.container.setPadding(0, StatusBarUtils.getStatusBarHeight(this), 0, resources.getDimensionPixelSize(R.dimen.dp20))
        binding.toolbar.btnClose.visibility = View.INVISIBLE
        binding.toolbar.btnBack.visibility = View.VISIBLE
        binding.toolbar.tvTitle.text = getString(R.string.str_preview_template)
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(EXTRA_TEMPLATE)) template = intent.getSerializableExtra(EXTRA_TEMPLATE) as Template
        else finish()
    }

    private fun setupUI() {
        configTemplateVideo()
        GlideHandler.setImageFormUrl(binding.imgThumbPreview,  "${ApplicationContext.getNetworkContext().videoURL}${template?.thumbUrlImageString()}")
    }

    private fun configTemplateVideo() {
        val mediaUriWallpaperLive = Uri.parse("${ApplicationContext.getNetworkContext().videoURL}${template?.originUrlString()}")
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

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        playable?.playerView = null
        playable?.release()
        playable = null
        EventBus.getDefault().unregister(this)
    }

    private fun checkPermissionReadExternalStorage(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun showDialogRequestPermissionStorage(){
        dialogRequestPermissionStorage = DialogRequestPermissionStorage(onClickButtonLater = {
            ToastUtil.showToast(resources.getString(R.string.txt_explain_permission_storage),this)
        }, onClickButtonRequestPermission = {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RequestCode.PERMISSION_READ_EXTERNAL_STORAGE.requestCode
            )
        })
        dialogRequestPermissionStorage?.show(supportFragmentManager, "DIALOG_REQUEST_PERMISSION")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == RequestCode.PERMISSION_READ_EXTERNAL_STORAGE.requestCode) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    RequestCode.PERMISSION_WRITE_EXTERNAL_STORAGE.requestCode
                )
            }
            if (requestCode == RequestCode.PERMISSION_WRITE_EXTERNAL_STORAGE.requestCode) {
                navigationManager.navigationToEditorScreen(template = template)
                dialogRequestPermissionStorage?.dismiss()
            }
        } else {
            ToastUtil.showToast(
                resources.getString(R.string.txt_explain_permission_storage),
                this
            )
            dialogRequestPermissionStorage?.dismiss()
        }
    }

    @SuppressLint("RtlHardcoded")
    @Subscribe
    fun onMessageEvent(event: MessageEvent) {
        when (event.message) {
            MessageEvent.FINISH_TEMPLATE_ACTIVITY ->{
                finish()
            }
        }
        EventBus.getDefault().removeStickyEvent(event)
    }
}