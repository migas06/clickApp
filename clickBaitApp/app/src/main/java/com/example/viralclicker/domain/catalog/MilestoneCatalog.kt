package com.example.viralclicker.domain.catalog

import com.example.viralclicker.domain.model.MilestoneDefinition
import com.example.viralclicker.domain.model.MilestoneTrigger

object MilestoneCatalog {
    val all: List<MilestoneDefinition> = listOf(
        MilestoneDefinition("first_thousand", "Going Viral 🎉", "Earn 1,000 Viral Points", MilestoneTrigger.ALL_TIME_POINTS, 1_000),
        MilestoneDefinition("first_million", "Micro-Famous ⭐", "Earn 1,000,000 Viral Points", MilestoneTrigger.ALL_TIME_POINTS, 1_000_000),
        MilestoneDefinition("first_billion", "Internet Celebrity 🌟", "Earn 1,000,000,000 Viral Points", MilestoneTrigger.ALL_TIME_POINTS, 1_000_000_000),
        MilestoneDefinition("first_trillion", "Meme God 👑", "Earn 1 Trillion Viral Points", MilestoneTrigger.ALL_TIME_POINTS, 1_000_000_000_000L),
        MilestoneDefinition("first_quadrillion", "Digital Deity 🌌", "Earn 1 Quadrillion Viral Points", MilestoneTrigger.ALL_TIME_POINTS, 1_000_000_000_000_000L),
        MilestoneDefinition("first_prestige", "Gone Viral 🔥", "Complete your first prestige", MilestoneTrigger.PRESTIGE_COUNT, 1),
        MilestoneDefinition("five_prestiges", "Serial Viralogist 🚀", "Prestige 5 times", MilestoneTrigger.PRESTIGE_COUNT, 5),
        MilestoneDefinition("ten_prestiges", "Infinite Loop 🔄", "Prestige 10 times", MilestoneTrigger.PRESTIGE_COUNT, 10),
        MilestoneDefinition("ten_upgrades", "Shopper 🛍️", "Own 10 upgrades total", MilestoneTrigger.TOTAL_UPGRADES_OWNED, 10),
        MilestoneDefinition("fifty_upgrades", "Upgrade Addict 💊", "Own 50 upgrades total", MilestoneTrigger.TOTAL_UPGRADES_OWNED, 50)
    )
}
