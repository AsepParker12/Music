package com.music.app.core.vault.di

import android.content.Context
import androidx.room.Room
import com.music.app.core.vault.data.VAULT_MIGRATION_1_2
import com.music.app.core.vault.data.VAULT_MIGRATION_2_3
import com.music.app.core.vault.data.VAULT_MIGRATION_3_4
import com.music.app.core.vault.data.VAULT_MIGRATION_4_5
import com.music.app.core.vault.data.VAULT_MIGRATION_5_6
import com.music.app.core.vault.data.VaultDatabase
import com.music.app.core.vault.data.VaultItemDao
import com.music.app.core.vault.data.VaultReplayHeatmapDao
import com.music.app.core.vault.data.VaultResumePositionDao
import com.music.app.core.vault.data.VaultVideoBookmarkDao
import com.music.app.core.vault.data.VaultVideoSubtitleDao
import com.music.app.core.vault.vaultDirectory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VaultModule {
    @Provides
    @Singleton
    fun provideVaultDatabase(@ApplicationContext context: Context): VaultDatabase =
        Room.databaseBuilder(context, VaultDatabase::class.java, File(context.vaultDirectory, "vault.db").path)
            .addMigrations(VAULT_MIGRATION_1_2, VAULT_MIGRATION_2_3, VAULT_MIGRATION_3_4, VAULT_MIGRATION_4_5, VAULT_MIGRATION_5_6)
            .build()

    @Provides
    fun provideVaultItemDao(database: VaultDatabase): VaultItemDao = database.itemDao()

    @Provides
    fun provideVaultResumePositionDao(database: VaultDatabase): VaultResumePositionDao = database.resumePositionDao()

    @Provides
    fun provideVaultVideoSubtitleDao(database: VaultDatabase): VaultVideoSubtitleDao = database.videoSubtitleDao()

    @Provides
    fun provideVaultReplayHeatmapDao(database: VaultDatabase): VaultReplayHeatmapDao = database.replayHeatmapDao()

    @Provides
    fun provideVaultVideoBookmarkDao(database: VaultDatabase): VaultVideoBookmarkDao = database.videoBookmarkDao()
}
