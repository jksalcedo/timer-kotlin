package com.jksalcedo.timerkotlin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {

    // Initialize the ViewModel using the viewModels delegate
    private val timerViewModel: TimerViewModel by viewModels()

    // UI elements
    private lateinit var countdownTextView: TextView
    private lateinit var startCountdownButton: Button
    private lateinit var pauseCountdownButton: Button
    private lateinit var resumeCountdownButton: Button
    private lateinit var stopCountdownButton: Button
    private lateinit var restartCountdownButton: Button

    private lateinit var stopwatchTextView: TextView
    private lateinit var startStopwatchButton: Button
    private lateinit var pauseStopwatchButton: Button
    private lateinit var lapStopwatchButton: Button
    private lateinit var stopStopwatchButton: Button
    private lateinit var resetStopwatchButton: Button
    private lateinit var lapTimesTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements by finding their IDs
        countdownTextView = findViewById(R.id.countdownTextView)
        startCountdownButton = findViewById(R.id.startCountdownButton)
        pauseCountdownButton = findViewById(R.id.pauseCountdownButton)
        resumeCountdownButton = findViewById(R.id.resumeCountdownButton)
        stopCountdownButton = findViewById(R.id.stopCountdownButton)
        restartCountdownButton = findViewById(R.id.restartCountdownButton)

        stopwatchTextView = findViewById(R.id.stopwatchTextView)
        startStopwatchButton = findViewById(R.id.startStopwatchButton)
        pauseStopwatchButton = findViewById(R.id.pauseStopwatchButton)
        lapStopwatchButton = findViewById(R.id.lapStopwatchButton)
        stopStopwatchButton = findViewById(R.id.stopStopwatchButton)
        resetStopwatchButton = findViewById(R.id.resetStopwatchButton)
        lapTimesTextView = findViewById(R.id.lapTimesTextView)

        // Set up click listeners for countdown timer buttons
        startCountdownButton.setOnClickListener {
            // Start a 60-second countdown
            timerViewModel.startCountdown(60.seconds)
        }
        pauseCountdownButton.setOnClickListener {
            timerViewModel.pauseCountdown()
        }
        resumeCountdownButton.setOnClickListener {
            timerViewModel.resumeCountdown()
        }
        stopCountdownButton.setOnClickListener {
            timerViewModel.stopCountdown()
        }
        restartCountdownButton.setOnClickListener {
            timerViewModel.restartCountdown()
        }

        // Set up click listeners for stopwatch buttons
        startStopwatchButton.setOnClickListener {
            timerViewModel.startStopwatch()
        }
        pauseStopwatchButton.setOnClickListener {
            timerViewModel.pauseStopwatch()
        }
        lapStopwatchButton.setOnClickListener {
            timerViewModel.lapStopwatch()
        }
        stopStopwatchButton.setOnClickListener {
            timerViewModel.stopStopwatch()
        }
        resetStopwatchButton.setOnClickListener {
            timerViewModel.resetStopwatch()
        }

        // Observe the ViewModel's StateFlows and update the UI
        // lifecycleScope.launch and repeatOnLifecycle ensure that collection
        // happens only when the activity is in a STARTED state, saving resources.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe Countdown Timer text
                launch {
                    timerViewModel.countdownText.collect { text ->
                        countdownTextView.text = text
                    }
                }
                // Observe Stopwatch text
                launch {
                    timerViewModel.stopwatchText.collect { text ->
                        stopwatchTextView.text = text
                    }
                }
                // Observe Lap Times
                launch {
                    timerViewModel.lapTimes.collect { lapTimes ->
                        if (lapTimes.isEmpty()) {
                            lapTimesTextView.text = "Lap Times: "
                        } else {
                            val formattedLaps = lapTimes.joinToString(separator = "\n") { duration ->
                                // Using the same formatDuration logic from ViewModel here for consistent display
                                val totalSeconds = duration.inWholeSeconds
                                val minutes = totalSeconds / 60
                                val seconds = totalSeconds % 60
                                val milliseconds = (duration - totalSeconds.seconds).inWholeMilliseconds
                                String.format("%02d:%02d.%03d", minutes, seconds, milliseconds)
                            }
                            lapTimesTextView.text = "Lap Times:\n$formattedLaps"
                        }
                    }
                }
            }
        }
    }
}
