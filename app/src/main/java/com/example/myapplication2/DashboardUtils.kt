package com.example.myapplication2

data class MonthlySpend(
    val month: String,
    val amount: Double
)

object DashboardUtils {

    fun filterSubscriptionsByMonth(
        subscriptions: List<Subscription>,
        month: String
    ): List<Subscription> {
        return subscriptions.filter { it.month == month }
    }

    fun calculateTotalSpend(
        subscriptions: List<Subscription>
    ): Double {
        return subscriptions.sumOf { it.cost }
    }

    fun calculateRemainingBudget(
        budget: Double,
        totalSpend: Double
    ): Double {
        return budget - totalSpend
    }

    fun buildMonthlySpendData(
        subscriptions: List<Subscription>,
        months: List<String>
    ): List<MonthlySpend> {
        return months.map { month ->
            MonthlySpend(
                month = month,
                amount = subscriptions
                    .filter { it.month == month }
                    .sumOf { it.cost }
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