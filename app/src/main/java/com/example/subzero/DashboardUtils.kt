package com.example.subzero

import com.example.subzero.global.Utility

data class MonthlySpend(
    val month: String,
    val amount: Double
)

object DashboardUtils {

    private val utility = Utility()

    fun filterSubscriptionsByMonth(
        subscriptions: List<Subscription>,
        month: String
    ): List<Subscription> {
        return subscriptions.filter { it.month == month }
    }

    /**
     * Returns subscriptions that were added in [selectedMonth] or any earlier month in [months].
     * This mirrors the management screen's carry-over behaviour: a subscription added in January
     * remains visible (and counts toward spend) in February, March, etc.
     *
     * Subscriptions whose month is not found in [months] default to index 0 (always visible).
     */
    fun filterWithCarryOver(
        subscriptions: List<Subscription>,
        months: List<String>,
        selectedMonth: String
    ): List<Subscription> {
        val selectedIdx = months.indexOf(selectedMonth)
        if (selectedIdx == -1) return filterSubscriptionsByMonth(subscriptions, selectedMonth)
        return subscriptions.filter { sub ->
            val subIdx = months.indexOf(sub.month).let { if (it == -1) 0 else it }
            subIdx <= selectedIdx
        }
    }

    /**
     * Sums subscriptions normalised to their monthly equivalent cost so that
     * yearly, weekly, etc. billing cycles are all comparable on the same scale.
     */
    fun calculateTotalSpend(
        subscriptions: List<Subscription>
    ): Double {
        return subscriptions.sumOf { utility.normaliseToMonthly(it.cost, it.billingCycle) }
    }

    fun calculateRemainingBudget(
        budget: Double,
        totalSpend: Double
    ): Double {
        return budget - totalSpend
    }

    /**
     * Builds chart data for [months] using carry-over accumulation: each bar includes
     * subscriptions added in that month AND all earlier months, matching the spend total
     * shown in the summary card for that month.
     */
    fun buildMonthlySpendData(
        subscriptions: List<Subscription>,
        months: List<String>
    ): List<MonthlySpend> {
        return months.mapIndexed { idx, month ->
            MonthlySpend(
                month = month,
                amount = subscriptions
                    .filter { sub ->
                        val subIdx = months.indexOf(sub.month).let { if (it == -1) 0 else it }
                        subIdx <= idx
                    }
                    .sumOf { utility.normaliseToMonthly(it.cost, it.billingCycle) }
            )
        }
    }

    fun buildHeaderText(selectedMonth: String): String {
        return "$selectedMonth spending overview"
    }

    fun buildSubscriptionTitle(selectedMonth: String): String {
        return "$selectedMonth Subscriptions"
    }

    fun budgetPercentLeft(
        budget: Double,
        remaining: Double
    ): Int {
        if (budget <= 0) return 0
        return ((remaining / budget) * 100).toInt().coerceAtLeast(0)
    }
}