package com.example.myapplication2

import org.junit.Assert.*
import org.junit.Test

class DashboardUtilsTest {

    private val subs = listOf(
        Subscription(1, "A", 10.0, 1, "Jan"),
        Subscription(2, "B", 20.0, 2, "Jan"),
        Subscription(3, "C", 30.0, 3, "Feb"),
        Subscription(4, "D", 40.0, 4, "Mar")
    )

    @Test
    fun filterByMonth() {
        val result = DashboardUtils.filterSubscriptionsByMonth(subs, "Jan")
        assertEquals(2, result.size)
    }

    @Test
    fun totalSpend() {
        val result = DashboardUtils.calculateTotalSpend(subs)
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun remainingBudget() {
        val result = DashboardUtils.calculateRemainingBudget(200.0, 100.0)
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun monthlyDataSize() {
        val result = DashboardUtils.buildMonthlySpendData(subs, listOf("Jan", "Feb"))
        assertEquals(2, result.size)
    }

    @Test
    fun headerText() {
        val result = DashboardUtils.buildHeaderText("Jan")
        assertEquals("Jan spending overview", result)
    }

    @Test
    fun titleText() {
        val result = DashboardUtils.buildSubscriptionTitle("Feb")
        assertEquals("Feb Subscriptions", result)
    }

    @Test
    fun percentLeft() {
        val result = DashboardUtils.budgetPercentLeft(200.0, 50.0)
        assertEquals(25, result)
    }

    @Test
    fun emptyListTotal() {
        val result = DashboardUtils.calculateTotalSpend(emptyList())
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun noMonthMatch() {
        val result = DashboardUtils.filterSubscriptionsByMonth(subs, "Dec")
        assertTrue(result.isEmpty())
    }

    @Test
    fun negativeRemaining() {
        val result = DashboardUtils.calculateRemainingBudget(50.0, 100.0)
        assertEquals(-50.0, result, 0.001)
    }
}