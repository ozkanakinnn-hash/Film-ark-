package com.example.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.sqrt

data class ShakeSampleResult(
    val gForce: Float,
    val normalizedIntensity: Float,
    val didTriggerShake: Boolean
)

/**
 * Pure Kotlin accelerometer shake detector with gravity normalization, multi-peak slop window,
 * strong-jolt shortcut, and post-trigger cooldown so normal handling doesn't misfire.
 */
class ShakeDetector(
    private val shakeThresholdG: Float = 2.0f,
    private val strongSingleThresholdG: Float = 2.65f,
    private val minPeakCount: Int = 2,
    private val slopWindowMs: Long = 600L,
    private val minPeakSpacingMs: Long = 80L,
    private val cooldownMs: Long = 1200L
) {
    private var lastTriggerTimestampMs: Long = Long.MIN_VALUE / 2
    private var lastPeakTimestampMs: Long = Long.MIN_VALUE / 2
    private var peakCount: Int = 0

    fun onAccelerometerReading(
        x: Float,
        y: Float,
        z: Float,
        timestampMs: Long
    ): ShakeSampleResult {
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH
        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        val intensity = ((gForce - 1.0f) / (shakeThresholdG - 1.0f)).coerceIn(0f, 1f)

        val inCooldown = (timestampMs - lastTriggerTimestampMs) < cooldownMs
        if (inCooldown) {
            return ShakeSampleResult(
                gForce = gForce,
                normalizedIntensity = intensity,
                didTriggerShake = false
            )
        }

        if (gForce >= strongSingleThresholdG) {
            lastTriggerTimestampMs = timestampMs
            lastPeakTimestampMs = timestampMs
            peakCount = 0
            return ShakeSampleResult(
                gForce = gForce,
                normalizedIntensity = 1f,
                didTriggerShake = true
            )
        }

        if (gForce >= shakeThresholdG) {
            val sinceLastPeak = timestampMs - lastPeakTimestampMs
            if (sinceLastPeak > slopWindowMs) {
                peakCount = 1
                lastPeakTimestampMs = timestampMs
            } else if (sinceLastPeak >= minPeakSpacingMs) {
                peakCount += 1
                lastPeakTimestampMs = timestampMs
            }

            if (peakCount >= minPeakCount) {
                lastTriggerTimestampMs = timestampMs
                peakCount = 0
                return ShakeSampleResult(
                    gForce = gForce,
                    normalizedIntensity = 1f,
                    didTriggerShake = true
                )
            }
        } else if ((timestampMs - lastPeakTimestampMs) > slopWindowMs) {
            peakCount = 0
        }

        return ShakeSampleResult(
            gForce = gForce,
            normalizedIntensity = intensity,
            didTriggerShake = false
        )
    }

    fun reset() {
        lastTriggerTimestampMs = Long.MIN_VALUE / 2
        lastPeakTimestampMs = Long.MIN_VALUE / 2
        peakCount = 0
    }
}

/**
 * Registers a battery-conscious [Sensor.TYPE_ACCELEROMETER] listener tied to Compose lifecycle
 * (`RESUMED` only) and invokes [onShakeTriggered] when a deliberate shake is detected.
 *
 * @return true if the device exposes an accelerometer hardware sensor, false otherwise.
 */
@Composable
fun rememberAccelerometerShakeEffect(
    enabled: Boolean,
    isSpinning: Boolean,
    onShakeIntensityChange: (Float) -> Unit,
    onShakeTriggered: () -> Unit
): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnIntensity by rememberUpdatedState(onShakeIntensityChange)
    val currentOnShake by rememberUpdatedState(onShakeTriggered)

    val sensorManager = remember(context) {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }
    val accelerometer = remember(sensorManager) {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
    val shakeDetector = remember { ShakeDetector() }

    DisposableEffect(sensorManager, accelerometer, enabled, isSpinning, lifecycleOwner) {
        if (!enabled || isSpinning || sensorManager == null || accelerometer == null) {
            shakeDetector.reset()
            currentOnIntensity(0f)
            return@DisposableEffect onDispose { }
        }

        var isRegistered = false
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
                if (event.values.size < 3) return

                val timestampMs = if (event.timestamp > 0L) {
                    event.timestamp / 1_000_000L
                } else {
                    SystemClock.elapsedRealtime()
                }
                val result = shakeDetector.onAccelerometerReading(
                    x = event.values[0],
                    y = event.values[1],
                    z = event.values[2],
                    timestampMs = timestampMs
                )
                currentOnIntensity(result.normalizedIntensity)
                if (result.didTriggerShake) {
                    currentOnShake()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        fun registerIfNeeded() {
            if (!isRegistered) {
                shakeDetector.reset()
                isRegistered = sensorManager.registerListener(
                    listener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        }

        fun unregisterIfNeeded() {
            if (isRegistered) {
                sensorManager.unregisterListener(listener)
                isRegistered = false
                currentOnIntensity(0f)
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> registerIfNeeded()
                Lifecycle.Event.ON_PAUSE -> unregisterIfNeeded()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            registerIfNeeded()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            unregisterIfNeeded()
        }
    }

    return accelerometer != null
}
