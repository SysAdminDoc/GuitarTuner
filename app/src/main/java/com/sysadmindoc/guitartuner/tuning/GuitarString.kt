package com.sysadmindoc.guitartuner.tuning

data class GuitarString(
    val stringNumber: Int,
    val name: String,
    val scientificPitch: String,
    val frequencyHz: Double,
) {
    init {
        require(frequencyHz.isFinite() && frequencyHz > 0.0) {
            "GuitarString frequency must be a positive finite value."
        }
    }

    val displayName: String = "$stringNumber - $scientificPitch"
}
