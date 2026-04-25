@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.subzero

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.subzero.network.BillingCycleResponse
import com.example.subzero.network.CategoryResponse
import java.text.NumberFormat
import java.util.Locale

private val ManageBlue500 = Color(0xFF3B82F6)
private val ManageBlue600 = Color(0xFF2563EB)
private val ManagePurple500 = Color(0xFFA855F7)
private val ManageIndigo500 = Color(0xFF6366F1)
private val ManageWhite = Color(0xFFFFFFFF)
private val ManageGray100 = Color(0xFFF3F4F6)
private val ManageGray500 = Color(0xFF6B7280)
private val ManageGray700 = Color(0xFF374151)

private enum class ManageBottomTab {
    Manage, Insights, Alerts, Profile
}

@Composable
fun SubscriptionManagementScreen(
    vm: SubscriptionViewModel,
    onGoToDashboard: () -> Unit
) {
    val categories by vm.categories.collectAsState()
    val billingCycles by vm.billingCycles.collectAsState()
    val items by vm.items.collectAsState()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val months = listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr")
    var selectedMonth by remember { mutableStateOf("Nov") }

    val filteredItems = items.filter { it.month == selectedMonth }

    var showAddDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Subscription?>(null) }
    var deleting by remember { mutableStateOf<Subscription?>(null) }
    var viewing by remember { mutableStateOf<Subscription?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(ManageBottomTab.Manage) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = ManageWhite,
                    actionIconContentColor = ManageWhite
                ),
                title = {
                    Column {
                        Text(
                            text = "Central Management",
                            fontWeight = FontWeight.Bold,
                            color = ManageWhite
                        )
                        Text(
                            text = "Manage subscriptions by month",
                            style = MaterialTheme.typography.bodySmall,
                            color = ManageWhite.copy(alpha = 0.9f)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onGoToDashboard) {
                        Text("Back", color = ManageWhite)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    errorMessage = null
                    showAddDialog = true
                },
                containerColor = ManageBlue600,
                contentColor = ManageWhite
            ) {
                Text("+")
            }
        },
        bottomBar = {
            ManageBottomBar(
                selectedTab = selectedTab,
                onManageClick = { selectedTab = ManageBottomTab.Manage },
                onInsightsClick = { selectedTab = ManageBottomTab.Insights },
                onAlertsClick = { selectedTab = ManageBottomTab.Alerts },
                onProfileClick = { selectedTab = ManageBottomTab.Profile }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(ManageBlue500, ManagePurple500, ManageIndigo500)
                    )
                )
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonthSelectorRow(
                months = months,
                selectedMonth = selectedMonth,
                onMonthSelected = { selectedMonth = it }
            )

            errorMessage?.let { message ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ManageWhite)
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (filteredItems.isEmpty()) {
                EmptyManagementState(
                    month = selectedMonth,
                    onAddClick = { showAddDialog = true }
                )
            } else {
                ManagementSummaryCard(
                    month = selectedMonth,
                    total = filteredItems.sumOf { it.cost },
                    count = filteredItems.size,
                    currency = currency
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredItems) { sub ->
                        SubscriptionManagementCard(
                            sub = sub,
                            currency = currency,
                            onView = { viewing = sub },
                            onEdit = { editing = sub },
                            onDelete = { deleting = sub }
                        )
                    }
                }
            }
        }

        // Add dialog — uses API-backed categories/billing cycles
        if (showAddDialog) {
            SubscriptionDialog(
                title = "Add Subscription",
                buttonText = "Add",
                initial = null,
                categories = categories,
                billingCycles = billingCycles,
                onDismiss = { showAddDialog = false },
                onSave = { name, cost, renewalDate, categoryId, billingCycleId ->
                    vm.add(name, cost, categoryId, billingCycleId, renewalDate)
                    showAddDialog = false
                }
            )
        }

        // View dialog
        viewing?.let { subscription ->
            AlertDialog(
                onDismissRequest = { viewing = null },
                title = { Text(subscription.name) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Cost: ${currency.format(subscription.cost)}")
                        Text("Category: ${subscription.category ?: "—"}")
                        Text("Billing Cycle: ${subscription.billingCycle ?: "—"}")
                        Text("Renewal Date: ${subscription.renewalDate ?: "—"}")
                        Text("Month: ${subscription.month}")
                        Text("ID: ${subscription.id}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewing = null }) {
                        Text("Close", color = ManageBlue600)
                    }
                }
            )
        }

        // Edit dialog — also uses API-backed categories/billing cycles
        editing?.let { subscription ->
            SubscriptionDialog(
                title = "Edit Subscription",
                buttonText = "Save",
                initial = subscription,
                categories = categories,
                billingCycles = billingCycles,
                onDismiss = { editing = null },
                onSave = { name, cost, renewalDate, categoryId, billingCycleId ->
                    vm.edit(subscription.id, name, cost, categoryId, billingCycleId, renewalDate)
                    editing = null
                    errorMessage = null
                }
            )
        }

        // Delete dialog
        deleting?.let { subscription ->
            AlertDialog(
                onDismissRequest = { deleting = null },
                title = { Text("Delete Subscription") },
                text = {
                    Text("Are you sure you want to delete ${subscription.name}?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.delete(subscription.id)
                            deleting = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleting = null }) {
                        Text("Cancel", color = ManageBlue600)
                    }
                }
            )
        }
    }
}

// ── Month selector ────────────────────────────────────────────────────────────

