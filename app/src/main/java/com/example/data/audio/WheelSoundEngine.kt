package com.example.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.example.data.haptic.WheelHapticController
import com.example.data.haptic.rememberWheelHapticController
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

enum class WheelSoundCue {
    SPIN_START,
    SLICE_TICK_FAST,
    SLICE_TICK_MID,
    SLICE_TICK_SLOW,
    STOP_CELEBRATE,
    STOP_HORROR
}

enum class PegClickProfile(
    val durationMs: Int,
    val transientFreqHz: Double,
    val bodyStartFreqHz: Double,
    val bodyEndFreqHz: Double,
    val gain: Double
) {
    FAST(
        durationMs = 25,
        transientFreqHz = 2150.0,
        bodyStartFreqHz = 860.0,
        bodyEndFreqHz = 340.0,
        gain = 0.62
    ),
    MID(
        durationMs = 28,
        transientFreqHz = 1850.0,
        bodyStartFreqHz = 740.0,
        bodyEndFreqHz = 265.0,
        gain = 0.80
    ),
    SLOW(
        durationMs = 32,
        transientFreqHz = 1520.0,
        bodyStartFreqHz = 620.0,
        bodyEndFreqHz = 210.0,
        gain = 0.94
    )
}

interface WheelAudioPlayer {
    fun play(cue: WheelSoundCue, volumeScale: Float = 1f)
    fun stopAll()
    fun release()
}

/**
 * Generates crisp, studio-tuned 16-bit mono 44.1 kHz PCM waveforms simulating
 * an acrylic/wooden wheel flapper snapping across physical pegs, plus reveal chimes.
 */
object WheelPcmSynthesizer {
    const val SAMPLE_RATE = 44_100
    private const val MIN_STATIC_SAMPLES = 1_102 // >= 25 ms (~2204 bytes) for safe static AudioTrack buffers

    fun pcmForCue(cue: WheelSoundCue): ShortArray = when (cue) {
        WheelSoundCue.SPIN_START -> generateSpinStartPcm()
        WheelSoundCue.SLICE_TICK_FAST -> generatePegClickPcm(PegClickProfile.FAST)
        WheelSoundCue.SLICE_TICK_MID -> generatePegClickPcm(PegClickProfile.MID)
        WheelSoundCue.SLICE_TICK_SLOW -> generatePegClickPcm(PegClickProfile.SLOW)
        WheelSoundCue.STOP_CELEBRATE -> generateCelebrateStopPcm()
        WheelSoundCue.STOP_HORROR -> generateHorrorStopPcm()
    }

