package com.qujindai.locowiki.flashrecall.v2.qa

import org.json.JSONObject

data class ModelSmokeResult(
    val asrReady: Boolean,
    val transcript: String,
    val speakerReady: Boolean,
    val embeddingSize: Int,
    val sampleCount: Int,
    val error: String,
) {
    fun toJson(): String = JSONObject()
        .put("asr_ready", asrReady)
        .put("transcript", transcript)
        .put("speaker_ready", speakerReady)
        .put("embedding_size", embeddingSize)
        .put("sample_count", sampleCount)
        .put("error", error)
        .toString(2)
}
