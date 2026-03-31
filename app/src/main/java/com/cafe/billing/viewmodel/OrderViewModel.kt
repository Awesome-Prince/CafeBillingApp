package com.cafe.billing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafe.billing.data.models.CartItem
import com.cafe.billing.data.models.MenuItem
import com.cafe.billing.data.models.SalesOrder
import com.cafe.billing.data.repository.MenuRepository
import com.cafe.billing.data.repository.SalesRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// ORDER VIEW MODEL
// Manages:
//  - The list of available menu items (read from DB)
//  - The current cart (in-memory, reset after each order)
//  - Saving the final order to the database
// ============================================================

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val salesRepository: SalesRepository
) : ViewModel() {

    // ── Menu items ────────────────────────────────────────
    /** All menu items available for ordering */
    val menuItems: StateFlow<List<MenuItem>> = menuRepository
        .getAllMenuItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // ── Cart state ────────────────────────────────────────
    /** In-memory map: menuItemId → CartItem */
    private val _cartItems = MutableStateFlow<Map<Int, CartItem>>(emptyMap())
    val cartItems: StateFlow<Map<Int, CartItem>> = _cartItems.asStateFlow()

    /** List view of the cart (for UI rendering) */
    val cartList: StateFlow<List<CartItem>> = _cartItems
        .map { it.values.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Running total of the cart */
    val cartTotal: StateFlow<Double> = _cartItems
        .map { cart -> cart.values.sumOf { it.lineTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    /** Total number of individual items in cart */
    val cartItemCount: StateFlow<Int> = _cartItems
        .map { cart -> cart.values.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Search ────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredMenuItems: StateFlow<List<MenuItem>> = _searchQuery
        .debounce(200L)
        .flatMapLatest { query ->
            if (query.isBlank()) menuRepository.getAllMenuItems()
            else menuRepository.searchMenuItems(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Order saved state ─────────────────────────────────
    /** Emits the saved order ID when an order is successfully placed */
    private val _savedOrderId = MutableStateFlow<Int?>(null)
    val savedOrderId: StateFlow<Int?> = _savedOrderId.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // ── Cart actions ──────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Add one unit of the given menu item to the cart.
     * If already in cart, increments quantity.
     */
    fun addToCart(item: MenuItem) {
        val current = _cartItems.value.toMutableMap()
        val existing = current[item.id]
        current[item.id] = existing?.copy(quantity = existing.quantity + 1)
            ?: CartItem(menuItem = item, quantity = 1)
        _cartItems.value = current
    }

    /**
     * Remove one unit from the cart.
     * If quantity reaches 0, removes the item entirely.
     */
    fun removeFromCart(item: MenuItem) {
        val current = _cartItems.value.toMutableMap()
        val existing = current[item.id] ?: return
        if (existing.quantity <= 1) {
            current.remove(item.id)
        } else {
            current[item.id] = existing.copy(quantity = existing.quantity - 1)
        }
        _cartItems.value = current
    }

    /** Get quantity of an item currently in cart (0 if not in cart) */
    fun getQuantityInCart(itemId: Int): Int =
        _cartItems.value[itemId]?.quantity ?: 0

    /** Clear the entire cart */
    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    // ── Order submission ──────────────────────────────────

    /**
     * Finalise and save the current cart as a SalesOrder.
     * Navigates to bill screen by emitting the new order ID.
     */
    fun placeOrder() {
        val cart = _cartItems.value
        if (cart.isEmpty()) {
            _snackbarMessage.value = "Cart is empty!"
            return
        }

        viewModelScope.launch {
            val order = SalesOrder(
                itemsJson  = Gson().toJson(cart.values.toList()),
                totalAmount = cart.values.sumOf { it.lineTotal },
                timestamp   = System.currentTimeMillis()
            )
            val newId = salesRepository.saveOrder(order)
            _savedOrderId.value = newId.toInt()
            clearCart()
        }
    }

    fun onOrderNavigated() {
        _savedOrderId.value = null
    }

    fun onSnackbarShown() {
        _snackbarMessage.value = null
    }
}