    fun generatePegClickPcm(profile: PegClickProfile): ShortArray {
        val sampleCount = maxOf((SAMPLE_RATE * profile.durationMs) / 1000, MIN_STATIC_SAMPLES)
        val out = ShortArray(sampleCount)
        var bodyPhase = 0.0
        var lcgState = 0x5DEECE66DL

        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE
            val normalizedTime = i.toDouble() / sampleCount

            // Fast 0.4ms attack ramp eliminates hardware speaker pops
            val attack = (i / (SAMPLE_RATE * 0.0004)).coerceIn(0.0, 1.0)

            // Crisp peg impact transient + subtle damped woody click noise
            val transientEnv = exp(-t / 0.0018)
            val transientWave = sin(2.0 * PI * profile.transientFreqHz * t) * transientEnv * 0.55

            // Deterministic LCG burst for natural tactile snap texture in first 3ms
            lcgState = (lcgState * 6364136223846793005L + 1442695040888963407L)
            val noiseSample = (((lcgState shr 33).toInt() and 0xFFFF) / 32768.0 - 1.0)
            val noiseBurst = noiseSample * exp(-t / 0.0011) * 0.28

            // Warm downward-sweeping peg resonance body
            val bodyFreq = profile.bodyStartFreqHz +
                (profile.bodyEndFreqHz - profile.bodyStartFreqHz) * (1.0 - exp(-normalizedTime * 5.0))
            bodyPhase += 2.0 * PI * bodyFreq / SAMPLE_RATE
            val bodyEnv = exp(-t / 0.0065)
            val bodyWave = (sin(bodyPhase) + 0.25 * sin(bodyPhase * 2.0)) * bodyEnv * 0.65

            // Smooth tail fade out on final 3ms
            val tailFade = if (normalizedTime > 0.85) ((1.0 - normalizedTime) / 0.15).coerceIn(0.0, 1.0) else 1.0

            val mixed = (transientWave + noiseBurst + bodyWave) * attack * tailFade * profile.gain
            out[i] = (mixed.coerceIn(-0.98, 0.98) * Short.MAX_VALUE).roundToInt().toShort()
        }
        return out
    }

    fun generateSpinStartPcm(): ShortArray {
        val durationMs = 45
        val sampleCount = maxOf((SAMPLE_RATE * durationMs) / 1000, MIN_STATIC_SAMPLES)
        val out = ShortArray(sampleCount)
        var phase = 0.0

        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE
            val norm = i.toDouble() / sampleCount
            val attack = (i / (SAMPLE_RATE * 0.0006)).coerceIn(0.0, 1.0)
            val tail = (1.0 - norm).coerceIn(0.0, 1.0)

            val sweepFreq = 460.0 + (1120.0 - 460.0) * norm
            phase += 2.0 * PI * sweepFreq / SAMPLE_RATE
            val sweep = sin(phase) * exp(-t / 0.018) * 0.55
            val latchSnap = sin(2.0 * PI * 1950.0 * t) * exp(-t / 0.0022) * 0.45

            val mixed = (sweep + latchSnap) * attack * tail * 0.78
            out[i] = (mixed.coerceIn(-0.98, 0.98) * Short.MAX_VALUE).roundToInt().toShort()
        }
        return out
    }

    fun generateCelebrateStopPcm(): ShortArray {
        val durationMs = 240
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val out = ShortArray(sampleCount)
        val secondNoteStartSample = (SAMPLE_RATE * 0.09).toInt() // Synchronized with [0, 35, 55, 80]ms double haptic

        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE
            val attack1 = (i / (SAMPLE_RATE * 0.002)).coerceIn(0.0, 1.0)
            val note1Env = exp(-t / 0.055) * attack1
            val note1 = (sin(2.0 * PI * 523.25 * t) + 0.30 * sin(2.0 * PI * 1046.5 * t)) * note1Env * 0.48

            var note2 = 0.0
            if (i >= secondNoteStartSample) {
                val t2 = (i - secondNoteStartSample).toDouble() / SAMPLE_RATE
                val attack2 = ((i - secondNoteStartSample) / (SAMPLE_RATE * 0.002)).coerceIn(0.0, 1.0)
                val note2Env = exp(-t2 / 0.085) * attack2
                note2 = (sin(2.0 * PI * 783.99 * t2) + 0.28 * sin(2.0 * PI * 1567.98 * t2)) * note2Env * 0.56
            }

            val tail = if (i > sampleCount - 440) ((sampleCount - i) / 440.0).coerceIn(0.0, 1.0) else 1.0
            val mixed = (note1 + note2) * tail * 0.85
            out[i] = (mixed.coerceIn(-0.98, 0.98) * Short.MAX_VALUE).roundToInt().toShort()
        }
        return out
    }

    fun generateHorrorStopPcm(): ShortArray {
        val durationMs = 360
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val out = ShortArray(sampleCount)
        // Aligned with horror triple heartbeat haptic timings: [0, 60, 70, 45, 85, 115] -> 0ms, 130ms, 260ms
        val beatOffsetsSec = doubleArrayOf(0.0, 0.130, 0.260)
        val beatWeights = doubleArrayOf(0.75, 0.55, 0.95)

        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE
            var signal = 0.0
            for (b in beatOffsetsSec.indices) {
                val dt = t - beatOffsetsSec[b]
                if (dt >= 0.0) {
                    val attack = (dt / 0.003).coerceIn(0.0, 1.0)
                    val env = exp(-dt / 0.036) * attack
                    val thump = sin(2.0 * PI * (68.0 - 18.0 * (dt / 0.09).coerceAtMost(1.0)) * dt) * 0.72
                    val tensionOvertones = sin(2.0 * PI * 233.08 * dt) * exp(-dt / 0.022) * 0.28
                    signal += (thump + tensionOvertones) * env * beatWeights[b]
                }
            }
            val tail = if (i > sampleCount - 440) ((sampleCount - i) / 440.0).coerceIn(0.0, 1.0) else 1.0
            val mixed = signal * tail * 0.88
            out[i] = (mixed.coerceIn(-0.98, 0.98) * Short.MAX_VALUE).roundToInt().toShort()
        }
        return out
    }
}

