package com.sysadmindoc.guitartuner.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
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
import kotlinx.coroutines.cancelAndJoin
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
) : Closeable {
    private val _state = MutableStateFlow(TunerSessionState())
    val state: StateFlow<TunerSessionState> = _state.asStateFlow()

    @Volatile
    private var pitchDetector: YinPitchDetector = initialPitchDetector

    @Volatile
    private var currentStrings: List<GuitarString> = initialStrings

    @Volatile
    private var targetSelection: TuningTargetSelection = TuningTargetSelection.auto()

    @Volatile
    private var centsTolerance: Double = 5.0

    @Volatile
    private var tuningAnalyzer: TuningAnalyzer = TuningAnalyzer(currentStrings, targetSelection)

    @Volatile
    private var freezeAfterDecay: Boolean = false

    private val measurementFreeze = MeasurementFreeze()
    private val measurementSmoother = StableMeasurementSmoother()

    private var captureJob: Job? = null

    @Volatile
    private var activeRecord: AudioRecord? = null

    fun start() {
        if (captureJob?.isActive == true) return
        _state.value = _state.value.copy(
            isListening = true,
            errorMessage = null,
        )
        captureJob = scope.launch(dispatcher) {
            runCaptureLoop()
        }
    }

    fun stop() {
        val job = captureJob
        captureJob = null
        activeRecord?.safeStop()
        if (job?.isActive == true) {
            scope.launch(dispatcher) {
                job.cancelAndJoin()
            }
        }
        measurementSmoother.reset()
        _state.value = _state.value.copy(isListening = false)
    }

    fun notePermissionDenied() {
        _state.value = TunerSessionState(
            isListening = false,
            errorMessage = "Microphone permission is required for offline tuning.",
        )
    }

    fun setTuning(strings: List<GuitarString>) {
        currentStrings = strings
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
        pitchDetector = YinPitchDetector(PitchDetectorConfig(silenceRms = rms))
    }

    private fun rebuildAnalyzer() {
        tuningAnalyzer = TuningAnalyzer(
            strings = currentStrings,
            targetSelection = targetSelection,
            inTuneCents = centsTolerance,
        )
        measurementSmoother.reset()
    }

    fun setFreezeAfterDecay(enabled: Boolean) {
        freezeAfterDecay = enabled
    }

    override fun close() {
        stop()
        activeRecord?.safeRelease()
        activeRecord = null
    }

    @SuppressLint("MissingPermission")
    private fun runCaptureLoop() {
        val recorder = try {
            buildAudioRecord()
        } catch (exception: RuntimeException) {
            _state.value = TunerSessionState(
                isListening = false,
                errorMessage = exception.message ?: "Could not open the microphone.",
            )
            return
        }
        activeRecord = recorder

        try {
            recorder.startRecording()
            val recorderSampleRate = recorder.sampleRate.takeIf { it > 0 } ?: SampleRate
            val sourceLabel = recorder.audioSource.audioSourceLabel()
            _state.value = _state.value.copy(
                isListening = true,
                inputLevel = AudioInputLevel(sourceLabel = sourceLabel, sampleRateHz = recorderSampleRate),
                errorMessage = null,
            )
            Log.i(
                LogTag,
                "Started microphone capture source=$sourceLabel sampleRate=$recorderSampleRate bufferFrames=${recorder.bufferSizeInFrames}",
            )
            val readBuffer = ShortArray(ReadBufferSize)
            val frameBuffer = FloatArray(FrameSize)
            var frameFill = 0
            while (scope.isActive && captureJob?.isActive == true) {
                val read = recorder.read(readBuffer, 0, readBuffer.size, AudioRecord.READ_BLOCKING)
                if (read < 0) {
                    throw IllegalStateException(readErrorMessage(read))
                }
                if (read == 0) continue

                _state.value = _state.value.copy(
                    isListening = true,
                    inputLevel = AudioInputLevel.fromPcmRead(
                        samples = readBuffer,
                        length = read,
                        previous = _state.value.inputLevel,
                        sourceLabel = sourceLabel,
                        sampleRateHz = recorderSampleRate,
                    ),
                    errorMessage = null,
                )

                for (index in 0 until read) {
                    frameBuffer[frameFill] = readBuffer[index] / Short.MAX_VALUE.toFloat()
                    frameFill += 1
                    if (frameFill == FrameSize) {
                        analyzeFrame(frameBuffer.copyOf(), recorderSampleRate)
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
        } catch (exception: RuntimeException) {
            _state.value = TunerSessionState(
                isListening = false,
                pitchEstimate = _state.value.pitchEstimate.copy(status = SignalStatus.Unstable),
                errorMessage = exception.message ?: "Microphone capture stopped.",
            )
        } finally {
            recorder.safeStop()
            recorder.safeRelease()
            if (activeRecord === recorder) {
                activeRecord = null
            }
            _state.value = _state.value.copy(isListening = false)
        }
    }

    private fun analyzeFrame(samples: FloatArray, sampleRate: Int) {
        val estimate = pitchDetector.detect(samples, sampleRate)
        val measurement = measurementSmoother.apply(tuningAnalyzer.analyze(estimate))
        val measurementFrame = measurementFreeze.apply(
            estimate = estimate,
            measurement = measurement,
            enabled = freezeAfterDecay,
        )
        _state.value = _state.value.copy(
            isListening = true,
            isFrozen = measurementFrame.isFrozen,
            pitchEstimate = measurementFrame.pitchEstimate,
            measurement = measurementFrame.measurement,
            errorMessage = null,
        )
    }

    @SuppressLint("MissingPermission")
    private fun buildAudioRecord(): AudioRecord {
        val minimumBuffer = AudioRecord.getMinBufferSize(
            SampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minimumBuffer == AudioRecord.ERROR || minimumBuffer == AudioRecord.ERROR_BAD_VALUE) {
            throw IllegalStateException("The device does not support 44.1 kHz mono recording.")
        }

        val bufferBytes = maxOf(minimumBuffer, FrameSize * Short.SIZE_BYTES * 2)
        val sources = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaRecorder.AudioSource.VOICE_PERFORMANCE)
            }
            add(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            add(MediaRecorder.AudioSource.MIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                add(MediaRecorder.AudioSource.UNPROCESSED)
            }
        }

        for (source in sources) {
            val recorder = try {
                AudioRecord.Builder()
                    .setAudioSource(source)
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                            .build(),
                    )
                    .setBufferSizeInBytes(bufferBytes)
                    .build()
            } catch (exception: RuntimeException) {
                Log.w(LogTag, "Microphone source ${source.audioSourceLabel()} failed to build.", exception)
                null
            } ?: continue

            if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                Log.i(LogTag, "Selected microphone source ${source.audioSourceLabel()}.")
                return recorder
            }
            recorder.safeRelease()
        }

        throw IllegalStateException("Could not initialize the microphone.")
    }

    private fun AudioRecord.safeStop() {
        try {
            if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                stop()
            }
        } catch (_: IllegalStateException) {
            // Recorder may already be stopped by lifecycle disposal.
        }
    }

    private fun AudioRecord.safeRelease() {
        try {
            release()
        } catch (_: RuntimeException) {
            // Release is best-effort during lifecycle shutdown.
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
        const val SampleRate = 44_100
        const val FrameSize = 4_096
        const val FrameHopSize = 2_048
        const val ReadBufferSize = 1_024
    }
}
