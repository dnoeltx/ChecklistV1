package com.dnoel.checklistv1

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Due dates are stored as ISO-8601 date strings ("2026-08-17").
 *
 * Text rather than epoch millis because a due date is a *day*, not an instant:
 * ISO strings sort correctly with a plain SQL ORDER BY, are readable when
 * inspecting the database, and carry no timezone ambiguity.
 *
 * java.time is unavailable below API 26 and minSdk here is 24, so these use
 * SimpleDateFormat. A new instance per call because SimpleDateFormat is not
 * thread-safe, and Locale.US so the numeric pattern is stable across devices.
 */
private const val ISO_PATTERN = "yyyy-MM-dd"

private fun isoFormatter(zone: TimeZone) =
    SimpleDateFormat(ISO_PATTERN, Locale.US).apply { timeZone = zone }

/** Today in the device's local timezone. */
fun todayIso(): String = isoFormatter(TimeZone.getDefault()).format(Date())

/**
 * Material3's date picker reports the selection as UTC midnight, so it must be
 * read back in UTC. Formatting it locally would shift the date by a day for
 * anyone west of Greenwich — the exact bug that storing dates as instants
 * invites.
 */
fun isoFromUtcMillis(millis: Long): String = isoFormatter(utc()).format(Date(millis))

/** Inverse of [isoFromUtcMillis], for seeding the picker with the current value. */
fun utcMillisFromIso(iso: String): Long? = try {
    isoFormatter(utc()).parse(iso)?.time
} catch (e: ParseException) {
    null
}

/** "2026-08-18" -> "Aug 18". Falls back to the raw value if it cannot be parsed. */
fun formatIsoForDisplay(iso: String): String = try {
    val parsed = isoFormatter(utc()).parse(iso)
    if (parsed == null) iso
    else SimpleDateFormat("MMM d", Locale.getDefault())
        .apply { timeZone = utc() }
        .format(parsed)
} catch (e: ParseException) {
    iso
}

private fun utc() = TimeZone.getTimeZone("UTC")
