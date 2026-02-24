package com.example.viralclicker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.viralclicker.data.entity.*

@Database(
    entities = [
        PlayerEntity::class,
        OwnedUpgradeEntity::class,
        MilestoneEntity::class,
        PrestigeRunEntity::class,
        SkinEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun ownedUpgradeDao(): OwnedUpgradeDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun prestigeRunDao(): PrestigeRunDao
    abstract fun skinDao(): SkinDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE player ADD COLUMN currentStreak INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player ADD COLUMN lastLoginDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE player ADD COLUMN longestStreak INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
