package com.example.haptix.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a single haptic event in the timeline
 */
data class HapticEvent(
    @SerializedName("t")
    val timestamp: Long, // milliseconds from video start
    val type: String, // "primitive" or "waveform"
    val primitive: String? = null, // CLICK, TICK, THUD, HEAVY_CLICK, etc.
    val timings: LongArray? = null, // for waveform type
    val amplitudes: IntArray? = null, // for waveform type
    val repeat: Int = 1, // number of times to repeat
    val strength: Int? = null, // 0-255 strength override
    val duration: Long? = null // duration override in ms
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HapticEvent

        if (timestamp != other.timestamp) return false
        if (type != other.type) return false
        if (primitive != other.primitive) return false
        if (timings != null) {
            if (other.timings == null) return false
            if (!timings.contentEquals(other.timings)) return false
        } else if (other.timings != null) return false
        if (amplitudes != null) {
            if (other.amplitudes == null) return false
            if (!amplitudes.contentEquals(other.amplitudes)) return false
        } else if (other.amplitudes != null) return false
        if (repeat != other.repeat) return false
        if (strength != other.strength) return false
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (primitive?.hashCode() ?: 0)
        result = 31 * result + (timings?.contentHashCode() ?: 0)
        result = 31 * result + (amplitudes?.contentHashCode() ?: 0)
        result = 31 * result + repeat
        result = 31 * result + (strength ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        return result
    }
}