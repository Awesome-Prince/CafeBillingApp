package com.cafe.billing.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ============================================================
// SALES ORDER MODEL
// Represents a completed order saved to the database.
// ============================================================

/**
 * Represents a completed, paid order saved in "sales_orders" table.
 *
 * Room cannot store a List<CartItem> directly, so we use a TypeConverter
 * to serialize/deserialize it as a JSON string via Gson.
 *
 * @param id            Auto-generated unique ID
 * @param itemsJson     JSON string of List<CartItem> (converted automatically)
 * @param totalAmount   Grand total for this order
 * @param timestamp     Unix epoch milliseconds when the order was completed
 */
@Entity(tableName = "sales_orders")
@TypeConverters(CartItemListConverter::class)
data class SalesOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemsJson: String,          // Stored as JSON, use extension below to parse
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Parses the JSON string back into a proper List<CartItem>.
     * Call this when displaying order details.
     */
    fun getCartItems(): List<CartItem> {
        return Gson().fromJson(
            itemsJson,
            object : TypeToken<List<CartItem>>() {}.type
        ) ?: emptyList()
    }
}

// ============================================================
// TYPE CONVERTERS
// Teaches Room how to store complex types as simple strings.
// ============================================================

/**
 * Converts List<CartItem> ↔ JSON String for Room storage.
 */
class CartItemListConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromCartItemList(items: List<CartItem>): String {
        return gson.toJson(items)
    }

    @TypeConverter
    fun toCartItemList(json: String): List<CartItem> {
        val type = object : TypeToken<List<CartItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
