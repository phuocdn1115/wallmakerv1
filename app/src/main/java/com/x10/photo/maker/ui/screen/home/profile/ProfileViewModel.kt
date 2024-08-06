package com.x10.photo.maker.ui.screen.home.profile

import androidx.lifecycle.ViewModel
import com.x10.photo.maker.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(mainRepository: MainRepository): ViewModel() {
}