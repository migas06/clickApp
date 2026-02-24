package com.example.viralclicker.data.db

import androidx.room.*
import com.example.viralclicker.data.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player WHERE id = 1")
    suspend fun getPlayer(): PlayerEntity?

    @Query("SELECT * FROM player WHERE id = 1")
    fun observePlayer(): Flow<PlayerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(player: PlayerEntity)
}
