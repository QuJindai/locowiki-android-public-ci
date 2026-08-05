package com.qujindai.locowiki.flashrecall.v2.qa

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavHeaderParserTest {
    @Test
    fun parse_acceptsPcm16Mono16Khz() {
        val pcm = shortArrayOf(-32768, 0, 16384, 32767)
        val bytes = wav(pcm, sampleRate = 16000, channels = 1, bits = 16)

        val result = WavPcmParser.parse(ByteArrayInputStream(bytes))

        assertArrayEquals(floatArrayOf(-1f, 0f, 0.5f, 32767f / 32768f), result, 0.0001f)
    }

    @Test
    fun parse_rejectsWrongSampleRate() {
        val bytes = wav(shortArrayOf(1, 2), sampleRate = 44100, channels = 1, bits = 16)

        assertThrows(IllegalArgumentException::class.java) {
            WavPcmParser.parse(ByteArrayInputStream(bytes))
        }
    }

    private fun wav(samples: ShortArray, sampleRate: Int, channels: Int, bits: Int): ByteArray {
        val data = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        samples.forEach(data::putShort)
        val payload = data.array()
        val out = ByteArrayOutputStream()
        fun ascii(value: String) = out.write(value.toByteArray(Charsets.US_ASCII))
        fun leInt(value: Int) = out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array())
        fun leShort(value: Int) = out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value.toShort()).array())
        ascii("RIFF"); leInt(36 + payload.size); ascii("WAVE")
        ascii("fmt "); leInt(16); leShort(1); leShort(channels); leInt(sampleRate)
        leInt(sampleRate * channels * bits / 8); leShort(channels * bits / 8); leShort(bits)
        ascii("data"); leInt(payload.size); out.write(payload)
        return out.toByteArray()
    }
}
