package com.zukuplay.mediaplayer

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import dev.anilbeesetti.nextplayer.core.common.di.ApplicationScope
import dev.anilbeesetti.nextplayer.core.common.storagePermission
import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import dev.anilbeesetti.nextplayer.core.media.sync.MediaSynchronizer
import com.zukuplay.mediaplayer.BuildConfig
import com.zukuplay.mediaplayer.firebase.FirebaseManager
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class NextPlayerApplication : Application() {
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var mediaSynchronizer: MediaSynchronizer
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        try {
            // Initialize Firebase
            FirebaseManager.initialize(this)
            MobileAds.initialize(this) { }

            applicationScope.launch {
                try {
                    preferencesRepository.applicationPreferences.first()
                    preferencesRepository.playerPreferences.first()
                    if (ContextCompat.checkSelfPermission(this@NextPlayerApplication, storagePermission) == PackageManager.PERMISSION_GRANTED) {
                        mediaSynchronizer.startSync()
                    }
                } catch (e: Exception) { Timber.e(e, "Error during preferences initialization") }
            }
            if (BuildConfig.DEBUG) { Timber.plant(Timber.DebugTree()) }
        } catch (e: Exception) { Timber.e(e, "Error in Application onCreate") }
    }
}
