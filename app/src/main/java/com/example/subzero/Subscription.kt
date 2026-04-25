package com.example.subzero

data class Subscription(
    val id: Int,
    val name: String,
    val cost: Double,
    val month: String,
    val billingDay: Int = 1,
    val categoryId: Int? = null,
    val billingCycleId: Int? = null,
    val renewalDate: String? = null,
    val isActive: Boolean = true
)