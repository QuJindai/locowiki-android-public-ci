package com.qujindai.locowiki.flashrecall.v2.qa

import android.content.Context
import com.k2fsa.sherpa.onnx.*
import com.qujindai.locowiki.flashrecall.v2.audio.SherpaAudioEngine
import com.qujindai.locowiki.flashrecall.v2.speaker.SherpaSpeakerEngine

class QaModelSmokeRunner(private val context: Context) {
    fun run(assetPath: String): ModelSmokeResult {
        val samples = runCatching { WavPcmReader(context).readMono16Khz(assetPath) }
            .getOrElse { return ModelSmokeResult(false, "", false, 0, 0, "WAV: ${it.message}") }
        var transcript = ""
        var asrReady = false
        var speakerReady = false
        var embeddingSize = 0
        val errors = mutableListOf<String>()

        runCatching {
            val recognizer = OnlineRecognizer(
                context.assets,
                OnlineRecognizerConfig(
                    featConfig = FeatureConfig(sampleRate = SherpaAudioEngine.SAMPLE_RATE, featureDim = 80),
                    modelConfig = OnlineModelConfig(
                        transducer = OnlineTransducerModelConfig(
                            encoder = "${SherpaAudioEngine.MODEL_NAME}/encoder-epoch-99-avg-1.int8.onnx",
                            decoder = "${SherpaAudioEngine.MODEL_NAME}/decoder-epoch-99-avg-1.onnx",
                            joiner = "${SherpaAudioEngine.MODEL_NAME}/joiner-epoch-99-avg-1.int8.onnx",
                        ),
                        tokens = "${SherpaAudioEngine.MODEL_NAME}/tokens.txt",
                        numThreads = 2,
                        provider = "cpu",
                        modelType = "zipformer",
                        modelingUnit = "cjkchar",
                    ),
                    enableEndpoint = false,
                    decodingMethod = "greedy_search",
                ),
            )
            try {
                val stream = recognizer.createStream("")
                try {
                    stream.acceptWaveform(samples, SherpaAudioEngine.SAMPLE_RATE)
                    stream.acceptWaveform(FloatArray(SherpaAudioEngine.SAMPLE_RATE), SherpaAudioEngine.SAMPLE_RATE)
                    stream.inputFinished()
                    while (recognizer.isReady(stream)) recognizer.decode(stream)
                    transcript = recognizer.getResult(stream).text.trim()
                    asrReady = true
                } finally {
                    stream.release()
                }
            } finally {
                recognizer.release()
            }
        }.onFailure { errors += "ASR: ${it.message ?: it.javaClass.simpleName}" }

        runCatching {
            SherpaSpeakerEngine(context).use { engine ->
                engine.initialize().getOrThrow()
                val embedding = requireNotNull(engine.compute(samples)) { "speaker embedding was null" }
                embeddingSize = embedding.size
                require(embedding.all { it.isFinite() }) { "speaker embedding contains non-finite values" }
                speakerReady = true
            }
        }.onFailure { errors += "Speaker: ${it.message ?: it.javaClass.simpleName}" }

        return ModelSmokeResult(
            asrReady = asrReady,
            transcript = transcript,
            speakerReady = speakerReady,
            embeddingSize = embeddingSize,
            sampleCount = samples.size,
            error = errors.joinToString(" | "),
        )
    }
}
