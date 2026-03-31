package com.cafe.billing.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cafe.billing.data.models.SalesOrder
import com.cafe.billing.utils.DateUtils
import com.cafe.billing.viewmodel.SalesHistoryViewModel

// ============================================================
// SALES HISTORY SCREEN
// Shows:
//  1. Today's analytics summary cards (total sales, order count, avg order)
//  2. Full list of all past orders, newest first
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: SalesHistoryViewModel = hiltViewModel()
) {
    val allOrders      by viewModel.allOrders.collectAsState()
    val todayTotal     by viewModel.todaysTotalSales.collectAsState()
    val todayCount     by viewModel.todaysOrderCount.collectAsState()
    val todayAvg       by viewModel.todaysAverageOrder.collectAsState()
    val snackMsg       by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMsg) {
        snackMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Today's analytics ─────────────────────────
            item {
                Text(
                    "Today's Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnalyticsCard(
                        modifier = Modifier.weight(1f),
                        label = "Revenue",
                        value = DateUtils.formatCurrency(todayTotal),
                        icon  = Icons.Default.CurrencyRupee
                    )
                    AnalyticsCard(
                        modifier = Modifier.weight(1f),
                        label = "Orders",
                        value = todayCount.toString(),
                        icon  = Icons.Default.ReceiptLong
                    )
                    AnalyticsCard(
                        modifier = Modifier.weight(1f),
                        label = "Avg Order",
                        value = DateUtils.formatCurrency(todayAvg),
                        icon  = Icons.Default.TrendingUp
                    )
                }
            }

            // ── Divider ───────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "All Orders (${allOrders.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
            }

            // ── Empty state ───────────────────────────────
            if (allOrders.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ReceiptLong, null,
                                Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            Spacer(Modifier.height(12.dp))
                            Text("No orders yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Orders will appear here after billing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            } else {
                // ── Order cards ───────────────────────────
                items(allOrders, key = { it.id }) { order ->
                    SalesOrderCard(
                        order    = order,
                        onDelete = { viewModel.deleteOrder(order) }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Analytics Card ─────────────────────────────────────────

@Composable
fun AnalyticsCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null,
                Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(4.dp))
            Text(
                text  = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

// ── Sales Order Card ───────────────────────────────────────

@Composable
fun SalesOrderCard(
    order: SalesOrder,
    onDelete: () -> Unit
) {
    var expanded        by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val cartItems = remember(order) { order.getCartItems() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = "Order #${order.id}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = DateUtils.formatFull(order.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text  = DateUtils.formatCurrency(order.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Expand toggle
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }

                // Delete
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }

            // Expanded items list
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.menuItem.name} ×${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f))
                        Text(DateUtils.formatCurrency(item.lineTotal),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    }

    // Delete confirmation
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Order #${order.id}?") },
            text  = { Text("This will permanently remove the order record.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
