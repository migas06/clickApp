package com.magicteamdev0.viralclicker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey val id: Int = 1,
    val viralPoints: String = "0",
    val allTimePoints: String = "0",
    val pointsPerClick: String = "1",
    val pointsPerSecond: String = "0",
    val prestigeCount: Int = 0,
    val prestigeMultiplier: Double = 1.0,
    val lastSavedAt: Long = System.currentTimeMillis(),
    val boostActiveUntil: Long? = null,
    val noAdsPurchased: Boolean = false,
    val activeSkinId: String = "default",
    val currentStreak: Int = 0,
    val lastLoginDate: String = "",
    val longestStreak: Int = 0
)
