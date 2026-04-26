@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.subzero

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.subzero.network.MonthlySpendResponse
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.res.painterResource

private val Blue500   = Color(0xFF3B82F6)
private val Blue600   = Color(0xFF2563EB)
private val Purple500 = Color(0xFFA855F7)
private val Indigo500 = Color(0xFF6366F1)
private val White     = Color(0xFFFFFFFF)
private val Gray50    = Color(0xFFF9FAFB)
private val Gray200   = Color(0xFFE5E7EB)
private val Gray500   = Color(0xFF6B7280)
private val Gray700   = Color(0xFF374151)
private val Green500  = Color(0xFF22C55E)
private val Orange500 = Color(0xFFF59E0B)

private enum class DashboardTab { Manage, Insights, Alerts, Profile }

@Composable
fun SubscriptionDashboardScreen(
    vm: SubscriptionViewModel,
    onGoToManage: () -> Unit,
    onGoToInsights: () -> Unit = {},
    onGoToAlerts: () -> Unit = {},
    onGoToProfile: () -> Unit = {}
) {
    val allItems          by vm.items.collectAsState()
    val monthlyBudget     by vm.monthlyBudget.collectAsState()
    val monthlyHistory    by vm.monthlySpendHistory.collectAsState()
    val isLoading         by vm.isLoading.collectAsState()
    val budgetResult      by vm.budgetUpdateResult.collectAsState()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    // Build month labels from API history; fall back to last 6 calendar months
    val months = remember(monthlyHistory) {
        monthlyHistory.map { it.month_label }.ifEmpty {
            listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr")
        }
    }

    var selectedMonth     by remember(months) { mutableStateOf(months.lastOrNull() ?: "Apr") }
    var selectedTab       by remember { mutableStateOf(DashboardTab.Manage) }
    var showBudgetDialog  by remember { mutableStateOf(false) }

    // Snackbar for budget save feedback
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(budgetResult) {
        if (budgetResult != null) {
            snackbarHostState.showSnackbar(
                if (budgetResult == true) "Budget saved!" else "Failed to save budget"
            )
            vm.clearBudgetResult()
        }
    }

    // Carry-over: include subscriptions added in selectedMonth AND all earlier months,
    // mirroring the management screen. Spend is normalised to monthly equivalent.
    val selectedMonthItems = DashboardUtils.filterWithCarryOver(allItems, months, selectedMonth)
    val totalSpend         = DashboardUtils.calculateTotalSpend(selectedMonthItems)
    val remainingBudget    = DashboardUtils.calculateRemainingBudget(monthlyBudget, totalSpend)

    // Chart: use history when available, otherwise build from subscriptions with carry-over
    // accumulation so each bar matches the corresponding month's summary card total.
    val monthlyChartData: List<MonthlySpend> = remember(monthlyHistory, allItems, months) {
        if (monthlyHistory.isNotEmpty()) {
            monthlyHistory.map { MonthlySpend(it.month_label, it.total_spend) }
        } else {
            DashboardUtils.buildMonthlySpendData(allItems, months)
        }
    }

    if (showBudgetDialog) {
        BudgetEditDialog(
            currentBudget = monthlyBudget,
            currency = currency,
            onDismiss = { showBudgetDialog = false },
            onSave = { newBudget ->
                vm.updateBudget(newBudget)
                showBudgetDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            DashboardBottomBar(
                selectedTab     = selectedTab,
                onManageClick   = { selectedTab = DashboardTab.Manage;   onGoToManage() },
                onInsightsClick = { selectedTab = DashboardTab.Insights; onGoToInsights() },
                onAlertsClick   = { selectedTab = DashboardTab.Alerts;   onGoToAlerts() },
                onProfileClick  = { selectedTab = DashboardTab.Profile;  onGoToProfile() }
            )
        },
        containerColor = Gray50
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Blue500, Purple500, Indigo500)))
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = White
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    item { DashboardHeader(selectedMonth) }

                    item {
                        DashboardMonthSelectorRow(
                            months          = months,
                            selectedMonth   = selectedMonth,
                            onMonthSelected = { selectedMonth = it }
                        )
                    }

                    item {
                        SpendingChartCard(
                            monthlyData   = monthlyChartData,
                            selectedMonth = selectedMonth
                        )
                    }

                    item {
                        SummaryCard(
                            budget     = monthlyBudget,
                            totalSpend = totalSpend,
                            remaining  = remainingBudget,
                            currency   = currency,
                            onEditBudget = { showBudgetDialog = true }
                        )
                    }

                    if (monthlyBudget > 0 && totalSpend > monthlyBudget) {
                        item {
                            BudgetOverageCard(
                                overage  = totalSpend - monthlyBudget,
                                budget   = monthlyBudget,
                                currency = currency
                            )
                        }
                    }

                    item {
                        Text(
                            text       = "${selectedMonth.uppercase()} SUBSCRIPTIONS",
                            color      = White.copy(alpha = 0.96f),
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (selectedMonthItems.isEmpty()) {
                        item {
                            AddFirstSubscriptionCard(
                                month           = selectedMonth,
                                onAddFirstClick = onGoToManage
                            )
                        }
                    } else {
                        item {
                            ThisMonthSubscriptionsCard(
                                month         = selectedMonth,
                                items         = selectedMonthItems,
                                currency      = currency,
                                onManageClick = onGoToManage
                            )
                        }

                        items(selectedMonthItems) { sub ->
                            IndividualSubscriptionCard(
                                subscription  = sub,
                                currency      = currency,
                                onManageClick = onGoToManage
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

// ── Budget Overage Card ───────────────────────────────────────────────────────

@Composable
private fun BudgetOverageCard(overage: Double, budget: Double, currency: NumberFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier             = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                tint               = Color(0xFFD32F2F),
                modifier           = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text       = "Over Budget",
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFFD32F2F),
                    style      = MaterialTheme.typography.titleSmall
                )
                Text(
                    text  = "You're ${currency.format(overage)} over your ${currency.format(budget)} monthly budget.",
                    color = Color(0xFFB71C1C),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ── Budget Edit Dialog ────────────────────────────────────────────────────────

@Composable
private fun BudgetEditDialog(
    currentBudget: Double,
    currency: NumberFormat,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Monthly Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Enter your monthly subscription budget:",
                    color = Gray500,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = {
                        budgetText = it
                        error = null
                    },
                    label = { Text("Budget (\$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsed = budgetText.toDoubleOrNull()
                when {
                    parsed == null -> error = "Please enter a valid number"
                    parsed < 0     -> error = "Budget cannot be negative"
                    else           -> onSave(parsed)
                }
            }) {
                Text("Save", color = Blue600, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray500)
            }
        }
    )
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(selectedMonth: String) {
    Column {
        Text(
            text       = "Subscriptions",
            color      = White,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = DashboardUtils.buildHeaderText(selectedMonth),
            color = White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ── Month selector ────────────────────────────────────────────────────────────

@Composable
private fun DashboardMonthSelectorRow(
    months: List<String>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        months.forEach { month ->
            val selected = month == selectedMonth
            Card(
                modifier = Modifier.clickable { onMonthSelected(month) },
                shape    = RoundedCornerShape(10.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = if (selected) White else White.copy(alpha = 0.18f)
                )
            ) {
                Box(
                    modifier        = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = month,
                        color      = if (selected) Blue600 else White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Bar chart ─────────────────────────────────────────────────────────────────

@Composable
private fun SpendingChartCard(
    monthlyData: List<MonthlySpend>,
    selectedMonth: String
) {
    Card(
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.14f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = "Subscription Spend",
                color      = White,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier              = Modifier.fillMaxWidth().height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                val maxValue = (monthlyData.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(1.0)
                monthlyData.forEach { item ->
                    val selected = item.month == selectedMonth
                    val fraction = (item.amount / maxValue).toFloat().coerceIn(0.08f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (item.amount > 0) {
                            Text(
                                text  = "$${item.amount.toInt()}",
                                color = White.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height((100 * fraction).dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) White else White.copy(alpha = 0.45f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = item.month, color = White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// ── Summary card ──────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    budget: Double,
    totalSpend: Double,
    remaining: Double,
    currency: NumberFormat,
    onEditBudget: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Monthly Summary",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Gray700,
                    modifier   = Modifier.weight(1f)
                )
                TextButton(onClick = onEditBudget) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit budget",
                        modifier = Modifier.size(16.dp),
                        tint = Blue600
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Budget", color = Blue600, style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow("Monthly Budget",     currency.format(budget),     Gray700)
            Spacer(modifier = Modifier.height(10.dp))
            SummaryRow("Subscription Spend", currency.format(totalSpend), Gray700)
            Spacer(modifier = Modifier.height(10.dp))
            SummaryRow(
                "Remaining Budget",
                currency.format(remaining),
                if (remaining >= 0) Green500 else Orange500
            )
            if (budget > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val pct = (totalSpend / budget * 100).toInt().coerceIn(0, 100)
                LinearProgressIndicator(
                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color    = if (pct < 80) Blue500 else Orange500,
                    trackColor = Gray200
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Gray200)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text     = if (budget > 0) "${DashboardUtils.budgetPercentLeft(budget, remaining)}% of budget remaining."
                else "No budget set. Tap 'Budget' to set one.",
                color    = Gray500,
                style    = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier        = Modifier.size(24.dp).clip(CircleShape).background(Blue500.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Blue600))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text       = label,
            modifier   = Modifier.weight(1f),
            color      = Gray700,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(text = value, color = valueColor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

// ── Subscription list cards ───────────────────────────────────────────────────

@Composable
private fun AddFirstSubscriptionCard(month: String, onAddFirstClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(DashboardUtils.buildSubscriptionTitle(month), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Gray700)
            Spacer(modifier = Modifier.height(10.dp))
            Text("You have no subscriptions for $month yet.", color = Gray500, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onAddFirstClick) { Text("Add First Subscription", color = Blue600) }
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
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(DashboardUtils.buildSubscriptionTitle(month), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Gray700)
            Spacer(modifier = Modifier.height(12.dp))
            items.forEachIndexed { index, sub ->
                DashboardSubscriptionLineItem(name = sub.name, amount = "${currency.format(sub.cost)}/mo")
                if (index != items.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Gray200)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onManageClick) { Text("Manage Subscriptions", color = Blue600) }
        }
    }
}

@Composable
private fun IndividualSubscriptionCard(
    subscription: Subscription,
    currency: NumberFormat,
    onManageClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(subscription.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Gray700)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Monthly Cost: ${currency.format(subscription.cost)}", color = Gray700)
            if (subscription.renewalDate != null) {
                Text("Next Renewal: ${subscription.renewalDate.take(10)}", color = Gray500)
            }
            Text("Month: ${subscription.month}", color = Gray500)
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onManageClick) { Text("View in Manage", color = Blue600) }
        }
    }
}

@Composable
private fun DashboardSubscriptionLineItem(name: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Blue600))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = name, modifier = Modifier.weight(1f), color = Gray700, style = MaterialTheme.typography.bodyLarge)
        Text(text = amount, color = Gray700, fontWeight = FontWeight.SemiBold)
    }
}

// ── Bottom bar ────────────────────────────────────────────────────────────────

@Composable
private fun DashboardBottomBar(
    selectedTab: DashboardTab,
    onManageClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(White),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        DashboardBottomBarItem("Manage",   painterResource(R.drawable.ic_grid),        selectedTab == DashboardTab.Manage,   onManageClick)
        DashboardBottomBarItem("Insights", painterResource(R.drawable.ic_trending_up),    selectedTab == DashboardTab.Insights, onInsightsClick)
        DashboardBottomBarItem("Alerts",   painterResource(R.drawable.ic_noti_bell),   selectedTab == DashboardTab.Alerts,   onAlertsClick)
        DashboardBottomBarItem("Profile",  painterResource(R.drawable.ic_person),      selectedTab == DashboardTab.Profile,  onProfileClick)
    }
}

@Composable
private fun DashboardBottomBarItem(label: String, icon: Painter, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) Blue600 else Gray500
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(painter = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Normal)
    }
}
