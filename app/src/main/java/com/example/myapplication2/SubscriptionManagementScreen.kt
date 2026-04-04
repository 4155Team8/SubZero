@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

private val ManagementBackgroundTop = Color(0xFFD81B60)
private val ManagementBackgroundBottom = Color(0xFFF06292)
private val ManagementCardColor = Color.White
private val ManagementTextDark = Color(0xFF2B2B2B)
private val ManagementMuted = Color(0xFF6F6F6F)

@Composable
fun SubscriptionManagementScreen(
    vm: SubscriptionViewModel,
    onGoToDashboard: () -> Unit
) {
    val items by vm.items.collectAsState()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val months = listOf("Feb", "Mar", "Apr", "May", "Jun", "Jul")
    var selectedMonth by remember { mutableStateOf("Jun") }

    val filteredItems = items.filter { it.month == selectedMonth }

    var showAddDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Subscription?>(null) }
    var deleting by remember { mutableStateOf<Subscription?>(null) }
    var viewing by remember { mutableStateOf<Subscription?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    Column {
                        Text(
                            text = "Central Management",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Manage subscriptions by month",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onGoToDashboard) {
                        Text("Dashboard", color = Color.White)
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
                containerColor = Color.White,
                contentColor = Color(0xFFE53935)
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(ManagementBackgroundTop, ManagementBackgroundBottom)
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
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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

        if (showAddDialog) {
            SubscriptionDialog(
                title = "Add Subscription",
                buttonText = "Add",
                initial = null,
                defaultMonth = selectedMonth,
                onDismiss = { showAddDialog = false },
                onSave = { name, cost, billingDay, month ->
                    try {
                        vm.add(name, cost, billingDay, month)
                        showAddDialog = false
                        errorMessage = null
                    } catch (e: IllegalArgumentException) {
                        errorMessage = e.message ?: "Invalid input"
                    }
                }
            )
        }

        viewing?.let { subscription ->
            AlertDialog(
                onDismissRequest = { viewing = null },
                title = { Text(subscription.name) },
                text = {
                    Column {
                        Text("Cost: ${currency.format(subscription.cost)}")
                        Text("Billing Day: ${subscription.billingDay}")
                        Text("Month: ${subscription.month}")
                        Text("ID: ${subscription.id}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewing = null }) {
                        Text("Close")
                    }
                }
            )
        }

        editing?.let { subscription ->
            SubscriptionDialog(
                title = "Edit Subscription",
                buttonText = "Save",
                initial = subscription,
                defaultMonth = subscription.month,
                onDismiss = { editing = null },
                onSave = { name, cost, billingDay, month ->
                    try {
                        vm.edit(subscription.id, name, cost, billingDay, month)
                        editing = null
                        errorMessage = null
                    } catch (e: IllegalArgumentException) {
                        errorMessage = e.message ?: "Invalid input"
                    }
                }
            )
        }

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
                        Text("Cancel")
                    }
                }
            )
        }
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
                Text(
                    text = month,
                    modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                    color = if (selected) Color(0xFFE53935) else Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

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
        colors = CardDefaults.cardColors(containerColor = ManagementCardColor)
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
                color = ManagementTextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Monthly Total: ${currency.format(total)}",
                color = ManagementTextDark
            )
            Text(
                text = "Subscriptions: $count",
                color = ManagementTextDark
            )
        }
    }
}

@Composable
private fun EmptyManagementState(
    month: String,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ManagementCardColor)
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
                color = ManagementTextDark
            )
            Text(
                text = "Add your first subscription for $month.",
                style = MaterialTheme.typography.bodyMedium,
                color = ManagementMuted
            )
            Button(onClick = onAddClick) {
                Text("Add First Subscription")
            }
        }
    }
}

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
        colors = CardDefaults.cardColors(containerColor = ManagementCardColor),
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
                color = ManagementTextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cost: ${currency.format(sub.cost)}",
                color = ManagementTextDark
            )
            Text(
                text = "Billing Day: ${sub.billingDay}",
                color = ManagementTextDark
            )
            Text(
                text = "Month: ${sub.month}",
                color = ManagementMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onView) {
                    Text("View")
                }
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun SubscriptionDialog(
    title: String,
    buttonText: String,
    initial: Subscription?,
    defaultMonth: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, String) -> Unit
) {
    val months = listOf("Feb", "Mar", "Apr", "May", "Jun", "Jul")

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var costText by remember { mutableStateOf(initial?.cost?.toString() ?: "") }
    var billingDayText by remember { mutableStateOf(initial?.billingDay?.toString() ?: "") }
    var month by remember { mutableStateOf(initial?.month ?: defaultMonth) }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        localError = null
                    },
                    label = { Text("Name") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = {
                        costText = it
                        localError = null
                    },
                    label = { Text("Cost") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = billingDayText,
                    onValueChange = {
                        billingDayText = it
                        localError = null
                    },
                    label = { Text("Billing Day (1-31)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text(
                    text = "Month",
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    months.forEach { item ->
                        Card(
                            modifier = Modifier.clickable {
                                month = item
                                localError = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (month == item) Color(0xFFD81B60) else Color(0xFFF3F3F3)
                            )
                        ) {
                            Text(
                                text = item,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (month == item) Color.White else Color.Black
                            )
                        }
                    }
                }

                localError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costText.toDoubleOrNull()
                    val billingDay = billingDayText.toIntOrNull()

                    when {
                        name.isBlank() -> localError = "Name is required"
                        cost == null || cost < 0 -> localError = "Enter a valid cost"
                        billingDay == null || billingDay !in 1..31 -> localError = "Billing day must be 1-31"
                        else -> onSave(name, cost, billingDay, month)
                    }
                }
            ) {
                Text(buttonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}