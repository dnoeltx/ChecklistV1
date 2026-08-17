package com.dnoel.checklistv1

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Due dates are stored as ISO-8601 date strings ("2026-08-17").
 *
 * Text rather than epoch millis because a due date is a *day*, not an instant:
 * ISO strings sort correctly with a plain SQL ORDER BY, are readable when
 * inspecting the database, and carry no timezone ambiguity.
 *
 * java.time is unavailable below API 26 and minSdk here is 24, so this uses
 * SimpleDateFormat. A new instance per call because SimpleDateFormat is not
 * thread-safe. Locale.US pins the numeric pattern regardless of device locale.
 */
fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
