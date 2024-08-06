package com.x10.photo.maker.ui.screen.editor.filter_brightness

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.x10.photo.maker.data.model.FilterBrightness
import com.x10.photo.maker.eventbus.HandleImageEvent
import com.x10.photo.maker.model.ImageSelected
import com.x10.photo.maker.utils.PhotoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FilterBrightnessViewModel @Inject constructor(@ApplicationContext val context: Context) : ViewModel() {
    private val currentFilterBrightness = MutableLiveData<FilterBrightness>()
    val myFilterBrightness: LiveData<FilterBrightness> = currentFilterBrightness
    fun generateFilter(imageSelected: ImageSelected?) {
        val filterBrightnessOne = FilterBrightness()
        filterBrightnessOne.indexCreated = 0
        val filterBrightnessTwo = FilterBrightness()
        filterBrightnessTwo.indexCreated = 1
        val filterBrightnessThree = FilterBrightness()
        filterBrightnessThree.indexCreated = 2
        val filterBrightnessFour = FilterBrightness()
        filterBrightnessFour.indexCreated = 3
        val filterBrightnessFive = FilterBrightness()
        filterBrightnessFive.indexCreated = 4
        /**
         * Open IO thread to initialization filters
         */
        CoroutineScope(Dispatchers.IO).launch {
            val uriBitmapSetWhenSave =
                if (imageSelected?.uriResultFilterImageInCache != null) Uri.parse(imageSelected.uriResultFilterImageInCache)
                else Uri.fromFile(File(imageSelected?.uriInput ?: ""))


            /**
             * Initialization filter brightness three is origin (in middle no filter)
             */
            filterBrightnessThree.apply {
                bitmapImageCropped = MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    Uri.parse(imageSelected?.uriResultCutImageInCache)
                )
                isOrigin = true
                preview = true
            }
            filterBrightnessThree.bitmapImageOrigin = MediaStore.Images.Media.getBitmap(context.contentResolver, uriBitmapSetWhenSave)
            withContext(Dispatchers.Main) {
                filterBrightnessThree.isLoading = false
                currentFilterBrightness.postValue(filterBrightnessThree)
                EventBus.getDefault().post(
                    HandleImageEvent(
                        HandleImageEvent.UPDATE_FILTER_BRIGHTNESS_EVENT,
                        filterBrightnessThree
                    )
                )
            }


            /**
             * Initialization filter brightness one
             */
            filterBrightnessOne.bitmapImageCropped = PhotoUtils.changeBitmapContrastBrightness(
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    Uri.parse(imageSelected?.uriResultCutImageInCache)
                ), 0.7F, -50F
            )
            filterBrightnessOne.bitmapImageOrigin = PhotoUtils.changeBitmapContrastBrightness(
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver, uriBitmapSetWhenSave
                ), 0.7F, -50F
            )
            withContext(Dispatchers.Main) {
                filterBrightnessOne.isLoading = false
                currentFilterBrightness.postValue(filterBrightnessOne)
            }

            /**
             * Initialization filter brightness two
             */
            filterBrightnessTwo.bitmapImageCropped = PhotoUtils.changeBitmapContrastBrightness(
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    Uri.parse(imageSelected?.uriResultCutImageInCache)
                ), 0.75F, -20F
            )
            filterBrightnessTwo.bitmapImageOrigin = PhotoUtils.changeBitmapContrastBrightness(
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    uriBitmapSetWhenSave
                ), 0.75F, -20F
            )
            withContext(Dispatchers.Main) {
                filterBrightnessTwo.isLoading = false
                currentFilterBrightness.postValue(filterBrightnessTwo)
            }

            /**
             * Initialization filter brightness four
             */
            filterBrightnessFour.bitmapImageCropped = PhotoUtils.changeBitmapContrastBrightness(
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    Uri.parse(imageSelected?.uriResultCutImageInCache)
                ), 1.2F, 20F
            )
            filterBrightnessFour.bitmapImageOrigin = PhotoUtils.changeBitmapContrastBrightness(
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    uriBitmapSetWhenSave
                ), 1.2F, 2.0F
            )
            withContext(Dispatchers.Main) {
                filterBrightnessFour.isLoading = false
                currentFilterBrightness.postValue(filterBrightnessFour)
            }

            /**
             * Initialization filter brightness five
             */
            filterBrightnessFive.bitmapImageCropped = PhotoUtils.changeBitmapContrastBrightness(
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    Uri.parse(imageSelected?.uriResultCutImageInCache)
                ), 1.3F, 50F
            )
            filterBrightnessFive.bitmapImageOrigin = PhotoUtils.changeBitmapContrastBrightness(
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    uriBitmapSetWhenSave
                ), 1.3F, 50F
            )
            withContext(Dispatchers.Main) {
                filterBrightnessFive.isLoading = false
                currentFilterBrightness.postValue(filterBrightnessFive)
            }
        }
    }
}