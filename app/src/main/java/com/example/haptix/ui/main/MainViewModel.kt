package com.example.haptix.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.haptix.data.VideoRepository
import com.example.haptix.model.VideoItem
import com.example.haptix.util.Logger
import kotlinx.coroutines.launch

/**
 * ViewModel for MainActivity
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val videoRepository = VideoRepository(application)

    private val _uiState = MutableLiveData<MainUiState>()
    val uiState: LiveData<MainUiState> = _uiState

    /**
     * Load videos from repository
     */
    fun loadVideos() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            
            videoRepository.loadVideos()
                .onSuccess { videos ->
                    _uiState.value = MainUiState.Success(videos)
                }
                .onFailure { exception ->
                    _uiState.value = MainUiState.Error(
                        exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }
}

/**
 * UI state for MainActivity
 */
sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val videos: List<VideoItem>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}
