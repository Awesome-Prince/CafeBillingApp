package com.cafe.billing.data.repository

import com.cafe.billing.data.dao.SalesOrderDao
import com.cafe.billing.data.models.SalesOrder
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

// ============================================================
// SALES REPOSITORY
// Handles all operations related to completed orders & analytics.
// ============================================================

@Singleton
class SalesRepository @Inject constructor(
    private val salesOrderDao: SalesOrderDao
) {

    /** Stream of all past orders, newest first */
    fun getAllOrders(): Flow<List<SalesOrder>> =
        salesOrderDao.getAllOrders()

    /**
     * Save a completed order to the database.
     * @return the new row ID
     */
    suspend fun saveOrder(order: SalesOrder): Long =
        salesOrderDao.insertOrder(order)

    /** Delete an order (e.g., mistaken entry) */
    suspend fun deleteOrder(order: SalesOrder) =
        salesOrderDao.deleteOrder(order)

    // ── Analytics helpers ──────────────────────────────────

    /**
     * Returns a Flow of today's total revenue.
     * Automatically recalculates whenever new orders are saved.
     */
    fun getTodaysTotalSales(): Flow<Double?> {
        val (start, end) = todayRange()
        return salesOrderDao.getTotalSalesBetween(start, end)
    }

    /** Flow of the number of orders placed today */
    fun getTodaysOrderCount(): Flow<Int> {
        val (start, end) = todayRange()
        return salesOrderDao.getOrderCountBetween(start, end)
    }

    /** Flow of orders placed today, newest first */
    fun getTodaysOrders(): Flow<List<SalesOrder>> {
        val (start, end) = todayRange()
        return salesOrderDao.getOrdersByDateRange(start, end)
    }

    // ── Private helpers ────────────────────────────────────

    /**
     * Returns the start (00:00:00.000) and end (23:59:59.999)
     * of today as Unix epoch milliseconds.
     */
    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        // Start of day
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        // End of day
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }
}
