package com.alexredchets.belkaplayer

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val mainRepo: MainRepo,
    private val netManager: NetManager,
    val player: Player
) : ViewModel() {

    private val _currentVideoData = MutableStateFlow<VideoData?>(null)
    val currentVideoData: StateFlow<VideoData?>
        get() = _currentVideoData.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?>
        get() = _errorMessage.asStateFlow()

    private val _loadingVisible = MutableStateFlow(false)
    val loadingVisible: StateFlow<Boolean>
        get() = _loadingVisible.asStateFlow()

    private var allVideos: List<VideoData?> = emptyList()

    private val videoResponses = savedStateHandle.getStateFlow(VIDEO_URIS_KEY, emptyList<VideoResponseItem>())

    init {
        player.prepare()
        player.addListener(object : Player.Listener {

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

               _currentVideoData.value = allVideos.firstOrNull { it?.mediaItem == mediaItem }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)

                handleErrors(error)
            }
        })
    }

    private fun addVideoUri(responseItem: VideoResponseItem) {
        savedStateHandle[VIDEO_URIS_KEY] = videoResponses.value + responseItem
        player.addMediaItem(MediaItem.fromUri(responseItem.fullURL ?: ""))
        VideoData(
            title = responseItem.title ?: NO_INFO_VALUE,
            author = responseItem.author?.name ?: NO_INFO_VALUE,
            description = responseItem.description ?: NO_INFO_VALUE,
            uri = Uri.parse(responseItem.fullURL),
            mediaItem = MediaItem.fromUri(Uri.parse(responseItem.fullURL))
        ).also {
            if (allVideos.isEmpty()) {
                _currentVideoData.value = it
            }
            allVideos = allVideos + it
        }
    }

    override fun onCleared() {
        super.onCleared()

        player.release()
    }

    private fun handleErrors(throwable: Throwable?) {
        throwable?.message?.run {
            _errorMessage.value = this
        }
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun loadVideos() {
        if (netManager.isConnected.not()) {
            handleErrors(Exception("No network connection"))
        } else {
            viewModelScope.launch {
                mainRepo.loadVideos()
                    .applyCommonHandling(
                        doOnError = {
                            handleErrors(it)
                        }
                    )
                    .collect { state ->
                        when (state) {
                            is State.Success -> {
                                state.data.sortedBy { it.publishedAt }.map {
                                    addVideoUri(
                                        it
                                    )
                                }
                            }

                            is State.Progress -> {
                                _loadingVisible.value = state.isLoading
                            }

                            is State.Failure -> {
                                handleErrors(state.exception)
                            }
                        }
                    }
            }
        }
    }

    companion object {
        private const val VIDEO_URIS_KEY = "videoResponses"
        private const val NO_INFO_VALUE = "-"
    }
}