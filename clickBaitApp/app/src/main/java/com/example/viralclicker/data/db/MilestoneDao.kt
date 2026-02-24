package com.example.viralclicker.data.db

import androidx.room.*
import com.example.viralclicker.data.entity.MilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones")
    suspend fun getAll(): List<MilestoneEntity>

    @Query("SELECT * FROM milestones")
    fun observeAll(): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MilestoneEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entities: List<MilestoneEntity>)
}
