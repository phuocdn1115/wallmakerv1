package com.x10.photo.maker.ui.screen.home.profile

import androidx.fragment.app.viewModels
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseFragment
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.databinding.LayoutProfileBinding
import com.x10.photo.maker.navigation.NavigationManager
import com.x10.photo.maker.utils.setSafeOnClickListener
import com.blankj.utilcode.util.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment<LayoutProfileBinding>(){

    @Inject
    lateinit var navigationManager: NavigationManager

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    private val profileViewModel: ProfileViewModel by viewModels()
    override fun getContentLayout(): Int {
        return R.layout.layout_profile
    }

    override fun initView() {
    }

    override fun initListener() {
        binding.tvLabelPrivacyPolicy.setSafeOnClickListener {
            if (NetworkUtils.isConnected()) {
                navigationManager.navigationToPrivacyScreen()
            } else showSnackBarNoInternet()
        }
    }

    override fun observerLiveData() {
    }

    override fun getLayoutLoading(): BaseLoadingView? = null
}