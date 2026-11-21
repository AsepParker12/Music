package com.music.app.core.data.quickplay

import com.music.app.core.common.di.ApplicationScope
import com.music.app.core.data.LibraryContent
import com.music.app.core.data.LibraryRepository
import com.music.app.core.data.isLoading
import com.music.app.core.data.itemsOrEmpty
import com.music.app.core.datastore.QuickPlayMode
import com.music.app.core.datastore.QuickPlaySettings
import com.music.app.core.model.PlaybackParent
import com.music.app.core.model.Track
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the quick play button and the launcher shortcut play, named as the library has it now. */
sealed interface QuickPlaySource {
    /** The collection played, or null for every track - which is also what the queue plays from. */
    val parent: PlaybackParent?

    data object AllTracks : QuickPlaySource {
        override val parent: PlaybackParent? get() = null
    }

    data object RecentlyPlayed : QuickPlaySource {
        override val parent: PlaybackParent get() = PlaybackParent.RecentlyPlayed
    }

    data object MostPlayed : QuickPlaySource {
        override val parent: PlaybackParent get() = PlaybackParent.MostPlayed
    }

    data class Collection(override val parent: PlaybackParent, val name: String) : QuickPlaySource
}

/** Quick play as the library's button shows it - what it plays, and how - and so what a tap on it plays. */
data class QuickPlay(val mode: QuickPlayMode, val source: QuickPlaySource)

/**
 * The one place the quick play source is read and chosen - by the library's button, the settings and
 * the player alike, so each of them always agrees on what quick play plays.
 *
 * A chosen collection since gone from the library - a deleted playlist, an album rescanned away - reads
 * as [QuickPlaySource.AllTracks] rather than leaving a button that plays nothing. The choice itself is
 * kept, so it holds again should the collection come back.
 */
@Singleton
class QuickPlaySourceRepository @Inject constructor(
    private val repository: LibraryRepository,
    private val quickPlaySettings: QuickPlaySettings,
    @ApplicationScope private val scope: CoroutineScope,
) {

    /** Held for the whole process, so a screen opening shows the source already named. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val source: StateFlow<QuickPlaySource> = quickPlaySettings.source.flow
        .flatMapLatest(::sourceOf)
        .stateIn(scope, SharingStarted.Eagerly, QuickPlaySource.AllTracks)

    /**
     * The source as it stands, waited for rather than read from [source] - which starts out as every
     * track until the library names the chosen collection. What playing it, as the app opens, needs.
     */
    suspend fun current(): QuickPlaySource = sourceOf(quickPlaySettings.source.value).first()

    /**
     * Makes [parent] - every track, while null - what quick play plays from now on. Written on the
     * application's scope, so a screen closing as it chooses cannot cancel the choice.
     */
    fun choose(parent: PlaybackParent?) {
        scope.launch { quickPlaySettings.setSource(parent) }
    }

    /**
     * [parent]'s tracks - every track, while null - in the order its own list shows them, once read. A
     * source with nothing in it - a listening history not yet begun, a playlist emptied - gives every
     * track instead, so quick play never plays nothing; [parent] comes back null then, as it plays from
     * no one collection.
     */
    suspend fun tracks(parent: PlaybackParent?): Pair<PlaybackParent?, List<Track>> {
        val tracks = parent?.let { read(repository.collectionTracks(it)) }.orEmpty()
        return if (tracks.isEmpty()) null to read(repository.tracks) else parent to tracks
    }

    private suspend fun read(tracks: Flow<LibraryContent<Track>>): List<Track> =
        tracks.first { !it.isLoading }.itemsOrEmpty

    private fun sourceOf(parent: PlaybackParent?): Flow<QuickPlaySource> =
        when (parent) {
            null -> flowOf(QuickPlaySource.AllTracks)
            PlaybackParent.RecentlyPlayed -> flowOf(QuickPlaySource.RecentlyPlayed)
            PlaybackParent.MostPlayed -> flowOf(QuickPlaySource.MostPlayed)
            else -> repository.collectionName(parent).map { name ->
                if (name == null) QuickPlaySource.AllTracks else QuickPlaySource.Collection(parent, name)
            }
        }
}
