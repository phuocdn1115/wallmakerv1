package com.x10.photo.maker.ui.screen.home.videos

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.x10.photo.maker.ads.nativeads.NativeAdsInHomeManager
import com.x10.photo.maker.ads.openapp.OpenAppAdsManager
import com.x10.photo.maker.aplication.ApplicationContext
import com.x10.photo.maker.base.BaseFragment
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.base.ConnectionLiveData
import com.x10.photo.maker.base.Result
import com.x10.photo.maker.data.model.Wallpaper
import com.x10.photo.maker.data.response.DataHomeResponse
import com.x10.photo.maker.enums.*
import com.x10.photo.maker.eventbus.MessageEvent
import com.x10.photo.maker.model.Data
import com.x10.photo.maker.model.NativeAds
import com.x10.photo.maker.model.NewVideo
import com.x10.photo.maker.navigation.NavigationManager
import com.x10.photo.maker.ui.adapter.VideosHomeAdapter
import com.x10.photo.maker.ui.dialog.DialogConfirmDeleteVideo
import com.x10.photo.maker.ui.dialog.DialogRequestPermissionStorage
import com.x10.photo.maker.ui.screen.home.MainViewModel
import com.x10.photo.maker.utils.ToastUtil
import com.x10.photo.maker.utils.setSafeOnClickListener
import com.alo.ringo.tracking.base_event.StateDownloadType
import com.alo.ringo.tracking.base_event.StatusType
import com.x10.photo.maker.R
import com.x10.photo.maker.databinding.LayoutVideoBinding
import com.alo.wall.maker.tracking.EventTrackingManager
import com.alo.wall.maker.tracking.MakerEventDefinition.Companion.EVENT_EV2_G2_CLICK_CREATE_VIDEO
import com.alo.wall.maker.tracking.MakerEventDefinition.Companion.EVENT_EV2_G2_CLICK_TEMPLATE
import com.alo.wall.maker.tracking.MakerEventDefinition.Companion.EVENT_EV2_G5_LOAD_PREVIEW
import com.blankj.utilcode.util.NetworkUtils
import com.google.android.material.appbar.AppBarLayout
import com.x10.photo.maker.utils.extension.getDeviceId
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.math.abs
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class VideosFragment : BaseFragment<LayoutVideoBinding>(){

    @Inject
    lateinit var openAppAdsManager: OpenAppAdsManager

    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

    @Inject
    lateinit var eventTrackingManager: EventTrackingManager

    private val mainViewModel: MainViewModel by activityViewModels()
    var dialogRequestPermissionStorage: DialogRequestPermissionStorage? = null
    private var dataListVideo = arrayListOf<Data>()
    private lateinit var adapterVideosHome : VideosHomeAdapter
    private val listVideoUser = LinkedList<Data>()
    private var currentPositionItemClicked: Int?= 0
    var dialogDeleteVideo : DialogConfirmDeleteVideo ?= null

    @Inject
    lateinit var nativeAdsInHomeManager: NativeAdsInHomeManager

    companion object {
        fun newInstance(): VideosFragment {
            return VideosFragment()
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.layout_video
    }

    override fun initView() {
        setupRecyclerView()
        setupFooterLoadMore()
        handleBackPressed()
    }

    override fun initListener() {
        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                binding.collapsingToolbar.title = ""
                binding.viewToolbarCollapsed.visibility = View.VISIBLE
            } else {
                binding.collapsingToolbar.title = resources.getText(R.string.text_tab_videos)
                binding.viewToolbarCollapsed.visibility = View.GONE
            }
        })
        binding.cardViewNewVideoToolBar.setSafeOnClickListener {
            eventTrackingManager.sendContentEvent(eventName = EVENT_EV2_G2_CLICK_CREATE_VIDEO)
            if (checkPermissionReadExternalStorage()) {
                navigationManager.navigationToEditorScreen()
            } else {
                showDialogRequestPermissionStorage()
            }
        }
    }

    private fun setupFooterLoadMore(){
        binding.refreshLayout.setHeaderHeight(0F)
        binding.refreshLayout.setFooterHeight(100F)
        binding.refreshLayout.setOnLoadMoreListener {
            binding.refreshLayout.finishLoadMore()
        }
    }

    override fun observerLiveData() {
        mainViewModel.apply {
            dataHomeResult.observe(this@VideosFragment){ result ->
                when(result){
                    is Result.InProgress ->{
                        if (dataListVideo.isEmpty())
                            showLoading(true)
                    }
                    is Result.Success ->{
                        showLoading(false){
                            processData(result.data)
                            adapterVideosHome.notifyDataSetChanged()
                        }
                        /**
                         * Check load Ads first time
                         */
                        if(!ApplicationContext.getAdsContext().isLoadAds){
                            ApplicationContext.getAdsContext().isLoadAds = true
                            openAppAdsManager.switchOnOff(true)
                            mainViewModel.loadAds()
                        }

                    }
                    is Result.Failure -> {
                    }
                    is Result.Error -> {
                    }
                    is Result.Failures<*> -> {
                    }
                }
            }
            listVideoUserResult.observe(this@VideosFragment){
                listVideoUser.addAll(it)
            }
        }
        connectionLiveData.observe(this@VideosFragment) { isConnected ->
            if (isConnected && !ApplicationContext.getAdsContext().isLoadAds) {
                mainViewModel.loadAds()
            }
        }
    }

    override fun getLayoutLoading(): BaseLoadingView = binding.viewLoading

    private fun handleBackPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                    openAppAdsManager.switchOnOff(false)
                }
            })
    }

    private fun processData(result: DataHomeResponse){
        val dataResponse = result.data.data as ArrayList<Data>
        dataListVideo.add(NewVideo())
        for (position in dataResponse.indices) {
            val itemInDataResponse = dataResponse[position] as Wallpaper
            when (itemInDataResponse.type) {
                DataType.VIDEO_MADE_BY_USER.type -> {
                    listVideoUser.poll()?.let { dataResponse.set(position, it) }
                }
                DataType.NATIVE_ADS.type -> {
                    dataResponse[position] = NativeAds()
                }
                DataType.VIDEO_TEMPLATE_TYPE.type -> {
                    dataResponse[position] = itemInDataResponse.convertToTemplateObject()
                }
            }
        }
        dataListVideo.addAll(listVideoUser)
        dataListVideo.addAll(dataResponse)
    }

    private fun setupRecyclerView() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvVideos.layoutManager = layoutManager
        adapterVideosHome = VideosHomeAdapter(
            dataList = dataListVideo,
            mContext = requireContext(),
            onClickItemNewVideo = {
                eventTrackingManager.sendContentEvent(eventName = EVENT_EV2_G2_CLICK_CREATE_VIDEO)
                if (checkPermissionReadExternalStorage()) {
                    navigationManager.navigationToEditorScreen()
                } else {
                    showDialogRequestPermissionStorage()
                }
            },
            onClickItemUserImage = { position, userImage ->
                if (NetworkUtils.isConnected()) {
                    currentPositionItemClicked = position
                    navigationManager.navigationToSetWallpaperActivity(userImage.convertToWallpaperDownloaded())
                } else showSnackBarNoInternet()
            },
            onClickItemUserVideo = { position, userVideo ->
                if (NetworkUtils.isConnected()) {
                    currentPositionItemClicked = position
                    navigationManager.navigationToSetWallpaperActivity(userVideo.convertToWallpaperDownloaded())
                } else showSnackBarNoInternet()
            },
            onClickDelete = { position ->
                dialogDeleteVideo = DialogConfirmDeleteVideo(onClickDelete = {
                    mainViewModel.deleteVideoUser(dataListVideo[position])
                    dataListVideo.removeAt(position)
                    adapterVideosHome.notifyItemRemoved(position)
                    adapterVideosHome.notifyItemRangeChanged(position, dataListVideo.size)
                })
                dialogDeleteVideo?.show(parentFragmentManager, "dialog_confirm_delete")
            },
            onClickTemplate = { position, template ->
                eventTrackingManager.sendContentEvent(
                    eventName = EVENT_EV2_G2_CLICK_TEMPLATE,
                    contentId = template.id.toString()
                )
                navigationManager.navigationToPreviewTemplateActivity(template)
            }
        )
        adapterVideosHome.setNativeAdsManager(nativeAdsInHomeManager)
        binding.rvVideos.adapter = adapterVideosHome
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
                navigationManager.navigationToEditorScreen()
                dialogRequestPermissionStorage?.dismiss()
            }
        } else {
            ToastUtil.showToast(
                resources.getString(R.string.txt_explain_permission_storage),
                requireContext()
            )
            dialogRequestPermissionStorage?.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @SuppressLint("RtlHardcoded")
    @Subscribe
    fun onMessageEvent(event: MessageEvent) {
        when (event.message) {
            MessageEvent.SAVED_VIDEO_USER ->{
                val eventObj = event.objRealm
                if(eventObj?.wallpaperType == WallpaperType.VIDEO_USER_TYPE.value){
                    event.objRealm?.convertToVideoUser()?.let { dataListVideo.add(1, it) }
                }else if(eventObj?.wallpaperType == WallpaperType.IMAGE_USER_TYPE.value){
                    event.objRealm?.convertToImageUser()?.let { dataListVideo.add(1, it) }
                }
                adapterVideosHome.notifyItemInserted(1)
                adapterVideosHome.notifyItemRangeChanged(1, dataListVideo.size)
            }
            MessageEvent.RENAME_IMAGE_VIDEO ->{
                val dataObj = event.objRealm
                val wallpaperType = dataObj?.wallpaperType
                if(wallpaperType == WallpaperType.VIDEO_USER_TYPE.value){
                    dataListVideo[currentPositionItemClicked!!] = dataObj.convertToVideoUser()
                    adapterVideosHome.notifyItemChanged(currentPositionItemClicked!!, dataObj.convertToVideoUser())
                }else if(wallpaperType == WallpaperType.IMAGE_USER_TYPE.value){
                    dataListVideo[currentPositionItemClicked!!] = dataObj.convertToImageUser()
                    adapterVideosHome.notifyItemChanged(currentPositionItemClicked!!, dataObj.convertToImageUser())
                }
            }
        }
        EventBus.getDefault().removeStickyEvent(event)
    }
}