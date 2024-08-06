package com.x10.maker.video

import com.hw.photomovie.model.PhotoData

class PhotoDataMock : PhotoData("", 2) {
    override fun prepareData(targetState: Int, onDataLoadListener: OnDataLoadListener) {}
}
