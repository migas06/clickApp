package com.magicteamdev0.viralclicker.domain.model

import java.math.BigDecimal

/**
 * Static definition of an upgrade in the catalog.
 * Not persisted — defined in code, referenced by ID.
 */
data class UpgradeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val category: UpgradeCategory,
    val baseCost: ViralPoints,
    val costScalingFactor: Double = 1.15,
    val baseEffect: ViralPoints,             // per click or per second
    val unlockAtAllTimePoints: ViralPoints = ViralPoints.ZERO
) {
    fun costForCount(owned: Int): ViralPoints {
        if (owned == 0) return baseCost
        val scale = BigDecimal(costScalingFactor).pow(owned)
        return ViralPoints(baseCost.value.multiply(scale))
    }

    fun totalEffect(owned: Int): ViralPoints =
        ViralPoints(baseEffect.value.multiply(BigDecimal(owned)))

    fun tier(): UpgradeTier = when {
        baseCost.value >= BigDecimal("1000000") -> UpgradeTier.LEGENDARY
        baseCost.value >= BigDecimal("50000") -> UpgradeTier.EPIC
        baseCost.value >= BigDecimal("2000") -> UpgradeTier.RARE
        baseCost.value >= BigDecimal("200") -> UpgradeTier.UNCOMMON
        else -> UpgradeTier.COMMON
    }
}

enum class UpgradeCategory {
    CLICKER,    // increases points per click
    GENERATOR   // increases points per second
}

enum class UpgradeTier {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
}
