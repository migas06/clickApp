package com.magicteamdev0.viralclicker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prestige_runs")
data class PrestigeRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val runNumber: Int,
    val allTimePointsAtPrestige: String,
    val multiplierEarned: Double,
    val prestigedAt: Long = System.currentTimeMillis()
)
