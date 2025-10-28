package dev.anilbeesetti.nextplayer.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.anilbeesetti.nextplayer.core.common.media.SimpleMediaCoordinator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SimpleMediaModule {
    
    @Provides
    @Singleton
    fun provideSimpleMediaCoordinator(): SimpleMediaCoordinator {
        return SimpleMediaCoordinator()
    }
} 