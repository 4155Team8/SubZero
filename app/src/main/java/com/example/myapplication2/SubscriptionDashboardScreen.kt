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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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

private val DashboardBackgroundTop = Color(0xFFA855F7)
private val DashboardBackgroundBottom = Color(0xFFA855F7)
private val DashboardCardColor = Color(0xFFF7F3F4)
private val DashboardAccent = Color(0xFFE53935)
private val DashboardAccentSoft = Color(0xFFFFCDD2)
private val DashboardGreen = Color(0xFF2E7D32)
private val DashboardTextDark = Color(0xFF2B2B2B)
private val DashboardMuted = Color(0xFF7A7A7A)

@Composable
fun SubscriptionDashboardScreen(
    vm: SubscriptionViewModel,
    onGoToManage: () -> Unit
) {
    val allItems by vm.items.collectAsState()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val months = listOf("Feb", "Mar", "Apr", "May", "Jun", "Jul")
    var selectedMonth by remember { mutableStateOf("Jun") }

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
                containerColor = Color.White,
                contentColor = DashboardAccent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add subscription")
            }
        },
        bottomBar = {
            SubscriptionBottomBar(
                onDashboardClick = {},
                onManageClick = onGoToManage,
                onSpendingClick = {},
                onAlertsClick = {},
                onProfileClick = {}
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(DashboardBackgroundTop, DashboardBackgroundBottom)
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
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

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
                        color = Color.White.copy(alpha = 0.95f),
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

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(selectedMonth: String) {
    Column {
        Text(
            text = "Subscriptions",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$selectedMonth spending overview",
            color = Color.White.copy(alpha = 0.9f),
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        months.forEach { month ->
            val selected = month == selectedMonth

            Card(
                modifier = Modifier
                    .width(48.dp)
                    .clickable { onMonthSelected(month) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) Color.White else Color.White.copy(alpha = 0.18f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = month,
                        color = if (selected) DashboardAccent else Color.White,
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
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Subscription Spend",
                color = Color.White,
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
                                    if (selected) Color.White
                                    else Color.White.copy(alpha = 0.45f)
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.month,
                            color = Color.White,
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
        colors = CardDefaults.cardColors(containerColor = DashboardCardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SummaryRow("Monthly Budget", currency.format(budget), DashboardTextDark)

            Spacer(modifier = Modifier.height(10.dp))

            SummaryRow("Subscription Spend", currency.format(totalSpend), DashboardTextDark)

            Spacer(modifier = Modifier.height(10.dp))

            SummaryRow(
                "Remaining Budget",
                currency.format(remaining),
                if (remaining >= 0) DashboardGreen else Color.Red
            )

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = Color.LightGray.copy(alpha = 0.7f))

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (budget > 0) {
                    "${((remaining / budget) * 100).toInt().coerceAtLeast(0)}% left for other spending."
                } else {
                    "No budget set."
                },
                color = DashboardMuted,
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
                .background(DashboardAccentSoft),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(DashboardAccent)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = DashboardTextDark,
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
        colors = CardDefaults.cardColors(containerColor = DashboardCardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$month Subscriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DashboardTextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "You have no subscriptions for $month yet.",
                color = DashboardMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onAddFirstClick) {
                Text("Add First Subscription")
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
        colors = CardDefaults.cardColors(containerColor = DashboardCardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$month Subscriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DashboardTextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            items.forEachIndexed { index, sub ->
                SubscriptionLineItem(
                    name = sub.name,
                    amount = "${currency.format(sub.cost)}/mo"
                )

                if (index != items.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.55f))
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onManageClick) {
                Text("Manage Subscriptions")
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
        colors = CardDefaults.cardColors(containerColor = DashboardCardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DashboardTextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Monthly Cost: ${currency.format(subscription.cost)}",
                color = DashboardTextDark
            )

            Text(
                text = "Billing Day: ${subscription.billingDay}",
                color = DashboardMuted
            )

            Text(
                text = "Month: ${subscription.month}",
                color = DashboardMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onManageClick) {
                Text("View in Manage")
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
                .background(DashboardAccent)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = name,
            modifier = Modifier.weight(1f),
            color = DashboardTextDark,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = amount,
            color = DashboardTextDark,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SubscriptionBottomBar(
    onDashboardClick: () -> Unit,
    onManageClick: () -> Unit,
    onSpendingClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarItem("Dashboard", Icons.Default.Home, false, onDashboardClick)
        BottomBarItem("Manage", Icons.Default.Build, false, onManageClick)
        BottomBarItem("Spending", Icons.Default.Home, true, onSpendingClick)
        BottomBarItem("Alerts", Icons.Default.Notifications, false, onAlertsClick)
        BottomBarItem("Profile", Icons.Default.Person, false, onProfileClick)
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) DashboardAccent else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
