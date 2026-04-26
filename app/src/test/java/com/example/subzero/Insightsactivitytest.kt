package com.example.subzero

import com.example.subzero.network.SubscriptionResponse
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class InsightsActivityTest {

    // Static function to test normaliseToMonthly logic without needing Activity context
    private fun normaliseToMonthly(cost: Double, billingCycle: String): Double {
        return when {
            billingCycle.contains("daily",    ignoreCase = true) -> cost * 30
            billingCycle.contains("biweekly", ignoreCase = true) -> cost * 2.17
            billingCycle.contains("weekly",   ignoreCase = true) -> cost * 4.33
            billingCycle.contains("month",    ignoreCase = true) -> cost
            billingCycle.contains("year",     ignoreCase = true) -> cost / 12
            else -> cost
        }
    }

    // ------------------- Billing cycle: daily -------------------

    @Test
    fun dailyLowercaseMultipliesBy30() {
        assertEquals(30.0, normaliseToMonthly(1.0, "daily"), 0.001)
    }

    @Test
    fun dailyUppercaseMultipliesBy30() {
        assertEquals(30.0, normaliseToMonthly(1.0, "Daily"), 0.001)
    }

    @Test
    fun dailyAllCapsMultipliesBy30() {
        assertEquals(300.0, normaliseToMonthly(10.0, "DAILY"), 0.001)
    }

    @Test
    fun dailyCostZeroStaysZero() {
        assertEquals(0.0, normaliseToMonthly(0.0, "daily"), 0.001)
    }

    // ------------------- Billing cycle: weekly -------------------

    @Test
    fun weeklyMultipliesBy4Point33() {
        assertEquals(43.3, normaliseToMonthly(10.0, "weekly"), 0.001)
    }

    @Test
    fun weeklyMixedCaseMultipliesBy4Point33() {
        assertEquals(43.3, normaliseToMonthly(10.0, "Weekly"), 0.001)
    }

    // Biweekly must be checked BEFORE weekly since "biweekly" contains "weekly"
    @Test
    fun biweeklyIsNotMatchedByWeeklyBranch() {
        val biweekly = normaliseToMonthly(10.0, "biweekly")
        val weekly   = normaliseToMonthly(10.0, "weekly")
        assertNotEquals(weekly, biweekly, 0.001)
    }

    // ------------------- Billing cycle: biweekly -------------------

    @Test
    fun biweeklyMultipliesBy2Point17() {
        assertEquals(21.7, normaliseToMonthly(10.0, "biweekly"), 0.001)
    }

    @Test
    fun biweeklyUppercaseMultipliesBy2Point17() {
        assertEquals(21.7, normaliseToMonthly(10.0, "Biweekly"), 0.001)
    }

    // ------------------- Billing cycle: monthly -------------------

    @Test
    fun monthlyReturnsCostUnchanged() {
        assertEquals(15.99, normaliseToMonthly(15.99, "monthly"), 0.001)
    }

    @Test
    fun monthlyWithParenthesesReturnsCostUnchanged() {
        assertEquals(15.99, normaliseToMonthly(15.99, "monthly (30 days)"), 0.001)
    }

    @Test
    fun monthlyAllCapsReturnsCostUnchanged() {
        assertEquals(9.99, normaliseToMonthly(9.99, "MONTHLY"), 0.001)
    }

    @Test
    fun monthlyWithZeroCostReturnsZero() {
        assertEquals(0.0, normaliseToMonthly(0.0, "monthly"), 0.001)
    }

    // ------------------- Billing cycle: yearly -------------------

    @Test
    fun yearlyDividesCostBy12() {
        assertEquals(10.0, normaliseToMonthly(120.0, "yearly"), 0.001)
    }


    @Test
    fun yearlyUppercaseDividesCostBy12() {
        assertEquals(10.0, normaliseToMonthly(120.0, "Yearly"), 0.001)
    }

    @Test
    fun yearlyWithLargeCostDividesCorrectly() {
        assertEquals(100.0, normaliseToMonthly(1200.0, "yearly"), 0.001)
    }

    // ------------------- Fallback (unknown cycle) -------------------

    @Test
    fun unknownCycleReturnsCostUnchanged() {
        assertEquals(9.99, normaliseToMonthly(9.99, "every 2 months"), 0.001)
    }

    @Test
    fun emptyCycleReturnsCostUnchanged() {
        assertEquals(5.0, normaliseToMonthly(5.0, ""), 0.001)
    }

    @Test
    fun randomStringCycleReturnsCostUnchanged() {
        assertEquals(7.0, normaliseToMonthly(7.0, "quarterly"), 0.001)
    }

    // ------------------- Monthly total calculation -------------------

    @Test
    fun monthlyTotalSumsAllSubscriptions() {
        val subs = listOf(
            SubscriptionResponse(1, "Netflix",  12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify",  9.99,  "Music",         "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "Adobe",    54.99, "Productivity",  "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val total = subs.sumOf { normaliseToMonthly(it.cost, it.billing_cycle) }
        assertEquals(77.97, total, 0.01)
    }

    @Test
    fun monthlyTotalWithMixedCyclesIsCorrect() {
        val subs = listOf(
            SubscriptionResponse(1, "Netflix",  12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Adobe",    120.0, "Productivity",  "yearly",  "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val total = subs.sumOf { normaliseToMonthly(it.cost, it.billing_cycle) }
        assertEquals(22.99, total, 0.01)
    }

    @Test
    fun monthlyTotalForEmptyListIsZero() {
        val total = emptyList<SubscriptionResponse>().sumOf { normaliseToMonthly(it.cost, it.billing_cycle) }
        assertEquals(0.0, total, 0.001)
    }

    @Test
    fun monthlyTotalForSingleSubscriptionIsCorrect() {
        val subs = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val total = subs.sumOf { normaliseToMonthly(it.cost, it.billing_cycle) }
        assertEquals(12.99, total, 0.001)
    }

    // ------------------- Category grouping -------------------

    @Test
    fun singleCategoryGroupsAllSubscriptions() {
        val subs = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Hulu",    7.99,  "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val byCategory = subs.groupBy { it.category }
        assertEquals(1, byCategory.size)
        assertEquals(2, byCategory["Entertainment"]?.size)
    }

    @Test
    fun multiCategoryGroupsSeparatesCorrectly() {
        val subs = listOf(
            SubscriptionResponse(1, "Netflix",  12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify",  9.99,  "Music",         "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0,  "Food",          "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val byCategory = subs.groupBy { it.category }
        assertEquals(3, byCategory.size)
    }

    @Test
    fun categoryTotalsAreSortedDescending() {
        val subs = listOf(
            SubscriptionResponse(1, "Netflix",  12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Adobe",    54.99, "Productivity",  "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0,  "Food",          "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val byCategory = subs.groupBy { it.category }
        val sorted = byCategory
            .map { (cat, subList) -> Pair(cat, subList.sumOf { normaliseToMonthly(it.cost, it.billing_cycle) }) }
            .sortedByDescending { it.second }

        assertEquals("Productivity",  sorted[0].first)
        assertEquals("Food",          sorted[1].first)
        assertEquals("Entertainment", sorted[2].first)
    }

    @Test
    fun categoryWithOnlyYearlySubHasCorrectMonthlyTotal() {
        val subs = listOf(
            SubscriptionResponse(1, "Adobe", 120.0, "Productivity", "yearly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val byCategory = subs.groupBy { it.category }
        val total = byCategory["Productivity"]?.sumOf { normaliseToMonthly(it.cost, it.billing_cycle) } ?: 0.0
        assertEquals(10.0, total, 0.001)
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
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 12.01, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )

        val byCategory = subscriptions.groupBy { it.category }
        assertEquals(2, byCategory.size)
        assertEquals(2, byCategory["Entertainment"]?.size)
        assertEquals(1, byCategory["Food"]?.size)
    }

    @Test
    fun `category totals are calculated correctly`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 12.01, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
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
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 12.01, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )

        val monthlyTotal = subscriptions.sumOf { it.cost }
        assertEquals(43.0, monthlyTotal, 0.01)
    }

    // ------------------- Tests for Sorting -------------------

    @Test
    fun `categories are sorted by total descending`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "Adobe", 54.99, "Productivity", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
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
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 120.0, "Entertainment", "yearly", "2024-01-01", "2024-01-01", "2024-01-01")
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
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 12.01, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "Hulu", 7.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(4, "HBO", 15.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )

        val byCategory = subscriptions.groupBy { it.category }
        assertEquals(1, byCategory.size)
        assertEquals(4, byCategory["Entertainment"]?.size)

        val entertainmentTotal = byCategory["Entertainment"]?.sumOf { it.cost } ?: 0.0
        assertEquals(48.98, entertainmentTotal, 0.01)
    }

    // ------------------- Tests for Category Filtering -------------------

    @Test
    fun `filtering with empty selected categories shows all subscriptions`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 9.99, "Music", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val selectedCategories = mutableSetOf<String>()
        val filteredSubs = if (selectedCategories.isEmpty()) subscriptions else subscriptions.filter { it.category in selectedCategories }
        assertEquals(3, filteredSubs.size)
    }

    @Test
    fun `filtering with specific categories shows only matching subscriptions`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 9.99, "Music", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(4, "Hulu", 7.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val selectedCategories = mutableSetOf("Entertainment")
        val filteredSubs = if (selectedCategories.isEmpty()) subscriptions else subscriptions.filter { it.category in selectedCategories }
        assertEquals(2, filteredSubs.size)
        assertTrue(filteredSubs.all { it.category == "Entertainment" })
    }

    @Test
    fun `filtering with multiple categories shows matching subscriptions`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 9.99, "Music", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(4, "Adobe", 54.99, "Productivity", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val selectedCategories = mutableSetOf("Entertainment", "Music")
        val filteredSubs = if (selectedCategories.isEmpty()) subscriptions else subscriptions.filter { it.category in selectedCategories }
        assertEquals(2, filteredSubs.size)
        assertTrue(filteredSubs.all { it.category in selectedCategories })
    }

    @Test
    fun `filtering with non-existent category shows no subscriptions`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 9.99, "Music", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val selectedCategories = mutableSetOf("NonExistent")
        val filteredSubs = if (selectedCategories.isEmpty()) subscriptions else subscriptions.filter { it.category in selectedCategories }
        assertEquals(0, filteredSubs.size)
    }

    @Test
    fun `monthly total with filtering is correct`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 9.99, "Music", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val selectedCategories = mutableSetOf("Entertainment", "Music")
        val filteredSubs = if (selectedCategories.isEmpty()) subscriptions else subscriptions.filter { it.category in selectedCategories }
        val monthlyTotal = filteredSubs.sumOf { normaliseToMonthly(it.cost, it.billing_cycle) }
        assertEquals(22.98, monthlyTotal, 0.01)
    }

    @Test
    fun `category grouping with filtering excludes non-selected categories`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 9.99, "Music", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val selectedCategories = mutableSetOf("Entertainment")
        val filteredSubs = if (selectedCategories.isEmpty()) subscriptions else subscriptions.filter { it.category in selectedCategories }
        val byCategory = filteredSubs.groupBy { it.category }
        assertEquals(1, byCategory.size)
        assertTrue(byCategory.containsKey("Entertainment"))
        assertFalse(byCategory.containsKey("Music"))
        assertFalse(byCategory.containsKey("Food"))
    }

    @Test
    fun `category totals with filtering are sorted correctly`() {
        val subscriptions = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 9.99, "Music", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(3, "DoorDash", 18.0, "Food", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(4, "Adobe", 54.99, "Productivity", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val selectedCategories = mutableSetOf("Entertainment", "Productivity", "Food")
        val filteredSubs = if (selectedCategories.isEmpty()) subscriptions else subscriptions.filter { it.category in selectedCategories }
        val byCategory = filteredSubs.groupBy { it.category }
        val categoryTotals = byCategory
            .map { (cat, subs) -> Pair(cat, subs.sumOf { normaliseToMonthly(it.cost, it.billing_cycle) }) }
            .sortedByDescending { it.second }
        assertEquals("Productivity", categoryTotals[0].first)
        assertEquals("Food", categoryTotals[1].first)
        assertEquals("Entertainment", categoryTotals[2].first)
    }
}
