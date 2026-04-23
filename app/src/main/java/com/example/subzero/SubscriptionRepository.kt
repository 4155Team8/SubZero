package com.example.subzero

import android.content.Context
import com.example.subzero.global.ApiCalls
import com.example.subzero.network.SubscriptionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SubscriptionRepository(private val context: Context) {
    private val calls = ApiCalls()

    private val _items = MutableStateFlow<List<Subscription>>(emptyList())
    val items: StateFlow<List<Subscription>> = _items

    // nextId starts high to avoid collisions with server IDs
    private var nextId: Int = 100_000

    suspend fun loadFromApi() {
        val result = calls.loadSubscriptions(context)
        if (result != null) {
            _items.value = result.map { it.toSubscription() }
        }
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
        require(month in listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr")) {
            "Month must be Nov, Dec, Jan, Feb, Mar, or Apr"
        }
    }
}

/**
 * Maps the API response model to the local Subscription domain model.
 *
 * The server provides a billing_cycle string ("monthly", "yearly", etc.) and a
 * created_at timestamp.  We derive a display month from created_at and treat
 * the billing_cycle as the billingDay placeholder (defaulting to 1) since the
 * API does not currently expose a numeric billing-day field.
 */
private fun SubscriptionResponse.toSubscription(): Subscription {
    // Parse month abbreviation from created_at (e.g. "2024-11-03T..." → "Nov")
    val month = runCatching {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val date = sdf.parse(created_at.take(10))
        java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(date!!)
    }.getOrElse { "Jan" }

    return Subscription(
        id = id,
        name = name,
        cost = cost,
        month = month,
        billingDay = 1, // API does not expose a billing-day integer yet
        renewalDate = updated_at,
        isActive = true
    )
}