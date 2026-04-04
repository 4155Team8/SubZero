package com.example.myapplication2

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SubscriptionRepository {
    private val _items = MutableStateFlow<List<Subscription>>(
        listOf(
            Subscription(1, "Netflix", 12.99, 15, "Jun"),
            Subscription(2, "Spotify", 9.99, 20, "Jun"),
            Subscription(3, "Adobe Creative Cloud", 52.99, 5, "May"),
            Subscription(4, "Gym Membership", 35.00, 10, "Apr"),
            Subscription(5, "Amazon Prime", 14.99, 12, "Jul"),
            Subscription(6, "Hulu", 7.99, 8, "Mar"),
            Subscription(7, "Gym Membership", 35.00, 10, "Feb"),
            Subscription(8, "Gym Membership", 35.00, 10, "Mar"),
            Subscription(9, "Gym Membership", 35.00, 10, "May"),
            Subscription(10, "Gym Membership", 35.00, 10, "Jun"),
            Subscription(11, "Gym Membership", 35.00, 10, "Jul"),
            Subscription(12, "YouTube Premium", 13.99, 18, "Feb")
        )
    )

    val items: StateFlow<List<Subscription>> = _items

    private var nextId = 8

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
                if (sub.id == id) {
                    sub.copy(
                        name = name.trim(),
                        cost = cost,
                        billingDay = billingDay,
                        month = month
                    )
                } else {
                    sub
                }
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
        require(month in listOf("Feb", "Mar", "Apr", "May", "Jun", "Jul")) {
            "Month must be Feb, Mar, Apr, May, Jun, or Jul"
        }
    }
}