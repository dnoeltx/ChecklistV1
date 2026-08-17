package com.dnoel.checklistv1

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The date picker reports UTC midnight. Reading that back in a local timezone
 * shifts the date by a day for anyone behind UTC — including the user's own
 * America/Chicago — so these conversions are pinned to UTC and tested.
 */
class DateUtilsTest {

    // 2026-08-18T00:00:00Z
    private val aug18UtcMidnight = 1787011200000L

    @Test
    fun `isoFromUtcMillis reads the picker's UTC midnight as that same day`() {
        assertEquals("2026-08-18", isoFromUtcMillis(aug18UtcMidnight))
    }

    @Test
    fun `utcMillisFromIso is the inverse`() {
        assertEquals(aug18UtcMidnight, utcMillisFromIso("2026-08-18"))
    }

    @Test
    fun `round trip through the picker preserves the date`() {
        val original = "2026-12-25"
        val millis = utcMillisFromIso(original)!!
        assertEquals(original, isoFromUtcMillis(millis))
    }

    @Test
    fun `utcMillisFromIso returns null for unparseable input rather than throwing`() {
        assertNull(utcMillisFromIso("not-a-date"))
    }

    @Test
    fun `todayIso is a well formed ISO date`() {
        assertEquals(10, todayIso().length)
        assertEquals(2, todayIso().count { it == '-' })
    }

    @Test
    fun `formatIsoForDisplay shortens the date`() {
        assertEquals("Aug 18", formatIsoForDisplay("2026-08-18"))
    }

    @Test
    fun `formatIsoForDisplay falls back to the raw value when unparseable`() {
        assertEquals("garbage", formatIsoForDisplay("garbage"))
    }
}
