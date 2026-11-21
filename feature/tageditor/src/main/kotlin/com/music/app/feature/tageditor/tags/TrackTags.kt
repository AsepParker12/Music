package com.music.app.feature.tageditor.tags

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.feature.tageditor.R

/**
 * One tag the editor reads, shows and writes: how it is labelled, drawn and typed, the name TagLib gives it in every format
 * alike, and whether a video [describesVideos] - its title, its channel, its genre, its date and its description - rather
 * than a song alone. The order here is the order the fields are laid out in.
 */
enum class TagField(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    internal val key: String,
    val isNumeric: Boolean = false,
    val isMultiline: Boolean = false,
    val describesVideos: Boolean = false,
) {
    TITLE(R.string.tag_field_title, MusicIcons.Title, "TITLE", describesVideos = true),
    ARTIST(R.string.tag_field_artist, MusicIcons.Artist, "ARTIST", describesVideos = true),
    ALBUM(R.string.tag_field_album, MusicIcons.Album, "ALBUM"),
    ALBUM_ARTIST(R.string.tag_field_album_artist, MusicIcons.Group, "ALBUMARTIST"),
    GENRE(R.string.tag_field_genre, MusicIcons.Sell, "GENRE", describesVideos = true),
    YEAR(R.string.tag_field_year, MusicIcons.CalendarMonth, "DATE", isNumeric = true, describesVideos = true),
    TRACK_NUMBER(R.string.tag_field_track_number, MusicIcons.Numbers, "TRACKNUMBER", isNumeric = true),
    TRACK_TOTAL(R.string.tag_field_track_count, MusicIcons.Numbers, "TRACKTOTAL", isNumeric = true),
    DISC_NUMBER(R.string.tag_field_disc_number, MusicIcons.Numbers, "DISCNUMBER", isNumeric = true),
    COMPOSER(R.string.tag_field_composer, MusicIcons.Song, "COMPOSER"),
    COMMENT(R.string.tag_field_comment, MusicIcons.Comment, "COMMENT", describesVideos = true),
    LYRICS(R.string.tag_field_lyrics, MusicIcons.Lyrics, "LYRICS", isMultiline = true),
}

/**
 * A track's tags, one text per [TagField]. A field left blank is one the file does not carry, and is taken
 * out of it on save - so two sets of tags are equal exactly when saving one over the other would change
 * nothing.
 */
@Immutable
data class TrackTags(private val values: Map<TagField, String> = emptyMap()) {

    operator fun get(field: TagField): String = values[field].orEmpty()

    fun with(field: TagField, value: String): TrackTags =
        TrackTags(if (value.isBlank()) values - field else values + (field to value))

    /** These tags with every field [other] fills taken from it, the rest left as they are. */
    fun overlaidWith(other: TrackTags): TrackTags = TrackTags(values + other.values)

    /** These tags with every field they leave blank taken from [other], the rest left as they are. */
    fun filledFrom(other: TrackTags): TrackTags = TrackTags(other.values + values)

    /** The fields that carry something, in [TagField]'s order. */
    val filledFields: List<TagField> get() = TagField.entries.filter { it in values }
}

/** What [this] tag is called, wherever it is edited or shown. */
val TagField.label: String
    @Composable get() = stringResource(labelRes)
