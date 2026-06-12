package com.sysadmindoc.guitartuner.tuning

data class GuitarString(
    val stringNumber: Int,
    val name: String,
    val scientificPitch: String,
    val frequencyHz: Double,
) {
    val displayName: String = "$stringNumber - $scientificPitch"
}
