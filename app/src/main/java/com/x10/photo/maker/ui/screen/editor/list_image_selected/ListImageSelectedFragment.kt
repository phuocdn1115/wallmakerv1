package com.x10.photo.maker.ui.screen.editor.list_image_selected

import android.annotation.SuppressLint
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.x10.ringo.ext.CoroutineExt
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseFragment
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.databinding.LayoutListImageSelectedBinding
import com.x10.photo.maker.eventbus.HandleImageEvent
import com.x10.photo.maker.model.ImageSelected
import com.x10.photo.maker.ui.adapter.ImageSelectorHorizontalAdapter
import com.x10.photo.maker.ui.screen.editor.EditorViewModel
import com.x10.photo.maker.utils.MarginItemDecoration
import com.x10.photo.maker.utils.MarginItemDecorationRecycleViewImageSelected
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ListImageSelectedFragment: BaseFragment<LayoutListImageSelectedBinding>() {
    private val editorViewModel: EditorViewModel by activityViewModels()
    private val listImageSelected = ArrayList<ImageSelected?>()
    private var positionImageSelectedOld = 0
    private var imageSelectorHorizontalAdapter: ImageSelectorHorizontalAdapter?= null
    private var viewpagerHandleImage: ViewPager2 ?= null
    private var onAddImageListener : ((Boolean) -> Unit) ?= null
    companion object {
        fun newInstance(): ListImageSelectedFragment {
            return ListImageSelectedFragment()
        }
    }

    fun setViewpagerHandleImage(viewpagerHandleImage: ViewPager2) {
        this.viewpagerHandleImage = viewpagerHandleImage
    }

    fun setOnAddImageListener(onAddImageListener: (Boolean) -> Unit){
        this.onAddImageListener = onAddImageListener
    }

    override fun getContentLayout(): Int {
        return R.layout.layout_list_image_selected
    }

    override fun initView() {
        imageSelectorHorizontalAdapter = ImageSelectorHorizontalAdapter(
            listImageSelected,
            onClickImageSelected = { position, _ ->
                refreshItemSelectedInList(position)
            },
            onClickAddImage = {
                onAddImageListener?.invoke(true)
            }
        )
        binding.rvListImageSelected.layoutManager = GridLayoutManager(requireContext(),5, GridLayoutManager.VERTICAL, false)
        binding.rvListImageSelected.addItemDecoration(
            MarginItemDecoration(
                marginLeft = resources.getDimensionPixelOffset(R.dimen.dp10)
            )
        )
        binding.rvListImageSelected.adapter = imageSelectorHorizontalAdapter
    }

    private fun refreshItemSelectedInList(position: Int) {
        listImageSelected[positionImageSelectedOld]?.isSelected = false
        imageSelectorHorizontalAdapter?.notifyItemChanged(positionImageSelectedOld)
        listImageSelected[position]?.isSelected = true
        imageSelectorHorizontalAdapter?.notifyItemChanged(position)
        positionImageSelectedOld = position
        viewpagerHandleImage?.setCurrentItem(position, false)
    }

    override fun initListener() {

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun observerLiveData() {
        editorViewModel.apply {
            myCurrentImageSelected.observe(this@ListImageSelectedFragment) {
                if (it.isNotEmpty()) {
                    positionImageSelectedOld = 0
                    listImageSelected.clear()
                    listImageSelected.addAll(it)
                    if(listImageSelected.size < 5)
                        listImageSelected.add(ImageSelected())
                    imageSelectorHorizontalAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun getLayoutLoading(): BaseLoadingView? = null

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
            HandleImageEvent.REMOVE_IMAGE_SELECTED_FROM_HANDLE_IMAGE_SCREEN -> {
                if(positionImageSelectedOld == event.position){
                    listImageSelected.removeAt(positionImageSelectedOld)
                    imageSelectorHorizontalAdapter?.notifyItemRemoved(positionImageSelectedOld)
                    imageSelectorHorizontalAdapter?.notifyItemRangeChanged(positionImageSelectedOld,listImageSelected.size)
                    if(listImageSelected.last()?.uriInput != null && listImageSelected.size < 5){
                        listImageSelected.add(ImageSelected())
                        imageSelectorHorizontalAdapter?.notifyItemInserted(listImageSelected.size)
                    }
                    if(listImageSelected.size == 1){
                        CoroutineExt.runOnMainAfterDelay(500) {
                            onAddImageListener?.invoke(false)
                        }
                    }else{
                        var pos = event.position
                        if (pos != null) {
                            if(pos != 0) pos -= 1
                            refreshItemSelectedInList(pos)
                        }
                    }
                }
            }
            HandleImageEvent.UPDATE_IMAGE_FILTER_BRIGHTNESS_IN_LIST_IMAGE_SELECTED -> {
                val positionImageChange = viewpagerHandleImage?.currentItem
                if (positionImageChange != null) {
                    listImageSelected[positionImageChange]?.bitmap = event.imageSelected?.bitmap
                    imageSelectorHorizontalAdapter?.notifyItemChanged(positionImageChange)
                }
            }
        }
        EventBus.getDefault().removeStickyEvent(event)
    }
}