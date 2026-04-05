package com.example.subzero.global

import androidx.lifecycle.lifecycleScope
import com.example.subzero.ProfileActivity.UserProfile
import com.example.subzero.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.subzero.network.AuthRepository
import com.example.subzero.network.AuthResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import com.example.subzero.databinding.ActivityProfileBinding
import com.example.subzero.network.ApiClient
import com.example.subzero.network.SubscriptionResponse
import android.util.Log

class Utility {


    // pulls date and tells how long ago (or in the future) it is
    public fun timeAgo(isoString: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

        val date = sdf.parse(isoString) ?: return ""

        val calDate = Calendar.getInstance().apply { time = date }
        val today = Calendar.getInstance()

        // resets time to midnight so that dates r accurate
        calDate.set(Calendar.HOUR_OF_DAY, 0)
        calDate.set(Calendar.MINUTE, 0)
        calDate.set(Calendar.SECOND, 0)
        calDate.set(Calendar.MILLISECOND, 0)

        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val diffMillis = today.timeInMillis - calDate.timeInMillis
        val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

        return when {
            diffDays == 0 -> "Today"
            diffDays == 1 -> "Yesterday"
            diffDays > 1 -> "$diffDays days ago"
            diffDays == -1 -> "Tomorrow"
            diffDays < -1 -> "In ${-diffDays} days"
            else -> ""
        }
    }
    // formats to Apr 2026 (example)
    fun formatToMonthYear(input: String?): String? {
        // Input format
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

        // Output format
        val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        return try {
            val date = inputFormat.parse(input) ?: return ""
            outputFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }
}