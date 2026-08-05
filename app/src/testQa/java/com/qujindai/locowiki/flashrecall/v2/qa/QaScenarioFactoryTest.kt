package com.qujindai.locowiki.flashrecall.v2.qa

import com.qujindai.locowiki.flashrecall.v2.domain.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QaScenarioFactoryTest {
    @Test
    fun fullMeetingScenario_containsSelfOtherQuestionThreadAndEvidence() {
        val state = QaScenarioFactory.fullMeetingScenario()

        assertTrue(state.speakerProfile.enrolled)
        assertTrue(state.recentUtterances.any { it.speakerLabel == "SELF" })
        assertTrue(state.recentUtterances.any { it.speakerLabel == "A" })
        assertTrue(state.questionCandidates.any { it.questionType == QuestionType.QUESTION_TO_SELF })
        assertEquals("A", state.activeThread?.initiatorLabel)
        assertTrue(state.activeThread?.canonicalQuestion.orEmpty().contains("最终合同价"))
        assertTrue(state.answer?.answer.orEmpty().contains("12.3"))
        assertTrue(state.answer?.evidence.orEmpty().contains("设备采购定点表"))
        assertEquals(5, state.lastSessionSummary?.utteranceCount)
    }
}
