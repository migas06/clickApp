package com.magicteamdev0.viralclicker.data.repository

import com.magicteamdev0.viralclicker.data.db.*
import com.magicteamdev0.viralclicker.data.entity.*
import com.magicteamdev0.viralclicker.domain.catalog.MilestoneCatalog
import com.magicteamdev0.viralclicker.domain.model.ViralPoints
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val ownedUpgradeDao: OwnedUpgradeDao,
    private val milestoneDao: MilestoneDao,
    private val prestigeRunDao: PrestigeRunDao,
    private val skinDao: SkinDao
) {
    // ── Player ──────────────────────────────────────────────────────────

    suspend fun getOrCreatePlayer(): PlayerEntity {
        return playerDao.getPlayer() ?: PlayerEntity().also { playerDao.upsert(it) }
    }

    suspend fun savePlayer(player: PlayerEntity) = playerDao.upsert(player)

    fun observePlayer(): Flow<PlayerEntity?> = playerDao.observePlayer()

    // ── Owned Upgrades ──────────────────────────────────────────────────

    suspend fun getOwnedUpgrades(): List<OwnedUpgradeEntity> = ownedUpgradeDao.getAll()

    suspend fun saveOwnedUpgrade(entity: OwnedUpgradeEntity) = ownedUpgradeDao.upsert(entity)

    suspend fun deleteAllOwnedUpgrades() = ownedUpgradeDao.deleteAll()

    fun observeOwnedUpgrades(): Flow<List<OwnedUpgradeEntity>> = ownedUpgradeDao.observeAll()

    // ── Milestones ──────────────────────────────────────────────────────

    suspend fun initMilestones() {
        val seeds = MilestoneCatalog.all.map { MilestoneEntity(it.id) }
        milestoneDao.insertIfAbsent(seeds)
    }

    suspend fun getMilestones(): List<MilestoneEntity> = milestoneDao.getAll()

    suspend fun unlockMilestone(id: String, timestamp: Long) {
        milestoneDao.upsert(MilestoneEntity(id, timestamp))
    }

    fun observeMilestones(): Flow<List<MilestoneEntity>> = milestoneDao.observeAll()

    // ── Prestige Runs ────────────────────────────────────────────────────

    suspend fun addPrestigeRun(
        runNumber: Int,
        allTimePoints: ViralPoints,
        multiplierEarned: Double
    ) {
        prestigeRunDao.insert(
            PrestigeRunEntity(
                runNumber = runNumber,
                allTimePointsAtPrestige = allTimePoints.value.toPlainString(),
                multiplierEarned = multiplierEarned
            )
        )
    }

    // ── Skins ────────────────────────────────────────────────────────────

    suspend fun getOwnedSkins(): List<SkinEntity> = skinDao.getAll()

    suspend fun setActiveSkin(skinId: String) = skinDao.setActiveSkin(skinId)

    suspend fun unlockSkin(skinId: String) {
        val existing = skinDao.getAll().find { it.id == skinId }
        if (existing != null) skinDao.upsert(existing.copy(owned = true))
    }
}
