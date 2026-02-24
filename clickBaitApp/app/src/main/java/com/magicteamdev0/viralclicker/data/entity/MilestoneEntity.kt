package com.magicteamdev0.viralclicker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val unlockedAt: Long? = null
)
