package com.example.haptix.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.haptix.databinding.ActivityMainBinding
import com.example.haptix.ui.player.PlayerActivity
import com.example.haptix.util.Logger
import com.example.haptix.util.toJson

/**
 * Main activity displaying a grid of video cards
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var videoAdapter: VideoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        
        // Load videos
        viewModel.loadVideos()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Haptix"
    }

    private fun setupRecyclerView() {
        videoAdapter = VideoListAdapter { video ->
            openPlayerActivity(video)
        }
        
        binding.recyclerViewVideos.apply {
            adapter = videoAdapter
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is MainUiState.Loading -> {
                    showLoading(true)
                    showError(false)
                }
                is MainUiState.Success -> {
                    showLoading(false)
                    showError(false)
                    videoAdapter.submitList(state.videos)
                    Logger.i("Loaded ${state.videos.size} videos")
                }
                is MainUiState.Error -> {
                    showLoading(false)
                    showError(true, state.message)
                    Logger.e("Error loading videos: ${state.message}")
                }
            }
        }
    }

    private fun openPlayerActivity(video: com.example.haptix.model.VideoItem) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_VIDEO_TITLE, video.title)
            putExtra(PlayerActivity.EXTRA_VIDEO_URL, video.videoUrl)
            putExtra(PlayerActivity.EXTRA_HAPTICS_JSON, video.haptics.toJson())
        }
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(show: Boolean, message: String? = null) {
        binding.textViewError.visibility = if (show) View.VISIBLE else View.GONE
        message?.let { binding.textViewError.text = it }
    }
}