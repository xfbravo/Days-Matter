package com.example.daysmatter.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.daysmatter.entity.AnniversaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnniversaryDao{
    @Insert
    suspend fun insert(anniversary: AnniversaryEntity)

    @Query("SELECT * FROM anniversaries")
    fun getAllAnniversaries(): Flow<List<AnniversaryEntity>>

    @Update
    suspend fun update(anniversary: AnniversaryEntity)

    @Delete
    suspend fun delete(anniversary: AnniversaryEntity)

    @Query("SELECT * FROM anniversaries WHERE id = :id")
    suspend fun getAnniversaryById(id: Int): AnniversaryEntity
}