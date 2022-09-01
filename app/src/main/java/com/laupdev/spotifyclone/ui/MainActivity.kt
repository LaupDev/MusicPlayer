package com.laupdev.spotifyclone.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.laupdev.spotifyclone.R
import com.laupdev.spotifyclone.adapters.SwipeSongAdapter
import com.laupdev.spotifyclone.data.entities.Song
import com.laupdev.spotifyclone.databinding.ActivityMainBinding
import com.laupdev.spotifyclone.exoplayer.isPlaying
import com.laupdev.spotifyclone.exoplayer.toSong
import com.laupdev.spotifyclone.other.Status.*
import com.laupdev.spotifyclone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var currentlyPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeToObservables()

        binding.vpSong.adapter = swipeSongAdapter

        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                } else {
                    currentlyPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        binding.ivPlayPause.setOnClickListener {
            currentlyPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }

        swipeSongAdapter.setItemClickListener {
            navController.navigate(R.id.action_global_toSongFragment)
        }
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        with(swipeSongAdapter.songs.indexOf(song)) {
            if (this != -1) {
                binding.vpSong.currentItem = this
                currentlyPlayingSong = song
            }
        }
    }

    private fun hideBottomBar() {
        binding.ivCurSongImage.isVisible = false
        binding.vpSong.isVisible = false
        binding.ivPlayPause.isVisible = false
    }

    private fun showBottomBar() {
        binding.ivCurSongImage.isVisible = true
        binding.vpSong.isVisible = true
        binding.ivPlayPause.isVisible = true
    }

    private fun subscribeToObservables() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                glide.load((currentlyPlayingSong ?: songs[0]).imageUrl)
                                    .into(binding.ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(currentlyPlayingSong ?: return@observe)
                        }
                    }
                    ERROR -> Unit
                    LOADING -> Unit
                }
            }
        }

        mainViewModel.currentlyPlayingSong.observe(this) {
            it?.let { mediaMetadata ->
                currentlyPlayingSong = mediaMetadata.toSong()
                glide.load(currentlyPlayingSong?.imageUrl).into(binding.ivCurSongImage)
                switchViewPagerToCurrentSong(currentlyPlayingSong ?: return@observe)
            }
        }

        mainViewModel.playbackState.observe(this) {
            playbackState = it
            binding.ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause
                else R.drawable.ic_play
            )
        }

        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> {
                        Snackbar.make(
                            binding.rootLayout,
                            result.message ?: "An unknown error occurred",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> {
                        Snackbar.make(
                            binding.rootLayout,
                            result.message ?: "An unknown error occurred",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}