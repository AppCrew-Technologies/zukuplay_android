package dev.anilbeesetti.nextplayer.core.ads.di

import com.google.firebase.firestore.FirebaseFirestore
import dev.anilbeesetti.nextplayer.core.ads.AdManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.anilbeesetti.nextplayer.core.ads.AdMobManager
import dev.anilbeesetti.nextplayer.core.ads.repository.AdRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdModule {
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideAdRepository(firestore: FirebaseFirestore): AdRepository {
        return AdRepository(firestore)
    }
    
    @Provides
    @Singleton
    fun provideAdMobManager(): AdMobManager {
        return AdMobManager()
    }
    
    @Provides
    @Singleton
    fun provideAdManager(
        adRepository: AdRepository,
        adMobManager: AdMobManager
    ): AdManager {
        return AdManager(adRepository, adMobManager)
    }
}
