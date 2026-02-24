package com.magicteamdev0.viralclicker.di

import android.content.Context
import androidx.room.Room
import com.magicteamdev0.viralclicker.data.datastore.SettingsDataStore
import com.magicteamdev0.viralclicker.data.db.*
import com.magicteamdev0.viralclicker.data.repository.GameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "viral_clicker_db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun providePlayerDao(db: AppDatabase): PlayerDao = db.playerDao()

    @Provides
    fun provideOwnedUpgradeDao(db: AppDatabase): OwnedUpgradeDao = db.ownedUpgradeDao()

    @Provides
    fun provideMilestoneDao(db: AppDatabase): MilestoneDao = db.milestoneDao()

    @Provides
    fun providePrestigeRunDao(db: AppDatabase): PrestigeRunDao = db.prestigeRunDao()

    @Provides
    fun provideSkinDao(db: AppDatabase): SkinDao = db.skinDao()

    @Provides
    @Singleton
    fun provideGameRepository(
        playerDao: PlayerDao,
        ownedUpgradeDao: OwnedUpgradeDao,
        milestoneDao: MilestoneDao,
        prestigeRunDao: PrestigeRunDao,
        skinDao: SkinDao
    ): GameRepository = GameRepository(playerDao, ownedUpgradeDao, milestoneDao, prestigeRunDao, skinDao)

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
        SettingsDataStore(context)
}
