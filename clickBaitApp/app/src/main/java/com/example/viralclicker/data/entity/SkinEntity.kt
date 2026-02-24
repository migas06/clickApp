package com.example.viralclicker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skins")
data class SkinEntity(
    @PrimaryKey val id: String,
    val name: String,
    val skuId: String,
    val owned: Boolean = false,
    val active: Boolean = false
)
