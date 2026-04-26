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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.subzero.global.Utility
import com.example.subzero.network.BillingCycleResponse
import com.example.subzero.network.CategoryResponse
import com.example.subzero.network.MonthlySpendResponse
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    onGoToDashboard: () -> Unit,
    onGoToInsights: () -> Unit = {},
    onGoToAlerts: () -> Unit = {},
    onGoToProfile: () -> Unit = {}
) {
    val categories      by vm.categories.collectAsState()
    val billingCycles   by vm.billingCycles.collectAsState()
    val items           by vm.items.collectAsState()
    val monthlyHistory  by vm.monthlySpendHistory.collectAsState()
    val monthlyHides    by vm.monthlyHides.collectAsState()
    val monthlyBudget   by vm.monthlyBudget.collectAsState()
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val util     = remember { Utility() }

    // Derive the months list from API history (same source as dashboard), fall back to last 6 months
    val months = remember(monthlyHistory) {
        monthlyHistory.map { it.month_label }.ifEmpty {
            listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr")
        }
    }
    var selectedMonth by remember(months) { mutableStateOf(months.lastOrNull() ?: "Apr") }

    // Carry-over + soft-delete filter:
    val selectedIdx  = months.indexOf(selectedMonth)
    val filteredItems = items.filter { sub ->
        val subIdx   = months.indexOf(sub.month).let { if (it == -1) 0 else it }
        val hideFrom = monthlyHides[sub.id] ?: Int.MAX_VALUE
        subIdx <= selectedIdx && selectedIdx < hideFrom
    }

    // Unique categories available in the current month view (for filter chips)
    val availableCategories = remember(filteredItems) {
        filteredItems.mapNotNull { it.category }.distinct().sorted()
    }
    // Normalised monthly total before search/filter (used in summary card + overage check).
    // Matches the normalisation used by DashboardUtils and Insights so the number is consistent.
    val monthTotal = filteredItems.sumOf { util.normaliseToMonthly(it.cost, it.billingCycle) }

    var showAddDialog  by remember { mutableStateOf(false) }
    var editing    by remember { mutableStateOf<Subscription?>(null) }
    var removing   by remember { mutableStateOf<Subscription?>(null) }   // soft remove (month-forward)
    var deleting   by remember { mutableStateOf<Subscription?>(null) }   // hard delete (API)
    var viewing    by remember { mutableStateOf<Subscription?>(null) }
    var errorMessage  by remember { mutableStateOf<String?>(null) }
    var selectedTab   by remember { mutableStateOf(ManageBottomTab.Manage) }
    var searchQuery   by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf<String?>(null) }

    // Items after search text and category chip filters are applied
    val displayItems = filteredItems.filter { sub ->
        (searchQuery.isBlank() || sub.name.contains(searchQuery, ignoreCase = true)) &&
        (categoryFilter == null || sub.category == categoryFilter)
    }

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
                onInsightsClick = { selectedTab = ManageBottomTab.Insights; onGoToInsights() },
                onAlertsClick   = { selectedTab = ManageBottomTab.Alerts;   onGoToAlerts() },
                onProfileClick  = { selectedTab = ManageBottomTab.Profile;  onGoToProfile() }
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

            SearchAndFilterRow(
                searchQuery      = searchQuery,
                onSearchChange   = { searchQuery = it },
                categories       = availableCategories,
                selectedCategory = categoryFilter,
                onCategorySelected = { categoryFilter = it }
            )

            // Budget overage banner (uses full-month total, not filtered)
            if (monthlyBudget > 0 && monthTotal > monthlyBudget) {
                ManageOverageBanner(
                    overage  = monthTotal - monthlyBudget,
                    budget   = monthlyBudget,
                    currency = currency
                )
            }

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

            if (displayItems.isEmpty()) {
                if (filteredItems.isEmpty()) {
                    EmptyManagementState(
                        month = selectedMonth,
                        onAddClick = { showAddDialog = true }
                    )
                } else {
                    NoSearchResultsState(
                        onClearFilter = { searchQuery = ""; categoryFilter = null }
                    )
                }
            } else {
                ManagementSummaryCard(
                    month    = selectedMonth,
                    total    = monthTotal,
                    count    = filteredItems.size,
                    currency = currency
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(displayItems) { sub ->
                        SubscriptionManagementCard(
                            sub = sub,
                            currency = currency,
                            util = util,
                            onView   = { viewing  = sub },
                            onEdit   = { editing  = sub },
                            onRemove = { removing = sub }
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

        // View dialog — also contains the hard-delete option
        viewing?.let { subscription ->
            AlertDialog(
                onDismissRequest = { viewing = null },
                title = { Text(subscription.name, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Cost: ${currency.format(subscription.cost)}")
                        Text("Category: ${subscription.category ?: "—"}")
                        Text("Billing Cycle: ${subscription.billingCycle ?: "—"}")
                        Text("Renewal Date: ${util.formatRenewalDate(subscription.renewalDate)}")
                        Text("Month: ${subscription.month}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewing = null
                                deleting = subscription
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Permanently Delete", color = ManageWhite)
                        }
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

        // Soft-remove dialog — hides from this month onwards, keeps history intact
        removing?.let { subscription ->
            AlertDialog(
                onDismissRequest = { removing = null },
                title = { Text("Remove from $selectedMonth onwards?") },
                text = {
                    Text(
                        "${subscription.name} will no longer appear in $selectedMonth and future months, " +
                        "but will still show in earlier months where it was active."
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        vm.removeFromMonth(subscription.id, months.indexOf(selectedMonth))
                        removing = null
                    }) { Text("Remove") }
                },
                dismissButton = {
                    TextButton(onClick = { removing = null }) {
                        Text("Cancel", color = ManageBlue600)
                    }
                }
            )
        }

        // Hard-delete confirmation (reached via View → Permanently Delete)
        deleting?.let { subscription ->
            AlertDialog(
                onDismissRequest = { deleting = null },
                title = { Text("Permanently Delete?") },
                text = {
                    Text(
                        "This will permanently remove ${subscription.name} from all months and cannot be undone."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { vm.delete(subscription.id); deleting = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Delete Forever") }
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
    util: Utility,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
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
            Text(
                text = "Renewal: ${util.formatRenewalDate(sub.renewalDate)}",
                color = ManageGray500
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onView)   { Text("View",   color = ManageBlue600) }
                TextButton(onClick = onEdit)   { Text("Edit",   color = ManageBlue600) }
                TextButton(onClick = onRemove) { Text("Remove", color = Color.Red) }
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

                // Renewal date — date picker
                DatePickerField(
                    value = renewalDate,
                    onDateSelected = { renewalDate = it; localError = null }
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

// ── Search + filter row ───────────────────────────────────────────────────────

@Composable
private fun SearchAndFilterRow(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            placeholder   = { Text("Search subscriptions…", color = ManageWhite.copy(alpha = 0.55f)) },
            leadingIcon   = {
                Icon(Icons.Default.Search, contentDescription = null,
                    tint = ManageWhite.copy(alpha = 0.7f))
            },
            trailingIcon  = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search",
                            tint = ManageWhite)
                    }
                }
            },
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = ManageWhite,
                unfocusedBorderColor = ManageWhite.copy(alpha = 0.45f),
                focusedTextColor     = ManageWhite,
                unfocusedTextColor   = ManageWhite,
                cursorColor          = ManageWhite
            ),
            singleLine    = true,
            shape         = RoundedCornerShape(16.dp),
            modifier      = Modifier.fillMaxWidth()
        )

        if (categories.isNotEmpty()) {
            Row(
                modifier              = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                val allSelected = selectedCategory == null
                Card(
                    modifier = Modifier.clickable { onCategorySelected(null) },
                    shape    = RoundedCornerShape(20.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = if (allSelected) ManageWhite else ManageWhite.copy(alpha = 0.18f)
                    )
                ) {
                    Text(
                        text       = "All",
                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        color      = if (allSelected) ManageBlue600 else ManageWhite,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                categories.forEach { cat ->
                    val selected = selectedCategory == cat
                    Card(
                        modifier = Modifier.clickable {
                            onCategorySelected(if (selected) null else cat)
                        },
                        shape  = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) ManageWhite else ManageWhite.copy(alpha = 0.18f)
                        )
                    ) {
                        Text(
                            text       = cat,
                            modifier   = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            color      = if (selected) ManageBlue600 else ManageWhite,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ── Budget overage banner (management) ───────────────────────────────────────

@Composable
private fun ManageOverageBanner(overage: Double, budget: Double, currency: NumberFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                tint               = Color(0xFFD32F2F),
                modifier           = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text       = "Over Budget",
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFFD32F2F),
                    style      = MaterialTheme.typography.titleSmall
                )
                Text(
                    text  = "${currency.format(overage)} over your ${currency.format(budget)} monthly budget.",
                    color = Color(0xFFB71C1C),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ── No-results state (search/filter produced zero matches) ────────────────────

@Composable
private fun NoSearchResultsState(onClearFilter: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = ManageWhite)
    ) {
        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement   = Arrangement.spacedBy(10.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = Icons.Default.SearchOff,
                contentDescription = null,
                tint               = ManageGray500,
                modifier           = Modifier.size(36.dp)
            )
            Text(
                text       = "No results",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = ManageGray700
            )
            Text(
                text  = "No subscriptions match your search or filter.",
                style = MaterialTheme.typography.bodyMedium,
                color = ManageGray500
            )
            TextButton(onClick = onClearFilter) {
                Text("Clear filters", color = ManageBlue600)
            }
        }
    }
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
            .height(68.dp)
            .background(ManageWhite),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ManageBottomBarItem(
            label = "Manage",
            icon = painterResource(R.drawable.ic_grid),
            selected = selectedTab == ManageBottomTab.Manage,
            onClick = onManageClick
        )
        ManageBottomBarItem(
            label = "Insights",
            icon = painterResource(R.drawable.ic_trending_up),
            selected = selectedTab == ManageBottomTab.Insights,
            onClick = onInsightsClick
        )
        ManageBottomBarItem(
            label = "Alerts",
            icon = painterResource(R.drawable.ic_noti_bell),
            selected = selectedTab == ManageBottomTab.Alerts,
            onClick = onAlertsClick
        )
        ManageBottomBarItem(
            label = "Profile",
            icon = painterResource(R.drawable.ic_person),
            selected = selectedTab == ManageBottomTab.Profile,
            onClick = onProfileClick
        )
    }
}

@Composable
private fun ManageBottomBarItem(
    label: String,
    icon: Painter,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) ManageBlue600 else ManageGray500

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { onClick() }
    ) {
        androidx.compose.material3.Icon(
            painter = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val displayValue = remember(value) {
        if (value.isBlank()) "" else Utility().formatRenewalDate(value)
    }

    OutlinedTextField(
        value          = displayValue,
        onValueChange  = {},
        readOnly       = true,
        label          = { Text("Renewal Date") },
        placeholder    = { Text("Tap to pick a date") },
        trailingIcon   = {
            IconButton(onClick = { showPicker = true }) {
                Icon(
                    imageVector        = Icons.Default.DateRange,
                    contentDescription = "Pick date",
                    tint               = ManageBlue600
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
    )

    if (showPicker) {
        val initialMillis = remember(value) {
            if (value.isNotBlank()) ymdToUtcMillis(value)
            else System.currentTimeMillis()
        }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(utcMillisToYmd(millis))
                    }
                    showPicker = false
                }) { Text("OK", color = ManageBlue600) }
            },
            dismissButton    = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = ManageGray500)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// parse dates
private fun ymdToUtcMillis(date: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.parse(date.take(10))?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

// format date for api sub
private fun utcMillisToYmd(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}