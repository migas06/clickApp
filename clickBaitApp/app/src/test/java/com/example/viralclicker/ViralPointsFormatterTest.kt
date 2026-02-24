package com.example.viralclicker

import com.example.viralclicker.domain.model.ViralPoints
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class ViralPointsFormatterTest {

    @Test fun `zero displays as 0`() = assertEquals("0", ViralPoints.fromLong(0).toDisplayString())
    @Test fun `999 displays as 999`() = assertEquals("999", ViralPoints.fromLong(999).toDisplayString())
    @Test fun `1000 displays as 1_00K`() = assertEquals("1.00K", ViralPoints.fromLong(1_000).toDisplayString())
    @Test fun `999900 displays as 999_90K`() = assertEquals("999.90K", ViralPoints(BigDecimal("999900")).toDisplayString())
    @Test fun `1M displays as 1_00M`() = assertEquals("1.00M", ViralPoints.fromLong(1_000_000).toDisplayString())
    @Test fun `1B displays as 1_00B`() = assertEquals("1.00B", ViralPoints.fromLong(1_000_000_000L).toDisplayString())
    @Test fun `1T displays as 1_00T`() = assertEquals("1.00T", ViralPoints(BigDecimal("1000000000000")).toDisplayString())
    @Test fun `1Qa displays as 1_00Qa`() = assertEquals("1.00Qa", ViralPoints(BigDecimal("1000000000000000")).toDisplayString())
}
