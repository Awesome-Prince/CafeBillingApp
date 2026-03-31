package com.cafe.billing.data.repository

import com.cafe.billing.data.dao.MenuItemDao
import com.cafe.billing.data.models.MenuItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// ============================================================
// MENU REPOSITORY
// The repository is the single source of truth for menu data.
// ViewModels call repository methods — they never talk to the
// DAO directly. This keeps ViewModels clean and testable.
// ============================================================

@Singleton
class MenuRepository @Inject constructor(
    private val menuItemDao: MenuItemDao
) {

    /** Stream of all menu items, auto-updating on changes */
    fun getAllMenuItems(): Flow<List<MenuItem>> =
        menuItemDao.getAllMenuItems()

    /** Filtered stream matching the search query */
    fun searchMenuItems(query: String): Flow<List<MenuItem>> =
        menuItemDao.searchMenuItems(query)

    /** Add a new dish to the menu */
    suspend fun addMenuItem(item: MenuItem) =
        menuItemDao.insertMenuItem(item)

    /** Save changes to an existing dish */
    suspend fun updateMenuItem(item: MenuItem) =
        menuItemDao.updateMenuItem(item)

    /** Remove a dish from the menu */
    suspend fun deleteMenuItem(item: MenuItem) =
        menuItemDao.deleteMenuItem(item)
}
