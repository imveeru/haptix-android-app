package com.example.haptix.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the complete haptics timeline for a video
 */
data class HapticsTimeline(
    val version: Int,
    val events: List<HapticEvent>
) {
    /**
     * Get events sorted by timestamp for efficient processing
     */
    fun getSortedEvents(): List<HapticEvent> {
        return events.sortedBy { it.timestamp }
    }
    
    /**
     * Get events within a time range (inclusive)
     */
    fun getEventsInRange(startMs: Long, endMs: Long): List<HapticEvent> {
        return events.filter { it.timestamp in startMs..endMs }
    }
    
    /**
     * Get the next event after the given timestamp
     */
    fun getNextEvent(afterMs: Long): HapticEvent? {
        return events.filter { it.timestamp > afterMs }
            .minByOrNull { it.timestamp }
    }
}