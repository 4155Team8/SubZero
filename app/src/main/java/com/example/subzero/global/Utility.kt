package com.example.subzero.global

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

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
     * Formats a "YYYY-MM-DD" date string to a readable form (e.g. "Apr 15, 2026").
     * Safely takes only the first 10 chars so full ISO timestamps also work.
     */
    fun formatRenewalDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "—"
        return try {
            val sdf    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date   = sdf.parse(dateStr.take(10)) ?: return dateStr
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    /**
     * Returns true if [dateStr] ("YYYY-MM-DD" or ISO timestamp) falls in the current
     * calendar month or any earlier month. Used by Insights to exclude future subscriptions.
     * Returns true on null/blank/parse-error so those items are included rather than silently dropped.
     */
    fun isCurrentMonthOrEarlier(dateStr: String?): Boolean {
        if (dateStr.isNullOrBlank()) return true
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(dateStr.take(10)) ?: return true
            val sub = Calendar.getInstance().apply { time = date }
            val now = Calendar.getInstance()
            sub.get(Calendar.YEAR) < now.get(Calendar.YEAR) ||
            (sub.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
             sub.get(Calendar.MONTH) <= now.get(Calendar.MONTH))
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Converts a subscription cost to its normalized monthly equivalent.
     * Matches the billing-cycle strings used by the backend's billingcycle table.
     * A null/unrecognised cycle is treated as monthly (no change).
     */
    fun normaliseToMonthly(cost: Double, billingCycle: String?): Double {
        if (billingCycle == null) return cost
        return when {
            billingCycle.contains("daily",    ignoreCase = true) -> cost * 30
            billingCycle.contains("biweekly", ignoreCase = true) -> cost * 2.17
            billingCycle.contains("weekly",   ignoreCase = true) -> cost * 4.33
            billingCycle.contains("month",    ignoreCase = true) -> cost
            billingCycle.contains("year",     ignoreCase = true) -> cost / 12
            else -> cost
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