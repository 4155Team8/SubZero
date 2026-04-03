package com.example.subzero

import com.example.subzero.network.SubscriptionResponse
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InsightsActivityTest {

    // access normaliseToMonthly
    private val activity = object : InsightsActivity() {
        fun testNormalise(cost: Double, cycle: String) = normaliseToMonthly(cost, cycle)
    }

    // normalize func

    @Test
    fun normaliseToMonthlyReturnsCostTimes30ForDaily() {
        val result = activity.testNormalise(1.0, "Daily")
        assertEquals(30.0, result, 0.001)
    }

    @Test
    fun normaliseToMonthlyReturnsCostTimes433ForWeekly() {
        val result = activity.testNormalise(10.0, "Weekly")
        assertEquals(43.3, result, 0.001)
    }

    @Test
    fun normaliseToMonthlyReturnsCostTimes217ForBiweekly() {
        val result = activity.testNormalise(10.0, "Biweekly")
        assertEquals(21.7, result, 0.001)
    }

    @Test
    fun normaliseToMonthlyReturnsCostAsisForMonthly() {
        val result = activity.testNormalise(15.99, "Monthly (30 days)")
        assertEquals(15.99, result, 0.001)
    }

    @Test
    fun normaliseToMonthlyReturnsCostDividedBy12ForYearly() {
        val result = activity.testNormalise(120.0, "Yearly")
        assertEquals(10.0, result, 0.001)
    }

    @Test
    fun normaliseToMonthlyReturnsCostAsisForCustom() {
        val result = activity.testNormalise(9.99, "Every 2 months")
        assertEquals(9.99, result, 0.001)
    }

    @Test
    fun normaliseToMonthlyIsCaseInsensitive() {
        assertEquals(
            activity.testNormalise(10.0, "monthly (30 days)"),
            activity.testNormalise(10.0, "Monthly (30 days)"),
            0.001
        )
    }
}