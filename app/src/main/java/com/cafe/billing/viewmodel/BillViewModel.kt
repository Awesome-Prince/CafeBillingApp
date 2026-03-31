package com.cafe.billing.viewmodel

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafe.billing.data.models.SalesOrder
import com.cafe.billing.data.repository.SalesRepository
import com.cafe.billing.utils.BillPrintAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// BILL VIEW MODEL
// Loads a specific completed order and handles printing.
// ============================================================

@HiltViewModel
class BillViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _order = MutableStateFlow<SalesOrder?>(null)
    val order: StateFlow<SalesOrder?> = _order.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Load a specific order by ID.
     * Called when navigating to the Bill screen.
     */
    fun loadOrder(orderId: Int) {
        viewModelScope.launch {
            // Collect the all-orders flow and find the right one.
            // In a larger app you'd add getOrderById() to the DAO.
            salesRepository.getAllOrders()
                .map { orders -> orders.find { it.id == orderId } }
                .collect { found ->
                    _order.value = found
                    _isLoading.value = false
                }
        }
    }

    /**
     * Trigger the Android system print dialog.
     * Uses a custom PrintDocumentAdapter that renders the bill as a PDF.
     *
     * @param context   Activity context (needed for PrintManager)
     * @param order     The order to print
     */
    fun printBill(context: Context, order: SalesOrder) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

        val jobName = "Cafe Bill #${order.id}"
        val printAdapter = BillPrintAdapter(context, order)

        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("default", "Default", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        // This opens the system print dialog (includes Save as PDF option)
        printManager.print(jobName, printAdapter, printAttributes)
    }
}
