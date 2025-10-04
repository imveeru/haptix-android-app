package com.example.haptix.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.haptix.util.Logger

/**
 * Checks device capabilities for haptic feedback
 */
class HapticsCapabilities(private val context: Context) {
    
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Check if the device has a vibrator
     */
    fun hasVibrator(): Boolean {
        return try {
            vibrator.hasVibrator()
        } catch (e: Exception) {
            Logger.e("Error checking vibrator capability", e)
            false
        }
    }
    
    /**
     * Check if the device supports vibration primitives (API 31+)
     */
    fun supportsPrimitives(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                vibrator.areAllPrimitivesSupported(
                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                    VibrationEffect.Composition.PRIMITIVE_TICK,
                    VibrationEffect.Composition.PRIMITIVE_THUD,
                    VibrationEffect.Composition.PRIMITIVE_HEAVY_CLICK
                )
            } catch (e: Exception) {
                Logger.e("Error checking primitives support", e)
                false
            }
        } else {
            false
        }
    }
    
    /**
     * Check if a specific primitive is supported
     */
    fun isPrimitiveSupported(primitive: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                vibrator.isPrimitiveSupported(primitive)
            } catch (e: Exception) {
                Logger.e("Error checking primitive support: $primitive", e)
                false
            }
        } else {
            false
        }
    }
    
    /**
     * Check if the device supports custom waveforms
     */
    fun supportsWaveform(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
    
    /**
     * Get overall haptics capability status
     */
    fun getCapabilityStatus(): HapticsCapabilityStatus {
        return HapticsCapabilityStatus(
            hasVibrator = hasVibrator(),
            supportsPrimitives = supportsPrimitives(),
            supportsWaveform = supportsWaveform(),
            isDoNotDisturbBlocking = false // This would need to be checked separately
        )
    }
}

/**
 * Data class representing device haptic capabilities
 */
data class HapticsCapabilityStatus(
    val hasVibrator: Boolean,
    val supportsPrimitives: Boolean,
    val supportsWaveform: Boolean,
    val isDoNotDisturbBlocking: Boolean
) {
    val isHapticsSupported: Boolean
        get() = hasVibrator && (supportsPrimitives || supportsWaveform)
}