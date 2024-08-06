package com.x10.photo.maker.navigation

import android.content.Context
import android.content.Intent
import com.x10.photo.maker.data.model.Template
import com.x10.photo.maker.data.model.Wallpaper
import com.x10.photo.maker.data.realm_model.WallpaperDownloaded
import com.x10.photo.maker.ui.screen.editor.EditorActivity
import com.x10.photo.maker.ui.screen.home.MainActivity
import com.x10.photo.maker.ui.screen.home.profile.PrivacyActivity
import com.x10.photo.maker.ui.screen.home.template.PreviewTemplateActivity
import com.x10.photo.maker.ui.screen.preview_from_home.PreviewActivity
import com.x10.photo.maker.ui.screen.set_from_home.SetWallpaperActivity
import com.x10.photo.maker.utils.*
import javax.inject.Singleton

@Singleton
class NavigationManager (private val context: Context) {

    fun navigationToEditorScreen(template: Template? = null) {
        val intent = Intent(context, EditorActivity::class.java)
        if (template != null) intent.putExtra(EXTRA_TEMPLATE, template)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun navigationToPreviewScreen(
        dataModel: Wallpaper
    ){
        val intent = Intent(context, PreviewActivity::class.java)
        intent.putExtra(EXTRA_DATA_MODEL, dataModel)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun navigationToSetWallpaperActivity(
        wallpaperDownloaded: WallpaperDownloaded
    ){
        val intent = Intent(context, SetWallpaperActivity::class.java)
        intent.putExtra(EXTRA_WALLPAPER_DOWNLOADED, wallpaperDownloaded )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun navigationToHomeScreen() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun navigationToPrivacyScreen() {
        val intent = Intent(context, PrivacyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun navigationToPreviewTemplateActivity(template: Template){
        val intent = Intent(context, PreviewTemplateActivity::class.java)
        intent.putExtra(EXTRA_TEMPLATE, template)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}