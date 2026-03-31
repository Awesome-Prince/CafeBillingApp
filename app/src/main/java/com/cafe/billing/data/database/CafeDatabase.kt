package com.cafe.billing.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cafe.billing.data.dao.MenuItemDao
import com.cafe.billing.data.dao.SalesOrderDao
import com.cafe.billing.data.models.CartItemListConverter
import com.cafe.billing.data.models.MenuItem
import com.cafe.billing.data.models.SalesOrder

// ============================================================
// ROOM DATABASE
// The central database class. Room uses this to create the
// SQLite database file on the device.
//
// - entities:  all @Entity classes (= tables)
// - version:   increment this when you change the schema
// - exportSchema: false keeps things tidy for a small app
// ============================================================

@Database(
    entities  = [MenuItem::class, SalesOrder::class],
    version   = 1,
    exportSchema = false
)
@TypeConverters(CartItemListConverter::class)
abstract class CafeDatabase : RoomDatabase() {

    /** Provides access to menu_items table operations */
    abstract fun menuItemDao(): MenuItemDao

    /** Provides access to sales_orders table operations */
    abstract fun salesOrderDao(): SalesOrderDao

    companion object {
        // Volatile ensures the instance is always up-to-date across threads
        @Volatile
        private var INSTANCE: CafeDatabase? = null

        /**
         * Returns the singleton database instance.
         * Creates it on first call using the double-checked locking pattern.
         *
         * @param context Application context (not Activity context)
         */
        fun getDatabase(context: Context): CafeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CafeDatabase::class.java,
                    "cafe_billing_database"   // SQLite file name on device
                )
                    // When schema changes, just recreate the database.
                    // For production you'd use migrations instead.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
