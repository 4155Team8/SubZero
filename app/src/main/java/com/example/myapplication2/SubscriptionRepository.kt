package com.example.myapplication2

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SubscriptionRepository {
    private val _items = MutableStateFlow<List<Subscription>>(
        listOf(
            Subscription(1, "Netflix", 12.99, 15, "Nov"),
            Subscription(2, "Spotify", 9.99, 20, "Nov"),
            Subscription(3, "Adobe Creative Cloud", 52.99, 5, "Nov"),
            Subscription(4, "Gym Membership", 35.00, 10, "Dec"),
            Subscription(5, "Amazon Prime", 14.99, 12, "Dec"),
            Subscription(6, "Hulu", 12.99, 8, "Dec"),
            Subscription(7, "YouTube Premium", 13.99, 18, "Jan"),
            Subscription(8, "Apple Music", 10.99, 22, "Jan"),
            Subscription(9, "Disney+", 8.99, 14, "Jan"),
            Subscription(10, "HBO Max", 15.99, 9, "Feb"),
            Subscription(11, "Paramount+", 6.99, 3, "Feb"),
            Subscription(12, "Peacock", 5.99, 25, "Feb"),
            Subscription(13, "Canva Pro", 14.99, 7, "Mar"),
            Subscription(14, "Notion Plus", 8.00, 11, "Mar"),
            Subscription(15, "iCloud+", 2.99, 16, "Mar"),
            Subscription(16, "Dropbox", 11.99, 21, "Apr"),
            Subscription(17, "Crunchyroll", 12.99, 6, "Apr"),
            Subscription(18, "Microsoft 365", 6.99, 28, "Apr"),
            Subscription(19, "ESPN+", 10.99, 4, "Nov"),
            Subscription(20, "Uber One", 9.99, 17, "Dec"),
            Subscription(21, "Duolingo Plus", 12.99, 13, "Jan"),
            Subscription(22, "ChatGPT Plus", 20.00, 1, "Feb"),
            Subscription(23, "PlayStation Plus", 17.99, 23, "Mar"),
            Subscription(24, "Spotify", 9.99, 13, "Dec"),
            Subscription(25, "Spotify", 9.99, 19, "Jan"),
            Subscription(26, "Spotify", 9.99, 12, "Feb"),
            Subscription(27, "Spotify", 9.99, 15, "Mar"),
            Subscription(28, "Spotify", 9.99, 11, "Apr"),
            Subscription(29, "Nintendo Switch Online", 3.99, 30, "Apr")
        )
    )

    val items: StateFlow<List<Subscription>> = _items

    private var nextId = 25

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
        require(month in listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr")) {
            "Month must be Nov, Dec, Jan, Feb, Mar, or Apr"
        }
    }
}