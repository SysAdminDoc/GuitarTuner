package com.sysadmindoc.guitartuner.audio

import com.sysadmindoc.guitartuner.pitch.PitchEstimate
import com.sysadmindoc.guitartuner.tuning.TuningMeasurement

data class TunerSessionState(
    val isListening: Boolean = false,
    val isFrozen: Boolean = false,
    val pitchEstimate: PitchEstimate = PitchEstimate.silence(),
    val measurement: TuningMeasurement = TuningMeasurement.waiting(),
    val errorMessage: String? = null,
)
