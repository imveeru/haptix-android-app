package com.example.haptix.data

import android.content.Context
import com.example.haptix.model.VideoItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository for managing video data
 */
class VideoRepository(private val context: Context) {
    
    private val gson = Gson()
    
    /**
     * Load videos from assets/videos.json
     */
    suspend fun loadVideos(): Result<List<VideoItem>> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open("videos.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<VideoItem>>() {}.type
            val videos = gson.fromJson<List<VideoItem>>(json, listType)
            Result.success(videos)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a specific video by title
     */
    suspend fun getVideoByTitle(title: String): Result<VideoItem?> = withContext(Dispatchers.IO) {
        try {
            val videosResult = loadVideos()
            videosResult.fold(
                onSuccess = { videos ->
                    val video = videos.find { it.title == title }
                    Result.success(video)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}