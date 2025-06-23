# Timer Library
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=flat&logo=kotlin&logoColor=white) [![](https://jitpack.io/v/jksalcedo/timer-kotlin.svg)](https://jitpack.io/#jksalcedo/timer-kotlin) 

The Timer library provides robust and flexible CountdownTimer and Stopwatch functionalities implemented using Kotlin Coroutines Flow. It's ideal for applications requiring precise time management, such as quiz apps, workout trackers, or game timers.

## Features

**Pure Kotlin Logic**: The core timer logic is platform-agnostic, making it highly reusable in any JVM-based Kotlin project.

**Reactive Updates**: Leverages kotlinx.coroutines.flow to provide real-time updates for elapsedTime, remainingTime, and isRunning status.

### CountdownTimer:

Start, pause, resume, stop, and restart functionalities.

Emits the remainingTime until zero.

### Stopwatch:

Tracks total elapsedTime.

Supports lap() times (individual durations between lap presses).

Supports split() times (total elapsed time at specific marks).

Start, pause, stop, and reset functionalities.

High Precision: Uses kotlin.time.Duration and kotlin.time.TimeSource.Monotonic for accurate time tracking, even with millisecond precision.

## Installation

Add the JitPack repository to your project's root build.gradle.kts (or build.gradle):

```groovy
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_NON_DECLARED_REPOSITORIES)
    repositories {
    google()
    mavenCentral()
    maven { url 'https://jitpack.io' } // Add this line
    }
}
```

Then, add the timer-kotlin dependency to your app's build.gradle.kts (or build.gradle):

```groovy
// app/build.gradle.kts
dependencies {
    implementation("com.github.jksalcedo:timer-kotlin:1.0.0") // Or the latest version
    // Ensure you also have kotlinx-coroutines-android for ViewModelScope
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.activity:activity-ktx:1.9.0") // For viewModels() delegate
}
```

## Usage

Initialize CountdownTimer and Stopwatch instances within a CoroutineScope (e.g., viewModelScope for Android apps). Observe their StateFlows to update your UI.
1. TimerViewModel.kt (Example ViewModel)

It's highly recommended to encapsulate timer logic within a ViewModel for lifecycle awareness and separation of concerns.
```kotlin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.timer.CountdownTimer
import com.jksalcedo.timer.Stopwatch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TimerViewModel : ViewModel() {

    // --- Countdown Timer ---
    private val countdownTimer = CountdownTimer(viewModelScope)

    val countdownText = countdownTimer.remainingTime
        .map { duration -> formatDuration(duration) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            formatDuration(Duration.ZERO)
        )

    val isCountdownRunning = countdownTimer.isRunning.asStateFlow()

    fun startCountdown(duration: Duration) { countdownTimer.start(duration) }
    fun pauseCountdown() { countdownTimer.pause() }
    fun resumeCountdown() { countdownTimer.resume() }
    fun stopCountdown() { countdownTimer.stop() }
    fun restartCountdown() { countdownTimer.restart() }

    // --- Stopwatch ---
    private val stopwatch = Stopwatch(viewModelScope)

    val stopwatchText = stopwatch.elapsedTime
        .map { duration -> formatDuration(duration, includeMilliseconds = true) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            formatDuration(Duration.ZERO, includeMilliseconds = true)
        )

    val isStopwatchRunning = stopwatch.isRunning.asStateFlow()
    val lapTimes = stopwatch.lapTimes.asStateFlow()
    val splitTimes = stopwatch.splitTimes.asStateFlow()

    fun startStopwatch() { stopwatch.start() }
    fun pauseStopwatch() { stopwatch.pause() }
    fun stopStopwatch() { stopwatch.stop() }
    fun lapStopwatch() { stopwatch.lap() }
    fun splitStopwatch() { stopwatch.split() }
    fun resetStopwatch() { stopwatch.reset() }

    // --- Utility formatting function ---
    private fun formatDuration(duration: Duration, includeMilliseconds: Boolean = false): String {
        val totalSeconds = duration.inWholeSeconds
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val milliseconds = (duration - totalSeconds.seconds).inWholeMilliseconds

        return if (includeMilliseconds) {
            String.format("%02d:%02d.%03d", minutes, seconds, milliseconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
```

2. MainActivity.kt (Example UI Integration)

Observe the StateFlows from your ViewModel to update UI elements.
```kotlin

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

    private val timerViewModel: TimerViewModel by viewModels()

    // UI elements (declare as lateinit var and initialize in onCreate)
    private lateinit var countdownTextView: TextView
    private lateinit var startCountdownButton: Button
    private lateinit var var pauseCountdownButton: Button
    private lateinit var var resumeCountdownButton: Button
    private lateinit var var stopCountdownButton: Button
    private lateinit var var restartCountdownButton: Button

    private lateinit var stopwatchTextView: TextView
    private lateinit var startStopwatchButton: Button
    private lateinit var pauseStopwatchButton: Button
    private lateinit var lapStopwatchButton: Button
    private lateinit var splitStopwatchButton: Button
    private lateinit var stopStopwatchButton: Button
    private lateinit var resetStopwatchButton: Button
    private lateinit var lapTimesTextView: TextView
    private lateinit var splitTimesTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
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
        splitStopwatchButton = findViewById(R.id.splitStopwatchButton)
        stopStopwatchButton = findViewById(R.id.stopStopwatchButton)
        resetStopwatchButton = findViewById(R.id.resetStopwatchButton)
        lapTimesTextView = findViewById(R.id.lapTimesTextView)
        splitTimesTextView = findViewById(R.id.splitTimesTextView)

        // Set up click listeners for countdown timer buttons
        startCountdownButton.setOnClickListener { timerViewModel.startCountdown(60.seconds) }
        pauseCountdownButton.setOnClickListener { timerViewModel.pauseCountdown() }
        resumeCountdownButton.setOnClickListener { timerViewModel.resumeCountdown() }
        stopCountdownButton.setOnClickListener { timerViewModel.stopCountdown() }
        restartCountdownButton.setOnClickListener { timerViewModel.restartCountdown() }

        // Set up click listeners for stopwatch buttons
        startStopwatchButton.setOnClickListener { timerViewModel.startStopwatch() }
        pauseStopwatchButton.setOnClickListener { timerViewModel.pauseStopwatch() }
        lapStopwatchButton.setOnClickListener { timerViewModel.lapStopwatch() }
        splitStopwatchButton.setOnClickListener { timerViewModel.splitStopwatch() }
        stopStopwatchButton.setOnClickListener { timerViewModel.stopStopwatch() }
        resetStopwatchButton.setOnClickListener { timerViewModel.resetStopwatch() }

        // Observe the ViewModel's StateFlows and update the UI
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { timerViewModel.countdownText.collect { text -> countdownTextView.text = text } }
                launch { timerViewModel.stopwatchText.collect { text -> stopwatchTextView.text = text } }
                launch {
                    timerViewModel.lapTimes.collect { lapTimes ->
                        val formattedLaps = lapTimes.joinToString(separator = "\n") { duration ->
                            val totalSeconds = duration.inWholeSeconds
                            val minutes = totalSeconds / 60
                            val seconds = totalSeconds % 60
                            val milliseconds = (duration - totalSeconds.seconds).inWholeMilliseconds
                            String.format("%02d:%02d.%03d", minutes, seconds, milliseconds)
                        }
                        lapTimesTextView.text = if (lapTimes.isEmpty()) "Lap Times: " else "Lap Times:\n$formattedLaps"
                    }
                }
                launch {
                    timerViewModel.splitTimes.collect { splitTimes ->
                        val formattedSplits = splitTimes.joinToString(separator = "\n") { duration ->
                            val totalSeconds = duration.inWholeSeconds
                            val minutes = totalSeconds / 60
                            val seconds = totalSeconds % 60
                            val milliseconds = (duration - totalSeconds.seconds).inWholeMilliseconds
                            String.format("%02d:%02d.%03d", minutes, seconds, milliseconds)
                        }
                        splitTimesTextView.text = if (splitTimes.isEmpty()) "Split Times: " else "Split Times:\n$formattedSplits"
                    }
                }
            }
        }
    }
}
```

3. activity_main.xml (Example Layout)
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:app="http://schemas.android.com/apk/res-auto"
xmlns:tools="http://schemas.android.com/tools"
android:layout_width="match_parent"
android:layout_height="match_parent"
android:orientation="vertical"
android:padding="16dp"
android:gravity="center_horizontal"
tools:context=".MainActivity">

    <!-- Countdown Timer Section -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Countdown Timer"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginTop="24dp"
        android:layout_marginBottom="8dp"/>

    <TextView
        android:id="@+id/countdownTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="00:00"
        android:textSize="48sp"
        android:textStyle="bold"
        android:textColor="@android:color/black"
        tools:text="05:30"/>

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="16dp">

        <Button
            android:id="@+id/startCountdownButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Start (60s)"
            android:layout_marginEnd="8dp"/>

        <Button
            android:id="@+id/pauseCountdownButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Pause"
            android:layout_marginEnd="8dp"/>

        <Button
            android:id="@+id/resumeCountdownButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Resume"
            android:layout_marginEnd="8dp"/>

        <Button
            android:id="@+id/stopCountdownButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Stop" />

    </LinearLayout>

    <Button
        android:id="@+id/restartCountdownButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Restart Last Countdown"
        android:layout_marginTop="8dp"/>

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@android:color/darker_gray"
        android:layout_marginTop="32dp"
        android:layout_marginBottom="32dp"/>

    <!-- Stopwatch Section -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Stopwatch"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginBottom="8dp"/>

    <TextView
        android:id="@+id/stopwatchTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="00:00.000"
        android:textSize="48sp"
        android:textStyle="bold"
        android:textColor="@android:color/black"
        tools:text="01:23.456"/>

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="16dp">

        <Button
            android:id="@+id/startStopwatchButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Start"
            android:layout_marginEnd="8dp"/>

        <Button
            android:id="@+id/pauseStopwatchButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Pause"
            android:layout_marginEnd="8dp"/>

        <Button
            android:id="@+id/lapStopwatchButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Lap"
            android:layout_marginEnd="8dp"/>

        <Button
            android:id="@+id/splitStopwatchButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Split"
            android:layout_marginEnd="8dp"/>

        <Button
            android:id="@+id/stopStopwatchButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Stop" />

    </LinearLayout>

    <Button
        android:id="@+id/resetStopwatchButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Reset"
        android:layout_marginTop="8dp"/>

    <TextView
        android:id="@+id/lapTimesTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Lap Times: "
        android:layout_marginTop="16dp"
        android:textSize="16sp"
        android:gravity="center_horizontal"/>

    <TextView
        android:id="@+id/splitTimesTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Split Times: "
        android:layout_marginTop="8dp"
        android:textSize="16sp"
        android:gravity="center_horizontal"/>

</LinearLayout>
```
