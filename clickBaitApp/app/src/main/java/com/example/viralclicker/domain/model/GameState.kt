package com.example.viralclicker.domain.model

/**
 * Immutable snapshot of all UI-relevant game state.
 * Emitted by GameViewModel as a StateFlow.
 */
data class GameState(
    val viralPoints: ViralPoints = ViralPoints.ZERO,
    val allTimePoints: ViralPoints = ViralPoints.ZERO,
    val pointsPerClick: ViralPoints = ViralPoints.fromLong(1L),
    val pointsPerSecond: ViralPoints = ViralPoints.ZERO,
    val prestigeMultiplier: Double = 1.0,
    val prestigeCount: Int = 0,
    val boostActiveUntil: Long? = null,         // epoch ms, null = no boost
    val ownedUpgrades: Map<String, Int> = emptyMap(), // upgradeId -> count
    val milestones: List<MilestoneState> = emptyList(),
    val noAdsPurchased: Boolean = false,
    val activeSkinId: String = "default",
    val offlineEarnings: ViralPoints? = null,   // non-null when first shown
    val prestigeAvailable: Boolean = false,
    val comboCount: Int = 0,
    val comboMultiplier: Double = 1.0,
    val nextMilestone: NextMilestoneProgress? = null,
    val dailyStreak: Int = 0,
    val dailyBonus: ViralPoints? = null
) {
    /** Returns 2.0 if boost active & not expired, 1.0 otherwise */
    fun boostMultiplier(nowMs: Long): Double =
        if (boostActiveUntil != null && nowMs < boostActiveUntil) 2.0 else 1.0

    fun effectivePointsPerClick(nowMs: Long): ViralPoints =
        pointsPerClick * (prestigeMultiplier * boostMultiplier(nowMs))

    fun effectivePointsPerSecond(nowMs: Long): ViralPoints =
        pointsPerSecond * (prestigeMultiplier * boostMultiplier(nowMs))
}

data class MilestoneState(
    val id: String,
    val name: String,
    val description: String,
    val unlocked: Boolean,
    val unlockedAt: Long?
)

data class NextMilestoneProgress(
    val name: String,
    val progress: Float,       // 0.0 to 1.0, with endowed progress applied
    val currentValue: String,
    val targetValue: String
)
