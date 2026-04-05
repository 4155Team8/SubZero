package com.example.subzero
import com.example.subzero.R
import org.junit.Test
import android.content.Intent
import android.graphics.Color
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import android.graphics.drawable.ColorDrawable
import org.robolectric.annotation.Config

@Config(sdk=[34])
@RunWith(RobolectricTestRunner::class)
class AlertsActivityTest {

    private lateinit var activity: AlertsActivity

    @Before
    fun setUp(){
        activity = Robolectric.buildActivity(AlertsActivity::class.java).create().resume().get()
    }
    @Test
    fun testNotificationToggle_UpdatesUI(){
        val card = activity.findViewById<CardView>(R.id.cardNotifications)
        val statusText = activity.findViewById<TextView>(R.id.tvNotificationStatus)

        //Initial check
        assertEquals("Notifications Enabled", statusText.text)

        //First click -- Toggle to Disable
        card.performClick()
        assertEquals("Notifications Disabled", statusText.text)
        assertEquals(Color.DKGRAY, statusText.currentTextColor)

        //Second click -- Toggle to Enable again
        card.performClick()
        assertEquals("Notifications Enabled", statusText.text)
        assertEquals(Color.WHITE, statusText.currentTextColor)
    }

    @Test
    fun testNavigationToInsights(){
        val navInsights = activity.findViewById<android.widget.LinearLayout>(R.id.navInsights)
        navInsights.performClick()

        val expectedIntent =  Intent(activity, InsightsActivity::class.java)
        val actualIntent = shadowOf(activity).nextStartedActivity
        assertEquals(expectedIntent.component, actualIntent.component)
    }

    @Test
    fun testNavigationToProfile(){
        val navProfile = activity.findViewById<android.widget.LinearLayout>(R.id.navProfile)
        navProfile.performClick()

        val expectedIntent =  Intent(activity, ProfileActivity::class.java)
        val actualIntent = shadowOf(activity).nextStartedActivity
        assertEquals(expectedIntent.component, actualIntent.component)
    }
    @Test
    fun testInitialNotificationState(){
        val statusText = activity.findViewById<TextView>(R.id.tvNotificationStatus)

        assertEquals("Notifications Enabled", statusText.text)
        assertEquals(Color.WHITE, statusText.currentTextColor)
    }
    @Test
    fun testNotificationToggle_MultipleClicks(){
        val card = activity.findViewById<CardView>(R.id.cardNotifications)
        val statusText = activity.findViewById<TextView>(R.id.tvNotificationStatus)

        repeat(5){
            card.performClick()
        }
        //odd number of clicks --> Disabled
        assertEquals("Notifications Disabled", statusText.text)

    }


}