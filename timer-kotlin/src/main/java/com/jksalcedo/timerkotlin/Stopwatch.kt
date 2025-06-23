package com.jksalcedo.timerkotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A versatile stopwatch that can track elapsed time, individual lap deltas, and total time splits.
 */
class Stopwatch(
    private val coroutineScope: CoroutineScope,
    private val tickInterval: Duration = 100.milliseconds,
    private val timeSource: TimeSource = TimeSource.Monotonic
) {

    private var stopwatchJob: Job? = null

    private val _elapsedTime = MutableStateFlow(Duration.ZERO)
    val elapsedTime: StateFlow<Duration> = _elapsedTime.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // --- Lap Time (Deltas) ---
    private val _lapTimes = MutableStateFlow<List<Duration>>(emptyList())
    /**
     * A list of individual lap durations (time between each lap press).
     */
    val lapTimes: StateFlow<List<Duration>> = _lapTimes.asStateFlow()

    // --- Split Time (Timestamps) ---
    private val _splitTimes = MutableStateFlow<List<Duration>>(emptyList())
    /**
     * A list of total elapsed times recorded at each split press.
     */
    val splitTimes: StateFlow<List<Duration>> = _splitTimes.asStateFlow()


    // Internal state for time tracking
    private var lastStartTimeMark: TimeMark? = null
    private var accumulatedTimeOnPause: Duration = Duration.ZERO

    // Internal state specifically for lap delta calculation
    private var lastLapTimeMark: TimeMark? = null
    private var accumulatedLapTimeOnPause: Duration = Duration.ZERO

    /**
     * Starts or resumes the stopwatch.
     */
    fun start() {
        if (_isRunning.value) return
        _isRunning.value = true

        // Set the start marks for the current run segment for BOTH total time and lap time.
        val now = timeSource.markNow()
        lastStartTimeMark = now
        lastLapTimeMark = now

        startTicking()
    }

    /**
     * Pauses the stopwatch.
     */
    fun pause() {
        if (!_isRunning.value) return
        _isRunning.value = false

        // Capture the elapsed time for BOTH total and lap segments.
        lastStartTimeMark?.let { accumulatedTimeOnPause += it.elapsedNow() }
        lastLapTimeMark?.let { accumulatedLapTimeOnPause += it.elapsedNow() }

        stopwatchJob?.cancel()
    }

    /**
     * Stops and resets the stopwatch completely.
     */
    fun stop() {
        stopwatchJob?.cancel()
        _isRunning.value = false
        _elapsedTime.value = Duration.ZERO
        _lapTimes.value = emptyList()
        _splitTimes.value = emptyList() // Also reset splits
        accumulatedTimeOnPause = Duration.ZERO
        accumulatedLapTimeOnPause = Duration.ZERO
        lastStartTimeMark = null
        lastLapTimeMark = null
    }

    /**
     * Records a LAP time. This calculates the time elapsed since the last lap was recorded.
     */
    fun lap() {
        if (!_isRunning.value) return

        // A lap's duration is the time accumulated before any pauses in this lap,
        // plus the time since the stopwatch was last resumed for this lap.
        val currentLapSegment = lastLapTimeMark?.elapsedNow() ?: Duration.ZERO
        val totalLapDuration = accumulatedLapTimeOnPause + currentLapSegment

        if (totalLapDuration > Duration.ZERO) {
            _lapTimes.value = _lapTimes.value + totalLapDuration
        }

        // Reset for the next lap. The next lap starts now.
        lastLapTimeMark = timeSource.markNow()
        accumulatedLapTimeOnPause = Duration.ZERO
    }

    /**
     * Records a SPLIT time. This records the total elapsed time at this moment.
     */
    fun split() {
        if (!_isRunning.value) return

        // Get the current total elapsed time by using the exact same logic as the main timer.
        val currentRunSegment = lastStartTimeMark?.elapsedNow() ?: Duration.ZERO
        val totalElapsedTimeAtSplit = accumulatedTimeOnPause + currentRunSegment

        _splitTimes.value = _splitTimes.value + totalElapsedTimeAtSplit
    }

    /**
     * Resets the stopwatch to zero. Continues running if it was running.
     */
    fun reset() {
        val wasRunning = _isRunning.value
        stop()
        if (wasRunning) {
            start()
        }
    }

    private fun startTicking() {
        stopwatchJob = coroutineScope.launch {
            while (isActive) {
                val currentRunSegment = lastStartTimeMark?.elapsedNow() ?: Duration.ZERO
                _elapsedTime.value = accumulatedTimeOnPause + currentRunSegment
                delay(tickInterval)
            }
        }
    }
}