package com.x10.photo.maker.ui.screen.editor.filter_brightness

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseFragment
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.data.model.FilterBrightness
import com.x10.photo.maker.databinding.LayoutListFilterBrightnessBinding
import com.x10.photo.maker.eventbus.HandleImageEvent
import com.x10.photo.maker.model.ImageSelected
import com.x10.photo.maker.ui.adapter.FilterBrightnessAdapter
import com.x10.photo.maker.utils.MarginItemDecoration
import com.x10.photo.maker.utils.PhotoUtils
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File

@AndroidEntryPoint
class FilterBrightnessFragment: BaseFragment<LayoutListFilterBrightnessBinding>() {
    private val viewModel: FilterBrightnessViewModel by viewModels()
    private val listFilterBrightness = ArrayList<FilterBrightness?>()
    private var positionPreviewOld = 2
    private var filterBrightnessAdapter: FilterBrightnessAdapter?= null
    private var imageSelectedFilter: ImageSelected?= null
    companion object {
        fun newInstance(): FilterBrightnessFragment {
            return FilterBrightnessFragment()
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.layout_list_filter_brightness
    }

    override fun initView() {
        filterBrightnessAdapter = FilterBrightnessAdapter(
            listFilterBrightness,
            onClickItemFilter = { position, filterBrightness ->
                listFilterBrightness[positionPreviewOld]?.preview = false
                filterBrightnessAdapter?.notifyItemChanged(positionPreviewOld)
                listFilterBrightness[position]?.preview = true
                filterBrightnessAdapter?.notifyItemChanged(position)
                positionPreviewOld = position
                EventBus.getDefault().post(HandleImageEvent(HandleImageEvent.UPDATE_FILTER_BRIGHTNESS_EVENT, filterBrightness))
            },
            onClickSaveFilter = { _, filterBrightness ->
                PhotoUtils.saveImageToCache(filterBrightness?.bitmapImageOrigin, requireContext(),
                    imageSelectedFilter, onSuccess = {
                        imageSelectedFilter?.uriResultCutImageInCache = it.toString()
                        imageSelectedFilter?.uriResultFilterImageInCache = it.toString()
                        Log.d("SAVE_FILTERED_IMAGE", "$it")
                    }, onFailure = { message ->
                        Log.d("SAVE_FILTERED_IMAGE", message ?:"")
                    })
                imageSelectedFilter?.bitmap = filterBrightness?.bitmapImageOrigin
                EventBus.getDefault().post(HandleImageEvent(HandleImageEvent.SAVE_FILTER_BRIGHTNESS_EVENT, filterBrightness, imageSelectedFilter))
            },
            activity = requireActivity()
        )
        binding.rvListFilterBrightness.layoutManager = GridLayoutManager(requireContext(),5, GridLayoutManager.VERTICAL, false)
        binding.rvListFilterBrightness.addItemDecoration(
            MarginItemDecoration(
                marginLeft = resources.getDimensionPixelOffset(R.dimen.dp10)
            )
        )
        binding.rvListFilterBrightness.adapter = filterBrightnessAdapter
    }

    override fun initListener() {}


    override fun observerLiveData() {
        viewModel.apply {
            myFilterBrightness.observe(this@FilterBrightnessFragment) { filterBrightness ->
                listFilterBrightness[filterBrightness.indexCreated] = filterBrightness
                filterBrightnessAdapter?.notifyItemChanged(filterBrightness.indexCreated)
            }
        }
    }

    override fun getLayoutLoading(): BaseLoadingView? = null

    @SuppressLint("NotifyDataSetChanged")
    private fun generateDefaultFilterBrightness() {
        positionPreviewOld = 2
        listFilterBrightness.clear()
        for (index in 0..4) {
            val filterBrightness = FilterBrightness()
            filterBrightness.indexCreated = index
            listFilterBrightness.add(filterBrightness)
        }
        filterBrightnessAdapter?.notifyDataSetChanged()
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
    fun onMessageEvent(event: HandleImageEvent) {
        when (event.message) {
            HandleImageEvent.UPDATE_IMAGE_LIST_EVENT -> {
                if (event.isGotoFilterMode == true) {
                    imageSelectedFilter = event.imageSelected
                    generateDefaultFilterBrightness()
                    viewModel.generateFilter(imageSelectedFilter)
                }
            }
        }
        EventBus.getDefault().removeStickyEvent(event)
    }
}