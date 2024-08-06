package com.x10.photo.maker.ui.screen.editor.preview

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.x10.photo.maker.data.realm_model.WallpaperDownloaded
import com.x10.photo.maker.enums.WallpaperType
import com.x10.photo.maker.eventbus.MessageEvent
import com.x10.photo.maker.ui.dialog.DialogCancelPreview
import com.x10.photo.maker.utils.FileUtils
import com.x10.photo.maker.video.PhotoMovieFactoryUsingTemplate
import com.hw.photomovie.PhotoMovieFactory
import com.hw.photomovie.record.GLMovieRecorder
import com.hw.photomovie.record.GLMovieRecorder.OnRecordListener
import com.hw.photomovie.render.GLSurfaceMovieRenderer
import com.x10.photo.maker.data.model.Template
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat

fun PreviewVideoFragment.showRequestViewAdsBottomSheet() {
    requestViewAdsBottomSheet?.show(childFragmentManager, "requestViewAdsBottomSheet")
}

fun PreviewVideoFragment.showRequestViewAdsAgainBottomSheet() {
    requestViewAdsAgainBottomSheet?.show(childFragmentManager, "requestViewAdsAgainBottomSheet")
}

fun PreviewVideoFragment.showRewardedAds() {
    rewardedAdsManager.show(requireActivity()) {
        if (wallpaperType == WallpaperType.VIDEO_USER_TYPE) {
            saveVideo(onSaveVideoProgressListener)
            fragmentProgressSavingVideo?.show(childFragmentManager, FragmentProgressSavingVideo::class.java.name)
        }
        else saveImage(onSaveImageProgressListener)
    }
}

fun PreviewVideoFragment.saveVideo(onSaveVideoProgressListener: SaveVideoProgressListener) {
    photoMoviePlayer?.pause()
    val startRecodTime = System.currentTimeMillis()
    val recorder = GLMovieRecorder(activity)
    val file = FileUtils.initVideoFile(requireActivity())
    val bitrate = if (binding.glTexture.width * binding.glTexture.height > 1000 * 1500) 8000000 else 4000000
    recorder.configOutput(
        binding.glTexture.width,
        binding.glTexture.height,
        bitrate,
        30,
        1,
        file.absolutePath
    )
    Log.d("getAbsolutePath", file.absolutePath)
    val newPhotoMovie = if (template == null) PhotoMovieFactory.generatePhotoMovie(
        photoMovie?.photoSource,
        templateVideo?.template
    )
    else PhotoMovieFactoryUsingTemplate.generatePhotoMovies(template, photoDataList)
    val newMovieRenderer = GLSurfaceMovieRenderer(movieRender)
    newMovieRenderer.photoMovie = newPhotoMovie
    Log.d("CHECK_PHOTO_MOVIE", "${newPhotoMovie == photoMovie}")
    recorder.setDataSource(newMovieRenderer)
    recorder.startRecord(object : OnRecordListener {
        override fun onRecordFinish(success: Boolean) {
            val recordEndTime = System.currentTimeMillis()
            Log.i("Record", "record:" + (recordEndTime - startRecodTime))
            if (success) {
                onSaveVideoProgressListener.onSuccess(
                    saveObjVideo(file, template)
                )
            } else {
                onSaveVideoProgressListener.onFailure("Error!!!")
            }
        }

        override fun onRecordProgress(recordedDuration: Int, totalDuration: Int) {
            previewViewModel.sendProgressSavingVideo(recordedDuration, totalDuration)
        }
    })
}

@SuppressLint("SimpleDateFormat")
fun PreviewVideoFragment.saveImage(onSaveImageProgressListener: SaveImageProgressListener) {
    val saved: Boolean
    val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, Uri.parse(uriImagePrepareToSaveIfWallpaperIsImage))
    val name = String.format(
        "wallpaper_maker_%s",
        SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(System.currentTimeMillis())
    )
    val fos: OutputStream?
    onSaveImageProgressListener.onProgress()
    var filePath: String? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver: ContentResolver = requireContext().contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        val imageUri: Uri? =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = imageUri?.let { resolver.openOutputStream(it) }

        val cursor = imageUri?.let { context?.contentResolver?.query(it, arrayOf(MediaStore.Images.Media.DATA), null, null, null) }
        if(cursor != null){
            if(cursor.moveToFirst()){
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = cursor.getString(columnIndex)
                val wallpaperDownloaded = WallpaperDownloaded()
                wallpaperDownloaded.id = System.currentTimeMillis().toString()
                wallpaperDownloaded.createTime = System.currentTimeMillis()
                wallpaperDownloaded.name = name
                wallpaperDownloaded.wallpaperType = wallpaperType?.value.toString()
                wallpaperDownloaded.pathInStorage = filePath
                wallpaperDownloaded.imageThumb = filePath
                realmManager.save(wallpaperDownloaded)
                onSaveImageProgressListener.onSuccess(wallpaperDownloaded)
            }
        }

    } else {
        val imagesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).toString() + File.separator + name
        val file = File(imagesDir)
        if (!file.exists()) {
            file.mkdir()
        }
        val image = File(imagesDir, "$name.png")
        fos = FileOutputStream(image)

        val wallpaperDownloaded = WallpaperDownloaded()
        wallpaperDownloaded.id = System.currentTimeMillis().toString()
        wallpaperDownloaded.createTime = System.currentTimeMillis()
        wallpaperDownloaded.name = name
        wallpaperDownloaded.wallpaperType = wallpaperType?.value.toString()
        wallpaperDownloaded.pathInStorage = image.absolutePath
        wallpaperDownloaded.imageThumb = uriImagePrepareToSaveIfWallpaperIsImage

        realmManager.save(wallpaperDownloaded)
        onSaveImageProgressListener.onSuccess(wallpaperDownloaded)

    }
    saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos) == true
    if (!saved) onSaveImageProgressListener.onFailure("Save image failure!!")
    Log.d("SAVE_IMAGE", "$saved")
    fos?.flush()
    fos?.close()
}

fun PreviewVideoFragment.saveObjVideo(file : File, template: Template?) : WallpaperDownloaded{
    val wallpaperDownloaded = WallpaperDownloaded()
    wallpaperDownloaded.id = System.currentTimeMillis().toString()
    wallpaperDownloaded.createTime = System.currentTimeMillis()
    wallpaperDownloaded.name = file.name
    wallpaperDownloaded.wallpaperType = wallpaperType?.value.toString()
    wallpaperDownloaded.pathInStorage = file.absolutePath
    wallpaperDownloaded.isTemplate = template != null
    wallpaperDownloaded.idTemplate = template?.id.toString()
    wallpaperDownloaded.imageThumb = dataTemplateVideo?.listImageSelected?.first()?.uriResultCutImageInCache
    Log.d("SAVE_OBJ_REALM", "${wallpaperDownloaded.imageThumb}")
    realmManager.save(wallpaperDownloaded)
    return wallpaperDownloaded
}

fun PreviewVideoFragment.showDialogCancelPreview(){
    dialogCancelPreview = DialogCancelPreview(onClickExit = {
        EventBus.getDefault().post(MessageEvent(MessageEvent.FINISH_TEMPLATE_ACTIVITY))
        activity?.finish()
    })
    dialogCancelPreview?.show(childFragmentManager,DialogCancelPreview::class.java.name)
}