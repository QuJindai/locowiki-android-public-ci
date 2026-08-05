package com.qujindai.locowiki.flashrecall.v2.qa

import android.content.Context
import com.qujindai.locowiki.flashrecall.v2.ui.FlashRecallUiState
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class QaDiagnosticsWriter(private val context: Context) {
    fun write(state: FlashRecallUiState): File {
        val root = File(context.getExternalFilesDir(null), "qa").also { it.mkdirs() }
        val output = File(root, "qa-diagnostics.zip")
        ZipOutputStream(output.outputStream().buffered()).use { zip ->
            put(zip, "state.json", stateJson(state).toString(2))
            put(zip, "utterances.jsonl", state.recentUtterances.joinToString("\n") { utterance ->
                JSONObject()
                    .put("utterance_id", utterance.utteranceId)
                    .put("speaker", utterance.speakerLabel)
                    .put("text", utterance.text)
                    .put("question_type", utterance.questionType.name)
                    .put("target_self_score", utterance.targetSelfScore)
                    .put("thread_id", utterance.threadId)
                    .toString()
            })
            put(zip, "question-thread.json", JSONObject()
                .put("thread_id", state.activeThread?.threadId ?: JSONObject.NULL)
                .put("initiator", state.activeThread?.initiatorLabel ?: JSONObject.NULL)
                .put("canonical_question", state.activeThread?.canonicalQuestion ?: JSONObject.NULL)
                .put("utterance_count", state.activeThread?.utteranceCount ?: 0)
                .toString(2))
            put(zip, "answer.json", JSONObject()
                .put("question", state.currentQueryText)
                .put("answer", state.answer?.answer ?: JSONObject.NULL)
                .put("evidence", state.answer?.evidence ?: JSONObject.NULL)
                .put("route", state.answer?.timing?.route ?: JSONObject.NULL)
                .put("end_to_end_ms", state.endToEndMs)
                .toString(2))
            put(zip, "device.json", JSONObject()
                .put("model", state.diagnostic?.model ?: JSONObject.NULL)
                .put("android", state.diagnostic?.androidVersion ?: JSONObject.NULL)
                .put("abi", state.diagnostic?.abi ?: JSONObject.NULL)
                .put("asr_model", state.diagnostic?.asrModel ?: JSONObject.NULL)
                .put("privacy_mode", true)
                .toString(2))
        }
        return output
    }

    private fun stateJson(state: FlashRecallUiState): JSONObject = JSONObject()
        .put("message", state.message)
        .put("speaker_mode", state.speakerMode.storageValue)
        .put("self_enrolled", state.speakerProfile.enrolled)
        .put("current_query", state.currentQueryText)
        .put("question_candidates", JSONArray(state.questionCandidates.map { it.text }))
        .put("utterance_count", state.recentUtterances.size)
        .put("archive_title", state.lastSessionSummary?.title ?: JSONObject.NULL)

    private fun put(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
}
