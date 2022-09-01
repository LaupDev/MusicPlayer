package com.laupdev.spotifyclone.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laupdev.spotifyclone.exoplayer.MusicService
import com.laupdev.spotifyclone.exoplayer.MusicServiceConnection
import com.laupdev.spotifyclone.exoplayer.currentPlaybackPosition
import com.laupdev.spotifyclone.other.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    private val _currentSongDuration = MutableLiveData<Long>()
    val currentSongDuration: LiveData<Long> = _currentSongDuration

    private val _currentPlayerPosition = MutableLiveData<Long>()
    val currentPlayerPosition: LiveData<Long> = _currentPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while (true) {
                playbackState.value?.currentPlaybackPosition?.let { position ->
                    if (currentPlayerPosition.value != position) {
                        _currentPlayerPosition.postValue(position)
                        _currentSongDuration.postValue(MusicService.currentSongDuration)
                    }
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }
}