package com.cafe.billing.data.dao

import androidx.room.*
import com.cafe.billing.data.models.MenuItem
import kotlinx.coroutines.flow.Flow

// ============================================================
// MENU ITEM DAO
// Data Access Object: defines all SQL operations for menu_items.
// Room generates the implementation at compile time.
// ============================================================

@Dao
interface MenuItemDao {

    /**
     * Observe all menu items, ordered alphabetically.
     * Returns a Flow so the UI auto-updates when data changes.
     */
    @Query("SELECT * FROM menu_items ORDER BY name ASC")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    /**
     * Search menu items by name (case-insensitive partial match).
     * Used for the search/filter feature on the menu management screen.
     */
    @Query("SELECT * FROM menu_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchMenuItems(query: String): Flow<List<MenuItem>>

    /**
     * Insert a new menu item. If an item with the same ID already exists,
     * replace it (handles both insert and update in one call).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItem)

    /**
     * Update an existing menu item by matching its primary key.
     */
    @Update
    suspend fun updateMenuItem(item: MenuItem)

    /**
     * Delete a menu item permanently.
     */
    @Delete
    suspend fun deleteMenuItem(item: MenuItem)

    /**
     * Get a single menu item by ID. Useful for edit screens.
     */
    @Query("SELECT * FROM menu_items WHERE id = :id")
    suspend fun getMenuItemById(id: Int): MenuItem?
}
