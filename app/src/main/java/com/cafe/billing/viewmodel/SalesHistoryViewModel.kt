package com.cafe.billing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafe.billing.data.models.SalesOrder
import com.cafe.billing.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// SALES HISTORY VIEW MODEL
// Powers the Sales History screen + today's analytics summary.
// ============================================================

@HiltViewModel
class SalesHistoryViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    // ── All orders ────────────────────────────────────────
    val allOrders: StateFlow<List<SalesOrder>> = salesRepository
        .getAllOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // ── Today's analytics ─────────────────────────────────
    /** Total revenue collected today */
    val todaysTotalSales: StateFlow<Double> = salesRepository
        .getTodaysTotalSales()
        .map { it ?: 0.0 }          // Null means no orders; treat as 0
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0
        )

    /** Number of orders placed today */
    val todaysOrderCount: StateFlow<Int> = salesRepository
        .getTodaysOrderCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    /** Average order value today (0 if no orders) */
    val todaysAverageOrder: StateFlow<Double> = combine(
        todaysTotalSales, todaysOrderCount
    ) { total, count ->
        if (count > 0) total / count else 0.0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0.0
    )

    // ── Delete ────────────────────────────────────────────
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun deleteOrder(order: SalesOrder) {
        viewModelScope.launch {
            salesRepository.deleteOrder(order)
            _snackbarMessage.value = "Order #${order.id} deleted"
        }
    }

    fun onSnackbarShown() {
        _snackbarMessage.value = null
    }
}
