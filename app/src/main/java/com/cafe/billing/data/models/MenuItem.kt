package com.cafe.billing.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// ============================================================
// DATA MODELS
// These are the core data classes used throughout the app.
// Each @Entity class maps to a Room database table.
// ============================================================

/**
 * Represents a single dish/item on the café menu.
 * Stored in the "menu_items" table in Room.
 *
 * @param id        Auto-generated unique ID
 * @param name      Display name of the dish (e.g., "Masala Dosa")
 * @param price     Price in the local currency (e.g., 60.0)
 */
@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double
)

/**
 * Represents a single item inside a cart / bill.
 * This is NOT stored directly in Room — it lives in memory during order-taking.
 * It IS serialized to JSON before being saved inside a SalesOrder.
 *
 * @param menuItem  The MenuItem being ordered
 * @param quantity  How many of this item were ordered
 */
data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int = 1
) {
    /** Convenience: total price for this line (price × quantity) */
    val lineTotal: Double get() = menuItem.price * quantity
}
