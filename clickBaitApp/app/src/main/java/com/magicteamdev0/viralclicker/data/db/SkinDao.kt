package com.magicteamdev0.viralclicker.data.db

import androidx.room.*
import com.magicteamdev0.viralclicker.data.entity.SkinEntity

@Dao
interface SkinDao {
    @Query("SELECT * FROM skins")
    suspend fun getAll(): List<SkinEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SkinEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entities: List<SkinEntity>)

    @Query("UPDATE skins SET active = (id = :skinId)")
    suspend fun setActiveSkin(skinId: String)
}
