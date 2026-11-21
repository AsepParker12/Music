package com.music.app.feature.download

import android.text.format.Formatter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.designsystem.component.MusicConnectedChoice
import com.music.app.core.designsystem.component.MusicGroupRow
import com.music.app.core.designsystem.component.MusicRowGroup
import com.music.app.core.designsystem.component.MusicValueRow
import com.music.app.core.designsystem.component.sheet.MusicBottomSheet
import com.music.app.core.designsystem.theme.buttonPressShapes
import com.music.app.core.downloader.AudioTrack
import com.music.app.core.downloader.AudioTrackKind
import com.music.app.core.downloader.MediaInfo
import com.music.app.core.downloader.TRANSCRIBED_CAPTIONS
import com.music.app.core.navigation.LocalNavigator
import com.music.app.feature.download.settings.SubtitleSettingsScreen
import com.music.app.core.downloader.captionLanguageOf
import com.music.app.core.downloader.translatedCaptionCode
import com.music.app.core.downloader.captionGroups
import com.music.app.core.downloader.estimatedSizeBytes
import com.music.app.core.downloader.pickedCaptions
import com.music.app.core.downloader.transcribersFor
import com.music.app.core.downloader.videoQualities
import com.music.app.core.model.AudioFormat
import com.music.app.core.model.DownloadChoices
import com.music.app.core.model.DownloadKind
import com.music.app.core.model.VideoQuality
import com.music.app.core.designsystem.icon.MusicIcons

/** A picker's line under its title. */
@Composable
private fun PickerHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
    )
}

/** A row picked as one of several - its radio leading it - the picker closing with the pick. */
@Composable
private fun RadioRow(label: String, value: String?, selected: Boolean, shapes: ListItemShapes, onClick: () -> Unit) {
    MusicValueRow(
        label = label,
        shapes = shapes,
        value = value,
        leading = { RadioButton(selected = selected, onClick = null) },
        trailing = null,
        onClick = onClick,
    )
}

/**
 * How good the download is, for [info]: a video's qualities, each with what it comes to for this video - or a song's formats -
 * kept for every download while "Use for every download" is on; and, for the few who want one, a format of the video's own.
 */
