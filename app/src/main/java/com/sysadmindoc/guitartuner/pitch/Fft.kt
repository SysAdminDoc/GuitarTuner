package com.sysadmindoc.guitartuner.pitch

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal object Fft {
    fun forwardReal(input: DoubleArray, n: Int): Pair<DoubleArray, DoubleArray> {
        val real = DoubleArray(n)
        input.copyInto(real, endIndex = minOf(input.size, n))
        val imag = DoubleArray(n)
        transform(real, imag, false)
        return real to imag
    }

    fun inverse(real: DoubleArray, imag: DoubleArray, n: Int): DoubleArray {
        val rCopy = real.copyOf(n)
        val iCopy = imag.copyOf(n)
        transform(rCopy, iCopy, true)
        for (i in 0 until n) {
            rCopy[i] /= n
        }
        return rCopy
    }

    private fun transform(real: DoubleArray, imag: DoubleArray, inverse: Boolean) {
        val n = real.size
        bitReversalPermutation(real, imag, n)
        var step = 2
        while (step <= n) {
            val halfStep = step / 2
            val angle = (if (inverse) 2.0 else -2.0) * PI / step
            val wReal = cos(angle)
            val wImag = sin(angle)
            var i = 0
            while (i < n) {
                var curReal = 1.0
                var curImag = 0.0
                for (j in 0 until halfStep) {
                    val evenIdx = i + j
                    val oddIdx = evenIdx + halfStep
                    val tReal = curReal * real[oddIdx] - curImag * imag[oddIdx]
                    val tImag = curReal * imag[oddIdx] + curImag * real[oddIdx]
                    real[oddIdx] = real[evenIdx] - tReal
                    imag[oddIdx] = imag[evenIdx] - tImag
                    real[evenIdx] += tReal
                    imag[evenIdx] += tImag
                    val nextReal = curReal * wReal - curImag * wImag
                    curImag = curReal * wImag + curImag * wReal
                    curReal = nextReal
                }
                i += step
            }
            step *= 2
        }
    }

    private fun bitReversalPermutation(real: DoubleArray, imag: DoubleArray, n: Int) {
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                var tmp = real[i]; real[i] = real[j]; real[j] = tmp
                tmp = imag[i]; imag[i] = imag[j]; imag[j] = tmp
            }
        }
    }

    fun nextPowerOfTwo(n: Int): Int {
        var v = n - 1
        v = v or (v shr 1)
        v = v or (v shr 2)
        v = v or (v shr 4)
        v = v or (v shr 8)
        v = v or (v shr 16)
        return v + 1
    }
}
