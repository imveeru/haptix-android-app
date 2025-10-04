package com.example.haptix.haptics

import android.os.Build
import android.os.VibrationEffect
import com.example.haptix.model.HapticEvent
import com.example.haptix.util.Logger

/**
 * Maps haptic events to Android VibrationEffect objects
 */
class HapticsMapper(private val capabilities: HapticsCapabilities) {
    
    /**
     * Map a haptic event to a VibrationEffect
     */
    fun mapEventToEffect(event: HapticEvent): VibrationEffect? {
        return try {
            when (event.type) {
                "primitive" -> mapPrimitiveEvent(event)
                "waveform" -> mapWaveformEvent(event)
                else -> {
                    Logger.w("Unknown haptic event type: ${event.type}")
                    null
                }
            }
        } catch (e: Exception) {
            Logger.e("Error mapping haptic event to effect", e)
            null
        }
    }
    
    /**
     * Map primitive event to VibrationEffect
     */
    private fun mapPrimitiveEvent(event: HapticEvent): VibrationEffect? {
        if (!capabilities.supportsPrimitives() || event.primitive == null) {
            // Fallback to waveform approximation
            return mapPrimitiveToWaveform(event.primitive, event.strength)
        }
        
        val primitive = mapStringToPrimitive(event.primitive)
        if (!capabilities.isPrimitiveSupported(primitive)) {
            // Fallback to waveform approximation
            return mapPrimitiveToWaveform(event.primitive, event.strength)
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val composition = VibrationEffect.startComposition()
                    .addPrimitive(primitive, event.strength?.let { it / 255f } ?: 1f)
                
                // Add repeat if specified
                if (event.repeat > 1) {
                    composition.repeat()
                }
                
                composition.compose()
            } catch (e: Exception) {
                Logger.e("Error creating primitive composition", e)
                mapPrimitiveToWaveform(event.primitive, event.strength)
            }
        } else {
            // Fallback for older APIs
            mapPrimitiveToWaveform(event.primitive, event.strength)
        }
    }
    
    /**
     * Map waveform event to VibrationEffect
     */
    private fun mapWaveformEvent(event: HapticEvent): VibrationEffect? {
        if (!capabilities.supportsWaveform() || 
            event.timings == null || 
            event.amplitudes == null) {
            return null
        }
        
        return try {
            VibrationEffect.createWaveform(event.timings, event.amplitudes, -1)
        } catch (e: Exception) {
            Logger.e("Error creating waveform effect", e)
            null
        }
    }
    
    /**
     * Map primitive string to VibrationEffect.Composition primitive constant
     */
    private fun mapStringToPrimitive(primitive: String): Int {
        return when (primitive.uppercase()) {
            "CLICK" -> VibrationEffect.Composition.PRIMITIVE_CLICK
            "TICK" -> VibrationEffect.Composition.PRIMITIVE_TICK
            "THUD" -> VibrationEffect.Composition.PRIMITIVE_THUD
            "HEAVY_CLICK" -> VibrationEffect.Composition.PRIMITIVE_HEAVY_CLICK
            "SPIN" -> VibrationEffect.Composition.PRIMITIVE_SPIN
            "QUICK_RISE" -> VibrationEffect.Composition.PRIMITIVE_QUICK_RISE
            "SLOW_RISE" -> VibrationEffect.Composition.PRIMITIVE_SLOW_RISE
            "QUICK_FALL" -> VibrationEffect.Composition.PRIMITIVE_QUICK_FALL
            else -> {
                Logger.w("Unknown primitive type: $primitive, defaulting to CLICK")
                VibrationEffect.Composition.PRIMITIVE_CLICK
            }
        }
    }
    
    /**
     * Fallback: Map primitive to waveform approximation
     */
    private fun mapPrimitiveToWaveform(primitive: String?, strength: Int?): VibrationEffect? {
        if (!capabilities.supportsWaveform()) {
            return null
        }
        
        val amplitude = (strength ?: 255).coerceIn(0, 255)
        
        val (timings, amplitudes) = when (primitive?.uppercase()) {
            "CLICK" -> {
                longArrayOf(0, 50) to intArrayOf(0, amplitude)
            }
            "HEAVY_CLICK" -> {
                longArrayOf(0, 100, 20, 50) to intArrayOf(0, amplitude, 0, amplitude / 2)
            }
            "TICK" -> {
                longArrayOf(0, 20) to intArrayOf(0, amplitude)
            }
            "THUD" -> {
                longArrayOf(0, 80) to intArrayOf(0, amplitude)
            }
            "SPIN" -> {
                longArrayOf(0, 50, 30, 50, 30, 50) to intArrayOf(0, amplitude, 0, amplitude, 0, amplitude)
            }
            "QUICK_RISE" -> {
                longArrayOf(0, 100) to intArrayOf(0, amplitude)
            }
            "SLOW_RISE" -> {
                longArrayOf(0, 200) to intArrayOf(0, amplitude)
            }
            "QUICK_FALL" -> {
                longArrayOf(0, 50, 50) to intArrayOf(amplitude, amplitude, 0)
            }
            else -> {
                // Default to a simple click
                longArrayOf(0, 50) to intArrayOf(0, amplitude)
            }
        }
        
        return try {
            VibrationEffect.createWaveform(timings, amplitudes, -1)
        } catch (e: Exception) {
            Logger.e("Error creating fallback waveform", e)
            null
        }
    }
}