package com.example.subzero.global

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Utility {

    /**
     * Accepts an ISO-8601 timestamp and returns a human-readable relative date string.
     * e.g. "Today", "Yesterday", "3 days ago", "Tomorrow", "In 5 days"
     */
    fun timeAgo(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

        val date = sdf.parse(isoString) ?: return ""

        val calDate = Calendar.getInstance().apply {
            time = this@apply.also { it.time = date }.time
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }

        val diffDays = ((today.timeInMillis - calDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        return when {
            diffDays == 0  -> "Today"
            diffDays == 1  -> "Yesterday"
            diffDays > 1   -> "$diffDays days ago"
            diffDays == -1 -> "Tomorrow"
            else           -> "In ${-diffDays} days"
        }
    }

    /**
     * Formats an ISO-8601 timestamp to "MMM yyyy" (e.g. "Apr 2026").
     */
    fun formatToMonthYear(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val inputFormat  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return try {
            val date = inputFormat.parse(input) ?: return ""
            outputFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }
}