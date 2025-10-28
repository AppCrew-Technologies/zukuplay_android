package dev.anilbeesetti.nextplayer.screens

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _audioState = MutableStateFlow<AudioState>(AudioState.Loading)
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadAudioFiles()
    }

    fun loadAudioFiles() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _audioState.value = AudioState.Loading
            
            try {
                val audioFiles = fetchAudioFiles()
                val folders = groupAudioFilesByFolder(audioFiles)
                
                _audioState.value = AudioState.Success(
                    AudioState.AudioData(
                        audioFiles = audioFiles,
                        folders = folders,
                        recentlyPlayedAudio = audioFiles.firstOrNull(),
                        firstAudio = audioFiles.firstOrNull()
                    )
                )
            } catch (e: Exception) {
                _audioState.value = AudioState.Error
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun onRefresh() {
        loadAudioFiles()
    }

    private suspend fun fetchAudioFiles(): List<AudioFile> = withContext(Dispatchers.IO) {
        val audioFiles = mutableListOf<AudioFile>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.ALBUM_ID
        )
        
        // Exclude non-music audio files like ringtones, notifications, etc.
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown Title"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(dataColumn) ?: ""
                val size = cursor.getLong(sizeColumn)
                val timestamp = cursor.getLong(dateModifiedColumn) * 1000 // Convert to milliseconds
                val albumId = cursor.getLong(albumIdColumn)
                
                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val albumArtUri = getAlbumArtUri(albumId)
                
                val audioFile = AudioFile(
                    id = id.toString(),
                    title = title,
                    artist = artist,
                    album = album,
                    duration = duration,
                    path = path,
                    uriString = contentUri.toString(),
                    albumArtUri = albumArtUri,
                    size = size,
                    timestamp = timestamp
                )
                
                audioFiles.add(audioFile)
            }
        }
        
        audioFiles
    }
    
    private fun getAlbumArtUri(albumId: Long): String {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId).toString()
    }
    
    private fun groupAudioFilesByFolder(audioFiles: List<AudioFile>): List<AudioFolder> {
        val folderMap = mutableMapOf<String, MutableList<AudioFile>>()
        
        // Group audio files by folder
        audioFiles.forEach { audioFile ->
            val path = audioFile.path
            val lastSeparatorIndex = path.lastIndexOf('/')
            
            if (lastSeparatorIndex > 0) {
                val folderPath = path.substring(0, lastSeparatorIndex)
                val folderName = folderPath.substring(folderPath.lastIndexOf('/') + 1)
                
                if (!folderMap.containsKey(folderPath)) {
                    folderMap[folderPath] = mutableListOf()
                }
                
                folderMap[folderPath]?.add(audioFile)
            }
        }
        
        // Convert to folder objects
        return folderMap.map { (folderPath, files) ->
            val folderName = folderPath.substring(folderPath.lastIndexOf('/') + 1)
            
            AudioFolder(
                name = folderName,
                path = folderPath,
                audioCount = files.size,
                thumbnailUri = files.firstOrNull()?.albumArtUri
            )
        }.sortedBy { it.name }
    }
} 