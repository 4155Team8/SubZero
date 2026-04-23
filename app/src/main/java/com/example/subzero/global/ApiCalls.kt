package com.example.subzero.global

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.subzero.ProfileActivity.UserProfile
import com.example.subzero.SessionManager
import com.example.subzero.network.*
import com.example.subzero.network.ApiClient
import com.example.subzero.network.NameRequest
import com.example.subzero.network.NameResponse
import com.example.subzero.network.ProfileResponse
import com.example.subzero.network.SubscriptionResponse
import kotlinx.coroutines.launch

class ApiCalls {
    var util = Utility()

    data class fullResponse(
        val email: String?,
        val name: String?,
        val remindersEnabled: Boolean?,
        val createdAt: String?,
        val subscriptions: List<SubscriptionResponse>?,
        val numSubs: Int?,
        val reminders: List<AlertResponse>?
    )

    suspend fun loadFull(cont: Context): fullResponse? {
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

            fullResponse(email, name, remindersEnabled, memberSince, subscriptions, numSubs, reminders)

        } catch (e: Exception) {
            Log.e("PROFILE_ERROR", e.localizedMessage ?: "", e)
            Toast.makeText(cont, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }
    suspend fun loadSubscriptions(cont: Context): List<SubscriptionResponse>? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val subResponse = ApiClient.instance.getSubscriptions("Bearer $token")
            subResponse.body()
        } catch (e: Exception) {
            Toast.makeText(cont, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }
    suspend fun loadProfile(cont: Context): ProfileResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val profResponse = ApiClient.instance.getProfile("Bearer $token")
            profResponse.body()
        } catch (e: Exception) {
            Toast.makeText(cont, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }
    suspend fun loadReminders(cont: Context): List<AlertResponse>? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val alertResponse = ApiClient.instance.getReminders("Bearer $token")
            alertResponse.body()
        } catch (e: Exception) {
            Toast.makeText(cont, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }
    suspend fun updateName(cont: Context, name: String?): NameResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val nameResponse = ApiClient.instance.changeName("Bearer $token", NameRequest(name))
            if (nameResponse.isSuccessful) nameResponse.body() else null
        } catch (e: Exception) {
            Log.e("Error", e.localizedMessage ?: "", e)
            null
        }
    }

    suspend fun updateEmail(cont: Context, email: String?): newEmailResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val emailResponse = ApiClient.instance.newEmail("Bearer $token", newEmailRequest(email))
            if (emailResponse.isSuccessful) emailResponse.body() else null
        } catch (e: Exception) {
            Log.e("Error", e.localizedMessage ?: "", e)
            null
        }
    }
    suspend fun updatePassword(cont: Context, password: String?): newPasswordResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val passResponse = ApiClient.instance.newPassword("Bearer $token", newPasswordRequest(password))
            if (passResponse.isSuccessful) passResponse.body() else null
        } catch (e: Exception) {
            Log.e("Error", e.localizedMessage ?: "", e)
            null
        }
    }

    suspend fun deleteAccount(cont: Context): deleteAccResponse? {
        val token = SessionManager.getToken((cont)) ?: return null
        return try {
            val delRes = ApiClient.instance.deleteAccount("Bearer $token")
            if (delRes.isSuccessful) delRes.body() else null
        } catch (e: Exception) {
            Log.e("Error", e.localizedMessage ?: "", e)
            null
    suspend fun clearAllAlerts(context: Context): Boolean {
        val token = SessionManager.getToken(context) ?: return false
        return try {
            val response = ApiClient.instance.clearAllAlerts("Bearer $token")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("API_ERROR", "Failed to delete alerts", e)
            false
        }
    }

}