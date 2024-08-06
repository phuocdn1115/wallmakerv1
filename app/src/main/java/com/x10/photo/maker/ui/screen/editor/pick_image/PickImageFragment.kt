package com.x10.photo.maker.ui.screen.editor.pick_image

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseFragment
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.data.model.Template
import com.x10.photo.maker.databinding.LayoutPickImageBinding
import com.x10.photo.maker.enums.EditorTabType
import com.x10.photo.maker.enums.RequestCode
import com.x10.photo.maker.eventbus.HandleImageEvent
import com.x10.photo.maker.navigation.NavigationManager
import com.x10.photo.maker.ui.adapter.ImagePickerAdapter
import com.x10.photo.maker.ui.adapter.ImageSelectedAdapter
import com.x10.photo.maker.ui.dialog.DialogRequestPermissionStorage
import com.x10.photo.maker.ui.screen.editor.EditorViewModel
import com.x10.photo.maker.ui.screen.home.MainViewModel
import com.x10.photo.maker.utils.*
import com.x10.photo.maker.utils.extension.widthScreen
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PickImageFragment: BaseFragment<LayoutPickImageBinding>() {
    @Inject
    lateinit var navigationManager: NavigationManager

    var dialogRequestPermissionStorage: DialogRequestPermissionStorage? = null
    private val editorViewModel: EditorViewModel by activityViewModels()
    private val pickImageViewModel: PickImageViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private  var imagePickerAdapter: ImagePickerAdapter?= null
    private  var imageSelectedAdapter: ImageSelectedAdapter?= null
    private var listImageGallery: ArrayList<String?> = arrayListOf()
    private var listImageSelected: ArrayList<String?> = arrayListOf()
    private var heightCalculator = 0
    private var template: Template? = null
    private lateinit var layoutManager : GridLayoutManager

    /**
     * @param viewPagerEditor is view pager in editor activity
     * Function to navigate SCREENS in activity parent
     */
    private var viewPagerEditor: ViewPager2 ?= null

    companion object {
        fun newInstance(template : Template? = null) : PickImageFragment {
            val pickImageFragment = PickImageFragment()
            if (template != null) {
                val bundle = Bundle()
                bundle.putSerializable(EXTRA_TEMPLATE, template)
                pickImageFragment.arguments = bundle
            }
            return pickImageFragment
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            activity?.finish()
        }
    }

    fun setViewPagerEditor(viewPagerEditor: ViewPager2) {
        this.viewPagerEditor = viewPagerEditor
    }

    override fun getContentLayout(): Int {
        return R.layout.layout_pick_image
    }

    override fun initView() {
        binding.layoutControlPickedImage.btnContinueContainer.isEnabled = false
        initToolbar()
        binding.layoutControlPickedImage.rvImagePicked.post {
            binding.layoutControlPickedImage.rvImagePicked.run {
                layoutParams.height = widthScreen / 5 + resources.getDimensionPixelOffset(R.dimen.dp10)
                requestLayout()
                heightCalculator = layoutParams.height
                visibility  = View.GONE
                setupAdapter()
            }
        }
        pickImageViewModel.getImageFromDevice()
    }

    private fun setupAdapter(){
        BlurViewUtils.setupBlurView(requireActivity(), binding.layoutControlPickedImage.blurViewContainer, binding.root.rootView as ViewGroup)
        imagePickerAdapter = ImagePickerAdapter(listImageGallery, onSelectedImageListener = { _, image ->
                updateListImageSelected(image)
            })

        layoutManager = GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
        binding.rvImagePicker.addItemDecoration(
            MarginItemDecorationRecycleViewImagePicker(
                resources.getDimensionPixelOffset(R.dimen.dp5),
                0,
                0,
                resources.getDimensionPixelOffset(R.dimen.dp5),
                heightCalculator * 2
            )
        )

        binding.rvImagePicker.layoutManager = layoutManager
        binding.rvImagePicker.adapter = imagePickerAdapter

        imageSelectedAdapter = ImageSelectedAdapter(listImageSelected, onRemoveImageListener = { position, image ->
            imagePickerAdapter?.toggleImageSelect(image, null)
        })
        binding.layoutControlPickedImage.rvImagePicked.layoutManager = GridLayoutManager(requireContext(),5, GridLayoutManager.VERTICAL, false)
        binding.layoutControlPickedImage.rvImagePicked.addItemDecoration(
            MarginItemDecoration(
            marginLeft = resources.getDimensionPixelOffset(R.dimen.dp7)
        )
        )
        binding.layoutControlPickedImage.rvImagePicked.adapter = imageSelectedAdapter
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.layoutControlPickedImage.rvImagePicked)
    }

    private fun updateListImageSelected(image: String?, position : Int ?= null){
        if(!listImageSelected.contains(image)){
            listImageSelected.add(image)
            handleActiveButtonContinue()
            if(listImageSelected.size >= 2) binding.layoutControlPickedImage.tvDragDropImage.visibility = View.VISIBLE
            binding.layoutControlPickedImage.rvImagePicked.requestLayout()
            binding.layoutControlPickedImage.rvImagePicked.run {
                visibility = View.VISIBLE
                if(listImageSelected.size == 1){
                    AnimationAppUtils.animationSetHeight(0, heightCalculator, 500, this){
                    }
                }
            }
            imageSelectedAdapter?.notifyItemInserted(listImageSelected.size)
        }else if(listImageSelected.contains(image) || position != null){
            val positionRemoved = position ?: listImageSelected.indexOfFirst { item -> item == image }
            listImageSelected.removeAt(positionRemoved)
            if(listImageSelected.size < 2) binding.layoutControlPickedImage.tvDragDropImage.visibility = View.INVISIBLE
            handleActiveButtonContinue()
            if(listImageSelected.isEmpty()){
                binding.layoutControlPickedImage.rvImagePicked.run {
                    AnimationAppUtils.animationSetHeight(layoutParams.height, 0, 500, this){
                    }
                }
            }
            imageSelectedAdapter?.removeImageSelected(image, positionRemoved)
        }
    }

    fun handleActiveButtonContinue(){
        if(template == null && listImageSelected.isNotEmpty() || template != null && listImageSelected.size > 1){
            binding.layoutControlPickedImage.btnContinueContainer.isEnabled = true
            binding.layoutControlPickedImage.btnContinueContainer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_positive_btn)
            binding.layoutControlPickedImage.tvContinue.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.layoutControlPickedImage.icContinue.setImageResource(R.drawable.ic_to_right_arrow)
        }
        else{
            binding.layoutControlPickedImage.btnContinueContainer.isEnabled = false
            binding.layoutControlPickedImage.btnContinueContainer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_grey_color)
            binding.layoutControlPickedImage.tvContinue.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_898989))
            binding.layoutControlPickedImage.icContinue.setImageResource(R.drawable.ic_to_right_arrow_disable)
        }
    }

    /** Drag and drop item recycle view parameter */

    private var myTargetPosition = 0
    private var simpleCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT), 0){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPosition = viewHolder.absoluteAdapterPosition
            myTargetPosition = startPosition
            val endPosition = target.absoluteAdapterPosition
            myTargetPosition = endPosition
            Collections.swap(listImageSelected, startPosition, endPosition)
            binding.layoutControlPickedImage.rvImagePicked.adapter?.notifyItemMoved(startPosition, endPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if(actionState == ItemTouchHelper.ACTION_STATE_DRAG){
                viewHolder?.absoluteAdapterPosition?.let {
                    myTargetPosition = it
                    binding.layoutControlPickedImage.rvImagePicked.getChildAt(it)
                }?.let { imageSelectedAdapter?.onScaleZoomOut(it) }
            }
            else {
                if (binding.layoutControlPickedImage.rvImagePicked.getChildAt(myTargetPosition) != null) {
                    imageSelectedAdapter?.onScaleZoomIn(binding.layoutControlPickedImage.rvImagePicked.getChildAt(myTargetPosition))
                }
            }
        }
    }

    override fun initListener() {
        binding.toolBar.btnClose.setOnClickListener {
            if (editorViewModel.isFromHandleImageScreenToPickImageScreen) {
                editorViewModel.isFromHandleImageScreenToPickImageScreen = false
                viewPagerEditor?.setCurrentItem(EditorTabType.HANDLE_IMAGE_TAB.position, true)
            } else {
                activity?.onBackPressed()
            }
        }
        binding.layoutControlPickedImage.btnContinueContainer.setSafeOnClickListener {
            editorViewModel.setCurrentImageSelected(listImageSelected)
            viewPagerEditor?.setCurrentItem(EditorTabType.HANDLE_IMAGE_TAB.position, true)
        }
    }

    override fun observerLiveData() {
        pickImageViewModel.apply {
            myImageFromDevice.observe(this@PickImageFragment) {
                listImageGallery.addAll(it)
                imagePickerAdapter?.notifyDataSetChanged()
            }
        }
        editorViewModel.apply {
            myCurrentExampleTemplate.observe(this@PickImageFragment){
                template = it
            }
        }
    }

    override fun getLayoutLoading(): BaseLoadingView? {
        return null
    }

    private fun initToolbar() {
        paddingStatusBar(binding.toolBar.container)
        binding.toolBar.tvTitle.text = getString(R.string.text_toolbar)
        binding.toolBar.btnClose.setImageResource(R.drawable.ic_close)
        binding.toolBar.container.setBackgroundResource(R.drawable.bg_toolbar_pick_image)
        binding.toolBar.tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    override fun onResume() {
        super.onResume()
        activity?.onBackPressedDispatcher?.addCallback(onBackPressedCallback)
        StatusBarUtils.makeStatusBarTransparentAndDark(activity)
        if (!checkPermissionReadExternalStorage()) showDialogRequestPermissionStorage()
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
                pickImageViewModel.getImageFromDevice()
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
    fun onMessageEvent(event: HandleImageEvent) {
        when (event.message) {
            HandleImageEvent.REMOVE_IMAGE_SELECTED_FROM_HANDLE_IMAGE_SCREEN -> {
                if(event.position != null){
                    imagePickerAdapter?.toggleImageSelect(listImageSelected[event.position ?: 0])
                }
            }
        }
        EventBus.getDefault().removeStickyEvent(event)
    }
}