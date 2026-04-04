package com.example.myapplication2

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class SubscriptionViewModel(
    private val repo: SubscriptionRepository = SubscriptionRepository()
) : ViewModel() {

    val items: StateFlow<List<Subscription>> = repo.items

    fun add(name: String, cost: Double, billingDay: Int, month: String) {
        repo.add(name, cost, billingDay, month)
    }

    fun edit(id: Int, name: String, cost: Double, billingDay: Int, month: String) {
        repo.edit(id, name, cost, billingDay, month)
    }

    fun delete(id: Int) {
        repo.delete(id)
    }
}