package com.example.haptix.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.haptix.data.HapticsRepository
import com.example.haptix.model.HapticsTimeline
import com.example.haptix.util.Logger
import kotlinx.coroutines.launch

/**
 * ViewModel for PlayerActivity
 */
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val hapticsRepository = HapticsRepository()

    private val _uiState = MutableLiveData<PlayerUiState>()
    val uiState: LiveData<PlayerUiState> = _uiState

    /**
     * Load video data
     */
    fun loadVideo(videoUrl: String, hapticsJson: String) {
        _uiState.value = PlayerUiState.Loading
        
        // Validate inputs
        if (videoUrl.isBlank()) {
            _uiState.value = PlayerUiState.Error("Video URL is empty")
            return
        }
        
        if (hapticsJson.isBlank()) {
            _uiState.value = PlayerUiState.Error("Haptics data is empty")
            return
        }
        
        // For now, we'll just set success state with the data
        // The actual video loading and haptics parsing will be done in the activity
        _uiState.value = PlayerUiState.Success(videoUrl, hapticsJson)
    }

    /**
     * Parse haptics timeline from JSON
     */
    suspend fun parseHapticsTimeline(hapticsJson: String): Result<HapticsTimeline> {
        return try {
            hapticsRepository.parseHapticsTimeline(hapticsJson)
                .onSuccess { timeline ->
                    if (hapticsRepository.validateHapticsTimeline(timeline)) {
                        Logger.i("Haptics timeline parsed and validated successfully")
                    } else {
                        Logger.w("Haptics timeline validation failed")
                    }
                }
        } catch (e: Exception) {
            Logger.e("Error parsing haptics timeline", e)
            Result.failure(e)
        }
    }

    /**
     * Set error state
     */
    fun setError(message: String) {
        _uiState.value = PlayerUiState.Error(message)
    }
}

/**
 * UI state for PlayerActivity
 */
sealed class PlayerUiState {
    object Loading : PlayerUiState()
    data class Success(val videoUrl: String, val hapticsJson: String) : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}
