package com.qujindai.locowiki.flashrecall.v2.qa

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class QaScenarioComposeTest {
    @get:Rule
    val rule = createAndroidComposeRule<QaHarnessActivity>()

    @Test
    fun fullScenario_rendersStableQaEvidenceAndScreenshots() {
        rule.onNodeWithText("LocoWiki 极速召回 0.3.1-qa").performScrollTo().assertIsDisplayed()
        assertTrue(ScreenshotWriter.capture(rule.onRoot(), "01-top").length() > 0)

        rule.onNodeWithText("说话人识别（可选）").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("SELF已登记").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("A：2段｜置信 0.91").performScrollTo().assertIsDisplayed()
        assertTrue(ScreenshotWriter.capture(rule.onRoot(), "02-speakers").length() > 0)

        rule.onNodeWithText("当前连续问题线程").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("发起人：A｜发言 2 条｜状态 OPEN").performScrollTo().assertIsDisplayed()
        assertTrue(ScreenshotWriter.capture(rule.onRoot(), "03-thread").length() > 0)

        rule.onNodeWithText("本次查询问题").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("下一问题").performClick()
        rule.onNodeWithText("这个价格是哪一年的？").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("查询当前显示问题").performClick()

        rule.onNodeWithText("答案和证据").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("2025年DEMO-X1前视标定设备最终合同价为12.3万元。").performScrollTo().assertIsDisplayed()
        assertTrue(ScreenshotWriter.capture(rule.onRoot(), "04-answer").length() > 0)

        rule.onNodeWithText("完整会议归档").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("DEMO-X1-设备标定-在线QA").performScrollTo().assertIsDisplayed()
        assertTrue(ScreenshotWriter.capture(rule.onRoot(), "05-archive").length() > 0)
    }
}
