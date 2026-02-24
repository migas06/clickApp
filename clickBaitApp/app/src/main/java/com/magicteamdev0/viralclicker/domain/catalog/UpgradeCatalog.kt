package com.magicteamdev0.viralclicker.domain.catalog

import com.magicteamdev0.viralclicker.domain.model.UpgradeCategory
import com.magicteamdev0.viralclicker.domain.model.UpgradeDefinition
import com.magicteamdev0.viralclicker.domain.model.ViralPoints

object UpgradeCatalog {

    val all: List<UpgradeDefinition> = listOf(
        // ── CLICKER UPGRADES ────────────────────────────────────────────
        UpgradeDefinition(
            id = "better_finger",
            name = "Better Finger",
            emoji = "👆",
            description = "Your clicking technique improves dramatically.",
            category = UpgradeCategory.CLICKER,
            baseCost = ViralPoints.fromLong(15),
            baseEffect = ViralPoints.fromLong(1),
            unlockAtAllTimePoints = ViralPoints.ZERO
        ),
        UpgradeDefinition(
            id = "viral_thumb",
            name = "Viral Thumb",
            emoji = "👍",
            description = "Each tap generates a mini viral wave.",
            category = UpgradeCategory.CLICKER,
            baseCost = ViralPoints.fromLong(200),
            baseEffect = ViralPoints.fromLong(5),
            unlockAtAllTimePoints = ViralPoints.fromLong(100)
        ),
        UpgradeDefinition(
            id = "meme_gloves",
            name = "Meme Gloves",
            emoji = "🧤",
            description = "Special gloves that channel meme energy.",
            category = UpgradeCategory.CLICKER,
            baseCost = ViralPoints.fromLong(2_000),
            baseEffect = ViralPoints.fromLong(25),
            unlockAtAllTimePoints = ViralPoints.fromLong(1_000)
        ),
        UpgradeDefinition(
            id = "neural_tap",
            name = "Neural Tap",
            emoji = "🧠",
            description = "Brain-computer interface for hyperspeed tapping.",
            category = UpgradeCategory.CLICKER,
            baseCost = ViralPoints.fromLong(50_000),
            baseEffect = ViralPoints.fromLong(200),
            unlockAtAllTimePoints = ViralPoints.fromLong(25_000)
        ),
        UpgradeDefinition(
            id = "quantum_click",
            name = "Quantum Click",
            emoji = "⚛️",
            description = "Each click exists in multiple viral dimensions simultaneously.",
            category = UpgradeCategory.CLICKER,
            baseCost = ViralPoints.fromLong(1_000_000),
            baseEffect = ViralPoints.fromLong(2_000),
            unlockAtAllTimePoints = ViralPoints.fromLong(500_000)
        ),

        // ── GENERATOR UPGRADES ──────────────────────────────────────────
        UpgradeDefinition(
            id = "bot_farm",
            name = "Bot Farm",
            emoji = "🤖",
            description = "Automated bots like your content 24/7.",
            category = UpgradeCategory.GENERATOR,
            baseCost = ViralPoints.fromLong(100),
            baseEffect = ViralPoints.fromDouble(0.5),
            unlockAtAllTimePoints = ViralPoints.ZERO
        ),
        UpgradeDefinition(
            id = "influencer",
            name = "Influencer",
            emoji = "🌟",
            description = "A micro-influencer shares your memes non-stop.",
            category = UpgradeCategory.GENERATOR,
            baseCost = ViralPoints.fromLong(1_100),
            baseEffect = ViralPoints.fromDouble(4.0),
            unlockAtAllTimePoints = ViralPoints.fromLong(500)
        ),
        UpgradeDefinition(
            id = "algorithm",
            name = "The Algorithm",
            emoji = "📊",
            description = "You've cracked the recommendation algorithm.",
            category = UpgradeCategory.GENERATOR,
            baseCost = ViralPoints.fromLong(12_000),
            baseEffect = ViralPoints.fromDouble(20.0),
            unlockAtAllTimePoints = ViralPoints.fromLong(5_000)
        ),
        UpgradeDefinition(
            id = "meme_factory",
            name = "Meme Factory",
            emoji = "🏭",
            description = "Industrial-scale meme production operation.",
            category = UpgradeCategory.GENERATOR,
            baseCost = ViralPoints.fromLong(130_000),
            baseEffect = ViralPoints.fromDouble(100.0),
            unlockAtAllTimePoints = ViralPoints.fromLong(50_000)
        ),
        UpgradeDefinition(
            id = "viral_singularity",
            name = "Viral Singularity",
            emoji = "🌌",
            description = "The internet itself becomes your content farm.",
            category = UpgradeCategory.GENERATOR,
            baseCost = ViralPoints.fromLong(1_400_000),
            baseEffect = ViralPoints.fromDouble(500.0),
            unlockAtAllTimePoints = ViralPoints.fromLong(500_000)
        )
    )

    fun findById(id: String): UpgradeDefinition? = all.find { it.id == id }
}
