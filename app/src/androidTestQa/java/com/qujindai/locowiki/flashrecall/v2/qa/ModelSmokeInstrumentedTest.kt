package com.qujindai.locowiki.flashrecall.v2.qa

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ModelSmokeInstrumentedTest {
    @Test
    fun fixtureAudio_runsThroughPinnedAsrAndSpeakerModels() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val result = QaModelSmokeRunner(context).run("qa/question.wav")
        val qaDir = File(context.getExternalFilesDir(null), "qa").also { it.mkdirs() }
        File(qaDir, "model-smoke.json").writeText(result.toJson())

        assertTrue("ASR initialization failed: ${result.error}", result.asrReady)
        assertTrue("ASR returned no transcript: ${result.error}", result.transcript.isNotBlank())
        assertTrue("Speaker model failed: ${result.error}", result.speakerReady)
        assertTrue("Speaker embedding was empty", result.embeddingSize > 0)
    }
}
