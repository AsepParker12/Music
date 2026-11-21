package com.music.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.music.app.core.database.dao.AddedDateDao
import com.music.app.core.database.dao.FileYearDao
import com.music.app.core.database.dao.HeldLyricsDao
import com.music.app.core.database.dao.AlbumDao
import com.music.app.core.database.dao.ArrangementDao
import com.music.app.core.database.dao.ArtistDao
import com.music.app.core.database.dao.BackupDao
import com.music.app.core.database.dao.DownloadDao
import com.music.app.core.database.dao.FavoriteCollectionDao
import com.music.app.core.database.dao.GenreDao
import com.music.app.core.database.dao.LyricsIndexDao
import com.music.app.core.database.dao.MediaHashDao
import com.music.app.core.database.dao.OnlineHeatmapDao
import com.music.app.core.database.dao.PlayStatsDao
import com.music.app.core.database.dao.PlaylistDao
import com.music.app.core.database.dao.QueueItemDao
import com.music.app.core.database.dao.ReplayHeatmapDao
import com.music.app.core.database.dao.ResumePositionDao
import com.music.app.core.database.dao.TrackDao
import com.music.app.core.database.dao.TrackMoveDao
import com.music.app.core.database.dao.VideoBookmarkDao
import com.music.app.core.database.dao.VideoChapterDao
import com.music.app.core.database.dao.VideoExtrasDao
import com.music.app.core.database.dao.VideoSubtitleDao
import com.music.app.core.database.entity.AddedDateEntity
import com.music.app.core.database.entity.FileYearEntity
import com.music.app.core.database.entity.HeldLyricsEntity
import com.music.app.core.database.entity.AlbumEntity
import com.music.app.core.database.entity.ArrangementEntity
import com.music.app.core.database.entity.ArtistEntity
import com.music.app.core.database.entity.DownloadCommandEntity
import com.music.app.core.database.entity.DownloadCommandShortcutEntity
import com.music.app.core.database.entity.DownloadEntity
import com.music.app.core.database.entity.DownloadWatchEntity
import com.music.app.core.database.entity.FavoriteCollectionEntity
import com.music.app.core.database.entity.GenreEntity
import com.music.app.core.database.entity.LyricsIndexEntity
import com.music.app.core.database.entity.MediaHashEntity
import com.music.app.core.database.entity.OnlineHeatmapEntity
import com.music.app.core.database.entity.PlayStatsEntity
import com.music.app.core.database.entity.PlaylistEntity
import com.music.app.core.database.entity.PlaylistTrackEntity
import com.music.app.core.database.entity.QueueItemEntity
import com.music.app.core.database.entity.ReplayHeatmapEntity
import com.music.app.core.database.entity.ResumePositionEntity
import com.music.app.core.database.entity.TrackEntity
import com.music.app.core.database.entity.VideoBookmarkEntity
import com.music.app.core.database.entity.VideoChapterEntity
import com.music.app.core.database.entity.VideoExtraReadEntity
import com.music.app.core.database.entity.VideoSubtitleEntity

@Database(
    entities = [
        TrackEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        GenreEntity::class,
        QueueItemEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        PlayStatsEntity::class,
        ResumePositionEntity::class,
        ArrangementEntity::class,
        FavoriteCollectionEntity::class,
        LyricsIndexEntity::class,
        MediaHashEntity::class,
        VideoSubtitleEntity::class,
        AddedDateEntity::class,
        HeldLyricsEntity::class,
        FileYearEntity::class,
        ReplayHeatmapEntity::class,
        DownloadEntity::class,
        OnlineHeatmapEntity::class,
        VideoChapterEntity::class,
        VideoExtraReadEntity::class,
        DownloadCommandEntity::class,
        DownloadCommandShortcutEntity::class,
        DownloadWatchEntity::class,
        VideoBookmarkEntity::class,
    ],
    version = 40,
    exportSchema = true,
)
@TypeConverters(CoverArtUrisConverter::class, ArtistIdsConverter::class)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun genreDao(): GenreDao
    abstract fun queueItemDao(): QueueItemDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playStatsDao(): PlayStatsDao
    abstract fun resumePositionDao(): ResumePositionDao
    abstract fun arrangementDao(): ArrangementDao
    abstract fun favoriteCollectionDao(): FavoriteCollectionDao
    abstract fun lyricsIndexDao(): LyricsIndexDao
    abstract fun trackMoveDao(): TrackMoveDao

    abstract fun mediaHashDao(): MediaHashDao
    abstract fun backupDao(): BackupDao
    abstract fun videoSubtitleDao(): VideoSubtitleDao
    abstract fun addedDateDao(): AddedDateDao
    abstract fun heldLyricsDao(): HeldLyricsDao
    abstract fun fileYearDao(): FileYearDao
    abstract fun replayHeatmapDao(): ReplayHeatmapDao
    abstract fun downloadDao(): DownloadDao
    abstract fun onlineHeatmapDao(): OnlineHeatmapDao
    abstract fun videoChapterDao(): VideoChapterDao
    abstract fun videoExtrasDao(): VideoExtrasDao
    abstract fun videoBookmarkDao(): VideoBookmarkDao

    companion object {
        const val FILE_NAME = "music.db"
    }
}
