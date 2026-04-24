package com.example.subzero

import android.content.Context
import com.example.subzero.global.ApiCalls
import com.example.subzero.network.MonthlySpendResponse
import com.example.subzero.network.SubscriptionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SubscriptionRepository(private val context: Context) {
    private val calls = ApiCalls()

    private val _items = MutableStateFlow<List<Subscription>>(emptyList())
    val items: StateFlow<List<Subscription>> = _items

    private val _monthlyBudget = MutableStateFlow(0.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget

    private val _monthlySpendHistory = MutableStateFlow<List<MonthlySpendResponse>>(emptyList())
    val monthlySpendHistory: StateFlow<List<MonthlySpendResponse>> = _monthlySpendHistory

    // nextId starts high to avoid collisions with server IDs
    private var nextId: Int = 100_000

    suspend fun loadFromApi() {
        val dashboard = calls.loadDashboard(context)
        if (dashboard != null) {
            _items.value = dashboard.subscriptions.map { it.toSubscription() }
            _monthlyBudget.value = dashboard.monthly_budget
            _monthlySpendHistory.value = dashboard.monthly_spend
        }
    }

    suspend fun updateBudget(budget: Double): Boolean {
        val result = calls.updateBudget(context, budget)
        return if (result != null) {
            _monthlyBudget.value = result.monthly_budget
            true
        } else false
    }

    fun add(name: String, cost: Double, billingDay: Int, month: String) {
        validate(name, cost, billingDay, month)
        _items.update {
            it + Subscription(
                id = nextId++,
                name = name.trim(),
                cost = cost,
                billingDay = billingDay,
                month = month
            )
        }
    }

    fun edit(id: Int, name: String, cost: Double, billingDay: Int, month: String) {
        validate(name, cost, billingDay, month)
        _items.update { list ->
            list.map { sub ->
                if (sub.id == id) sub.copy(
                    name = name.trim(),
                    cost = cost,
                    billingDay = billingDay,
                    month = month
                ) else sub
            }
        }
    }

    fun delete(id: Int) {
        _items.update { list -> list.filterNot { it.id == id } }
    }

    private fun validate(name: String, cost: Double, billingDay: Int, month: String) {
        require(name.isNotBlank()) { "Name is required" }
        require(cost >= 0) { "Cost must be >= 0" }
        require(billingDay in 1..31) { "Billing day must be 1-31" }
    }
}

/**
 * Maps the API response model to the local Subscription domain model.
 * Uses renewal_date to derive the display month.
 */
private fun SubscriptionResponse.toSubscription(): Subscription {
    val dateStr = renewal_date.ifBlank { created_at }
    val month = runCatching {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val date = sdf.parse(dateStr.take(10))
        java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(date!!)
    }.getOrElse { "Jan" }

    return Subscription(
        id = id,
        name = name,
        cost = cost,
        month = month,
        billingDay = 1,
        categoryId = null,
        billingCycleId = null,
        renewalDate = renewal_date,
        isActive = true
    )
}
