package com.jksalcedo.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TimerViewModel : ViewModel() {

    // Countdown
    private val countdownTimer = CountdownTimer(viewModelScope)

    val countdownText = countdownTimer.remainingTime
        .map { duration -> formatDuration(duration) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            formatDuration(Duration.ZERO)
        )

    val isCountdownRunning = countdownTimer.isRunning

    fun startCountdown(duration: Duration) {
        countdownTimer.start(duration)
    }

    fun pauseCountdown() {
        countdownTimer.pause()
    }

    fun resumeCountdown() {
        countdownTimer.resume()
    }

    fun stopCountdown() {
        countdownTimer.stop()
    }

    fun restartCountdown() {
        countdownTimer.restart()
    }

    // Stopwatch
    private val stopwatch = Stopwatch(viewModelScope)

    val stopwatchText = stopwatch.elapsedTime
        .map { duration -> formatDuration(duration, includeMilliseconds = true) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            formatDuration(Duration.ZERO, includeMilliseconds = true)
        )

    val isStopwatchRunning = stopwatch.isRunning
    val lapTimes = stopwatch.lapTimes

    val splitTimes = stopwatch.splitTimes

    fun startStopwatch() {
        stopwatch.start()
    }

    fun pauseStopwatch() {
        stopwatch.pause()
    }

    fun stopStopwatch() {
        stopwatch.stop()
    }

    fun lapStopwatch() {
        stopwatch.lap()
    }

    fun recordSplitTime() {
        stopwatch.split()
    }

    fun resetStopwatch() {
        stopwatch.reset()
    }

    // --- Utility formatting function ---
    private fun formatDuration(duration: Duration, includeMilliseconds: Boolean = false): String {
        val totalSeconds = duration.inWholeSeconds
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val milliseconds = (duration - totalSeconds.seconds).inWholeMilliseconds

        return if (includeMilliseconds) {
            String.format(Locale.getDefault(), "%02d:%02d.%03d", minutes, seconds, milliseconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}