package com.magicteamdev0.viralclicker.data.db

import androidx.room.*
import com.magicteamdev0.viralclicker.data.entity.PrestigeRunEntity

@Dao
interface PrestigeRunDao {
    @Insert
    suspend fun insert(entity: PrestigeRunEntity)

    @Query("SELECT COUNT(*) FROM prestige_runs")
    suspend fun count(): Int
}
