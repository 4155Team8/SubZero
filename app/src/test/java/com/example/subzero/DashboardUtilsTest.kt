package com.example.subzero

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DashboardUtilsTest {

    // ------------------- Helpers -------------------

    private val months = listOf("Jan 2024", "Feb 2024", "Mar 2024", "Apr 2024", "May 2024")

    private fun sub(
        name: String,
        cost: Double,
        month: String,
        billingCycle: String? = "monthly"
    ) = Subscription(
        id = 0,
        name = name,
        cost = cost,
        month = month,
        billingDay = 1,
        billingCycle = billingCycle
    )

    // ===================================================================
    // filterWithCarryOver
    // ===================================================================

    @Test
    fun filterWithCarryOverReturnsEmptyForEmptyList() {
        val result = DashboardUtils.filterWithCarryOver(emptyList(), months, "Mar 2024")
        assertTrue(result.isEmpty())
    }

    @Test
    fun filterWithCarryOverIncludesSubscriptionFromSelectedMonth() {
        val subs = listOf(sub("Netflix", 12.99, "Mar 2024"))
        val result = DashboardUtils.filterWithCarryOver(subs, months, "Mar 2024")
        assertEquals(1, result.size)
    }

    @Test
    fun filterWithCarryOverIncludesSubscriptionFromEarlierMonth() {
        val subs = listOf(
            sub("Netflix", 12.99, "Jan 2024"),
            sub("Spotify", 9.99,  "Mar 2024")
        )
        val result = DashboardUtils.filterWithCarryOver(subs, months, "Mar 2024")
        assertEquals(2, result.size)
    }

    @Test
    fun filterWithCarryOverExcludesSubscriptionFromLaterMonth() {
        val subs = listOf(
            sub("Netflix", 12.99, "Jan 2024"),
            sub("Future",  5.00,  "May 2024")
        )
        val result = DashboardUtils.filterWithCarryOver(subs, months, "Feb 2024")
        assertEquals(1, result.size)
        assertEquals("Netflix", result[0].name)
    }

    @Test
    fun filterWithCarryOverForFirstMonthOnlyReturnsFirstMonthSubs() {
        val subs = listOf(
            sub("Netflix", 12.99, "Jan 2024"),
            sub("Spotify", 9.99,  "Feb 2024")
        )
        val result = DashboardUtils.filterWithCarryOver(subs, months, "Jan 2024")
        assertEquals(1, result.size)
        assertEquals("Netflix", result[0].name)
    }

    @Test
    fun filterWithCarryOverForLastMonthIncludesAll() {
        val subs = listOf(
            sub("A", 1.0, "Jan 2024"),
            sub("B", 2.0, "Feb 2024"),
            sub("C", 3.0, "Mar 2024"),
            sub("D", 4.0, "Apr 2024"),
            sub("E", 5.0, "May 2024")
        )
        val result = DashboardUtils.filterWithCarryOver(subs, months, "May 2024")
        assertEquals(5, result.size)
    }

    @Test
    fun filterWithCarryOverUnknownMonthDefaultsToIndexZero() {
        // Subscription with month not in the list defaults to index 0 → always visible
        val subs = listOf(sub("Old", 9.99, "Dec 2023"))
        val result = DashboardUtils.filterWithCarryOver(subs, months, "Jan 2024")
        assertEquals(1, result.size)
    }

    @Test
    fun filterWithCarryOverSelectedMonthNotInListFallsBackToExactMatch() {
        val subs = listOf(
            sub("Netflix", 12.99, "Jun 2024"),
            sub("Spotify", 9.99,  "Jul 2024")
        )
        // "Jun 2024" not in months → falls back to filterSubscriptionsByMonth
        val result = DashboardUtils.filterWithCarryOver(subs, months, "Jun 2024")
        assertEquals(1, result.size)
        assertEquals("Netflix", result[0].name)
    }

    // ===================================================================
    // calculateTotalSpend
    // ===================================================================

    @Test
    fun calculateTotalSpendReturnsZeroForEmptyList() {
        assertEquals(0.0, DashboardUtils.calculateTotalSpend(emptyList()), 0.001)
    }

    @Test
    fun calculateTotalSpendSumsMonthlySubscriptions() {
        val subs = listOf(
            sub("Netflix", 12.99, "Jan 2024", "monthly"),
            sub("Spotify", 9.99,  "Jan 2024", "monthly")
        )
        assertEquals(22.98, DashboardUtils.calculateTotalSpend(subs), 0.01)
    }

    @Test
    fun calculateTotalSpendNormalisesYearlyCost() {
        // $120/year → $10/month
        val subs = listOf(sub("Adobe", 120.0, "Jan 2024", "yearly"))
        assertEquals(10.0, DashboardUtils.calculateTotalSpend(subs), 0.001)
    }

    @Test
    fun calculateTotalSpendNormalisesWeeklyCost() {
        // $10/week → $43.30/month
        val subs = listOf(sub("Weekly", 10.0, "Jan 2024", "weekly"))
        assertEquals(43.3, DashboardUtils.calculateTotalSpend(subs), 0.01)
    }

    @Test
    fun calculateTotalSpendNormalisesDailyCost() {
        // $1/day → $30/month
        val subs = listOf(sub("Daily", 1.0, "Jan 2024", "daily"))
        assertEquals(30.0, DashboardUtils.calculateTotalSpend(subs), 0.001)
    }

    @Test
    fun calculateTotalSpendMixedCyclesAreCorrect() {
        val subs = listOf(
            sub("Monthly", 12.99, "Jan 2024", "monthly"),  // 12.99
            sub("Yearly",  120.0, "Jan 2024", "yearly")    // 10.00
        )
        assertEquals(22.99, DashboardUtils.calculateTotalSpend(subs), 0.01)
    }

    @Test
    fun calculateTotalSpendNullBillingCycleReturnsCostAsIs() {
        val subs = listOf(sub("Unknown", 9.99, "Jan 2024", null))
        assertEquals(9.99, DashboardUtils.calculateTotalSpend(subs), 0.001)
    }

    // ===================================================================
    // buildMonthlySpendData
    // ===================================================================

    @Test
    fun buildMonthlySpendDataReturnsOneEntryPerMonth() {
        val result = DashboardUtils.buildMonthlySpendData(emptyList(), months)
        assertEquals(months.size, result.size)
    }

    @Test
    fun buildMonthlySpendDataAllZeroForNoSubscriptions() {
        val result = DashboardUtils.buildMonthlySpendData(emptyList(), months)
        result.forEach { assertEquals(0.0, it.amount, 0.001) }
    }

    @Test
    fun buildMonthlySpendDataMonthLabelsMatchInput() {
        val result = DashboardUtils.buildMonthlySpendData(emptyList(), months)
        result.forEachIndexed { i, entry -> assertEquals(months[i], entry.month) }
    }

    @Test
    fun buildMonthlySpendDataCarriesOverToLaterMonths() {
        // Subscription added in Jan — should appear in all subsequent months
        val subs = listOf(sub("Netflix", 10.0, "Jan 2024", "monthly"))
        val result = DashboardUtils.buildMonthlySpendData(subs, months)
        result.forEach { assertEquals(10.0, it.amount, 0.001) }
    }

    @Test
    fun buildMonthlySpendDataDoesNotBackfillIntoEarlierMonths() {
        // Subscription added in Mar — should only appear from Mar onwards
        val subs = listOf(sub("New", 10.0, "Mar 2024", "monthly"))
        val result = DashboardUtils.buildMonthlySpendData(subs, months)
        assertEquals(0.0,  result[0].amount, 0.001) // Jan
        assertEquals(0.0,  result[1].amount, 0.001) // Feb
        assertEquals(10.0, result[2].amount, 0.001) // Mar
        assertEquals(10.0, result[3].amount, 0.001) // Apr
        assertEquals(10.0, result[4].amount, 0.001) // May
    }

    @Test
    fun buildMonthlySpendDataAccumulatesAcrossMonths() {
        val subs = listOf(
            sub("A", 10.0, "Jan 2024", "monthly"),
            sub("B", 5.0,  "Feb 2024", "monthly")
        )
        val result = DashboardUtils.buildMonthlySpendData(subs, months)
        assertEquals(10.0, result[0].amount, 0.001) // Jan: A
        assertEquals(15.0, result[1].amount, 0.001) // Feb: A + B
        assertEquals(15.0, result[2].amount, 0.001) // Mar: A + B (carry)
    }

    @Test
    fun buildMonthlySpendDataNormalisesYearlyCostsInChart() {
        val subs = listOf(sub("Adobe", 120.0, "Jan 2024", "yearly"))
        val result = DashboardUtils.buildMonthlySpendData(subs, months)
        // $120/year = $10/month for every bar
        result.forEach { assertEquals(10.0, it.amount, 0.001) }
    }

    @Test
    fun buildMonthlySpendDataUnknownMonthSubscriptionAppearsFromStart() {
        // Month not in list → defaults to index 0, visible in all months
        val subs = listOf(sub("Old", 8.0, "Dec 2023", "monthly"))
        val result = DashboardUtils.buildMonthlySpendData(subs, months)
        result.forEach { assertEquals(8.0, it.amount, 0.001) }
    }

    @Test
    fun buildMonthlySpendDataEmptyMonthsListReturnsEmpty() {
        val subs = listOf(sub("Netflix", 12.99, "Jan 2024", "monthly"))
        val result = DashboardUtils.buildMonthlySpendData(subs, emptyList())
        assertTrue(result.isEmpty())
    }
}