/**
 * Android [AudioTrack] static-buffer pool backed implementation.
 * Uses a dedicated single-thread daemon executor so Compose spin animations stay lock-free.
 */
class AndroidWheelAudioPlayer : WheelAudioPlayer {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "WheelAudioEngineThread").apply { isDaemon = true }
    }

    private val trackPools = mutableMapOf<WheelSoundCue, List<AudioTrack>>()
    private val poolCursor = mutableMapOf<WheelSoundCue, Int>()
    @Volatile
    private var released = false

    init {
        executor.execute {
            if (!released) {
                warmUpTracks()
            }
        }
    }

    private fun warmUpTracks() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        WheelSoundCue.entries.forEach { cue ->
            val pcm = WheelPcmSynthesizer.pcmForCue(cue)
            val poolSize = when (cue) {
                WheelSoundCue.SLICE_TICK_FAST,
                WheelSoundCue.SLICE_TICK_MID,
                WheelSoundCue.SLICE_TICK_SLOW -> 2
                else -> 1
            }

            val tracks = buildList {
                repeat(poolSize) {
                    runCatching {
                        val bufferBytes = pcm.size * 2
                        val format = AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(WheelPcmSynthesizer.SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()

                        val track = AudioTrack.Builder()
                            .setAudioAttributes(attributes)
                            .setAudioFormat(format)
                            .setBufferSizeInBytes(bufferBytes)
                            .setTransferMode(AudioTrack.MODE_STATIC)
                            .build()

                        if (track.state == AudioTrack.STATE_INITIALIZED) {
                            track.write(pcm, 0, pcm.size, AudioTrack.WRITE_BLOCKING)
                            add(track)
                        } else {
                            track.release()
                        }
                    }
                }
            }
            if (tracks.isNotEmpty()) {
                trackPools[cue] = tracks
                poolCursor[cue] = 0
            }
        }
    }

    override fun play(cue: WheelSoundCue, volumeScale: Float) {
        if (released) return
        val clampedVolume = volumeScale.coerceIn(0f, 1f)
        runCatching {
            executor.execute {
                if (released) return@execute
                val pool = trackPools[cue] ?: return@execute
                if (pool.isEmpty()) return@execute
                val idx = (poolCursor[cue] ?: 0) % pool.size
                poolCursor[cue] = idx + 1
                val track = pool[idx]
                runCatching {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.stop()
                    }
                    track.reloadStaticData()
                    track.setVolume(clampedVolume)
                    track.play()
                }
            }
        }
    }

    override fun stopAll() {
        if (released) return
        runCatching {
            executor.execute {
                trackPools.values.flatten().forEach { track ->
                    runCatching {
                        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            track.stop()
                        }
                    }
                }
            }
        }
    }

    override fun release() {
        if (released) return
        released = true
        runCatching {
            executor.execute {
                trackPools.values.flatten().forEach { track ->
                    runCatching {
                        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            track.stop()
                        }
                        track.release()
                    }
                }
                trackPools.clear()
                poolCursor.clear()
            }
            executor.shutdown()
        }
    }
}

/**
 * Sound engine managing wheel spin clicks, start snap, and movie reveal stingers
 * with the same 24ms slice-tick throttle cadence as [WheelHapticController].
 */
