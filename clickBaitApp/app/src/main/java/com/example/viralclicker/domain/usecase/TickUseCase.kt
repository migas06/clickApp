package com.example.viralclicker.domain.usecase

import com.example.viralclicker.data.entity.PlayerEntity
import com.example.viralclicker.data.repository.GameRepository
import com.example.viralclicker.domain.model.ViralPoints
import javax.inject.Inject

class TickUseCase @Inject constructor(private val repository: GameRepository) {

    /**
     * Called every second by the coroutine ticker.
     * Returns an updated player entity for in-memory update.
     */
    fun execute(
        player: PlayerEntity,
        pointsPerSecond: ViralPoints,
        prestigeMultiplier: Double,
        boostMultiplier: Double
    ): PlayerEntity {
        if (pointsPerSecond.value.signum() == 0) return player
        val earned = pointsPerSecond * (prestigeMultiplier * boostMultiplier)
        val current = ViralPoints.fromString(player.viralPoints)
        val allTime = ViralPoints.fromString(player.allTimePoints)
        return player.copy(
            viralPoints = (current + earned).value.toPlainString(),
            allTimePoints = (allTime + earned).value.toPlainString()
        )
    }
}
