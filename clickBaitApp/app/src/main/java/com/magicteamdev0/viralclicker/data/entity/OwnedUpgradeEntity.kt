package com.magicteamdev0.viralclicker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owned_upgrades")
data class OwnedUpgradeEntity(
    @PrimaryKey val upgradeId: String,
    val count: Int = 0
)
