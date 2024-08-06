package com.x10.photo.maker.ui.screen.editor

import androidx.activity.viewModels
import com.x10.photo.maker.R
import com.x10.photo.maker.base.BaseActivity
import com.x10.photo.maker.base.BaseLoadingView
import com.x10.photo.maker.base.ConnectionLiveData
import com.x10.photo.maker.data.model.ObjectSegmentData
import com.x10.photo.maker.data.model.Template
import com.x10.photo.maker.databinding.ActivityEditorBinding
import com.x10.photo.maker.enums.SegmentType
import com.x10.photo.maker.ui.adapter.ViewPagerFragmentActivityAdapter
import com.x10.photo.maker.ui.screen.editor.handle_image.HandleImageFragment
import com.x10.photo.maker.ui.screen.editor.pick_image.PickImageFragment
import com.x10.photo.maker.ui.screen.editor.preview.PreviewVideoFragment
import com.x10.photo.maker.ui.screen.editor.set_wallpaper.SetWallpaperFragment
import com.x10.photo.maker.ui.screen.editor.template.TemplateFragment
import com.x10.photo.maker.utils.EXTRA_TEMPLATE
import com.x10.photo.maker.utils.StatusBarUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditorActivity : BaseActivity<ActivityEditorBinding>() {
    @Inject
    lateinit var connectionLiveData: ConnectionLiveData
    private val editorViewModel: EditorViewModel by viewModels()
    private lateinit var pickImageFragment: PickImageFragment
    private lateinit var handleImageFragment: HandleImageFragment
    private lateinit var previewFragment: PreviewVideoFragment
    private lateinit var templateFragment: TemplateFragment
    private lateinit var setWallpaperFragment: SetWallpaperFragment
    private var viewpagerEditorAdapter: ViewPagerFragmentActivityAdapter?= null
    private var template: Template? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_editor
    }

    override fun initView() {
        setConnectLiveData(connectionLiveData)
        StatusBarUtils.makeStatusBarTransparentAndLight(this)
        getDataFromIntent()
        initViewPager()
    }

    override fun initListener() {}

    override fun observerLiveData() {}

    override fun getLayoutLoading(): BaseLoadingView? = null

    private fun getDataFromIntent() {
        if (intent.hasExtra(EXTRA_TEMPLATE)) {
            template = intent.getSerializableExtra(EXTRA_TEMPLATE) as Template
            editorViewModel.setExampleTemplate(template)
        }
    }

    private fun initViewPager() {
        initFragmentInViewPager()
        viewpagerEditorAdapter = ViewPagerFragmentActivityAdapter(this)
        viewpagerEditorAdapter?.apply {
            addFragment(pickImageFragment)
            addFragment(handleImageFragment)
            addFragment(previewFragment)
            addFragment(templateFragment)
            addFragment(setWallpaperFragment)
        }
        binding.vpEditor.adapter = viewpagerEditorAdapter
        binding.vpEditor.offscreenPageLimit = viewpagerEditorAdapter?.itemCount ?: 5
        binding.vpEditor.isUserInputEnabled = false
    }


    private fun initFragmentInViewPager() {
        pickImageFragment = PickImageFragment.newInstance()
        pickImageFragment.setViewPagerEditor(binding.vpEditor)
        handleImageFragment = HandleImageFragment.newInstance()
        handleImageFragment.setViewPagerEditor(binding.vpEditor)
        previewFragment = PreviewVideoFragment.newInstance()
        previewFragment.setViewPagerEditor(binding.vpEditor)
        templateFragment = TemplateFragment.newInstance()
        templateFragment.setViewPagerEditor(binding.vpEditor)
        setWallpaperFragment = SetWallpaperFragment.newInstance()
    }
}