package com.example.haptix.ui.player

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.haptix.R
import com.example.haptix.databinding.ActivityPlayerBinding
import com.example.haptix.haptics.HapticsEngine
import com.example.haptix.haptics.HapticsScheduler
import com.example.haptix.util.Logger
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Activity for playing videos with synchronized haptic feedback
 */
class PlayerActivity : AppCompatActivity(), Player.Listener {

    companion object {
        const val EXTRA_VIDEO_TITLE = "video_title"
        const val EXTRA_VIDEO_URL = "video_url"
        const val EXTRA_HAPTICS_JSON = "haptics_json"
        
        private const val PREFS_NAME = "haptix_prefs"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    }

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModels()
    
    private var player: ExoPlayer? = null
    private var hapticsEngine: HapticsEngine? = null
    private var hapticsScheduler: HapticsScheduler? = null
    
    private val _hapticsEnabled = MutableStateFlow(true)
    private val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled
    
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on while playing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        setupToolbar()
        setupHapticsEngine()
        setupObservers()
        setupHapticsToggle()
        
        // Load video data from intent
        loadVideoFromIntent()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupHapticsEngine() {
        hapticsEngine = HapticsEngine(this)
        
        // Check if haptics are supported
        if (!hapticsEngine?.isHapticsSupported()!!) {
            binding.switchHaptics.isEnabled = false
            binding.switchHaptics.text = getString(R.string.haptics_not_supported)
            Toast.makeText(this, R.string.haptics_not_supported, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is PlayerUiState.Loading -> {
                    showLoading(true)
                    showError(false)
                }
                is PlayerUiState.Success -> {
                    showLoading(false)
                    showError(false)
                    setupPlayer(state.videoUrl)
                    setupHapticsScheduler(state.hapticsJson)
                }
                is PlayerUiState.Error -> {
                    showLoading(false)
                    showError(true, state.message)
                    Logger.e("Error loading video: ${state.message}")
                }
            }
        }
    }

    private fun setupHapticsToggle() {
        // Restore haptics preference
        val hapticsEnabled = sharedPreferences.getBoolean(KEY_HAPTICS_ENABLED, true)
        _hapticsEnabled.value = hapticsEnabled
        binding.switchHaptics.isChecked = hapticsEnabled
        
        // Update switch text
        updateHapticsToggleText()
        
        binding.switchHaptics.setOnCheckedChangeListener { _, isChecked ->
            _hapticsEnabled.value = isChecked
            updateHapticsToggleText()
            
            // Save preference
            sharedPreferences.edit()
                .putBoolean(KEY_HAPTICS_ENABLED, isChecked)
                .apply()
                
            Logger.d("Haptics toggle changed: $isChecked")
        }
    }

    private fun updateHapticsToggleText() {
        binding.switchHaptics.text = if (_hapticsEnabled.value) {
            getString(R.string.haptics_on)
        } else {
            getString(R.string.haptics_off)
        }
    }

    private fun loadVideoFromIntent() {
        val title = intent.getStringExtra(EXTRA_VIDEO_TITLE) ?: ""
        val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL) ?: ""
        val hapticsJson = intent.getStringExtra(EXTRA_HAPTICS_JSON) ?: ""
        
        if (title.isBlank() || videoUrl.isBlank() || hapticsJson.isBlank()) {
            Logger.e("Missing required intent extras")
            viewModel.setError("Missing video data")
            return
        }
        
        binding.textViewVideoTitle.text = title
        viewModel.loadVideo(videoUrl, hapticsJson)
    }

    private fun setupPlayer(videoUrl: String) {
        player = ExoPlayer.Builder(this).build().apply {
            addListener(this@PlayerActivity)
            
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
        }
        
        binding.playerView.player = player
        Logger.d("Player setup completed for URL: $videoUrl")
    }

    private fun setupHapticsScheduler(hapticsJson: String) {
        lifecycleScope.launch {
            viewModel.parseHapticsTimeline(hapticsJson)
                .onSuccess { timeline ->
                    player?.let { exoPlayer ->
                        hapticsEngine?.let { engine ->
                            hapticsScheduler = HapticsScheduler(
                                exoPlayer,
                                timeline,
                                engine,
                                hapticsEnabled
                            )
                            Logger.d("Haptics scheduler setup completed with ${timeline.events.size} events")
                        }
                    }
                }
                .onFailure { exception ->
                    Logger.e("Failed to parse haptics timeline", exception)
                    Toast.makeText(this@PlayerActivity, "Failed to load haptics data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        player?.let {
            if (it.playbackState == Player.STATE_IDLE) {
                it.prepare()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Cleanup resources
        hapticsScheduler?.cleanup()
        player?.release()
        player = null
        
        // Remove screen on flag
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        Logger.d("PlayerActivity destroyed and resources cleaned up")
    }

    // Player.Listener implementation
    
    override fun onPlaybackStateChanged(playbackState: Int) {
        Logger.d("Playback state changed: $playbackState")
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            Player.STATE_READY, Player.STATE_ENDED -> {
                binding.progressBar.visibility = View.GONE
            }
            Player.STATE_IDLE -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Logger.e("Player error occurred", error)
        showError(true, "Playback error: ${error.message}")
        binding.progressBar.visibility = View.GONE
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(show: Boolean, message: String? = null) {
        binding.textViewError.visibility = if (show) View.VISIBLE else View.GONE
        message?.let { binding.textViewError.text = it }
    }
}