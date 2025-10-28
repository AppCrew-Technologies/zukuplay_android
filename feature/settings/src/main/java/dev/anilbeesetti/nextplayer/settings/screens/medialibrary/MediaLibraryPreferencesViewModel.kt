package dev.anilbeesetti.nextplayer.settings.screens.medialibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anilbeesetti.nextplayer.core.data.repository.MediaRepository
import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.Folder
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MediaLibraryPreferencesViewModel @Inject constructor(
    mediaRepository: MediaRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val uiState = mediaRepository.getFoldersFlow()
        .map { FolderPreferencesUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FolderPreferencesUiState.Loading,
        )

    val preferences = preferencesRepository.applicationPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ApplicationPreferences(),
        )

    fun updateExcludeList(path: String) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(
                    excludeFolders = if (path in it.excludeFolders) {
                        it.excludeFolders - path
                    } else {
                        it.excludeFolders + path
                    },
                )
            }
        }
    }

    fun toggleShowFloatingPlayButton() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(showFloatingPlayButton = !it.showFloatingPlayButton)
            }
        }
    }

    fun toggleMarkLastPlayedMedia() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(markLastPlayedMedia = !it.markLastPlayedMedia)
            }
        }
    }

    fun toggleAutoScanLibrary() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(autoScanLibrary = !it.autoScanLibrary)
            }
        }
    }

    fun toggleShowHiddenFiles() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(showHiddenFiles = !it.showHiddenFiles)
            }
        }
    }

    fun toggleGroupByFolder() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(groupByFolder = !it.groupByFolder)
            }
        }
    }

    fun toggleHighQualityThumbnails() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(highQualityThumbnails = !it.highQualityThumbnails)
            }
        }
    }

    // Display Field Toggle Functions
    fun toggleShowDurationField() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(showDurationField = !it.showDurationField)
            }
        }
    }
    
    fun toggleShowSizeField() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(showSizeField = !it.showSizeField)
            }
        }
    }
    
    fun toggleShowResolutionField() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(showResolutionField = !it.showResolutionField)
            }
        }
    }
    
    fun toggleShowExtensionField() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(showExtensionField = !it.showExtensionField)
            }
        }
    }
    
    fun toggleShowPathField() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(showPathField = !it.showPathField)
            }
        }
    }
    
    fun toggleShowThumbnailField() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(showThumbnailField = !it.showThumbnailField)
            }
        }
    }
    
    fun toggleShowPlayedProgress() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(showPlayedProgress = !it.showPlayedProgress)
            }
        }
    }
}

sealed interface FolderPreferencesUiState {
    object Loading : FolderPreferencesUiState

    data class Success(val directories: List<Folder>) : FolderPreferencesUiState
}
