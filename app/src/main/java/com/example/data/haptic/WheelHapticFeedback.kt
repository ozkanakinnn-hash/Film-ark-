package com.example.data.haptic

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Physical haptic feedback controller using Android's [Vibrator] / [VibratorManager] motor
 * to simulate wheel slice pegs ticking during spin deceleration and a firm tactile lock-in
 * waveform when the wheel stops on the winning movie.
 */
class WheelHapticController(
    private val vibrator: Vibrator?,
    private val timeProviderMs: () -> Long = { SystemClock.elapsedRealtime() },
    private val minTickIntervalMs: Long = 24L
) {
    var isEnabled: Boolean = true
    private var lastTickTimeMs: Long = Long.MIN_VALUE / 2

    val hasHardwareVibrator: Boolean
        get() = vibrator?.hasVibrator() == true

    fun playSpinStart() {
        if (!isEnabled || !hasHardwareVibrator) return
        lastTickTimeMs = Long.MIN_VALUE / 2
        vibrateOneShot(durationMs = 22L, amplitude = 175)
    }

    /**
     * Triggered each time a wheel slice boundary crosses the top needle.
     * As [spinProgress] advances from 0f (fast) to 1f (decelerating), ticks grow
     * slightly firmer to mimic a physical peg slowing against the wheel flapper.
     */
    fun playSliceTick(spinProgress: Float, nowMs: Long = timeProviderMs()): Boolean {
        if (!isEnabled || !hasHardwareVibrator) return false
        if ((nowMs - lastTickTimeMs) < minTickIntervalMs) return false
        lastTickTimeMs = nowMs

        val clampedProgress = spinProgress.coerceIn(0f, 1f)
        val durationMs = (10L + (clampedProgress * 11f).toLong()).coerceIn(10L, 22L)
        val amplitude = (95 + (clampedProgress * 105f).toInt()).coerceIn(80, 210)
        vibrateOneShot(durationMs = durationMs, amplitude = amplitude)
        return true
    }

    /**
     * Triggered the moment the wheel stops on a movie.
     * Plays a celebratory double-pulse lock-in, or a dramatic triple-heartbeat rumble for horror titles.
     */
    fun playWheelStop(isHorror: Boolean) {
        if (!isEnabled || !hasHardwareVibrator) return
        if (isHorror) {
            // Suspenseful triple-pulse heartbeat rumble for horror reveal
            vibrateWaveform(
                timings = longArrayOf(0L, 60L, 70L, 45L, 85L, 115L),
                amplitudes = intArrayOf(0, 215, 0, 150, 0, 255)
            )
        } else {
            // Crisp celebratory double-tap lock-in for standard cinematic reveal
            vibrateWaveform(
                timings = longArrayOf(0L, 35L, 55L, 80L),
                amplitudes = intArrayOf(0, 180, 0, 255)
            )
        }
    }

    fun cancel() {
        runCatching { vibrator?.cancel() }
    }

    private fun vibrateOneShot(durationMs: Long, amplitude: Int) {
        val vib = vibrator ?: return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vib.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(durationMs, amplitude)
                } else {
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        }
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        val vib = vibrator ?: return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vib.hasAmplitudeControl()) {
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                } else {
                    VibrationEffect.createWaveform(timings, -1)
                }
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(timings, -1)
            }
        }
    }

    companion object {
        fun resolveVibrator(context: Context): Vibrator? {
            return runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
            }.getOrNull()
        }
    }
}

@Composable
fun rememberWheelHapticController(enabled: Boolean): WheelHapticController {
    val context = LocalContext.current
    val controller = remember(context) {
        WheelHapticController(WheelHapticController.resolveVibrator(context))
    }
    controller.isEnabled = enabled

    DisposableEffect(controller) {
        onDispose {
            controller.cancel()
        }
    }
    return controller
}
