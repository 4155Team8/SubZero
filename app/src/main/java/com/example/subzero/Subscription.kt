package com.example.subzero

data class Subscription(
    val id: Int,
    val name: String,
    val cost: Double,
    val month: String,
    val billingDay: Int,
    val categoryId: Int? = null,
    val billingCycleId: Int? = null,
    val category: String? = null,       // ← add
    val billingCycle: String? = null,   // ← add
    val renewalDate: String? = null,    // ← add
    val isActive: Boolean = true
)