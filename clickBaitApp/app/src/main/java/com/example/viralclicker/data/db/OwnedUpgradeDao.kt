package com.example.viralclicker.data.db

import androidx.room.*
import com.example.viralclicker.data.entity.OwnedUpgradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnedUpgradeDao {
    @Query("SELECT * FROM owned_upgrades")
    suspend fun getAll(): List<OwnedUpgradeEntity>

    @Query("SELECT * FROM owned_upgrades")
    fun observeAll(): Flow<List<OwnedUpgradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: OwnedUpgradeEntity)

    @Query("DELETE FROM owned_upgrades")
    suspend fun deleteAll()
}
