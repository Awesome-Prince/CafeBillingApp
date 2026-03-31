package com.cafe.billing.ui.bill

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cafe.billing.data.models.CartItem
import com.cafe.billing.data.models.SalesOrder
import com.cafe.billing.utils.DateUtils
import com.cafe.billing.viewmodel.BillViewModel

// ============================================================
// BILL SUMMARY SCREEN
// Displays the final receipt for a completed order.
// Staff can print it or start a new order.
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(
    orderId: Int,
    onNavigateBack: () -> Unit,
    onNewOrder: () -> Unit,
    viewModel: BillViewModel = hiltViewModel()
) {
    val order     by viewModel.order.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context   = LocalContext.current

    // Load the order when this screen is first shown
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Summary") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    // Print / Save as PDF
                    order?.let { o ->
                        IconButton(onClick = { viewModel.printBill(context, o) }) {
                            Icon(Icons.Default.Print, "Print Bill",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            order == null -> {
                Box(Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center) {
                    Text("Order not found", color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                val o = order!!
                BillContent(
                    order         = o,
                    modifier      = Modifier.padding(padding),
                    onPrint       = { viewModel.printBill(context, o) },
                    onNewOrder    = onNewOrder
                )
            }
        }
    }
}

// ── Bill Content ───────────────────────────────────────────

@Composable
fun BillContent(
    order: SalesOrder,
    modifier: Modifier = Modifier,
    onPrint: () -> Unit,
    onNewOrder: () -> Unit
) {
    val cartItems = remember(order) { order.getCartItems() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // ── Receipt card ──────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {

                    // Café header
                    Text(
                        text  = "☕ CAFÉ RECEIPT",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "Order #${order.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text  = DateUtils.formatFull(order.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    // Column headers
                    BillHeaderRow()

                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        // ── Line items ────────────────────────────────────
        items(cartItems) { cartItem ->
            BillLineItem(cartItem)
        }

        // ── Total row ─────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text  = "TOTAL",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text  = DateUtils.formatCurrency(order.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ── Thank you note ────────────────────────────────
        item {
            Text(
                text  = "🙏 Thank you for visiting!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }

        // ── Action buttons ────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Print button
                OutlinedButton(
                    onClick  = onPrint,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Print, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Print / PDF")
                }

                // New order button
                Button(
                    onClick  = onNewOrder,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.AddCircle, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("New Order")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Bill Header Row ────────────────────────────────────────

@Composable
fun BillHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Item",  modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text("Qty",   modifier = Modifier.weight(0.6f),
            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center)
        Text("Price", modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End)
        Text("Total", modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End)
    }
}

// ── Bill Line Item ─────────────────────────────────────────

@Composable
fun BillLineItem(cartItem: CartItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(cartItem.menuItem.name,
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium)
            Text("×${cartItem.quantity}",
                modifier = Modifier.weight(0.6f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold)
            Text(DateUtils.formatCurrency(cartItem.menuItem.price),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(DateUtils.formatCurrency(cartItem.lineTotal),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.SemiBold)
        }
    }
}
