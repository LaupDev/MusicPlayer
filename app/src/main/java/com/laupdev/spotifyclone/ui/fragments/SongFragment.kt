package com.laupdev.spotifyclone.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.RequestManager
import com.laupdev.spotifyclone.R
import com.laupdev.spotifyclone.data.entities.Song
import com.laupdev.spotifyclone.databinding.FragmentSongBinding
import com.laupdev.spotifyclone.exoplayer.isPlaying
import com.laupdev.spotifyclone.exoplayer.toSong
import com.laupdev.spotifyclone.other.Status
import com.laupdev.spotifyclone.other.Status.SUCCESS
import com.laupdev.spotifyclone.ui.viewmodels.MainViewModel
import com.laupdev.spotifyclone.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager

    private var _binding: FragmentSongBinding? = null
    private val binding
        get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()
    private val songViewModel: SongViewModel by viewModels()

    private var currentlyPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservables()

        binding.ivPlayPauseDetail.setOnClickListener {
            currentlyPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setCurrentPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })

        binding.ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        binding.ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTitleAndImage(song: Song) {
        binding.tvSongName.text = "${song.title} - ${song.subTitle}"
        glide.load(song.imageUrl).into(binding.ivSongImage)
    }

    private fun subscribeToObservables() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.data?.let { songs ->
                            if (currentlyPlayingSong == null && songs.isNotEmpty()) {
                                currentlyPlayingSong = songs[0]
                                updateTitleAndImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
        mainViewModel.currentlyPlayingSong.observe(viewLifecycleOwner) {
            it?.let {
                currentlyPlayingSong = it.toSong()
                updateTitleAndImage(currentlyPlayingSong!!)
            }
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            binding.ivPlayPauseDetail.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
            binding.seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.currentPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekbar) {
                binding.seekBar.progress = it.toInt()
                setCurrentPlayerTimeToTextView(it)
            }
        }
        songViewModel.currentSongDuration.observe(viewLifecycleOwner) {
            binding.seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            binding.tvSongDuration.text = dateFormat.format(it)
        }
    }

    private fun setCurrentPlayerTimeToTextView(milliseconds: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.tvCurTime.text = dateFormat.format(milliseconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}