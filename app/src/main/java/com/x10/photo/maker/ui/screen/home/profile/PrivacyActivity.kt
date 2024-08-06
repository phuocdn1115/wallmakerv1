package com.x10.photo.maker.ui.screen.home.profile

import com.x10.photo.maker.utils.StatusBarUtils
import com.x10.photo.maker.BuildConfig
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseActivity
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.databinding.ActivityPrivacyBinding
import com.x10.photo.maker.utils.setSafeOnClickListener

class PrivacyActivity : BaseActivity<ActivityPrivacyBinding>() {

    override fun getContentLayout(): Int {
        return R.layout.activity_privacy
    }

    override fun initView() {
        StatusBarUtils.makeStatusBarTransparentAndLight(this)
        paddingStatusBar(binding.root)
        binding.wvPrivacy.loadUrl(BuildConfig.POLICY_URL)
    }

    override fun initListener() {

        binding.imgBack.setSafeOnClickListener { onBackPressed() }
    }

    override fun observerLiveData() {

    }

    override fun getLayoutLoading(): BaseLoadingView? {
        return null
    }
}