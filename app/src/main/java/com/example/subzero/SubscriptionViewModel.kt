package com.example.subzero

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subzero.global.ApiCalls
import com.example.subzero.network.BillingCycleResponse
import com.example.subzero.network.CategoryResponse
import com.example.subzero.network.MonthlySpendResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val context: Context,
    private val repo: SubscriptionRepository = SubscriptionRepository(context)
) : ViewModel() {
    val calls = ApiCalls()
    val items: StateFlow<List<Subscription>> = repo.items
    val monthlyBudget: StateFlow<Double> = repo.monthlyBudget
    val monthlySpendHistory: StateFlow<List<MonthlySpendResponse>> = repo.monthlySpendHistory

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _budgetUpdateResult = MutableStateFlow<Boolean?>(null)
    val budgetUpdateResult: StateFlow<Boolean?> = _budgetUpdateResult

    // Per-month soft removals: subscriptionId → first month index from which it is hidden.
    // Populated by removeFromMonth(); survives configuration changes but resets on process death
    // (subscriptions are re-fetched from the API on next launch anyway).
    private val _monthlyHides = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val monthlyHides: StateFlow<Map<Int, Int>> = _monthlyHides

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories

    private val _billingCycles = MutableStateFlow<List<BillingCycleResponse>>(emptyList())
    val billingCycles: StateFlow<List<BillingCycleResponse>> = _billingCycles

    init {
        viewModelScope.launch {
            // load everything in parallel
            launch { repo.loadFromApi(); _isLoading.value = false }
            launch { _categories.value = calls.loadCategories(context) ?: emptyList() }
            launch { _billingCycles.value = calls.loadBillingCycles(context) ?: emptyList() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.loadFromApi()
            _isLoading.value = false
        }
    }

    fun updateBudget(budget: Double) {
        viewModelScope.launch {
            val success = repo.updateBudget(budget)
            _budgetUpdateResult.value = success
        }
    }

    fun clearBudgetResult() {
        _budgetUpdateResult.value = null
    }

    fun add(name: String, cost: Double, categoryId: Int, billingCycleId: Int, renewalDate: String) {
        viewModelScope.launch {
            val success = repo.add(name, cost, categoryId, billingCycleId, renewalDate)
        }
    }

    fun edit(id: Int, name: String, cost: Double, categoryId: Int, billingCycleId: Int, renewalDate: String) {
        viewModelScope.launch {
            repo.edit(id, name, cost, categoryId, billingCycleId, renewalDate)
        }
    }

    /**
     * Hides a subscription from [monthIndex] onwards in the management view.
     * Does NOT call the API — the subscription remains on the server and still
     * appears in months before [monthIndex].
     * If called again with an earlier index, the earlier index wins.
     */
    fun removeFromMonth(id: Int, monthIndex: Int) {
        _monthlyHides.update { current ->
            val existing = current[id] ?: Int.MAX_VALUE
            current + (id to minOf(existing, monthIndex))
        }
    }

    /** Hard-deletes the subscription from the API and removes it from all months. */
    fun delete(id: Int) {
        _monthlyHides.update { it - id }   // clear any soft-hide first
        viewModelScope.launch {
            repo.delete(id)
        }
    }
}
