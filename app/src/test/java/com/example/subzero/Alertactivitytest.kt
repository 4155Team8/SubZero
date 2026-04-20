package com.example.subzero

import android.content.Intent
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.subzero.network.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast
import retrofit2.Response

@Config(sdk = [34])
@RunWith(RobolectricTestRunner::class)
class Alertactivitytest {

    private lateinit var activity: AlertsActivity
    private lateinit var mockApiService: ApiService

    @Before
    fun setUp() {
        mockApiService = mock(ApiService::class.java)
        ApiClient.setInstanceForTesting(mockApiService)
        
        // Mock session
        SessionManager.saveSession(RuntimeEnvironment.getApplication(), "mock_token", 1, "test@example.com")

        activity = Robolectric.buildActivity(AlertsActivity::class.java)
            .create()
            .resume()
            .get()
    }

    @Test
    fun testNotificationToggle_UpdatesUI() {
        val card = activity.findViewById<CardView>(R.id.cardNotifications)
        val statusText = activity.findViewById<TextView>(R.id.tvNotificationStatus)

        // Initial state
        assertEquals("Notifications Enabled", statusText.text)

        // Click to disable
        card.performClick()
        assertEquals("Notifications Disabled", statusText.text)
        assertEquals(Color.DKGRAY, statusText.currentTextColor)

        // Click to enable
        card.performClick()
        assertEquals("Notifications Enabled", statusText.text)
        assertEquals(Color.WHITE, statusText.currentTextColor)
    }

    @Test
    fun testNavigationToInsights() {
        val navInsights = activity.findViewById<LinearLayout>(R.id.navInsights)
        navInsights.performClick()

        val expectedIntent = Intent(activity, InsightsActivity::class.java)
        val actualIntent = shadowOf(activity).nextStartedActivity
        assertEquals(expectedIntent.component, actualIntent.component)
    }

    @Test
    fun testNavigationToProfile() {
        val navProfile = activity.findViewById<LinearLayout>(R.id.navProfile)
        navProfile.performClick()

        val expectedIntent = Intent(activity, ProfileActivity::class.java)
        val actualIntent = shadowOf(activity).nextStartedActivity
        assertEquals(expectedIntent.component, actualIntent.component)
    }

    @Test
    fun testClearAll_ShowsDialog() {
        val btnClearAll = activity.findViewById<TextView>(R.id.btnClearAll)
        btnClearAll.performClick()

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertTrue(dialog.isShowing)
        
        val shadowDialog = shadowOf(dialog)
        assertEquals("Clear Notifications", shadowDialog.title)
        assertEquals("Are you sure you want to clear all notifications?", shadowDialog.message)
    }

    @Test
    fun testClearAll_PositiveButtonTriggersAPI() = runBlocking {
        // Setup mock response
        val mockResponse = MessageResponse("Success")
        `when`(mockApiService.clearAllAlerts(anyString())).thenReturn(Response.success(mockResponse))

        val btnClearAll = activity.findViewById<TextView>(R.id.btnClearAll)
        btnClearAll.performClick()

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick()

        // Robolectric handles coroutines on the main looper, but we need to let it flush
        shadowOf(android.os.Looper.getMainLooper()).idle()
        
        val latestToast = ShadowToast.getTextOfLatestToast()
        assertEquals("Alerts cleared", latestToast)
        
        val tvNewAlerts = activity.findViewById<TextView>(R.id.tvNewAlerts)
        assertEquals("0 new", tvNewAlerts.text)
    }
}