@Composable
private fun MonthSelectorRow(
    months: List<String>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        months.forEach { month ->
            val selected = month == selectedMonth
            Card(
                modifier = Modifier.clickable { onMonthSelected(month) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) ManageWhite else ManageWhite.copy(alpha = 0.18f)
                )
            ) {
                Text(
                    text = month,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (selected) ManageBlue600 else ManageWhite,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

// ── Summary card ──────────────────────────────────────────────────────────────

@Composable
private fun ManagementSummaryCard(
    month: String,
    total: Double,
    count: Int,
    currency: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ManageWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "$month Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ManageGray700
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Monthly Total: ${currency.format(total)}", color = ManageGray700)
            Text(text = "Subscriptions: $count", color = ManageGray700)
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyManagementState(
    month: String,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ManageWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "No subscriptions in $month",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ManageGray700
            )
            Text(
                text = "Add your first subscription for $month.",
                style = MaterialTheme.typography.bodyMedium,
                color = ManageGray500
            )
            Button(onClick = onAddClick) {
                Text("Add First Subscription")
            }
        }
    }
}

// ── Subscription list card ────────────────────────────────────────────────────

@Composable
private fun SubscriptionManagementCard(
    sub: Subscription,
    currency: NumberFormat,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ManageWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = sub.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ManageGray700
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Cost: ${currency.format(sub.cost)}", color = ManageGray700)
            Text(text = "Category: ${sub.category ?: "—"}", color = ManageGray700)
            Text(text = "Billing Cycle: ${sub.billingCycle ?: "—"}", color = ManageGray700)
            Text(text = "Renewal: ${sub.renewalDate ?: "—"}", color = ManageGray500)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onView) { Text("View", color = ManageBlue600) }
                TextButton(onClick = onEdit) { Text("Edit", color = ManageBlue600) }
                TextButton(onClick = onDelete) { Text("Delete", color = Color.Red) }
            }
        }
    }
}

// ── Add / Edit dialog ─────────────────────────────────────────────────────────

@Composable
private fun SubscriptionDialog(
    title: String,
    buttonText: String,
    initial: Subscription?,
    categories: List<CategoryResponse>,
    billingCycles: List<BillingCycleResponse>,
    onDismiss: () -> Unit,
    onSave: (name: String, cost: Double, renewalDate: String, categoryId: Int, billingCycleId: Int) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var costText by remember { mutableStateOf(initial?.cost?.toString() ?: "") }
    var renewalDate by remember { mutableStateOf(initial?.renewalDate?.take(10) ?: "") }

    // Pre-select category/billing cycle if editing
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull { it.id == initial?.categoryId })
    }
    var selectedBillingCycle by remember {
        mutableStateOf(billingCycles.firstOrNull { it.id == initial?.billingCycleId })
    }

    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; localError = null },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Cost
                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it; localError = null },
                    label = { Text("Cost") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Renewal date
                OutlinedTextField(
                    value = renewalDate,
                    onValueChange = { renewalDate = it; localError = null },
                    label = { Text("Renewal Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category picker
                Text("Category", fontWeight = FontWeight.SemiBold, color = ManageGray700)
                if (categories.isEmpty()) {
                    Text("Loading categories…", color = ManageGray500,
                        style = MaterialTheme.typography.bodySmall)
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val selected = selectedCategory?.id == cat.id
                            Card(
                                modifier = Modifier.clickable {
                                    selectedCategory = cat
                                    localError = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) ManageBlue600 else ManageGray100
                                )
                            ) {
                                Text(
                                    text = cat.name,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = if (selected) ManageWhite else ManageGray700,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Billing cycle picker
                Text("Billing Cycle", fontWeight = FontWeight.SemiBold, color = ManageGray700)
                if (billingCycles.isEmpty()) {
                    Text("Loading billing cycles…", color = ManageGray500,
                        style = MaterialTheme.typography.bodySmall)
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        billingCycles.forEach { cycle ->
                            val selected = selectedBillingCycle?.id == cycle.id
                            Card(
                                modifier = Modifier.clickable {
                                    selectedBillingCycle = cycle
                                    localError = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) ManageBlue600 else ManageGray100
                                )
                            ) {
                                Text(
                                    text = cycle.name,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = if (selected) ManageWhite else ManageGray700,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Error
                localError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val cost = costText.toDoubleOrNull()
                when {
                    name.isBlank() -> localError = "Name is required"
                    cost == null || cost <= 0 -> localError = "Enter a valid cost"
                    renewalDate.isBlank() -> localError = "Renewal date is required"
                    selectedCategory == null -> localError = "Select a category"
                    selectedBillingCycle == null -> localError = "Select a billing cycle"
                    else -> onSave(name, cost, renewalDate, selectedCategory!!.id, selectedBillingCycle!!.id)
                }
            }) {
                Text(buttonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ManageBlue600)
            }
        }
    )
}

// ── Bottom bar ────────────────────────────────────────────────────────────────

@Composable
private fun ManageBottomBar(
    selectedTab: ManageBottomTab,
    onManageClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ManageWhite)
            .navigationBarsPadding()
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ManageBottomBarItem(
            label = "Manage",
            icon = Icons.Default.GridView,
            selected = selectedTab == ManageBottomTab.Manage,
            onClick = onManageClick
        )
        ManageBottomBarItem(
            label = "Insights",
            icon = Icons.Default.TrendingUp,
            selected = selectedTab == ManageBottomTab.Insights,
            onClick = onInsightsClick
        )
        ManageBottomBarItem(
            label = "Alerts",
            icon = Icons.Default.Notifications,
            selected = selectedTab == ManageBottomTab.Alerts,
            onClick = onAlertsClick
        )
        ManageBottomBarItem(
            label = "Profile",
            icon = Icons.Default.Person,
            selected = selectedTab == ManageBottomTab.Profile,
            onClick = onProfileClick
        )
    }
}

@Composable
private fun ManageBottomBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) ManageBlue600 else ManageGray500

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        androidx.compose.material3.Icon(
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