package com.qujindai.locowiki.flashrecall.v2.query

import com.qujindai.locowiki.flashrecall.v2.domain.FactRecord
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerFormatterTest {
    @Test fun formatsPriceWithEvidence() {
        val fact = FactRecord(1, 1, "DEMO-X1前视标定设备", "device", listOf("那套设备"), "price", "最终合同价", "12.3", 12.3, "万元", 2025, "DEMO-X1", "示例一厂", "设备标定", "含税含安装", "verified", "采购定点表", "第12页", "2026-07-20")
        val card = AnswerFormatter().format(fact)
        assertTrue(card.answer.contains("12.3万元"))
        assertTrue(card.evidence.contains("第12页"))
    }
}
