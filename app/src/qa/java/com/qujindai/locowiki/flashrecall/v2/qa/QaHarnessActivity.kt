package com.qujindai.locowiki.flashrecall.v2.qa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qujindai.locowiki.flashrecall.v2.domain.MeetingContext
import com.qujindai.locowiki.flashrecall.v2.domain.QuestionType
import com.qujindai.locowiki.flashrecall.v2.domain.RecordMode
import com.qujindai.locowiki.flashrecall.v2.domain.SpeakerMode
import com.qujindai.locowiki.flashrecall.v2.ui.FlashRecallScreen
import com.qujindai.locowiki.flashrecall.v2.ui.FlashRecallUiState

class QaHarnessActivity : ComponentActivity() {
    private var uiState by mutableStateOf(QaScenarioFactory.fullMeetingScenario())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching { QaDiagnosticsWriter(this).write(uiState) }
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                FlashRecallScreen(
                    state = uiState,
                    onContextChange = ::updateContext,
                    onTypedQuestionChange = { uiState = uiState.copy(typedQuestion = it) },
                    onCurrentQueryTextChange = { uiState = uiState.copy(currentQueryText = it) },
                    onRecordModeChange = { uiState = uiState.copy(recordMode = it) },
                    onSpeakerModeChange = { uiState = uiState.copy(speakerMode = it) },
                    onCaptureSelfSample = {},
                    onDeleteSelfProfile = {},
                    onRelabelSpeaker = ::cycleSpeaker,
                    onReclusterSpeakers = { uiState = uiState.copy(message = "在线QA：会后重聚类按钮已触发") },
                    onStartMeeting = { uiState = uiState.copy(listening = true, message = "在线QA：会议模式已启动") },
                    onStopMeeting = { uiState = uiState.copy(listening = false, message = "在线QA：会议已停止并归档") },
                    onQueryTyped = { uiState = uiState.copy(currentQueryText = uiState.typedQuestion) },
                    onQueryLast = { selectCandidate(uiState.questionCandidates.lastIndex) },
                    onQuerySelected = { uiState = uiState.copy(message = "在线QA：当前显示问题已查询") },
                    onToggleUtterance = ::toggleUtterance,
                    onSelectOnly = ::selectOnly,
                    onMoveCandidate = ::moveCandidate,
                    onOpenImport = {},
                    onConfirmImport = {},
                    onCancelImport = {},
                    onResetSeed = {},
                    onExportLatency = {},
                    onExportMeeting = { runCatching { QaDiagnosticsWriter(this).write(uiState) } },
                )
            }
        }
    }

    private fun updateContext(context: MeetingContext) {
        uiState = uiState.copy(context = context)
    }

    private fun moveCandidate(delta: Int) {
        val candidates = uiState.questionCandidates
        if (candidates.isEmpty()) return
        val selected = uiState.selectedUtteranceIds.firstOrNull()
        val current = candidates.indexOfFirst { it.utteranceId == selected }.takeIf { it >= 0 } ?: 0
        selectCandidate((current + delta).coerceIn(0, candidates.lastIndex))
    }

    private fun selectCandidate(index: Int) {
        val item = uiState.questionCandidates.getOrNull(index) ?: return
        uiState = uiState.copy(
            selectedUtteranceIds = setOf(item.utteranceId),
            currentQueryText = item.text,
        )
    }

    private fun selectOnly(id: String) {
        val item = uiState.recentUtterances.firstOrNull { it.utteranceId == id } ?: return
        uiState = uiState.copy(selectedUtteranceIds = setOf(id), currentQueryText = item.text)
    }

    private fun toggleUtterance(id: String) {
        val selected = uiState.selectedUtteranceIds.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
        val merged = uiState.recentUtterances
            .filter { it.utteranceId in selected }
            .sortedBy { it.startMs }
            .joinToString(" ") { it.text }
        uiState = uiState.copy(selectedUtteranceIds = selected, currentQueryText = merged)
    }

    private fun cycleSpeaker(id: String) {
        val labels = listOf("UNKNOWN", "SELF", "A", "B", "C", "D")
        val updated = uiState.recentUtterances.map { item ->
            if (item.utteranceId != id) item else {
                val current = labels.indexOf(item.speakerLabel).takeIf { it >= 0 } ?: 0
                val next = labels[(current + 1) % labels.size]
                item.copy(
                    speakerId = if (next == "SELF") "SELF" else "qa_$next",
                    speakerLabel = next,
                    speakerConfidence = 1f,
                    speakerManual = true,
                    questionType = if (next == "SELF" && item.isQuestion) QuestionType.SELF_QUESTION else item.questionType,
                )
            }
        }
        uiState = uiState.copy(recentUtterances = updated)
    }
}
