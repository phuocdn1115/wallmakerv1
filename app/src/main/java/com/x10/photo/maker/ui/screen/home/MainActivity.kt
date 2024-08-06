package com.x10.photo.maker.ui.screen.home

import android.os.RemoteException
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.x10.photo.maker.utils.StatusBarUtils
import com.x10.photo.maker.base.BaseActivity
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.base.ConnectionLiveData
import com.x10.photo.maker.base.FirebaseManager
import com.x10.photo.maker.enums.HomeTabType
import com.x10.photo.maker.ui.adapter.ViewPagerFragmentActivityAdapter
import com.x10.photo.maker.ui.screen.home.profile.ProfileFragment
import com.x10.photo.maker.ui.screen.home.videos.VideosFragment
import com.x10.photo.maker.utils.CommonUtils
import com.x10.photo.maker.utils.extension.displayMetrics
import com.x10.photo.maker.R
import com.x10.photo.maker.databinding.ActivityMainBinding
import com.alo.wall.maker.tracking.EventTrackingManager
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.android.material.navigation.NavigationBarView
import com.x10.photo.maker.WallpaperMakerApp
import com.x10.photo.maker.aplication.ApplicationContext
import com.x10.photo.maker.utils.extension.getDeviceId
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

    @Inject
    lateinit var firebaseManager: FirebaseManager

    @Inject
    lateinit var eventTrackingManager: EventTrackingManager

    /**tracking install from Google PLay*/
    lateinit var installReferrerClient: InstallReferrerClient
    private val utmCampaign = "utm_campaign"
    private val utmSource = "utm_source"
    private val utmMedium = "utm_medium"
    private val utmTerm = "utm_term"
    private val utmContent = "utm_content"
    val sources = arrayOf(utmCampaign, utmSource, utmMedium, utmTerm, utmContent)

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var videoFragment: VideosFragment
    private lateinit var profileFragment: ProfileFragment
    private var viewpagerMainAdapter: ViewPagerFragmentActivityAdapter?= null
    private val onItemSelectedInBottomNavigationListener =
        NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_videos -> {
                    binding.vpMain.currentItem = HomeTabType.VIDEOS_TAB.position
                }
                R.id.menu_profile -> {
                    binding.vpMain.currentItem = HomeTabType.PROFILE_TAB.position
                }
            }
            return@OnItemSelectedListener true
        }

    private val onItemReselectedInBottomNavigationListener =
        NavigationBarView.OnItemReselectedListener { item ->
            when(item.itemId){
                R.id.menu_videos -> {
                    videoFragment.apply {
                        binding.rvVideos.scrollToPosition(0)
                        binding.appbar.setExpanded(true)
                    }
                }
                R.id.menu_profile -> {

                }
            }
        }

    override fun getContentLayout(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        initInstallTracking()
        setConnectLiveData(connectionLiveData)
        displayMetrics = CommonUtils.getScreen(baseContext)
        mainViewModel.getListVideoUser()
        StatusBarUtils.makeStatusBarTransparentAndDark(this)
        initViewPager()
    }

    override fun initListener() {
        binding.vpMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    HomeTabType.VIDEOS_TAB.position -> {
                        binding.bottomNavigationMain.selectedItemId = R.id.menu_videos
                    }
                    HomeTabType.PROFILE_TAB.position -> {
                        binding.bottomNavigationMain.selectedItemId = R.id.menu_profile
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun observerLiveData() {
        mainViewModel.apply {
            checkingInstallerIfNeedResult.observe(this@MainActivity){}
        }
    }

    private fun initInstallTracking(){
        installReferrerClient = InstallReferrerClient.newBuilder(this).build()
        installReferrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        try {
                            val response = installReferrerClient.installReferrer
                            val referrerUrl: String = response.installReferrer
                            val referrerClickTime = response.referrerClickTimestampSeconds
                            val appInstallTime = response.installBeginTimestampSeconds
                            val referrer = response.installReferrer

                            val getParams = getHashMapFromQuery(referrerUrl)
                            var utmCampaign: String? = ""
                            var utmSource: String? = ""
                            var utmMedium: String? = ""
                            var utmTerm: String? = ""
                            var utmContent: String? = ""

                            for (sourceType in sources) {
                                val source = getParams[sourceType]
                                if (source != null) {
                                    when (sourceType) {
                                        this@MainActivity.utmCampaign -> {
                                            utmCampaign = source
                                        }
                                        this@MainActivity.utmMedium -> {
                                            utmMedium = source
                                        }
                                        this@MainActivity.utmSource -> {
                                            utmSource = source
                                        }
                                        this@MainActivity.utmTerm -> {
                                            utmTerm = source
                                        }
                                        this@MainActivity.utmContent -> {
                                            utmContent = source
                                        }
                                        else -> {}
                                    }
                                }
                            }
                            mainViewModel.callApiCheckingInstallerIfNeed(
                                utmSource = utmSource,
                                utmCampaign = utmCampaign,
                                utmContent = utmContent,
                                utmMedium = utmMedium,
                                utmTerm = utmTerm
                            )
                            eventTrackingManager.setInstallationTracking(
                                utmMedium = utmMedium.toString(),
                                utmSource = utmSource.toString(),
                                mobileId = WallpaperMakerApp.getContext().getDeviceId(),
                                country = ApplicationContext.getNetworkContext().countryKey
                            )
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        } finally {
                            installReferrerClient.endConnection()
                        }
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {}
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {}
                }
            }

            override fun onInstallReferrerServiceDisconnected() {}
        })
    }

    fun getHashMapFromQuery(query: String): Map<String, String> {
        val queryPairs: MutableMap<String, String> = LinkedHashMap()
        val pairs = query.split("&".toRegex()).toTypedArray()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            queryPairs[URLDecoder.decode(pair.substring(0, idx), "UTF-8")] =
                URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
        }
        return queryPairs
    }

    override fun getLayoutLoading(): BaseLoadingView? = null

    private fun initViewPager() {
        initFragmentInViewPager()
        viewpagerMainAdapter = ViewPagerFragmentActivityAdapter(this)
        viewpagerMainAdapter?.apply {
            addFragment(videoFragment)
            addFragment(profileFragment)
        }
        binding.bottomNavigationMain.setOnItemSelectedListener(onItemSelectedInBottomNavigationListener)
        binding.bottomNavigationMain.setOnItemReselectedListener(onItemReselectedInBottomNavigationListener)
        binding.vpMain.adapter = viewpagerMainAdapter
        binding.vpMain.offscreenPageLimit = viewpagerMainAdapter?.itemCount ?: 1
    }

    private fun initFragmentInViewPager() {
        videoFragment = VideosFragment.newInstance()
        profileFragment = ProfileFragment.newInstance()
    }

}