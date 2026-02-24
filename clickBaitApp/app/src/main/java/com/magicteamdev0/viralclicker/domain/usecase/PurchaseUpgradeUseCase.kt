package com.magicteamdev0.viralclicker.domain.usecase

import com.magicteamdev0.viralclicker.data.entity.OwnedUpgradeEntity
import com.magicteamdev0.viralclicker.data.entity.PlayerEntity
import com.magicteamdev0.viralclicker.domain.catalog.UpgradeCatalog
import com.magicteamdev0.viralclicker.domain.model.UpgradeCategory
import com.magicteamdev0.viralclicker.domain.model.ViralPoints
import java.math.BigDecimal
import javax.inject.Inject

class PurchaseUpgradeUseCase @Inject constructor() {

    data class Result(
        val updatedPlayer: PlayerEntity,
        val updatedOwnedUpgrade: OwnedUpgradeEntity,
        val success: Boolean,
        val reason: String = ""
    )

    fun execute(
        upgradeId: String,
        player: PlayerEntity,
        ownedMap: Map<String, Int>
    ): Result {
        val def = UpgradeCatalog.findById(upgradeId)
            ?: return Result(player, OwnedUpgradeEntity(upgradeId), false, "Unknown upgrade")

        val owned = ownedMap[upgradeId] ?: 0
        val cost = def.costForCount(owned)
        val currentPoints = ViralPoints.fromString(player.viralPoints)

        if (!currentPoints.isGreaterThanOrEqual(cost)) {
            return Result(player, OwnedUpgradeEntity(upgradeId, owned), false, "Insufficient points")
        }

        val newOwned = owned + 1
        val newPoints = currentPoints - cost

        // Recompute pointsPerClick and pointsPerSecond
        val newOwnedMap = ownedMap.toMutableMap().apply { put(upgradeId, newOwned) }
        val newPpc = computeTotalEffect(newOwnedMap, UpgradeCategory.CLICKER)
            .coerceAtLeast(ViralPoints.fromLong(1))
        val newPps = computeTotalEffect(newOwnedMap, UpgradeCategory.GENERATOR)

        val updatedPlayer = player.copy(
            viralPoints = newPoints.value.toPlainString(),
            pointsPerClick = newPpc.value.toPlainString(),
            pointsPerSecond = newPps.value.toPlainString()
        )

        return Result(updatedPlayer, OwnedUpgradeEntity(upgradeId, newOwned), true)
    }

    private fun computeTotalEffect(ownedMap: Map<String, Int>, category: UpgradeCategory): ViralPoints {
        var total = BigDecimal.ZERO
        UpgradeCatalog.all.filter { it.category == category }.forEach { def ->
            val count = ownedMap[def.id] ?: 0
            if (count > 0) total = total.add(def.totalEffect(count).value)
        }
        return ViralPoints(total)
    }

    private fun ViralPoints.coerceAtLeast(min: ViralPoints): ViralPoints =
        if (this.value < min.value) min else this
}
