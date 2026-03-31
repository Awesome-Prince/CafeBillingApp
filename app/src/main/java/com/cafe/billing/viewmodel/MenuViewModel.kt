package com.cafe.billing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafe.billing.data.models.MenuItem
import com.cafe.billing.data.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// MENU VIEW MODEL
// Manages UI state for the Menu Management screen.
// Uses StateFlow so the Compose UI observes changes reactively.
// ============================================================

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val repository: MenuRepository
) : ViewModel() {

    // ── Search state ──────────────────────────────────────
    /** The current text in the search bar */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ── Menu items (reactive to search) ──────────────────
    /**
     * Flat-maps search query changes into the correct database query.
     * If query is blank → show all items; otherwise → show filtered items.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val menuItems: StateFlow<List<MenuItem>> = _searchQuery
        .debounce(300L)          // Wait 300ms after last keystroke before querying
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllMenuItems()
            else repository.searchMenuItems(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // ── Dialog state ──────────────────────────────────────
    /** Controls visibility of the Add/Edit dialog */
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    /** The item currently being edited, null when adding a new item */
    private val _editingItem = MutableStateFlow<MenuItem?>(null)
    val editingItem: StateFlow<MenuItem?> = _editingItem.asStateFlow()

    // ── UI feedback ───────────────────────────────────────
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // ── Public actions ────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** Open the dialog to add a new item (editingItem = null) */
    fun onAddItemClick() {
        _editingItem.value = null
        _showDialog.value = true
    }

    /** Open the dialog pre-filled with the item to edit */
    fun onEditItemClick(item: MenuItem) {
        _editingItem.value = item
        _showDialog.value = true
    }

    fun onDismissDialog() {
        _showDialog.value = false
        _editingItem.value = null
    }

    /**
     * Save item — inserts if id == 0, updates otherwise.
     * Validates that name is not blank and price > 0.
     */
    fun onSaveItem(name: String, priceText: String) {
        val price = priceText.toDoubleOrNull()

        if (name.isBlank()) {
            _snackbarMessage.value = "Item name cannot be empty"
            return
        }
        if (price == null || price <= 0) {
            _snackbarMessage.value = "Enter a valid price"
            return
        }

        viewModelScope.launch {
            val current = _editingItem.value
            if (current == null) {
                // Adding new item
                repository.addMenuItem(MenuItem(name = name.trim(), price = price))
                _snackbarMessage.value = "\"${name.trim()}\" added to menu"
            } else {
                // Updating existing item
                repository.updateMenuItem(current.copy(name = name.trim(), price = price))
                _snackbarMessage.value = "\"${name.trim()}\" updated"
            }
            onDismissDialog()
        }
    }

    fun onDeleteItem(item: MenuItem) {
        viewModelScope.launch {
            repository.deleteMenuItem(item)
            _snackbarMessage.value = "\"${item.name}\" removed from menu"
        }
    }

    /** Called after the snackbar has been shown */
    fun onSnackbarShown() {
        _snackbarMessage.value = null
    }
}
