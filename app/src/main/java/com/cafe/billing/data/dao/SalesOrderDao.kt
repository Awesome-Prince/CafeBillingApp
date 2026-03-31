package com.cafe.billing.data.dao

import androidx.room.*
import com.cafe.billing.data.models.SalesOrder
import kotlinx.coroutines.flow.Flow

// ============================================================
// SALES ORDER DAO
// Data Access Object: all SQL operations for sales_orders table.
// ============================================================

@Dao
interface SalesOrderDao {

    /**
     * Observe all orders, newest first.
     * Used in Sales History screen.
     */
    @Query("SELECT * FROM sales_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<SalesOrder>>

    /**
     * Get orders that fall within a date range (Unix timestamps in ms).
     * Used for daily analytics.
     *
     * @param startMs   Start of day (00:00:00) as epoch ms
     * @param endMs     End of day (23:59:59) as epoch ms
     */
    @Query("SELECT * FROM sales_orders WHERE timestamp BETWEEN :startMs AND :endMs ORDER BY timestamp DESC")
    fun getOrdersByDateRange(startMs: Long, endMs: Long): Flow<List<SalesOrder>>

    /**
     * Sum of all order totals between two timestamps.
     * Returns null if no orders exist in range.
     * Used for the daily sales analytics card.
     */
    @Query("SELECT SUM(totalAmount) FROM sales_orders WHERE timestamp BETWEEN :startMs AND :endMs")
    fun getTotalSalesBetween(startMs: Long, endMs: Long): Flow<Double?>

    /**
     * Count of all orders today.
     */
    @Query("SELECT COUNT(*) FROM sales_orders WHERE timestamp BETWEEN :startMs AND :endMs")
    fun getOrderCountBetween(startMs: Long, endMs: Long): Flow<Int>

    /**
     * Save a completed order to the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: SalesOrder): Long

    /**
     * Delete an order (e.g., for error correction).
     */
    @Delete
    suspend fun deleteOrder(order: SalesOrder)
}
