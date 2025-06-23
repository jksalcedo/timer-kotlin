package com.jksalcedo.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A versatile countdown timer that can be started, paused, resumed, and stopped.
 * Time updates are provided via a [StateFlow].
 *
 * @param coroutineScope The [CoroutineScope] in which the timer's internal coroutine will run.
 * This scope should typically be tied to a lifecycle (e.g., ViewModel's scope)
 * to ensure the timer stops when no longer needed.
 * @param tickInterval The interval at which the timer's remaining time is updated. Defaults to 1 second.
 */
class CountdownTimer(
    private val coroutineScope: CoroutineScope,
    private val tickInterval: Duration = 1.seconds
) {

    private val _remainingTime = MutableStateFlow(Duration.ZERO)
    /**
     * A [StateFlow] representing the time remaining on the countdown timer.
     * Emits [Duration.ZERO] when the timer is not active or has finished.
     */
    val remainingTime: StateFlow<Duration> = _remainingTime.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    /**
     * A [StateFlow] indicating whether the countdown timer is currently running.
     */
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var timerJob: Job? = null
    private var initialDuration: Duration = Duration.ZERO

    /**
     * Starts the countdown timer from the specified [duration].
     * If the timer is already running, it will be restarted with the new duration.
     *
     * @param duration The total duration for the countdown. Must be positive.
     */
    fun start(duration: Duration) {
        require(duration > Duration.ZERO) { "Duration must be positive" }
        stop() // Stop any existing timer first
        initialDuration = duration
        _remainingTime.value = duration
        startTicking()
    }

    /**
     * Pauses the countdown timer. The remaining time is preserved.
     * Has no effect if the timer is already paused or not running.
     */
    fun pause() {
        if (timerJob?.isActive == true) {
            timerJob?.cancel()
            _isRunning.value = false
        }
    }

    /**
     * Resumes the countdown timer from its current [remainingTime].
     * Has no effect if the timer is already running or if there's no time remaining to resume.
     */
    fun resume() {
        if (!isRunning.value && _remainingTime.value > Duration.ZERO) {
            startTicking()
        }
    }

    /**
     * Stops the countdown timer and resets the [remainingTime] to [Duration.ZERO].
     * Has no effect if the timer is not running.
     */
    fun stop() {
        timerJob?.cancel()
        timerJob = null
        _remainingTime.value = Duration.ZERO
        _isRunning.value = false
        initialDuration = Duration.ZERO
    }

    /**
     * Restarts the timer from its initial duration if it was set previously.
     * If no initial duration was set (i.e., `start` was never called with a positive duration),
     * this method has no effect.
     */
    fun restart() {
        if (initialDuration > Duration.ZERO) {
            start(initialDuration)
        } else {
            // Optionally log a warning or throw an error if no initial duration set
            println("Cannot restart: Timer has not been started with a duration yet.")
        }
    }

    private fun startTicking() {
        _isRunning.value = true
        timerJob = coroutineScope.launch {
            while (isActive && _remainingTime.value > Duration.ZERO) {
                delay(tickInterval.inWholeMilliseconds) // Delay by the tick interval
                _remainingTime.value -= tickInterval
                if (_remainingTime.value < Duration.ZERO) { // Prevent negative time due to delay
                    _remainingTime.value = Duration.ZERO
                }
            }
            if (_remainingTime.value == Duration.ZERO) {
                _isRunning.value = false // Timer finished
            }
        }
    }
}