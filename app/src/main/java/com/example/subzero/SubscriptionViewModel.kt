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

    fun edit(id: Int, name: String, cost: Double, billingDay: Int, month: String) =
        repo.edit(id, name, cost, billingDay, month)

    fun delete(id: Int) = repo.delete(id)
}
