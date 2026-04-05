package com.example.subzero.global

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.subzero.ProfileActivity.UserProfile
import com.example.subzero.SessionManager
import com.example.subzero.network.AlertResponse
import com.example.subzero.network.ApiClient
import com.example.subzero.network.SubscriptionResponse
import kotlinx.coroutines.launch

class ApiCalls {
    var util = Utility()

    data class response(
        val email: String?,
        val name: String?,
        val remindersEnabled: Boolean?,
        val createdAt: String?,
        val subscriptions: List<SubscriptionResponse>?,
        val numSubs: Int?,
        val reminders: List<AlertResponse>?
    )

    suspend fun loadProfile(cont: Context): response? {
        val token = SessionManager.getToken(cont) ?: return null

        return try {
            val profileResponse = ApiClient.instance.getProfile("Bearer $token")
            if (!profileResponse.isSuccessful) return null

            val body = profileResponse.body()
            val email = body?.email ?: "Not available"
            val name = body?.name ?: "John Doe"
            val remindersEnabled = body?.reminders_enabled == 1
            val memberSince = util.formatToMonthYear(body?.created_at)

            val subsResponse = ApiClient.instance.getSubscriptions("Bearer $token")
            val subscriptions: List<SubscriptionResponse> = if (subsResponse.isSuccessful) subsResponse.body() ?: emptyList() else emptyList()
            val numSubs = subscriptions.size

            val remResponse = ApiClient.instance.getReminders("Bearer $token")
            val reminders: List<AlertResponse> = if (remResponse.isSuccessful) remResponse.body() ?: emptyList() else emptyList()

            // debug
            Log.d("Thing", email + name + memberSince)

            response(email, name, remindersEnabled, memberSince, subscriptions, numSubs, reminders)

        } catch (e: Exception) {
            Log.e("PROFILE_ERROR", e.localizedMessage ?: "", e)
            Toast.makeText(cont, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}