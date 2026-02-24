package com.magicteamdev0.viralclicker.domain.model

data class MilestoneDefinition(
    val id: String,
    val name: String,
    val description: String,
    val triggerType: MilestoneTrigger,
    val threshold: Long
)

enum class MilestoneTrigger {
    ALL_TIME_POINTS,
    PRESTIGE_COUNT,
    TOTAL_UPGRADES_OWNED
}
