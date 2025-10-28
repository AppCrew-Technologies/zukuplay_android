package com.zukuplay.mediaplayer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val preferencesRepository: PreferencesRepository
) : ViewModel() {
    // ViewModel logic for managing preferences and app state
}
