package com.cafe.billing.di

import android.content.Context
import com.cafe.billing.data.dao.MenuItemDao
import com.cafe.billing.data.dao.SalesOrderDao
import com.cafe.billing.data.database.CafeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ============================================================
// HILT DATABASE MODULE
// Tells Hilt how to create the database and DAOs so they can
// be automatically injected wherever @Inject is used.
// ============================================================

@Module
@InstallIn(SingletonComponent::class)   // Lives for the whole app lifetime
object DatabaseModule {

    /**
     * Provides the singleton Room database instance.
     * @ApplicationContext is injected by Hilt automatically.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CafeDatabase {
        return CafeDatabase.getDatabase(context)
    }

    /**
     * Provides MenuItemDao from the database.
     * Hilt calls provideDatabase() first, then passes it here.
     */
    @Provides
    @Singleton
    fun provideMenuItemDao(database: CafeDatabase): MenuItemDao {
        return database.menuItemDao()
    }

    /**
     * Provides SalesOrderDao from the database.
     */
    @Provides
    @Singleton
    fun provideSalesOrderDao(database: CafeDatabase): SalesOrderDao {
        return database.salesOrderDao()
    }
}
