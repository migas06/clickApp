package com.example.viralclicker.domain.usecase

import com.example.viralclicker.data.entity.PlayerEntity
import com.example.viralclicker.data.repository.GameRepository
import com.example.viralclicker.domain.model.ViralPoints
import javax.inject.Inject

val PRESTIGE_THRESHOLD = ViralPoints.fromLong(1_000_000_000L) // 1 Billion

class PrestigeUseCase @Inject constructor(private val repository: GameRepository) {

    data class Result(
        val updatedPlayer: PlayerEntity,
        val multiplierGained: Double
    )

    suspend fun execute(player: PlayerEntity): Result {
        val currentPrestigeCount = player.prestigeCount + 1
        val multiplierGained = 0.10
        val newMultiplier = player.prestigeMultiplier * (1.0 + multiplierGained)

        // Record prestige run
        repository.addPrestigeRun(
            runNumber = currentPrestigeCount,
            allTimePoints = ViralPoints.fromString(player.allTimePoints),
            multiplierEarned = multiplierGained
        )

        // Delete all owned upgrades
        repository.deleteAllOwnedUpgrades()

        // Reset player
        val resetPlayer = player.copy(
            viralPoints = "0",
            pointsPerClick = "1",
            pointsPerSecond = "0",
            prestigeCount = currentPrestigeCount,
            prestigeMultiplier = newMultiplier,
            boostActiveUntil = null
        )
        repository.savePlayer(resetPlayer)

        return Result(resetPlayer, multiplierGained)
    }
}
