package com.example.daysmatter.db

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager

class DaysMatterApplication : Application() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val database: AppDatabase
            get() = INSTANCE ?: throw UninitializedPropertyAccessException("Database not initialized")

        fun initializeDatabase(applicationContext: Context) {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "days_matter_db_fixed"
                    ).build()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializeDatabase(applicationContext)
        WorkManager.initialize(this, Configuration.Builder().build())
    }
}