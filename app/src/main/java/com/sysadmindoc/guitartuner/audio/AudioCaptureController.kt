package com.sysadmindoc.guitartuner.audio

import android.annotation.SuppressLint
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.sysadmindoc.guitartuner.pitch.PhaseRefiner
import com.sysadmindoc.guitartuner.pitch.PitchDetectorConfig
import com.sysadmindoc.guitartuner.pitch.SignalStatus
import com.sysadmindoc.guitartuner.pitch.YinPitchDetector
import com.sysadmindoc.guitartuner.tuning.GuitarString
import com.sysadmindoc.guitartuner.tuning.MeasurementFreeze
import com.sysadmindoc.guitartuner.tuning.StableMeasurementSmoother
import com.sysadmindoc.guitartuner.tuning.StandardGuitarTuning
import com.sysadmindoc.guitartuner.tuning.TuningAnalyzer
import com.sysadmindoc.guitartuner.tuning.TuningTargetSelection
import java.io.Closeable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioCaptureController(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    initialPitchDetector: YinPitchDetector = YinPitchDetector(),
    initialStrings: List<GuitarString> = StandardGuitarTuning.strings,
    private val supportsUnprocessedSource: Boolean = false,
    private val audioManager: AudioManager? = null,
) : Closeable {
    private val _state = MutableStateFlow(TunerSessionState())
    val state: StateFlow<TunerSessionState> = _state.asStateFlow()

    @Volatile
    private var pitchDetector: YinPitchDetector = initialPitchDetector

    private var currentStrings: List<GuitarString> = initialStrings
    private var targetSelection: TuningTargetSelection = TuningTargetSelection.auto()
    private var centsTolerance: Double = 5.0
    private var a4Hz: Double = 440.0

    @Volatile
    private var tuningAnalyzer: TuningAnalyzer = TuningAnalyzer(currentStrings, targetSelection)

    @Volatile
    private var freezeAfterDecay: Boolean = false

    @Volatile
    private var preferredDeviceId: Int? = null

    private val measurementFreeze = MeasurementFreeze()
    private val measurementSmoother = StableMeasurementSmoother()
    private val phaseRefiner = PhaseRefiner()

    private var captureJob: Job? = null

    @Volatile
    private var activeRecord: AudioRecord? = null

    @Synchronized
    fun start() {
        if (captureJob?.isActive == true) return
        _state.value = _state.value.copy(
            isListening = true,
        )
        val job = scope.launch(dispatcher) {
            runCaptureLoop()
        }
        captureJob = job
        job.invokeOnCompletion {
            if (captureJob === job) {
                captureJob = null
            }
        }
    }

    @Synchronized
    fun stop() {
        val job = captureJob
        captureJob = null
        job?.cancel()
        activeRecord?.safeStop()
        measurementSmoother.reset()
        measurementFreeze.reset()
        phaseRefiner.reset()
        _state.value = _state.value.copy(isListening = false, micStolen = false)
    }

    fun notePermissionDenied() {
        _state.value = TunerSessionState(
            isListening = false,
            permissionError = true,
        )
    }

    fun clearPermissionError() {
        _state.value = _state.value.copy(permissionError = false)
    }

    fun setTuning(strings: List<GuitarString>, minFrequencyHz: Double = 70.0, maxFrequencyHz: Double = 450.0) {
        currentStrings = strings
        pitchDetector = YinPitchDetector(
            PitchDetectorConfig(
                silenceRms = pitchDetector.config.silenceRms,
                minFrequencyHz = minFrequencyHz,
                maxFrequencyHz = maxFrequencyHz,
            ),
        )
        rebuildAnalyzer()
    }

    fun setTargetSelection(selection: TuningTargetSelection) {
        targetSelection = selection
        rebuildAnalyzer()
    }

    fun setCentsTolerance(cents: Double) {
        centsTolerance = cents
        rebuildAnalyzer()
    }

    fun setNoiseGateRms(rms: Double) {
        pitchDetector = YinPitchDetector(
            pitchDetector.config.copy(silenceRms = rms),
        )
    }

    fun setA4Hz(hz: Double) {
        a4Hz = hz
        rebuildAnalyzer()
    }

    private fun rebuildAnalyzer() {
        tuningAnalyzer = TuningAnalyzer(
            strings = currentStrings,
            targetSelection = targetSelection,
            inTuneCents = centsTolerance,
            a4Hz = a4Hz,
        )
        measurementSmoother.reset()
        measurementFreeze.reset()
    }

    fun setFreezeAfterDecay(enabled: Boolean) {
        freezeAfterDecay = enabled
    }

    fun setPreferredDevice(deviceId: Int?) {
        preferredDeviceId = deviceId
    }

    override fun close() {
        stop()
        activeRecord?.safeRelease()
        activeRecord = null
    }

    @SuppressLint("MissingPermission")
    private suspend fun runCaptureLoop() {
        val excludedSources = mutableSetOf<Int>()
        val captureContext = currentCoroutineContext()

        while (captureContext.isActive) {
            val recorder = try {
                buildAudioRecord(excludedSources)
            } catch (_: RuntimeException) {
                if (!captureContext.isActive) return
                _state.value = TunerSessionState(
                    isListening = false,
                    audioError = AudioError.MicInitFailed,
                )
                return
            }
            activeRecord = recorder
            var retryWithNextSource = false

            try {
                recorder.startRecording()
                val recorderSampleRate = recorder.sampleRate.takeIf { it > 0 } ?: PreferredSampleRate
                val sourceLabel = recorder.audioSource.audioSourceLabel()
                _state.value = _state.value.copy(
                    isListening = true,
                    inputLevel = AudioInputLevel(sourceLabel = sourceLabel, sampleRateHz = recorderSampleRate),
                    audioError = null,
                )
                Log.i(
                    LogTag,
                    "Started microphone capture source=$sourceLabel sampleRate=$recorderSampleRate bufferFrames=${recorder.bufferSizeInFrames}",
                )
                retryWithNextSource = captureReadLoop(recorder, recorderSampleRate, sourceLabel)
                if (retryWithNextSource) {
                    excludedSources.add(recorder.audioSource)
                    Log.w(LogTag, "Source $sourceLabel produced only zeros during startup, trying next source.")
                }
            } catch (_: RuntimeException) {
                if (!captureContext.isActive) return
                _state.value = TunerSessionState(
                    isListening = false,
                    pitchEstimate = _state.value.pitchEstimate.copy(status = SignalStatus.Unstable),
                    audioError = AudioError.CaptureStopped,
                )
                return
            } finally {
                recorder.safeStop()
                recorder.safeRelease()
                if (activeRecord === recorder) {
                    activeRecord = null
                }
            }

            if (!retryWithNextSource) {
                if (captureContext.isActive) {
                    _state.value = _state.value.copy(isListening = false)
                }
                return
            }
        }
    }

    private suspend fun captureReadLoop(
        recorder: AudioRecord,
        sampleRate: Int,
        sourceLabel: String,
    ): Boolean {
        val captureContext = currentCoroutineContext()
        val readBuffer = ShortArray(ReadBufferSize)
        val frameBuffer = FloatArray(FrameSize)
        var frameFill = 0
        var startupReads = 0
        var startupHasAudio = false
        var hadLiveAudio = false
        var consecutiveZeroReads = 0

        while (captureContext.isActive) {
            val read = recorder.read(readBuffer, 0, readBuffer.size, AudioRecord.READ_BLOCKING)
            if (read < 0) {
                throw IllegalStateException(readErrorMessage(read))
            }
            if (read == 0) continue

            val inputLevel = AudioInputLevel.fromPcmRead(
                samples = readBuffer,
                length = read,
                previous = _state.value.inputLevel,
                sourceLabel = sourceLabel,
                sampleRateHz = sampleRate,
            )

            val isZeroRead = inputLevel.rms < ZeroFrameRms
            if (isZeroRead) {
                consecutiveZeroReads++
            } else {
                consecutiveZeroReads = 0
                hadLiveAudio = true
            }

            startupReads++
            if (!startupHasAudio && startupReads <= StartupCheckReads) {
                if (!isZeroRead) {
                    startupHasAudio = true
                } else if (startupReads == StartupCheckReads) {
                    return true
                }
            }

            val micStolen = hadLiveAudio && consecutiveZeroReads >= StolenMicZeroReads

            _state.value = _state.value.copy(
                isListening = true,
                inputLevel = inputLevel,
                micStolen = micStolen,
                audioError = null,
            )

            for (index in 0 until read) {
                frameBuffer[frameFill] = readBuffer[index] / Short.MAX_VALUE.toFloat()
                frameFill += 1
                if (frameFill == FrameSize) {
                    analyzeFrame(frameBuffer.copyOf(), sampleRate)
                    frameBuffer.copyInto(
                        destination = frameBuffer,
                        destinationOffset = 0,
                        startIndex = FrameHopSize,
                        endIndex = FrameSize,
                    )
                    frameFill = FrameSize - FrameHopSize
                }
            }
        }

        return false
    }

    private fun analyzeFrame(samples: FloatArray, sampleRate: Int) {
        val detector = pitchDetector
        val analyzer = tuningAnalyzer
        val freeze = freezeAfterDecay

        var estimate = detector.detect(samples, sampleRate)
        if (estimate.frequencyHz != null && estimate.status == SignalStatus.Detected) {
            val coarse = estimate.frequencyHz
            val refined = phaseRefiner.refine(samples, coarse, sampleRate)
            if (refined > 0 && refined < detector.config.maxFrequencyHz * 2) {
                estimate = estimate.copy(frequencyHz = refined)
            }
        } else {
            phaseRefiner.reset()
        }
        val measurement = measurementSmoother.apply(analyzer.analyze(estimate))
        val measurementFrame = measurementFreeze.apply(
            estimate = estimate,
            measurement = measurement,
            enabled = freeze,
        )
        _state.value = _state.value.copy(
            isListening = true,
            isFrozen = measurementFrame.isFrozen,
            pitchEstimate = measurementFrame.pitchEstimate,
            measurement = measurementFrame.measurement,
        )
    }

    @SuppressLint("MissingPermission")
    private fun buildAudioRecord(excludedSources: Set<Int> = emptySet()): AudioRecord {
        val sampleRates = listOf(PreferredSampleRate, FallbackSampleRate)
        val sources = buildList {
            if (supportsUnprocessedSource && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                add(MediaRecorder.AudioSource.UNPROCESSED)
            }
            add(MediaRecorder.AudioSource.MIC)
            add(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaRecorder.AudioSource.VOICE_PERFORMANCE)
            }
        }.filter { it !in excludedSources }

        for (sampleRate in sampleRates) {
            val minimumBuffer = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            if (minimumBuffer == AudioRecord.ERROR || minimumBuffer == AudioRecord.ERROR_BAD_VALUE) {
                continue
            }

            val bufferBytes = maxOf(minimumBuffer, FrameSize * Short.SIZE_BYTES * 2)

            for (source in sources) {
                val recorder = try {
                    AudioRecord.Builder()
                        .setAudioSource(source)
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                                .build(),
                        )
                        .setBufferSizeInBytes(bufferBytes)
                        .build()
                } catch (exception: RuntimeException) {
                    Log.w(LogTag, "Source ${source.audioSourceLabel()} at $sampleRate Hz failed.", exception)
                    null
                } ?: continue

                if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                    applyPreferredDevice(recorder)
                    Log.i(LogTag, "Selected source ${source.audioSourceLabel()} at $sampleRate Hz.")
                    return recorder
                }
                recorder.safeRelease()
            }
        }

        throw IllegalStateException("Could not initialize the microphone.")
    }

    fun availableInputDevices(): List<InputDeviceInfo> {
        val manager = audioManager ?: return emptyList()
        return manager.getDevices(AudioManager.GET_DEVICES_INPUTS).map { device ->
            InputDeviceInfo(
                id = device.id,
                label = device.productName.toString().ifBlank { deviceTypeLabel(device.type) },
                type = deviceTypeLabel(device.type),
            )
        }
    }

    private fun applyPreferredDevice(recorder: AudioRecord) {
        val deviceId = preferredDeviceId ?: return
        val manager = audioManager ?: return
        val device = manager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            .firstOrNull { it.id == deviceId }
        if (device != null) {
            recorder.setPreferredDevice(device)
            Log.i(LogTag, "Set preferred input device: ${device.productName} (id=$deviceId)")
        }
    }

    private fun deviceTypeLabel(type: Int): String = when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_MIC -> "Built-in mic"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired headset"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB device"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB headset"
        else -> "Input $type"
    }

    private fun AudioRecord.safeStop() {
        try {
            if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                stop()
            }
        } catch (_: IllegalStateException) {
        }
    }

    private fun AudioRecord.safeRelease() {
        try {
            release()
        } catch (_: RuntimeException) {
        }
    }

    private fun readErrorMessage(errorCode: Int): String = when (errorCode) {
        AudioRecord.ERROR_DEAD_OBJECT -> "Microphone capture stopped because Android invalidated the audio recorder."
        AudioRecord.ERROR_INVALID_OPERATION -> "Microphone read failed because the audio recorder is not recording."
        AudioRecord.ERROR_BAD_VALUE -> "Microphone read failed because Android rejected the read buffer."
        AudioRecord.ERROR -> "Microphone read failed with an unknown Android AudioRecord error."
        else -> "Microphone read failed with AudioRecord code $errorCode."
    }

    private fun Int.audioSourceLabel(): String = when (this) {
        MediaRecorder.AudioSource.VOICE_PERFORMANCE -> "Voice performance"
        MediaRecorder.AudioSource.VOICE_RECOGNITION -> "Voice recognition"
        MediaRecorder.AudioSource.MIC -> "Mic"
        MediaRecorder.AudioSource.UNPROCESSED -> "Unprocessed"
        MediaRecorder.AudioSource.DEFAULT -> "Android default"
        else -> "Source $this"
    }

    private companion object {
        const val LogTag = "GuitarTunerAudio"
        const val PreferredSampleRate = 48_000
        const val FallbackSampleRate = 44_100
        const val FrameSize = 4_096
        const val FrameHopSize = 2_048
        const val ReadBufferSize = 1_024
        const val ZeroFrameRms = 0.0001
        const val StartupCheckReads = 44
        const val StolenMicZeroReads = 88
    }
}
