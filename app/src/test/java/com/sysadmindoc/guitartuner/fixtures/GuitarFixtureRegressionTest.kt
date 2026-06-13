package com.sysadmindoc.guitartuner.fixtures

import com.sysadmindoc.guitartuner.pitch.SignalStatus
import com.sysadmindoc.guitartuner.pitch.YinPitchDetector
import com.sysadmindoc.guitartuner.tuning.StandardGuitarTuning
import com.sysadmindoc.guitartuner.tuning.TuningAnalyzer
import com.sysadmindoc.guitartuner.tuning.TuningStatus
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuitarFixtureRegressionTest {
    private val detector = YinPitchDetector()
    private val analyzer = TuningAnalyzer(StandardGuitarTuning.strings)

    @Test
    fun localGuitarFixturesAreReadablePcm16WavFiles() {
        for (fixture in fixtureManifest()) {
            val audio = readPcm16Wav("guitar-fixtures/${fixture.file}")

            assertEquals("${fixture.file} sample rate", 44_100, audio.sampleRate)
            assertTrue("${fixture.file} should contain enough samples", audio.samples.size >= 16_384)
            assertTrue("${fixture.file} should not be silent", audio.samples.any { abs(it) > 0.05f })
        }
    }

    @Test
    fun tuningResultsMatchKnownGoodStandardTargets() {
        for (fixture in fixtureManifest()) {
            val audio = readPcm16Wav("guitar-fixtures/${fixture.file}")
            val estimate = detector.detect(audio.samples, audio.sampleRate)
            val measurement = analyzer.analyze(estimate)

            assertEquals("${fixture.file} signal status", SignalStatus.Detected, estimate.status)
            assertNotNull("${fixture.file} frequency", estimate.frequencyHz)
            assertTrue(
                "${fixture.file} expected ${fixture.expectedHz} Hz, got ${estimate.frequencyHz}",
                abs(requireNotNull(estimate.frequencyHz) - fixture.expectedHz) < 1.5,
            )
            assertEquals("${fixture.file} tuning status", TuningStatus.InTune, measurement.status)
            assertEquals("${fixture.file} string", fixture.stringNumber, measurement.target?.stringNumber)
            assertEquals("${fixture.file} name", fixture.expectedName, measurement.target?.name)
            assertEquals("${fixture.file} pitch", fixture.expectedPitch, measurement.target?.scientificPitch)
            assertTrue("${fixture.file} cents", abs(measurement.cents ?: 99.0) <= 5.0)
        }
    }

    @Test
    fun liveSizedFramesDetectKnownGoodStandardTargets() {
        for (fixture in fixtureManifest()) {
            val audio = readPcm16Wav("guitar-fixtures/${fixture.file}")
            val detectedFrames = audio.samples
                .asIterable()
                .chunked(LiveFrameSize)
                .asSequence()
                .map { it.toFloatArray() }
                .map { detector.detect(it, audio.sampleRate) }
                .map { estimate -> estimate to analyzer.analyze(estimate) }
                .filter { (_, measurement) -> measurement.status != TuningStatus.WaitingForSignal }
                .toList()

            assertTrue("${fixture.file} should produce live-sized detections", detectedFrames.isNotEmpty())
            assertTrue(
                "${fixture.file} should identify string ${fixture.stringNumber} in at least one live frame",
                detectedFrames.any { (_, measurement) ->
                    measurement.target?.stringNumber == fixture.stringNumber &&
                        abs((measurement.frequencyHz ?: 0.0) - fixture.expectedHz) < 1.5
                },
            )
        }
    }

    private fun fixtureManifest(): List<GuitarFixture> {
        val text = resourceBytes("guitar-fixtures/standard-fixtures.csv").decodeToString()
        return text.lineSequence()
            .drop(1)
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.split(",")
                require(parts.size == 5) { "Invalid fixture manifest line: $line" }
                GuitarFixture(
                    file = parts[0],
                    stringNumber = parts[1].toInt(),
                    expectedName = parts[2],
                    expectedPitch = parts[3],
                    expectedHz = parts[4].toDouble(),
                )
            }
            .toList()
    }

    private fun readPcm16Wav(path: String): WavAudio {
        val bytes = resourceBytes(path)
        var offset = 0

        fun readAscii(count: Int): String {
            val value = bytes.decodeToString(offset, offset + count)
            offset += count
            return value
        }

        fun readIntLe(): Int {
            val value = (bytes[offset].toInt() and 0xff) or
                ((bytes[offset + 1].toInt() and 0xff) shl 8) or
                ((bytes[offset + 2].toInt() and 0xff) shl 16) or
                ((bytes[offset + 3].toInt() and 0xff) shl 24)
            offset += 4
            return value
        }

        fun readShortLe(): Int {
            val value = (bytes[offset].toInt() and 0xff) or ((bytes[offset + 1].toInt() and 0xff) shl 8)
            offset += 2
            return if (value >= 0x8000) value - 0x10000 else value
        }

        require(readAscii(4) == "RIFF") { "$path missing RIFF header" }
        readIntLe()
        require(readAscii(4) == "WAVE") { "$path missing WAVE header" }

        var audioFormat = 0
        var channels = 0
        var sampleRate = 0
        var bitsPerSample = 0
        var dataOffset = -1
        var dataSize = 0

        while (offset + 8 <= bytes.size) {
            val chunkId = readAscii(4)
            val chunkSize = readIntLe()
            val chunkStart = offset
            when (chunkId) {
                "fmt " -> {
                    audioFormat = readShortLe()
                    channels = readShortLe()
                    sampleRate = readIntLe()
                    readIntLe()
                    readShortLe()
                    bitsPerSample = readShortLe()
                }
                "data" -> {
                    dataOffset = offset
                    dataSize = chunkSize
                }
            }
            offset = chunkStart + chunkSize + (chunkSize and 1)
        }

        require(audioFormat == 1) { "$path must be PCM" }
        require(channels == 1) { "$path must be mono" }
        require(bitsPerSample == 16) { "$path must be 16-bit" }
        require(dataOffset >= 0) { "$path missing data chunk" }

        offset = dataOffset
        val sampleCount = dataSize / 2
        val samples = FloatArray(sampleCount) {
            (readShortLe() / 32768.0f).coerceIn(-1.0f, 1.0f)
        }
        return WavAudio(sampleRate = sampleRate, samples = samples)
    }

    private fun resourceBytes(path: String): ByteArray {
        val stream = requireNotNull(javaClass.classLoader?.getResourceAsStream(path)) {
            "Missing test resource: $path"
        }
        return stream.use { it.readBytes() }
    }
}

private data class GuitarFixture(
    val file: String,
    val stringNumber: Int,
    val expectedName: String,
    val expectedPitch: String,
    val expectedHz: Double,
)

private data class WavAudio(
    val sampleRate: Int,
    val samples: FloatArray,
)

private const val LiveFrameSize = 4_096
