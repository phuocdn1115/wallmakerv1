package com.x10.photo.maker.ui.screen.splash

import android.net.Uri
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import com.x10.ringo.ext.CoroutineExt
import com.x10.photo.maker.R
import com.x10.photo.maker.ads.openapp.OpenAppAdsManager
import com.x10.photo.maker.base.BaseActivity
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.base.ConnectionLiveData
import com.x10.photo.maker.databinding.ActivitySplashBinding
import com.x10.photo.maker.navigation.NavigationManager
import com.x10.photo.maker.ui.dialog.DialogRequestPermissionStorage
import com.x10.photo.maker.ui.screen.home.MainViewModel
import com.x10.photo.maker.utils.CommonUtils
import com.x10.photo.maker.utils.StatusBarUtils
import com.x10.photo.maker.utils.extension.displayMetrics
import com.x10.photo.maker.utils.pushDownClickAnimation
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import dagger.hilt.android.AndroidEntryPoint
import im.ene.toro.exoplayer.ExoPlayable
import im.ene.toro.exoplayer.Playable
import im.ene.toro.exoplayer.ToroExo
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    @Inject
    lateinit var openAppAdsManager: OpenAppAdsManager

    @Inject
    lateinit var navigationManager: NavigationManager
    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

    private val mainViewModel: MainViewModel by viewModels()

    private var playable: Playable? = null

    var dialogRequestPermission: DialogRequestPermissionStorage ?= null

    override fun initView() {
        openAppAdsManager.switchOnOff(false)
        StatusBarUtils.makeStatusBarTransparentAndLight(this)
        //don't load Ads here
        //mainViewModel.loadAds()
        val uri : Uri = RawResourceDataSource.buildRawResourceUri(R.raw.intro_splash)
        playable = ExoPlayable(ToroExo.with(this).defaultCreator, uri, null)
            .also {
                it.prepare(true)
                it.play()
            }
        playable?.playerView = binding.videoView
        binding.videoView.player?.repeatMode = Player.REPEAT_MODE_ONE
        playable?.addEventListener(object : Playable.EventListener {
            override fun onRenderedFirstFrame() {
                binding.videoView.animate().alpha(1f).duration = 300
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
        displayMetrics = CommonUtils.getScreen(baseContext)
        setupView()
    }
    override fun getContentLayout(): Int {
        return R.layout.activity_splash
    }

    override fun observerLiveData() {
        connectionLiveData.observe(this) {}
    }

    override fun getLayoutLoading(): BaseLoadingView? {
        return null
    }

    private fun setupView() {
        // Setup animation logo
        if (mainViewModel.checkIsFirstOpenApp()) {
            CoroutineExt.runOnMainAfterDelay(1200) {
                binding.view.visibility = View.VISIBLE
                binding.tvIntro.visibility = View.VISIBLE
                binding.constraint.visibility = View.VISIBLE
                binding.view.animation = AnimationUtils.loadAnimation(this, R.anim.show_splash)
                binding.tvIntro.animation = AnimationUtils.loadAnimation(this, R.anim.show_splash)
                binding.constraint.animation = AnimationUtils.loadAnimation(this, R.anim.show_splash)
            }
            mainViewModel.saveFirstOpenApp()
        } else {
            CoroutineExt.runOnMainAfterDelay(1200) {
                binding.view.visibility = View.VISIBLE
                binding.icLogoSplash.visibility = View.VISIBLE
                binding.view.animation = AnimationUtils.loadAnimation(this, R.anim.show_splash)
                binding.icLogoSplash.animation = AnimationUtils.loadAnimation(this, R.anim.show_splash)
                CoroutineExt.runOnMainAfterDelay(500) {
                    navigationManager.navigationToHomeScreen()
                    finish()
                }
            }
        }
    }

    override  fun initListener(){
        pushDownClickAnimation(0.95f, binding.btnMakeVideo){
            binding.btnMakeVideo.isEnabled = false
            navigationManager.navigationToHomeScreen()
            this.finish()
        }

    }
}