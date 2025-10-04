package com.example.haptix.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.haptix.model.HapticEvent
import com.example.haptix.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Engine for playing haptic feedback effects
 */
class HapticsEngine(private val context: Context) {
    
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    private val capabilities = HapticsCapabilities(context)
    private val mapper = HapticsMapper(capabilities)
    
    /**
     * Check if haptics are supported on this device
     */
    fun isHapticsSupported(): Boolean {
        return capabilities.getCapabilityStatus().isHapticsSupported
    }
    
    /**
     * Play a haptic event
     */
    suspend fun playHapticEvent(event: HapticEvent): Boolean = withContext(Dispatchers.Main) {
        try {
            if (!isHapticsSupported()) {
                Logger.w("Haptics not supported on this device")
                return@withContext false
            }
            
            val effect = mapper.mapEventToEffect(event)
            if (effect != null) {
                vibrator.vibrate(effect)
                Logger.d("Played haptic event: ${event.type} at ${event.timestamp}ms")
                true
            } else {
                Logger.w("Failed to create vibration effect for event")
                false
            }
        } catch (e: SecurityException) {
            Logger.e("Security exception playing haptic event - possibly in Do Not Disturb mode", e)
            false
        } catch (e: Exception) {
            Logger.e("Error playing haptic event", e)
            false
        }
    }
    
    /**
     * Play a primitive haptic effect
     */
    suspend fun playPrimitive(primitive: Int, strength: Float = 1f): Boolean = withContext(Dispatchers.Main) {
        try {
            if (!isHapticsSupported()) {
                Logger.w("Haptics not supported on this device")
                return@withContext false
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && capabilities.supportsPrimitives()) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(primitive, strength)
                    .compose()
                vibrator.vibrate(effect)
            } else {
                // Fallback to simple vibration
                val duration = when (primitive) {
                    VibrationEffect.Composition.PRIMITIVE_CLICK -> 50L
                    VibrationEffect.Composition.PRIMITIVE_HEAVY_CLICK -> 100L
                    VibrationEffect.Composition.PRIMITIVE_TICK -> 20L
                    VibrationEffect.Composition.PRIMITIVE_THUD -> 80L
                    else -> 50L
                }
                vibrator.vibrate(duration)
            }
            
            Logger.d("Played primitive haptic: $primitive")
            true
        } catch (e: SecurityException) {
            Logger.e("Security exception playing primitive haptic", e)
            false
        } catch (e: Exception) {
            Logger.e("Error playing primitive haptic", e)
            false
        }
    }
    
    /**
     * Play a custom waveform
     */
    suspend fun playWaveform(timings: LongArray, amplitudes: IntArray): Boolean = withContext(Dispatchers.Main) {
        try {
            if (!isHapticsSupported()) {
                Logger.w("Haptics not supported on this device")
                return@withContext false
            }
            
            if (capabilities.supportsWaveform()) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
                Logger.d("Played custom waveform with ${timings.size} segments")
                true
            } else {
                // Fallback to simple vibration
                val totalDuration = timings.sum()
                vibrator.vibrate(totalDuration)
                Logger.d("Played fallback vibration for ${totalDuration}ms")
                true
            }
        } catch (e: SecurityException) {
            Logger.e("Security exception playing waveform", e)
            false
        } catch (e: Exception) {
            Logger.e("Error playing waveform", e)
            false
        }
    }
    
    /**
     * Cancel any ongoing vibrations
     */
    fun cancel() {
        try {
            vibrator.cancel()
            Logger.d("Cancelled ongoing vibrations")
        } catch (e: Exception) {
            Logger.e("Error cancelling vibrations", e)
        }
    }
    
    /**
     * Get device capabilities
     */
    fun getCapabilities(): HapticsCapabilityStatus {
        return capabilities.getCapabilityStatus()
    }
}