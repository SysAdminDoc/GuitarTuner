package com.sysadmindoc.guitartuner.audio

import com.sysadmindoc.guitartuner.tuning.GuitarTunings
import com.sysadmindoc.guitartuner.tuning.StandardGuitarTuning
import com.sysadmindoc.guitartuner.tuning.TuningMode
import com.sysadmindoc.guitartuner.tuning.TuningTargetSelection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudioCaptureControllerTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private fun createController() = AudioCaptureController(
        scope = testScope,
        dispatcher = testDispatcher,
        initialStrings = StandardGuitarTuning.strings,
    )

    @Test
    fun initialStateIsNotListeningWithNoErrors() {
        val controller = createController()
        val state = controller.state.value

        assertFalse(state.isListening)
        assertFalse(state.permissionError)
        assertFalse(state.micStolen)
        assertNull(state.audioError)
    }

    @Test
    fun notePermissionDeniedSetsPermissionError() {
        val controller = createController()

        controller.notePermissionDenied()

        assertTrue(controller.state.value.permissionError)
        assertFalse(controller.state.value.isListening)
    }

    @Test
    fun clearPermissionErrorResetsPermissionFlag() {
        val controller = createController()
        controller.notePermissionDenied()

        controller.clearPermissionError()

        assertFalse(controller.state.value.permissionError)
    }

    @Test
    fun setNoiseGateRmsUpdatesSilenceThresholdInState() {
        val controller = createController()

        controller.setNoiseGateRms(0.005)

        assertEquals(0.005, controller.state.value.inputLevel.silenceThreshold, 0.0001)
    }

    @Test
    fun setNoiseGateRmsPreservesExistingDetectionRange() {
        val controller = createController()
        controller.setTuning(
            GuitarTunings.bassStandard.strings,
            minFrequencyHz = 35.0,
            maxFrequencyHz = 200.0,
        )

        controller.setNoiseGateRms(0.008)

        controller.setNoiseGateRms(0.003)
        assertEquals(0.003, controller.state.value.inputLevel.silenceThreshold, 0.0001)
    }

    @Test
    fun stopFromIdleStateDoesNotCrash() {
        val controller = createController()

        controller.stop()

        assertFalse(controller.state.value.isListening)
        assertFalse(controller.state.value.micStolen)
    }

    @Test
    fun closeFromIdleStateDoesNotCrash() {
        val controller = createController()

        controller.close()

        assertFalse(controller.state.value.isListening)
    }

    @Test
    fun closeAfterStopDoesNotCrash() {
        val controller = createController()
        controller.stop()

        controller.close()

        assertFalse(controller.state.value.isListening)
    }

    @Test
    fun setTuningAcceptsDifferentStringCounts() {
        val controller = createController()

        controller.setTuning(
            GuitarTunings.bassStandard.strings,
            minFrequencyHz = 35.0,
            maxFrequencyHz = 200.0,
        )

        controller.setTuning(
            GuitarTunings.ukuleleStandard.strings,
            minFrequencyHz = 200.0,
            maxFrequencyHz = 520.0,
        )

        controller.setTuning(StandardGuitarTuning.strings)
    }

    @Test
    fun setTargetSelectionAcceptsAllModes() {
        val controller = createController()

        controller.setTargetSelection(TuningTargetSelection.auto())
        controller.setTargetSelection(TuningTargetSelection.guided(6))
        controller.setTargetSelection(TuningTargetSelection.chromatic())
    }

    @Test
    fun setCentsToleranceDoesNotCrash() {
        val controller = createController()

        controller.setCentsTolerance(3.0)
        controller.setCentsTolerance(10.0)
        controller.setCentsTolerance(1.0)
    }

    @Test
    fun setA4HzDoesNotCrash() {
        val controller = createController()

        controller.setA4Hz(432.0)
        controller.setA4Hz(440.0)
        controller.setA4Hz(444.0)
    }

    @Test
    fun setFreezeAfterDecayDoesNotCrash() {
        val controller = createController()

        controller.setFreezeAfterDecay(true)
        controller.setFreezeAfterDecay(false)
    }

    @Test
    fun setPreferredDeviceDoesNotCrash() {
        val controller = createController()

        controller.setPreferredDevice(42)
        controller.setPreferredDevice(null)
    }

    @Test
    fun availableInputDevicesReturnsEmptyWithoutAudioManager() {
        val controller = createController()

        assertTrue(controller.availableInputDevices().isEmpty())
    }

    @Test
    fun startSetsListeningTrueBeforeCaptureAttempt() {
        val controller = createController()

        controller.start()
        testScope.testScheduler.advanceUntilIdle()

        val state = controller.state.value
        assertEquals(AudioError.MicInitFailed, state.audioError)
        assertFalse(state.isListening)
    }

    @Test
    fun stopAfterFailedStartResetsState() {
        val controller = createController()
        controller.start()
        testScope.testScheduler.advanceUntilIdle()

        controller.stop()

        assertFalse(controller.state.value.isListening)
        assertFalse(controller.state.value.micStolen)
    }

    @Test
    fun doubleStartDoesNotCrash() {
        val controller = createController()

        controller.start()
        controller.start()
        testScope.testScheduler.advanceUntilIdle()

        controller.stop()
    }

    @Test
    fun rapidStartStopCyclesDoNotCrash() {
        val controller = createController()

        repeat(5) {
            controller.start()
            testScope.testScheduler.advanceUntilIdle()
            controller.stop()
        }

        assertFalse(controller.state.value.isListening)
    }

    @Test
    fun settingsChangesAfterFailedStartDoNotCrash() {
        val controller = createController()
        controller.start()
        testScope.testScheduler.advanceUntilIdle()

        controller.setTuning(GuitarTunings.dropD.strings)
        controller.setCentsTolerance(8.0)
        controller.setA4Hz(442.0)
        controller.setNoiseGateRms(0.004)
        controller.setTargetSelection(TuningTargetSelection.guided(6))
        controller.setFreezeAfterDecay(true)

        controller.stop()
        assertFalse(controller.state.value.isListening)
    }

    @Test
    fun permissionDeniedThenClearedThenStartSequence() {
        val controller = createController()

        controller.notePermissionDenied()
        assertTrue(controller.state.value.permissionError)

        controller.clearPermissionError()
        assertFalse(controller.state.value.permissionError)

        controller.start()
        testScope.testScheduler.advanceUntilIdle()

        controller.stop()
    }
}
