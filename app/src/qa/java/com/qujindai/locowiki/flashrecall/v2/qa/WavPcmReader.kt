package com.qujindai.locowiki.flashrecall.v2.qa

import android.content.Context
import java.io.EOFException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavPcmParser {
    fun parse(input: InputStream): FloatArray {
        val riff = input.readExact(12)
        require(String(riff, 0, 4, Charsets.US_ASCII) == "RIFF") { "Not a RIFF file" }
        require(String(riff, 8, 4, Charsets.US_ASCII) == "WAVE") { "Not a WAVE file" }

        var audioFormat = -1
        var channels = -1
        var sampleRate = -1
        var bitsPerSample = -1
        var data: ByteArray? = null

        while (true) {
            val header = input.readAtMost(8)
            if (header.isEmpty()) break
            if (header.size != 8) throw EOFException("Truncated WAV chunk header")
            val chunkId = String(header, 0, 4, Charsets.US_ASCII)
            val chunkSize = ByteBuffer.wrap(header, 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
            require(chunkSize >= 0) { "Invalid WAV chunk size" }
            val body = input.readExact(chunkSize)
            if (chunkSize % 2 == 1) input.read()
            when (chunkId) {
                "fmt " -> {
                    require(body.size >= 16) { "Invalid fmt chunk" }
                    val fmt = ByteBuffer.wrap(body).order(ByteOrder.LITTLE_ENDIAN)
                    audioFormat = fmt.short.toInt() and 0xffff
                    channels = fmt.short.toInt() and 0xffff
                    sampleRate = fmt.int
                    fmt.int
                    fmt.short
                    bitsPerSample = fmt.short.toInt() and 0xffff
                }
                "data" -> data = body
            }
        }

        require(audioFormat == 1) { "Only PCM WAV is supported" }
        require(channels == 1) { "Only mono WAV is supported" }
        require(sampleRate == 16000) { "Expected 16000 Hz WAV" }
        require(bitsPerSample == 16) { "Expected PCM16 WAV" }
        val pcm = requireNotNull(data) { "WAV data chunk is missing" }
        require(pcm.size % 2 == 0) { "PCM16 data length must be even" }
        val shorts = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        return FloatArray(shorts.remaining()) { shorts.get() / 32768f }
    }

    private fun InputStream.readExact(size: Int): ByteArray {
        val output = ByteArray(size)
        var offset = 0
        while (offset < size) {
            val count = read(output, offset, size - offset)
            if (count < 0) throw EOFException("Unexpected end of WAV file")
            offset += count
        }
        return output
    }

    private fun InputStream.readAtMost(size: Int): ByteArray {
        val output = ByteArray(size)
        var offset = 0
        while (offset < size) {
            val count = read(output, offset, size - offset)
            if (count < 0) break
            offset += count
        }
        return if (offset == size) output else output.copyOf(offset)
    }
}

class WavPcmReader(private val context: Context) {
    fun readMono16Khz(assetPath: String): FloatArray =
        context.assets.open(assetPath).buffered().use(WavPcmParser::parse)
}
