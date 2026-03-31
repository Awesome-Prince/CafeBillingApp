package com.cafe.billing.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cafe.billing.data.models.MenuItem
import com.cafe.billing.utils.DateUtils
import com.cafe.billing.viewmodel.MenuViewModel

// ============================================================
// MENU MANAGEMENT SCREEN
// Allows staff to Add / Edit / Delete menu items.
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val menuItems    by viewModel.menuItems.collectAsState()
    val searchQuery  by viewModel.searchQuery.collectAsState()
    val showDialog   by viewModel.showDialog.collectAsState()
    val editingItem  by viewModel.editingItem.collectAsState()
    val snackMsg     by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar whenever a message is posted
    LaunchedEffect(snackMsg) {
        snackMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            // FAB to add a new item
            FloatingActionButton(
                onClick = viewModel::onAddItemClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add item",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(12.dp))

            // ── Search bar ────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search dishes…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    AnimatedVisibility(searchQuery.isNotBlank(), enter = fadeIn(), exit = fadeOut()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            Spacer(Modifier.height(12.dp))

            // ── Item count chip ───────────────────────────
            Text(
                text = "${menuItems.size} item${if (menuItems.size != 1) "s" else ""} on menu",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            if (menuItems.isEmpty()) {
                // Empty state
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(12.dp))
                        Text("No items yet", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tap + to add your first dish",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            } else {
                // ── Menu item list ────────────────────────
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(menuItems, key = { it.id }) { item ->
                        MenuItemCard(
                            item = item,
                            onEdit   = { viewModel.onEditItemClick(item) },
                            onDelete = { viewModel.onDeleteItem(item) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) } // Space for FAB
                }
            }
        }
    }

    // ── Add / Edit Dialog ─────────────────────────────────
    if (showDialog) {
        AddEditMenuItemDialog(
            editingItem = editingItem,
            onSave      = viewModel::onSaveItem,
            onDismiss   = viewModel::onDismissDialog
        )
    }
}

// ── Menu Item Card ─────────────────────────────────────────

@Composable
fun MenuItemCard(
    item: MenuItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Confirm before deleting
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item name + price
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = DateUtils.formatCurrency(item.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Edit button
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit",
                    tint = MaterialTheme.colorScheme.primary)
            }

            // Delete button
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete \"${item.name}\"?") },
            text  = { Text("This will remove the item from the menu permanently.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ── Add / Edit Dialog ──────────────────────────────────────

@Composable
fun AddEditMenuItemDialog(
    editingItem: MenuItem?,
    onSave: (name: String, price: String) -> Unit,
    onDismiss: () -> Unit
) {
    // Pre-fill if editing
    var name  by remember { mutableStateOf(editingItem?.name  ?: "") }
    var price by remember { mutableStateOf(editingItem?.price?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (editingItem == null) "Add Menu Item" else "Edit Menu Item")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name") },
                    placeholder = { Text("e.g. Masala Dosa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Price field (numeric keyboard)
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (₹)") },
                    placeholder = { Text("e.g. 60") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, price) }) {
                Text(if (editingItem == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
