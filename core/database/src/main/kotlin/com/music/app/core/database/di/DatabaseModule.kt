package com.music.app.core.database.di

import android.content.Context
import androidx.room.Room
import com.music.app.core.database.MIGRATION_4_5
import com.music.app.core.database.MIGRATION_5_6
import com.music.app.core.database.MIGRATION_6_7
import com.music.app.core.database.MIGRATION_7_8
import com.music.app.core.database.MIGRATION_8_9
import com.music.app.core.database.MIGRATION_9_10
import com.music.app.core.database.MIGRATION_10_11
import com.music.app.core.database.MIGRATION_11_12
import com.music.app.core.database.MIGRATION_12_13
import com.music.app.core.database.MIGRATION_13_14
import com.music.app.core.database.MIGRATION_14_15
import com.music.app.core.database.MIGRATION_15_16
import com.music.app.core.database.MIGRATION_16_17
import com.music.app.core.database.MIGRATION_17_18
import com.music.app.core.database.MIGRATION_18_19
import com.music.app.core.database.MIGRATION_19_20
import com.music.app.core.database.MIGRATION_20_21
import com.music.app.core.database.MIGRATION_21_22
import com.music.app.core.database.MIGRATION_22_23
import com.music.app.core.database.MIGRATION_23_24
import com.music.app.core.database.MIGRATION_24_25
import com.music.app.core.database.MIGRATION_25_26
import com.music.app.core.database.MIGRATION_26_27
import com.music.app.core.database.MIGRATION_27_28
import com.music.app.core.database.MIGRATION_28_29
import com.music.app.core.database.MIGRATION_29_30
import com.music.app.core.database.MIGRATION_30_31
import com.music.app.core.database.MIGRATION_31_32
import com.music.app.core.database.MIGRATION_32_33
import com.music.app.core.database.MIGRATION_33_34
import com.music.app.core.database.MIGRATION_34_35
import com.music.app.core.database.MIGRATION_35_36
import com.music.app.core.database.MIGRATION_36_37
import com.music.app.core.database.MIGRATION_37_38
import com.music.app.core.database.MIGRATION_38_39
import com.music.app.core.database.MIGRATION_39_40
import com.music.app.core.database.SeedBuiltInPlaylists
import com.music.app.core.database.MusicDatabase
import com.music.app.core.database.dao.AddedDateDao
import com.music.app.core.database.dao.DownloadDao
import com.music.app.core.database.dao.FileYearDao
import com.music.app.core.database.dao.OnlineHeatmapDao
import com.music.app.core.database.dao.VideoBookmarkDao
import com.music.app.core.database.dao.VideoChapterDao
import com.music.app.core.database.dao.VideoExtrasDao
import com.music.app.core.database.dao.ReplayHeatmapDao
import com.music.app.core.database.dao.HeldLyricsDao
import com.music.app.core.database.dao.AlbumDao
import com.music.app.core.database.dao.ArrangementDao
import com.music.app.core.database.dao.ArtistDao
import com.music.app.core.database.dao.BackupDao
import com.music.app.core.database.dao.FavoriteCollectionDao
import com.music.app.core.database.dao.GenreDao
import com.music.app.core.database.dao.LyricsIndexDao
import com.music.app.core.database.dao.MediaHashDao
import com.music.app.core.database.dao.PlayStatsDao
import com.music.app.core.database.dao.PlaylistDao
import com.music.app.core.database.dao.QueueItemDao
import com.music.app.core.database.dao.ResumePositionDao
import com.music.app.core.database.dao.TrackDao
import com.music.app.core.database.dao.TrackMoveDao
import com.music.app.core.database.dao.VideoSubtitleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMusicDatabase(@ApplicationContext context: Context): MusicDatabase =
        Room.databaseBuilder(context, MusicDatabase::class.java, MusicDatabase.FILE_NAME)
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32, MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36, MIGRATION_36_37, MIGRATION_37_38, MIGRATION_38_39, MIGRATION_39_40)
            // Seeds Favorites on a fresh install; MIGRATION_4_5 does the same for an existing one.
            .addCallback(SeedBuiltInPlaylists)
            // Still a net for a version pair no migration covers. Real migrations take precedence
            // when they exist, so from v4 onward playlists and play counts survive an upgrade.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideTrackDao(database: MusicDatabase): TrackDao = database.trackDao()

    @Provides
    fun providePlaylistDao(database: MusicDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun providePlayStatsDao(database: MusicDatabase): PlayStatsDao = database.playStatsDao()

    @Provides
    fun provideAlbumDao(database: MusicDatabase): AlbumDao = database.albumDao()

    @Provides
    fun provideArtistDao(database: MusicDatabase): ArtistDao = database.artistDao()

    @Provides
    fun provideGenreDao(database: MusicDatabase): GenreDao = database.genreDao()

    @Provides
    fun provideQueueItemDao(database: MusicDatabase): QueueItemDao = database.queueItemDao()

    @Provides
    fun provideResumePositionDao(database: MusicDatabase): ResumePositionDao = database.resumePositionDao()

    @Provides
    fun provideArrangementDao(database: MusicDatabase): ArrangementDao = database.arrangementDao()

    @Provides
    fun provideFavoriteCollectionDao(database: MusicDatabase): FavoriteCollectionDao = database.favoriteCollectionDao()

    @Provides
    fun provideLyricsIndexDao(database: MusicDatabase): LyricsIndexDao = database.lyricsIndexDao()

    @Provides
    fun provideTrackMoveDao(database: MusicDatabase): TrackMoveDao = database.trackMoveDao()

    @Provides
    fun provideMediaHashDao(database: MusicDatabase): MediaHashDao = database.mediaHashDao()

    @Provides
    fun provideBackupDao(database: MusicDatabase): BackupDao = database.backupDao()

    @Provides
    fun provideVideoSubtitleDao(database: MusicDatabase): VideoSubtitleDao = database.videoSubtitleDao()

    @Provides
    fun provideAddedDateDao(database: MusicDatabase): AddedDateDao = database.addedDateDao()

    @Provides
    fun provideHeldLyricsDao(database: MusicDatabase): HeldLyricsDao = database.heldLyricsDao()

    @Provides
    fun provideFileYearDao(database: MusicDatabase): FileYearDao = database.fileYearDao()

    @Provides
    fun provideReplayHeatmapDao(database: MusicDatabase): ReplayHeatmapDao = database.replayHeatmapDao()

    @Provides
    fun provideDownloadDao(database: MusicDatabase): DownloadDao = database.downloadDao()

    @Provides
    fun provideOnlineHeatmapDao(database: MusicDatabase): OnlineHeatmapDao = database.onlineHeatmapDao()

    @Provides
    fun provideVideoChapterDao(database: MusicDatabase): VideoChapterDao = database.videoChapterDao()

    @Provides
    fun provideVideoExtrasDao(database: MusicDatabase): VideoExtrasDao = database.videoExtrasDao()

    @Provides
    fun provideVideoBookmarkDao(database: MusicDatabase): VideoBookmarkDao = database.videoBookmarkDao()
}
