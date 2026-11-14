package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomWheelListDao {
    @Query("SELECT * FROM custom_wheel_lists ORDER BY isPreset DESC, updatedAtMillis DESC")
    fun getAllLists(): Flow<List<CustomWheelListEntity>>

    @Query("SELECT * FROM custom_wheel_lists ORDER BY isPreset DESC, updatedAtMillis DESC")
    suspend fun getAllListsOnce(): List<CustomWheelListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(entity: CustomWheelListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLists(entities: List<CustomWheelListEntity>)

    @Query("DELETE FROM custom_wheel_lists WHERE id = :listId")
    suspend fun deleteListById(listId: String)

    @Query("DELETE FROM custom_wheel_lists")
    suspend fun clearAll()
}
