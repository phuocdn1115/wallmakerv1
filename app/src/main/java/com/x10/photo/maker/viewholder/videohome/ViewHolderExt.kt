package com.x10.photo.maker.viewholder.videohome

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.x10.photo.maker.R
import com.x10.photo.maker.utils.extension.heightScreen
import com.x10.photo.maker.utils.extension.widthScreen

fun RecyclerView.ViewHolder.setupUIItemImgVideoHome(view: View) {
    when(view.layoutParams){
        is StaggeredGridLayoutManager.LayoutParams ->{
            val layoutParam = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
            view.post {
                layoutParam.height = view.width * 343 / 156
                view.requestLayout()
            }
        }
        else -> {
            val layoutParams = view.layoutParams
            view.post {
                layoutParams.width = view.context.resources.getDimensionPixelOffset(R.dimen.dp156)
                layoutParams.height = view.context.resources.getDimensionPixelOffset(R.dimen.dp343)
                view.requestLayout()
            }
        }
    }
}