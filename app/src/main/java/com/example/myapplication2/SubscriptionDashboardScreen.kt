@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

private val Blue500 = Color(0xFF3B82F6)
private val Blue600 = Color(0xFF2563EB)
private val Purple500 = Color(0xFFA855F7)
private val Indigo500 = Color(0xFF6366F1)
private val White = Color(0xFFFFFFFF)
private val Gray50 = Color(0xFFF9FAFB)
private val Gray200 = Color(0xFFE5E7EB)
private val Gray500 = Color(0xFF6B7280)
private val Gray700 = Color(0xFF374151)
private val Green500 = Color(0xFF22C55E)
private val Orange500 = Color(0xFFF59E0B)

private enum class BottomTab {
    Manage, Insights, Alerts, Profile
}

@Composable
fun SubscriptionDashboardScreen(
    vm: SubscriptionViewModel,
    onGoToManage: () -> Unit
) {
    val allItems by vm.items.collectAsState()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val months = listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr")
    var selectedMonth by remember { mutableStateOf("Nov") }
    var selectedTab by remember { mutableStateOf(BottomTab.Alerts) }

    val selectedMonthItems = allItems.filter { it.month == selectedMonth }
    val totalSpend = selectedMonthItems.sumOf { it.cost }
    val monthlyBudget = 200.0
    val remainingBudget = monthlyBudget - totalSpend

    val monthlyData = months.map { month ->
        MonthlySpend(
            month = month,
            amount = allItems.filter { it.month == month }.sumOf { it.cost }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onGoToManage,
                containerColor = Blue600,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add subscription")
            }
        },
        bottomBar = {
            SubscriptionBottomBar(
                selectedTab = selectedTab,
                onManageClick = {
                    selectedTab = BottomTab.Manage
                    onGoToManage()
                },
                onInsightsClick = { selectedTab = BottomTab.Insights },
                onAlertsClick = { selectedTab = BottomTab.Alerts },
                onProfileClick = { selectedTab = BottomTab.Profile }
            )
        },
        containerColor = Gray50
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Blue500, Purple500, Indigo500)
                    )
                )
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    DashboardHeader(selectedMonth)
                }

                item {
                    MonthSelectorRow(
                        months = months,
                        selectedMonth = selectedMonth,
                        onMonthSelected = { selectedMonth = it }
                    )
                }

                item {
                    SpendingChartCard(
                        monthlyData = monthlyData,
                        selectedMonth = selectedMonth
                    )
                }

                item {
                    SummaryCard(
                        budget = monthlyBudget,
                        totalSpend = totalSpend,
                        remaining = remainingBudget,
                        currency = currency
                    )
                }

                item {
                    Text(
                        text = "$selectedMonth SUBSCRIPTIONS",
                        color = White.copy(alpha = 0.96f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (selectedMonthItems.isEmpty()) {
                    item {
                        AddFirstSubscriptionCard(
                            month = selectedMonth,
                            onAddFirstClick = onGoToManage
                        )
                    }
                } else {
                    item {
                        ThisMonthSubscriptionsCard(
                            month = selectedMonth,
                            items = selectedMonthItems,
                            currency = currency,
                            onManageClick = onGoToManage
                        )
                    }

                    items(selectedMonthItems) { sub ->
                        IndividualSubscriptionCard(
                            subscription = sub,
                            currency = currency,
                            onManageClick = onGoToManage
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun DashboardHeader(selectedMonth: String) {
    Column {
        Text(
            text = "Subscriptions",
            color = White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$selectedMonth spending overview",
            color = White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MonthSelectorRow(
    months: List<String>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        months.forEach { month ->
            val selected = month == selectedMonth

            Card(
                modifier = Modifier.clickable { onMonthSelected(month) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) White else White.copy(alpha = 0.18f)
                )
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = month,
                        color = if (selected) Blue600 else White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingChartCard(
    monthlyData: List<MonthlySpend>,
    selectedMonth: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.14f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Subscription Spend",
                color = White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxValue = (monthlyData.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(1.0)

                monthlyData.forEach { item ->
                    val selected = item.month == selectedMonth
                    val fraction = (item.amount / maxValue).toFloat().coerceIn(0.08f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height((100 * fraction).dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) White else White.copy(alpha = 0.45f)
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.month,
                            color = White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    budget: Double,
    totalSpend: Double,
    remaining: Double,
    currency: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SummaryRow("Monthly Budget", currency.format(budget), Gray700)
            Spacer(modifier = Modifier.height(10.dp))
            SummaryRow("Subscription Spend", currency.format(totalSpend), Gray700)
            Spacer(modifier = Modifier.height(10.dp))
            SummaryRow(
                "Remaining Budget",
                currency.format(remaining),
                if (remaining >= 0) Green500 else Orange500
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Gray200)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (budget > 0) {
                    "${((remaining / budget) * 100).toInt().coerceAtLeast(0)}% left for other spending."
                } else {
                    "No budget set."
                },
                color = Gray500,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Blue500.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Blue600)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = Gray700,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AddFirstSubscriptionCard(
    month: String,
    onAddFirstClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$month Subscriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray700
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "You have no subscriptions for $month yet.",
                color = Gray500,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onAddFirstClick) {
                Text("Add First Subscription", color = Blue600)
            }
        }
    }
}

@Composable
private fun ThisMonthSubscriptionsCard(
    month: String,
    items: List<Subscription>,
    currency: NumberFormat,
    onManageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$month Subscriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray700
            )

            Spacer(modifier = Modifier.height(12.dp))

            items.forEachIndexed { index, sub ->
                SubscriptionLineItem(
                    name = sub.name,
                    amount = "${currency.format(sub.cost)}/mo"
                )

                if (index != items.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Gray200)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onManageClick) {
                Text("Manage Subscriptions", color = Blue600)
            }
        }
    }
}

@Composable
private fun IndividualSubscriptionCard(
    subscription: Subscription,
    currency: NumberFormat,
    onManageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray700
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Monthly Cost: ${currency.format(subscription.cost)}",
                color = Gray700
            )

            Text(
                text = "Billing Day: ${subscription.billingDay}",
                color = Gray500
            )

            Text(
                text = "Month: ${subscription.month}",
                color = Gray500
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onManageClick) {
                Text("View in Manage", color = Blue600)
            }
        }
    }
}

@Composable
private fun SubscriptionLineItem(
    name: String,
    amount: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Blue600)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = name,
            modifier = Modifier.weight(1f),
            color = Gray700,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = amount,
            color = Gray700,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SubscriptionBottomBar(
    selectedTab: BottomTab,
    onManageClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .navigationBarsPadding()
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarItem(
            label = "Manage",
            icon = Icons.Default.GridView,
            selected = selectedTab == BottomTab.Manage,
            onClick = onManageClick
        )
        BottomBarItem(
            label = "Insights",
            icon = Icons.Default.TrendingUp,
            selected = selectedTab == BottomTab.Insights,
            onClick = onInsightsClick
        )
        BottomBarItem(
            label = "Alerts",
            icon = Icons.Default.Notifications,
            selected = selectedTab == BottomTab.Alerts,
            onClick = onAlertsClick
        )
        BottomBarItem(
            label = "Profile",
            icon = Icons.Default.Person,
            selected = selectedTab == BottomTab.Profile,
            onClick = onProfileClick
        )
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) Blue600 else Gray500

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}