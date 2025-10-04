package com.example.haptix.data

import com.example.haptix.model.HapticsTimeline
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Repository for managing haptics data
 */
class HapticsRepository {
    
    private val gson = Gson()
    
    /**
     * Parse haptics timeline from JSON string
     */
    fun parseHapticsTimeline(jsonString: String): Result<HapticsTimeline> {
        return try {
            val timeline = gson.fromJson(jsonString, HapticsTimeline::class.java)
            Result.success(timeline)
        } catch (e: JsonSyntaxException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convert haptics timeline to JSON string
     */
    fun hapticsTimelineToJson(timeline: HapticsTimeline): Result<String> {
        return try {
            val json = gson.toJson(timeline)
            Result.success(json)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}