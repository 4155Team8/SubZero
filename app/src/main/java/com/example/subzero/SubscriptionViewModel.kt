package com.example.subzero

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val context: Context,
    private val repo: SubscriptionRepository = SubscriptionRepository(context)
) : ViewModel() {

    val items: StateFlow<List<Subscription>> = repo.items

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            repo.loadFromApi()
            _isLoading.value = false
        }
    }

    fun add(name: String, cost: Double, billingDay: Int, month: String) =
        repo.add(name, cost, billingDay, month)

    fun edit(id: Int, name: String, cost: Double, billingDay: Int, month: String) =
        repo.edit(id, name, cost, billingDay, month)

    fun delete(id: Int) = repo.delete(id)
}