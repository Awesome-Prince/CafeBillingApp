package com.cafe.billing.ui.order

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cafe.billing.data.models.MenuItem
import com.cafe.billing.utils.DateUtils
import com.cafe.billing.viewmodel.OrderViewModel

// ============================================================
// ORDER SCREEN
// Main working screen for staff.
// Left/top: menu items grid with +/- controls.
// Bottom: sticky cart summary + "Place Order" button.
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    onNavigateToBill: (Int) -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val menuItems    by viewModel.filteredMenuItems.collectAsState()
    val cartList     by viewModel.cartList.collectAsState()
    val cartTotal    by viewModel.cartTotal.collectAsState()
    val cartCount    by viewModel.cartItemCount.collectAsState()
    val searchQuery  by viewModel.searchQuery.collectAsState()
    val savedOrderId by viewModel.savedOrderId.collectAsState()
    val snackMsg     by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate to Bill screen when order is saved
    LaunchedEffect(savedOrderId) {
        savedOrderId?.let { id ->
            viewModel.onOrderNavigated()
            onNavigateToBill(id)
        }
    }

    LaunchedEffect(snackMsg) {
        snackMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("☕ New Order") },
                actions = {
                    // History button
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, "Sales History",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    // Menu management button
                    IconButton(onClick = onNavigateToMenu) {
                        Icon(Icons.Default.MenuBook, "Manage Menu",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Search bar ────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = { Text("Search dishes…") },
                leadingIcon  = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            // ── Menu items list ───────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (menuItems.isEmpty()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, null,
                                    Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                Spacer(Modifier.height(8.dp))
                                Text("No dishes found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Add items via Menu Management",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        }
                    }
                } else {
                    items(menuItems, key = { it.id }) { item ->
                        OrderMenuItemCard(
                            item     = item,
                            quantity = viewModel.getQuantityInCart(item.id),
                            onAdd    = { viewModel.addToCart(item) },
                            onRemove = { viewModel.removeFromCart(item) }
                        )
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // ── Cart summary panel ────────────────────────
            AnimatedVisibility(
                visible = cartCount > 0,
                enter   = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit    = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                CartSummaryPanel(
                    cartList  = cartList.map { it.menuItem.name to it.quantity },
                    cartTotal = cartTotal,
                    onClear   = viewModel::clearCart,
                    onPlace   = viewModel::placeOrder
                )
            }
        }
    }
}

// ── Order Menu Item Card ───────────────────────────────────

@Composable
fun OrderMenuItemCard(
    item: MenuItem,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (quantity > 0)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = DateUtils.formatCurrency(item.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quantity controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(quantity > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Decrease / remove button
                        FilledIconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(Icons.Default.Remove, "Decrease",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer)
                        }

                        // Quantity badge
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = quantity.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Add button
                FilledIconButton(
                    onClick = onAdd,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, "Add",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

// ── Cart Summary Panel ─────────────────────────────────────

@Composable
fun CartSummaryPanel(
    cartList: List<Pair<String, Int>>,
    cartTotal: Double,
    onClear: () -> Unit,
    onPlace: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Cart (${cartList.size} item${if (cartList.size != 1) "s" else ""})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                TextButton(onClick = onClear) {
                    Icon(Icons.Default.DeleteSweep, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear")
                }
            }

            // Cart items summary (show max 3, then "+N more")
            val displayed = cartList.take(3)
            displayed.forEach { (name, qty) ->
                Text(
                    "• $name × $qty",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            if (cartList.size > 3) {
                Text(
                    "+ ${cartList.size - 3} more item${if (cartList.size - 3 != 1) "s" else ""}…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Total + Place Order button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Text(
                        DateUtils.formatCurrency(cartTotal),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onPlace,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Icon(Icons.Default.Receipt, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Bill", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
