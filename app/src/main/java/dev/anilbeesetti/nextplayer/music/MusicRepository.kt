package dev.anilbeesetti.nextplayer.music

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Music repository based on Auxio's architecture for managing music data.
 * This handles music indexing, library management, and provides music data to the UI.
 */
class MusicRepository(
    private val context: Context
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Music library state
    private val _musicLibrary = MutableStateFlow<MusicLibrary?>(null)
    val musicLibrary: StateFlow<MusicLibrary?> = _musicLibrary.asStateFlow()
    
    // Indexing state
    private val _indexingState = MutableStateFlow<IndexingState>(IndexingState.NotIndexed)
    val indexingState: StateFlow<IndexingState> = _indexingState.asStateFlow()
    
    // Current song
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    // Playback queue
    private val _playbackQueue = MutableStateFlow<List<Song>>(emptyList())
    val playbackQueue: StateFlow<List<Song>> = _playbackQueue.asStateFlow()
    
    init {
        // Start initial music indexing
        refreshMusicLibrary()
    }
    
    /**
     * Refresh the music library by re-indexing all music files
     */
    fun refreshMusicLibrary() {
        repositoryScope.launch {
            _indexingState.value = IndexingState.Indexing
            try {
                val library = indexMusicLibrary()
                _musicLibrary.value = library
                _indexingState.value = IndexingState.Complete
            } catch (e: Exception) {
                _indexingState.value = IndexingState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Index the music library from device storage
     */
    private suspend fun indexMusicLibrary(): MusicLibrary {
        // This would integrate with Auxio's musikr module for actual music indexing
        // For now, we'll create a basic implementation
        val songs = mutableListOf<Song>()
        val albums = mutableListOf<Album>()
        val artists = mutableListOf<Artist>()
        val genres = mutableListOf<Genre>()
        
        // TODO: Implement actual music indexing using Auxio's musikr module
        // This would scan the device for music files and extract metadata
        
        return MusicLibrary(
            songs = songs,
            albums = albums,
            artists = artists,
            genres = genres
        )
    }
    
    /**
     * Play a specific song
     */
    fun playSong(song: Song) {
        _currentSong.value = song
        // TODO: Integrate with music service to start playback
    }
    
    /**
     * Play songs from a specific album
     */
    fun playAlbum(album: Album, startSong: Song? = null) {
        val albumSongs = _musicLibrary.value?.songs?.filter { it.albumId == album.id } ?: return
        _playbackQueue.value = albumSongs
        _currentSong.value = startSong ?: albumSongs.firstOrNull()
    }
    
    /**
     * Play songs from a specific artist
     */
    fun playArtist(artist: Artist, startSong: Song? = null) {
        val artistSongs = _musicLibrary.value?.songs?.filter { it.artistId == artist.id } ?: return
        _playbackQueue.value = artistSongs
        _currentSong.value = startSong ?: artistSongs.firstOrNull()
    }
    
    /**
     * Shuffle all songs
     */
    fun shuffleAll() {
        val allSongs = _musicLibrary.value?.songs?.shuffled() ?: return
        _playbackQueue.value = allSongs
        _currentSong.value = allSongs.firstOrNull()
    }
    
    /**
     * Add song to queue
     */
    fun addToQueue(song: Song) {
        val currentQueue = _playbackQueue.value.toMutableList()
        currentQueue.add(song)
        _playbackQueue.value = currentQueue
    }
    
    /**
     * Remove song from queue
     */
    fun removeFromQueue(song: Song) {
        val currentQueue = _playbackQueue.value.toMutableList()
        currentQueue.remove(song)
        _playbackQueue.value = currentQueue
    }
}

/**
 * Represents the state of music indexing
 */
sealed class IndexingState {
    object NotIndexed : IndexingState()
    object Indexing : IndexingState()
    object Complete : IndexingState()
    data class Error(val message: String) : IndexingState()
}

/**
 * Represents the complete music library
 */
data class MusicLibrary(
    val songs: List<Song>,
    val albums: List<Album>,
    val artists: List<Artist>,
    val genres: List<Genre>
)

/**
 * Represents a song in the music library
 */
data class Song(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String,
    val albumName: String,
    val genreId: String?,
    val genreName: String?,
    val duration: Long,
    val track: Int?,
    val disc: Int?,
    val year: Int?,
    val uri: Uri,
    val path: String,
    val size: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val albumArtUri: Uri?
)

/**
 * Represents an album in the music library
 */
data class Album(
    val id: String,
    val name: String,
    val artistId: String,
    val artistName: String,
    val year: Int?,
    val songCount: Int,
    val duration: Long,
    val albumArtUri: Uri?
)

/**
 * Represents an artist in the music library
 */
data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int,
    val songCount: Int,
    val genres: List<String>
)

/**
 * Represents a genre in the music library
 */
data class Genre(
    val id: String,
    val name: String,
    val songCount: Int,
    val artistCount: Int
) 