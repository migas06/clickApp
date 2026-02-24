package com.example.viralclicker.domain.usecase

import com.example.viralclicker.data.entity.PlayerEntity
import com.example.viralclicker.domain.model.ViralPoints
import java.time.LocalDate
import javax.inject.Inject

class DailyStreakUseCase @Inject constructor() {

    data class StreakResult(
        val updatedPlayer: PlayerEntity,
        val bonusAwarded: ViralPoints?,
        val currentStreak: Int,
        val isNewDay: Boolean
    )

    fun execute(player: PlayerEntity): StreakResult {
        val today = LocalDate.now().toString()
        val lastLogin = player.lastLoginDate
        val yesterday = LocalDate.now().minusDays(1).toString()

        return when {
            lastLogin == today -> {
                // Already logged in today
                StreakResult(player, null, player.currentStreak, false)
            }
            lastLogin == yesterday -> {
                // Streak continues
                val newStreak = player.currentStreak + 1
                val bonus = bonusForDay(newStreak)
                val currentPts = ViralPoints.fromString(player.viralPoints)
                val allTimePts = ViralPoints.fromString(player.allTimePoints)
                val updated = player.copy(
                    currentStreak = newStreak,
                    lastLoginDate = today,
                    longestStreak = maxOf(player.longestStreak, newStreak),
                    viralPoints = (currentPts + bonus).value.toPlainString(),
                    allTimePoints = (allTimePts + bonus).value.toPlainString()
                )
                StreakResult(updated, bonus, newStreak, true)
            }
            else -> {
                // Streak broken (or first ever login), start at day 1
                val bonus = bonusForDay(1)
                val currentPts = ViralPoints.fromString(player.viralPoints)
                val allTimePts = ViralPoints.fromString(player.allTimePoints)
                val updated = player.copy(
                    currentStreak = 1,
                    lastLoginDate = today,
                    viralPoints = (currentPts + bonus).value.toPlainString(),
                    allTimePoints = (allTimePts + bonus).value.toPlainString()
                )
                StreakResult(updated, bonus, 1, true)
            }
        }
    }

    private fun bonusForDay(day: Int): ViralPoints = when {
        day >= 7 -> ViralPoints.fromLong(10_000)
        day >= 5 -> ViralPoints.fromLong(2_000)
        day >= 3 -> ViralPoints.fromLong(500)
        day >= 2 -> ViralPoints.fromLong(250)
        else -> ViralPoints.fromLong(100)
    }
}
