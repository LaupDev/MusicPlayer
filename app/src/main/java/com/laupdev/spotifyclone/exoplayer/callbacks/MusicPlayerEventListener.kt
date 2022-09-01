package com.laupdev.spotifyclone.exoplayer.callbacks

import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.laupdev.spotifyclone.exoplayer.MusicService

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.Listener {

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (!isPlaying) {
            musicService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, "An unknown error occurred", Toast.LENGTH_LONG).show()
    }
}