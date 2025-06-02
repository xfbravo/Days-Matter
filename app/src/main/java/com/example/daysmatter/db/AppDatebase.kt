package com.example.daysmatter.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.daysmatter.dao.AnniversaryDao
import com.example.daysmatter.entity.AnniversaryEntity
import com.example.daysmatter.entity.LocalDateConverter

@Database(entities = [AnniversaryEntity::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase:RoomDatabase(){
    abstract fun  anniversaryDao(): AnniversaryDao
}