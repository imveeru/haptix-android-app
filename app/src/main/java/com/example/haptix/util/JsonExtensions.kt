package com.example.haptix.util

import com.example.haptix.model.HapticsTimeline
import com.google.gson.Gson

/**
 * JSON utility extensions
 */
object JsonExtensions {
    private val gson = Gson()
    
    /**
     * Convert HapticsTimeline to JSON string
     */
    fun toJson(timeline: HapticsTimeline): String {
        return gson.toJson(timeline)
    }
    
    /**
     * Parse JSON string to HapticsTimeline
     */
    fun fromJson(json: String): HapticsTimeline? {
        return try {
            gson.fromJson(json, HapticsTimeline::class.java)
        } catch (e: Exception) {
            null
        }
    }
}