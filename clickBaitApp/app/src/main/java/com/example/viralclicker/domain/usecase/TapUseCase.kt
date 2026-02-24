package com.example.viralclicker.domain.usecase

import com.example.viralclicker.data.entity.PlayerEntity
import com.example.viralclicker.data.repository.GameRepository
import com.example.viralclicker.domain.model.ViralPoints
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
