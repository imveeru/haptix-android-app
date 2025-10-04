package com.example.haptix.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a video item with its associated haptics data
 */
data class VideoItem(
    val title: String,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String,
    @SerializedName("videoUrl")
    val videoUrl: String,
    val haptics: HapticsTimeline
)