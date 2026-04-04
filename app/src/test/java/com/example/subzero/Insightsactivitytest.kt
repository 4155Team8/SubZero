package com.example.subzero

import com.example.subzero.network.SubscriptionResponse
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class InsightsActivityTest {

    // Static function to test normaliseToMonthly logic without needing Activity context
    companion object {
        fun normaliseToMonthly(cost: Double, billingCycle: String): Double {
            return when (billingCycle.lowercase()) {
                "daily" -> cost * 30
                "weekly" -> cost * 4.33
                "biweekly" -> cost * 2.17
                "monthly", "monthly (30 days)" -> cost
                "yearly" -> cost / 12
                else -> cost
            }
        }
    }

    // ------------------- Tests for Billing Cycle Normalization -------------------

    @Test
    fun `normaliseToMonthly returns cost times 30 for daily`() {
        val result = normaliseToMonthly(1.0, "Daily")
        assertEquals(30.0, result, 0.001)
    }

    @Test
    fun `normaliseToMonthly returns cost times 4_33 for weekly`() {
        val result = normaliseToMonthly(10.0, "Weekly")
        assertEquals(43.3, result, 0.001)
    }

    @Test
    fun `normaliseToMonthly returns cost times 2_17 for biweekly`() {
        val result = normaliseToMonthly(10.0, "Biweekly")
        assertEquals(21.7, result, 0.001)
    }

    @Test
    fun `normaliseToMonthly returns cost as-is for monthly`() {
        val result = normaliseToMonthly(15.99, "Monthly (30 days)")
        assertEquals(15.99, result, 0.001)
    }

    @Test
    fun `normaliseToMonthly returns cost divided by 12 for yearly`() {
        val result = normaliseToMonthly(120.0, "Yearly")
        assertEquals(10.0, result, 0.001)
    }

    @Test
    fun `normaliseToMonthly returns cost as-is for custom cycle`() {
        val result = normaliseToMonthly(9.99, "Every 2 months")
        assertEquals(9.99, result, 0.001)
    }

    @Test
    fun normaliseToMonthlyIsCaseInsensitive() {
        assertEquals(
            normaliseToMonthly(10.0, "monthly (30 days)"),
            normaliseToMonthly(10.0, "Monthly (30 days)"),
            0.001
        )
    }

    // ------------------- Tests for Data Grouping -------------------

    @Test
    fun `subscriptions are grouped correctly by category`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 12.01, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01")
        )

        val byCategory = subscriptions.groupBy { it.category }
        assertEquals(2, byCategory.size)
        assertEquals(2, byCategory["Entertainment"]?.size)
        assertEquals(1, byCategory["Food"]?.size)
    }

    @Test
    fun `category totals are calculated correctly`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 12.01, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01")
        )

        val byCategory = subscriptions.groupBy { it.category }
        val entertainmentTotal = byCategory["Entertainment"]?.sumOf { it.cost } ?: 0.0
        val foodTotal = byCategory["Food"]?.sumOf { it.cost } ?: 0.0

        assertEquals(25.0, entertainmentTotal, 0.01)
        assertEquals(18.0, foodTotal, 0.01)
    }

    @Test
    fun `monthly total is calculated correctly`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 12.01, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01")
        )

        val monthlyTotal = subscriptions.sumOf { it.cost }
        assertEquals(43.0, monthlyTotal, 0.01)
    }

    // ------------------- Tests for Sorting -------------------

    @Test
    fun `categories are sorted by total descending`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "Adobe", 54.99, "Productivity", "monthly", "2024-01-01", "2024-01-01")
        )

        val byCategory = subscriptions.groupBy { it.category }
        val categoryTotals = byCategory
            .map { (category, subs) -> Pair(category, subs.sumOf { it.cost }) }
            .sortedByDescending { it.second }

        assertEquals("Productivity", categoryTotals[0].first)
        assertEquals("Food", categoryTotals[1].first)
        assertEquals("Entertainment", categoryTotals[2].first)
    }

    // ------------------- Tests for Edge Cases -------------------

    @Test
    fun `empty subscriptions list`() {
        val subscriptions = emptyList<SubscriptionResponse>()
        assertEquals(0, subscriptions.size)
    }

    @Test
    fun `zero cost subscription`() {
        val cost = 0.0
        val normalized = normaliseToMonthly(cost, "monthly")
        assertEquals(0.0, normalized, 0.01)
    }

    @Test
    fun `large subscription cost`() {
        val cost = 9999.99
        val normalized = normaliseToMonthly(cost, "monthly")
        assertEquals(9999.99, normalized, 0.01)
    }

    @Test
    fun `small subscription cost`() {
        val cost = 0.99
        val normalized = normaliseToMonthly(cost, "monthly")
        assertEquals(0.99, normalized, 0.01)
    }

    // ------------------- Tests for Mixed Billing Cycles -------------------

    @Test
    fun `category total with mixed billing cycles`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 120.0, "Entertainment", "yearly", "2024-01-01", "2024-01-01")
        )

        val byCategory = subscriptions.groupBy { it.category }
        val categoryTotal = byCategory["Entertainment"]?.sumOf { sub ->
            normaliseToMonthly(sub.cost, sub.billing_cycle)
        } ?: 0.0

        assertEquals(22.99, categoryTotal, 0.01)
    }

    @Test
    fun `multiple subscriptions in single category`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 12.01, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "Hulu", 7.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(4, "HBO", 15.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01")
        )

        val byCategory = subscriptions.groupBy { it.category }
        assertEquals(1, byCategory.size)
        assertEquals(4, byCategory["Entertainment"]?.size)

        val entertainmentTotal = byCategory["Entertainment"]?.sumOf { it.cost } ?: 0.0
        assertEquals(48.98, entertainmentTotal, 0.01)
    }
}