class WheelSoundEngine(
    private val player: WheelAudioPlayer,
    private val timeProviderMs: () -> Long = { SystemClock.elapsedRealtime() },
    private val minTickIntervalMs: Long = 24L
) {
    var isEnabled: Boolean = true
    private var lastTickTimeMs: Long = Long.MIN_VALUE / 2

    fun playSpinStart() {
        if (!isEnabled) return
        lastTickTimeMs = Long.MIN_VALUE / 2
        player.play(WheelSoundCue.SPIN_START, volumeScale = 0.85f)
    }

    /**
     * Plays a speed-matched mechanical peg click synchronized with slice crossings.
     * Early spin -> brighter/lighter click; decelerating spin -> deeper/firmer clack.
     */
    fun playSliceTick(spinProgress: Float, nowMs: Long = timeProviderMs()): Boolean {
        if (!isEnabled) return false
        if ((nowMs - lastTickTimeMs) < minTickIntervalMs) return false
        lastTickTimeMs = nowMs

        val clampedProgress = spinProgress.coerceIn(0f, 1f)
        val cue = when {
            clampedProgress < 0.35f -> WheelSoundCue.SLICE_TICK_FAST
            clampedProgress < 0.72f -> WheelSoundCue.SLICE_TICK_MID
            else -> WheelSoundCue.SLICE_TICK_SLOW
        }
        val volume = (0.68f + clampedProgress * 0.32f).coerceIn(0.55f, 1f)
        player.play(cue, volumeScale = volume)
        return true
    }

    fun playWheelStop(isHorror: Boolean) {
        if (!isEnabled) return
        val cue = if (isHorror) WheelSoundCue.STOP_HORROR else WheelSoundCue.STOP_CELEBRATE
        player.play(cue, volumeScale = 0.95f)
    }

    fun playPreviewClick() {
        if (!isEnabled) return
        player.play(WheelSoundCue.SLICE_TICK_MID, volumeScale = 0.85f)
    }

    fun stopAll() {
        player.stopAll()
    }

    fun release() {
        player.release()
    }
}

/**
 * Unifies hardware haptic feedback and the wheel sound engine under a single
 * shared clock instant so physical ticks and audible clicks stay locked in sync.
 */
class WheelSpinFeedbackCoordinator(
    val hapticController: WheelHapticController,
    val soundEngine: WheelSoundEngine,
    private val timeProviderMs: () -> Long = { SystemClock.elapsedRealtime() }
) {
    fun onSpinStart() {
        hapticController.playSpinStart()
        soundEngine.playSpinStart()
    }

    fun onSliceCrossing(spinProgress: Float): Boolean {
        val nowMs = timeProviderMs()
        val hapticFired = hapticController.playSliceTick(spinProgress, nowMs)
        val soundFired = soundEngine.playSliceTick(spinProgress, nowMs)
        return hapticFired || soundFired
    }

    fun onWheelStop(isHorror: Boolean) {
        hapticController.playWheelStop(isHorror)
        soundEngine.playWheelStop(isHorror)
    }

    fun stopAll() {
        hapticController.cancel()
        soundEngine.stopAll()
    }
}

@Composable
fun rememberWheelSoundEngine(enabled: Boolean): WheelSoundEngine {
    val engine = remember {
        WheelSoundEngine(AndroidWheelAudioPlayer())
    }
    engine.isEnabled = enabled

    DisposableEffect(engine) {
        onDispose {
            engine.release()
        }
    }
    return engine
}

@Composable
fun rememberWheelFeedbackCoordinator(
    hapticEnabled: Boolean,
    soundEnabled: Boolean
): WheelSpinFeedbackCoordinator {
    val hapticController = rememberWheelHapticController(enabled = hapticEnabled)
    val soundEngine = rememberWheelSoundEngine(enabled = soundEnabled)
    return remember(hapticController, soundEngine) {
        WheelSpinFeedbackCoordinator(
            hapticController = hapticController,
            soundEngine = soundEngine
        )
    }
}
