package com.example.subzero

import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.subzero.network.RedundantGroupResponse
import com.example.subzero.network.RedundantSubscriptionItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [34])
@RunWith(RobolectricTestRunner::class)
class RedundantAlertsTest {

    private lateinit var activity: AlertsActivity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(AlertsActivity::class.java)
            .create()
            .resume()
            .get()
    }

    @Test
    fun testBuildRedundantWarning_ShowsDuplicateServiceName() {
        val groups = listOf(
            RedundantGroupResponse(
                subscription_name = "Spotify",
                subscriptions = listOf(
                    RedundantSubscriptionItem(1, "Spotify", "Music"),
                    RedundantSubscriptionItem(2, "Spotify", "Music")
                )
            )
        )

        val warningView = activity.buildRedundantWarning(groups)

        assertNotNull(warningView)

        val card = warningView as CardView
        val container = card.getChildAt(0) as LinearLayout

        val titleText = container.getChildAt(0) as TextView
        val subtitleText = container.getChildAt(1) as TextView
        val duplicateNameText = container.getChildAt(2) as TextView
        val countText = container.getChildAt(3) as TextView

        assertEquals("Duplicate subscriptions detected", titleText.text)
        assertEquals("You have multiple accounts for the same service:", subtitleText.text)
        assertEquals("• Spotify", duplicateNameText.text)
        assertEquals("2 accounts found", countText.text)
    }

    @Test
    fun testBuildRedundantWarning_ShowsMultipleDuplicateGroups() {
        val groups = listOf(
            RedundantGroupResponse(
                subscription_name = "Spotify",
                subscriptions = listOf(
                    RedundantSubscriptionItem(1, "Spotify", "Music"),
                    RedundantSubscriptionItem(2, "Spotify", "Music")
                )
            ),
            RedundantGroupResponse(
                subscription_name = "Netflix",
                subscriptions = listOf(
                    RedundantSubscriptionItem(3, "Netflix", "Streaming"),
                    RedundantSubscriptionItem(4, "Netflix", "Streaming"),
                    RedundantSubscriptionItem(5, "Netflix", "Streaming")
                )
            )
        )

        val warningView = activity.buildRedundantWarning(groups)

        val card = warningView as CardView
        val container = card.getChildAt(0) as LinearLayout

        val spotifyText = container.getChildAt(2) as TextView
        val spotifyCount = container.getChildAt(3) as TextView
        val netflixText = container.getChildAt(4) as TextView
        val netflixCount = container.getChildAt(5) as TextView

        assertEquals("• Spotify", spotifyText.text)
        assertEquals("2 accounts found", spotifyCount.text)
        assertEquals("• Netflix", netflixText.text)
        assertEquals("3 accounts found", netflixCount.text)
    }

    @Test
    fun testBuildRedundantWarning_SingleAccountEdgeCase() {
        val groups = listOf(
            RedundantGroupResponse(
                subscription_name = "Spotify",
                subscriptions = listOf(
                    RedundantSubscriptionItem(1, "Spotify", "Music")
                )
            )
        )

        val warningView = activity.buildRedundantWarning(groups)

        val card = warningView as CardView
        val container = card.getChildAt(0) as LinearLayout
        val countText = container.getChildAt(3) as TextView

        assertEquals("1 account found", countText.text)
    }

    @Test
    fun testBuildRedundantWarning_ViewCreatedSuccessfully() {
        val groups = listOf(
            RedundantGroupResponse(
                subscription_name = "Apple Music",
                subscriptions = listOf(
                    RedundantSubscriptionItem(1, "Apple Music", "Music"),
                    RedundantSubscriptionItem(2, "Apple Music", "Music")
                )
            )
        )

        val warningView = activity.buildRedundantWarning(groups)

        assertTrue(warningView is CardView)
    }
}

