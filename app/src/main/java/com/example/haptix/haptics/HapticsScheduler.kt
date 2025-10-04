package com.example.haptix.haptics

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.example.haptix.model.HapticEvent
import com.example.haptix.model.HapticsTimeline
import com.example.haptix.util.LogUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Scheduler for synchronizing haptic events with ExoPlayer timeline
 */
class HapticsScheduler(
    private val player: ExoPlayer,
    private val timeline: HapticsTimeline,
    private val hapticsEngine: HapticsEngine,
    private val isEnabled: Flow<Boolean>
) : Player.Listener {
    
    private var schedulingJob: Job? = null
    private var nextEventIndex = 0
    private val toleranceMs = 15L // Tolerance window for event firing
    private val schedulingDelayMs = 16L // Check every ~60fps
    
    private val sortedEvents = timeline.getSortedEvents()
    private val firedEvents = mutableSetOf<HapticEvent>()
    
    init {
        player.addListener(this)
    }
    
    /**
     * Start haptic scheduling
     */
    fun startScheduling() {
        if (schedulingJob?.isActive == true) {
            LogUtils.d("Scheduling already active")
            return
        }
        
        schedulingJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                try {
                    if (player.isPlaying) {
                        val currentPosition = player.currentPosition
                        val hapticsEnabled = isEnabled.first()
                        
                        if (hapticsEnabled) {
                            processEventsAtPosition(currentPosition)
                        }
                    }
                    
                    delay(schedulingDelayMs)
                } catch (e: Exception) {
                    LogUtils.e("Error in haptic scheduling loop", e)
                    delay(schedulingDelayMs)
                }
            }
        }
        
        LogUtils.d("Started haptic scheduling")
    }
    
    /**
     * Stop haptic scheduling
     */
    fun stopScheduling() {
        schedulingJob?.cancel()
        schedulingJob = null
        LogUtils.d("Stopped haptic scheduling")
    }
    
    /**
     * Process events at current position
     */
    private suspend fun processEventsAtPosition(currentPosition: Long) {
        val eventsToFire = mutableListOf<HapticEvent>()
        
        // Find events that should fire within tolerance window
        for (event in sortedEvents) {
            if (event.timestamp <= currentPosition + toleranceMs && 
                event.timestamp >= currentPosition - toleranceMs &&
                !firedEvents.contains(event)) {
                eventsToFire.add(event)
            }
        }
        
        // Fire events and mark as fired
        for (event in eventsToFire) {
            val success = hapticsEngine.playHapticEvent(event)
            if (success) {
                firedEvents.add(event)
                LogUtils.d("Fired haptic event: ${event.type} at ${event.timestamp}ms (actual: ${currentPosition}ms)")
            }
        }
    }
    
    /**
     * Reset scheduling state for seeking
     */
    private fun resetSchedulingState(newPosition: Long) {
        firedEvents.clear()
        
        // Mark events before new position as fired (for forward seeks)
        if (newPosition > 0) {
            for (event in sortedEvents) {
                if (event.timestamp < newPosition - toleranceMs) {
                    firedEvents.add(event)
                }
            }
        }
        
        LogUtils.d("Reset haptic scheduling state for position: ${newPosition}ms")
    }
    
    // Player.Listener implementation
    
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Logger.d("Player isPlaying changed: $isPlaying")
        if (isPlaying) {
            startScheduling()
        } else {
            // Don't stop scheduling on pause - keep it running for potential resume
        }
    }
    
    override fun onPlaybackStateChanged(playbackState: Int) {
        Logger.d("Player state changed: $playbackState")
        when (playbackState) {
            Player.STATE_IDLE, Player.STATE_ENDED -> {
                stopScheduling()
            }
            Player.STATE_BUFFERING -> {
                // Continue scheduling during buffering
            }
            Player.STATE_READY -> {
                // Player is ready, ensure scheduling is active if playing
                if (player.isPlaying) {
                    startScheduling()
                }
            }
        }
    }
    
    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        Logger.d("Player position discontinuity: ${oldPosition.positionMs} -> ${newPosition.positionMs}")
        resetSchedulingState(newPosition.positionMs)
    }
    
    override fun onPlayerError(error: PlaybackException) {
        Logger.e("Player error occurred", error)
        stopScheduling()
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopScheduling()
        player.removeListener(this)
        Logger.d("HapticsScheduler cleaned up")
    }
}