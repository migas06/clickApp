package com.magicteamdev0.viralclicker.domain.usecase

import com.magicteamdev0.viralclicker.data.entity.PlayerEntity
import com.magicteamdev0.viralclicker.data.repository.GameRepository
import com.magicteamdev0.viralclicker.domain.model.ViralPoints
import javax.inject.Inject

class TapUseCase @Inject constructor(private val repository: GameRepository) {

    /**
     * Called on each tap. Returns updated player entity for in-memory state update.
     * Caller is responsible for persisting periodically (not every tap, for performance).
     */
    fun execute(
        player: PlayerEntity,
        pointsPerClick: ViralPoints,
        prestigeMultiplier: Double,
        boostMultiplier: Double
    ): PlayerEntity {
        val earned = pointsPerClick * (prestigeMultiplier * boostMultiplier)
        val current = ViralPoints.fromString(player.viralPoints)
        val allTime = ViralPoints.fromString(player.allTimePoints)
        return player.copy(
            viralPoints = (current + earned).value.toPlainString(),
            allTimePoints = (allTime + earned).value.toPlainString()
        )
    }
}
