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
    
    
}
