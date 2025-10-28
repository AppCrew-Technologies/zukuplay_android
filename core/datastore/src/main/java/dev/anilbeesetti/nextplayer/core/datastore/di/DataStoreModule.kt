package dev.anilbeesetti.nextplayer.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.anilbeesetti.nextplayer.core.datastore.serializer.ApplicationPreferencesSerializer
import dev.anilbeesetti.nextplayer.core.datastore.serializer.PlayerPreferencesSerializer
import dev.anilbeesetti.nextplayer.core.datastore.serializer.StreamHistorySerializer
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.PlayerPreferences
import dev.anilbeesetti.nextplayer.core.model.StreamHistory
import javax.inject.Singleton

private const val APP_PREFERENCES_FILE_NAME = "app_preferences.pb"
private const val PLAYER_PREFERENCES_FILE_NAME = "player_preferences.pb"
private const val STREAM_HISTORY_FILE_NAME = "stream_history.pb"

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideApplicationPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<ApplicationPreferences> =
        DataStoreFactory.create(
            serializer = ApplicationPreferencesSerializer
        ) {
            context.dataStoreFile(APP_PREFERENCES_FILE_NAME)
        }

    @Provides
    @Singleton
    fun providePlayerPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<PlayerPreferences> =
        DataStoreFactory.create(
            serializer = PlayerPreferencesSerializer
        ) {
            context.dataStoreFile(PLAYER_PREFERENCES_FILE_NAME)
        }
        
    @Provides
    @Singleton
    fun provideStreamHistoryDataStore(
        @ApplicationContext context: Context
    ): DataStore<StreamHistory> =
        DataStoreFactory.create(
            serializer = StreamHistorySerializer
        ) {
            context.dataStoreFile(STREAM_HISTORY_FILE_NAME)
        }
} 