@Composable
internal fun QualitySheet(viewModel: DownloadViewModel, info: MediaInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val choices by viewModel.sheetChoices.collectAsStateWithLifecycle()
    var forEveryDownload by remember { mutableStateOf(true) }
    var isPickingFormat by remember { mutableStateOf(false) }
    fun sizeOf(bytes: Long?) = bytes?.let { Formatter.formatShortFileSize(context, it) }
    val isVideo = choices.kind == DownloadKind.VIDEO
    MusicBottomSheet(
        title = stringResource(if (isVideo) R.string.download_quality else R.string.download_format),
        onDismissRequest = onDismiss,
    ) {
        PickerHint(stringResource(R.string.download_quality_sizes))
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            val pick = { change: (DownloadChoices) -> DownloadChoices ->
                viewModel.onPickFormat(null)
                viewModel.onSheetChoicesChange(forEveryDownload, change)
                dismiss()
            }
            MusicRowGroup(
                if (isVideo) {
                    // A playlist's videos are read as they download: every quality is offered for them.
                    info.videoQualities.ifEmpty { VideoQuality.entries }.map<VideoQuality, MusicGroupRow> { quality ->
                        { shapes ->
                            RadioRow(
                                label = stringResource(quality.labelRes),
                                value = sizeOf(info.estimatedSizeBytes(choices.copy(videoQuality = quality))),
                                selected = quality == choices.videoQuality,
                                shapes = shapes,
                                onClick = { pick { it.copy(videoQuality = quality) } },
                            )
                        }
                    }
                } else {
                    AudioFormat.entries.map<AudioFormat, MusicGroupRow> { format ->
                        { shapes ->
                            RadioRow(
                                label = stringResource(format.labelRes),
                                value = sizeOf(info.estimatedSizeBytes(choices.copy(audioFormat = format))),
                                selected = format == choices.audioFormat,
                                shapes = shapes,
                                onClick = { pick { it.copy(audioFormat = format) } },
                            )
                        }
                    }
                },
            )
            MusicRowGroup(
                listOf(
                    { shapes ->
                        SwitchRow(
                            label = stringResource(R.string.download_for_every_download),
                            icon = MusicIcons.Bookmark,
                            shapes = shapes,
                            checked = forEveryDownload,
                            value = stringResource(R.string.download_for_every_download_off).takeIf { !forEveryDownload },
                        ) { forEveryDownload = it }
                    },
                    { shapes ->
                        MusicValueRow(
                            label = stringResource(R.string.download_option_format),
                            shapes = shapes,
                            icon = MusicIcons.Tune,
                            enabled = info.formats.isNotEmpty(),
                            onClick = { isPickingFormat = true },
                        )
                    },
                ),
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
    if (isPickingFormat) ExactFormatDialog(viewModel, info, onDismiss = { isPickingFormat = false })
}

/**
 * The videos's sounds - the one recorded first - one picked; a video's others, any added beside it as tracks of their own; and
 * whether every download keeps to the one recorded.
 */
@Composable
internal fun AudioTrackSheet(viewModel: DownloadViewModel, info: MediaInfo, onDismiss: () -> Unit) {
    val choices by viewModel.sheetChoices.collectAsStateWithLifecycle()
    val options by viewModel.options.collectAsStateWithLifecycle()
    val remembered by viewModel.choices.collectAsStateWithLifecycle()
    var alwaysOriginal by remember { mutableStateOf(remembered.audioLanguage == null) }
    val tracks = info.audioTracks
    val chosen = info.audioLanguageFor(choices.audioLanguage)
    MusicBottomSheet(title = stringResource(R.string.download_audio_track), onDismissRequest = onDismiss) {
        PickerHint(pluralStringResource(R.plurals.download_audio_tracks, tracks.size, tracks.size))
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            MusicRowGroup(
                tracks.map<AudioTrack, MusicGroupRow> { track ->
                    { shapes ->
                        RadioRow(
                            label = languageName(track.language),
                            value = stringResource(track.kind.summaryRes),
                            selected = track.language == chosen,
                            shapes = shapes,
                            onClick = {
                                viewModel.onAudioTrackChange(track.language.takeUnless { track.kind == AudioTrackKind.ORIGINAL }, alwaysOriginal)
                                dismiss()
                            },
                        )
                    }
                },
            )
            val others = tracks.filter { it.language != chosen }
            if (choices.kind == DownloadKind.VIDEO && others.isNotEmpty()) {
                SectionLabel(stringResource(R.string.download_audio_more))
                MusicRowGroup(
                    others.map<AudioTrack, MusicGroupRow> { track ->
                        { shapes ->
                            val isAdded = track.language in options.audioLanguages
                            MusicValueRow(
                                label = languageName(track.language),
                                shapes = shapes,
                                value = stringResource(track.kind.summaryRes),
                                trailing = if (isAdded) ({ Icon(MusicIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }) else null,
                                onClick = { viewModel.onMoreAudioToggle(track.language) },
                            )
                        }
                    },
                )
            }
            MusicRowGroup(
                listOf(
                    { shapes ->
                        SwitchRow(
                            label = stringResource(R.string.download_track_always_original),
                            icon = MusicIcons.Verified,
                            shapes = shapes,
                            checked = alwaysOriginal,
                            value = stringResource(R.string.download_track_remembered),
                        ) {
                            alwaysOriginal = it
                            viewModel.onAudioTrackChange(choices.audioLanguage, it)
                        }
                    },
                ),
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

/**
 * The video's captions, any picked - those made by people, those made from the speech, those translated, each group under its
 * heading - added into the video or as files beside it, or, downloading them alone, just which; Done keeps the picks.
 */
@Composable
internal fun SubtitlesSheet(viewModel: DownloadViewModel, info: MediaInfo, onDismiss: () -> Unit) {
    val choices by viewModel.sheetChoices.collectAsStateWithLifecycle()
    val options by viewModel.options.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val groups = info.captionGroups
    // The services that can transcribe this very link - none where neither can, whatever keys are given.
    val transcribers = info.transcribersFor(preferences)
    var picked by remember { mutableStateOf(info.pickedCaptions(choices, options, preferences, transcribers.isNotEmpty())) }
    var asFile by remember { mutableStateOf(choices.subtitlesAsFile) }
    var isAddingLanguage by remember { mutableStateOf(false) }
    fun pickRow(code: String, label: String, value: String? = null): MusicGroupRow = { shapes ->
        val isPicked = code in picked
        MusicValueRow(
            label = label,
            shapes = shapes,
            value = value,
            trailing = if (isPicked) ({ Icon(MusicIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }) else null,
            onClick = { picked = if (isPicked) picked - code else picked + code },
        )
    }
    MusicBottomSheet(title = stringResource(R.string.download_subtitles), onDismissRequest = onDismiss) {
        PickerHint(stringResource(R.string.download_subtitles_hint))
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            if (groups.made.isNotEmpty()) {
                SectionLabel(stringResource(R.string.download_subtitles_made))
                MusicRowGroup(groups.made.map { pickRow(it, languageName(it)) })
            }
            if (groups.spoken.isNotEmpty()) {
                SectionLabel(stringResource(R.string.download_subtitles_auto))
                val autoLabel = stringResource(R.string.download_subtitles_auto_language)
                MusicRowGroup(groups.spoken.map { pickRow(it, autoLabel.format(languageName(it))) })
            }
            // Translated by Music from the video's own - see CaptionTranslation.
            groups.translationSource?.let { source ->
                SectionLabel(stringResource(R.string.download_subtitles_translated))
                val translatedFrom = stringResource(R.string.download_subtitles_translated_from, languageName(source))
                val addLanguage: MusicGroupRow = { shapes ->
                    MusicValueRow(
                        label = stringResource(R.string.download_subtitles_add_language),
                        shapes = shapes,
                        icon = MusicIcons.Add,
                        onClick = { isAddingLanguage = true },
                    )
                }
                MusicRowGroup(
                    picked.filter { it != captionLanguageOf(it) }.map { code -> pickRow(code, languageName(captionLanguageOf(code)), translatedFrom) } + addLanguage,
                )
            }
            // Made from its speech by the user's own keys - see CaptionTranscription - or where to give one.
            SectionLabel(stringResource(R.string.download_subtitles_transcribed))
            MusicRowGroup(
                listOf(
                    if (transcribers.isNotEmpty()) {
                        val services = transcribers.map { it.title }
                        val by = services.getOrNull(1)?.let { stringResource(R.string.download_subtitles_transcribe_by_else, services[0], it) }
                            ?: stringResource(R.string.download_subtitles_transcribe_by, services[0])
                        pickRow(TRANSCRIBED_CAPTIONS, stringResource(R.string.download_subtitles_transcribe), by)
                    } else if (preferences.transcribes) {
                        // A key is given, but this link is none either service reads - said so, rather than offered to fail.
                        { shapes ->
                            MusicValueRow(
                                label = stringResource(R.string.download_subtitles_transcribe),
                                shapes = shapes,
                                value = stringResource(R.string.download_subtitles_transcribe_unavailable),
                                enabled = false,
                                trailing = null,
                                onClick = {},
                            )
                        }
                    } else {
                        { shapes ->
                            MusicValueRow(
                                label = stringResource(R.string.download_subtitles_transcribe),
                                shapes = shapes,
                                value = stringResource(R.string.download_subtitles_transcribe_needs_key),
                                onClick = {
                                    navigator.go(SubtitleSettingsScreen)
                                    dismiss()
                                },
                            )
                        }
                    },
                ),
            )
            if (choices.kind == DownloadKind.VIDEO) {
                SectionLabel(stringResource(R.string.download_subtitles_add))
                MusicConnectedChoice(
                    options = listOf(false, true),
                    selected = asFile,
                    onSelect = { asFile = it },
                    label = { stringResource(if (it) R.string.download_subtitles_as_file else R.string.download_subtitles_in_video) },
                    icon = { if (it) MusicIcons.Description else MusicIcons.Video },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Button(
                onClick = {
                    viewModel.onSubtitlesChange(picked, asFile)
                    dismiss()
                },
                shapes = buttonPressShapes(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(top = 16.dp),
            ) {
                Text(stringResource(R.string.download_done))
            }
        }
    }
    val source = groups.translationSource
    if (isAddingLanguage && source != null) {
        val pickedLanguages = picked.map(::captionLanguageOf)
        OptionDialog<String>(
            title = stringResource(R.string.download_subtitles_add_language),
            options = translatableLanguages().filter { it != captionLanguageOf(source) && it !in pickedLanguages },
            selected = null,
            label = { languageName(it) },
            onSelect = { language -> picked = picked + translatedCaptionCode(language, source) },
            onDismiss = { isAddingLanguage = false },
        )
    }
}

/** The quality row's value: the quality chosen - a song's format - and what it comes to, where that is known. */
@Composable
internal fun qualityValue(info: MediaInfo?, choices: DownloadChoices): String {
    val context = LocalContext.current
    val label = stringResource(if (choices.kind == DownloadKind.VIDEO) choices.videoQuality.labelRes else choices.audioFormat.labelRes)
    val size = info?.estimatedSizeBytes(choices)?.let { Formatter.formatShortFileSize(context, it) }
    return listOfNotNull(label, size).joinToString(" · ")
}
