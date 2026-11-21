package com.music.app.feature.download.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.designsystem.component.InfoSeparator
import com.music.app.core.designsystem.component.settings.SettingsChoiceItem
import com.music.app.core.designsystem.component.settings.SettingsList
import com.music.app.core.designsystem.component.settings.SettingsNavigationItem
import com.music.app.core.designsystem.component.settings.SettingsSection
import com.music.app.core.designsystem.component.settings.SettingsSectionDivider
import com.music.app.core.designsystem.component.settings.SettingsSwitchContainerItem
import com.music.app.core.designsystem.component.settings.SettingsSwitchItem
import com.music.app.core.designsystem.component.settings.SettingsSwitchSplitItem
import com.music.app.core.designsystem.component.settings.SettingsTextFieldDialog
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.downloader.KeyCheck
import com.music.app.core.downloader.ServiceUsage
import com.music.app.core.downloader.at
import com.music.app.core.model.SubtitleFormat
import com.music.app.core.model.TranscriptionService
import com.music.app.core.navigation.Screen
import com.music.app.feature.download.R
import java.text.NumberFormat

/**
 * Seal's Subtitle settings, and Music's way of making subtitles a video has none of: whether downloads take a video's
 * subtitles, in which languages and format, the site's own captions of the speech or not, and whether they are embedded in
 * the video - then the speech transcribed by the service chosen, each service by the user's own key, turned on and off
 * apart from it.
 */
data object SubtitleSettingsScreen : Screen {
    override val titleRes: Int get() = R.string.downloads_settings_subtitles_title

    @Composable
    override fun Content() {
        val viewModel: DownloadsSettingsViewModel = hiltViewModel()
        val preferences by viewModel.preferences.collectAsStateWithLifecycle()
        val choices by viewModel.choices.collectAsStateWithLifecycle()
        val supadataKeyCheck by viewModel.supadataKeyCheck.collectAsStateWithLifecycle()
        val groqRequests by viewModel.groqRequests.collectAsStateWithLifecycle()
        var isEditingLanguages by remember { mutableStateOf(false) }
        var editingKey by remember { mutableStateOf<TranscriptionService?>(null) }
        val keySet = stringResource(R.string.downloads_settings_key_set)
        val keysSummary = stringResource(R.string.downloads_settings_transcription_summary)
        val unconverted = stringResource(R.string.downloads_settings_subtitles_unconverted)

        SettingsList {
            SettingsSection(stringResource(R.string.downloads_settings_general_section)) {
                SettingsSwitchContainerItem(
                    title = stringResource(R.string.downloads_settings_download_subtitles),
                    checked = choices.captions,
                    onCheckedChange = { takes -> viewModel.updateChoices { it.copy(captions = takes) } },
                )
                SettingsNavigationItem(
                    title = stringResource(R.string.downloads_settings_subtitle_languages),
                    summary = preferences.subtitleLanguages.ifBlank { stringResource(R.string.downloads_settings_subtitle_languages_default) },
                    icon = MusicIcons.Language,
                    onClick = { isEditingLanguages = true },
                )
                SettingsChoiceItem(
                    title = stringResource(R.string.downloads_settings_convert_subtitles),
                    options = SubtitleFormat.entries.map { format -> format.extension?.uppercase() ?: unconverted },
                    selectedIndex = preferences.subtitleFormat.ordinal,
                    onSelect = { index -> viewModel.updatePreferences { it.copy(subtitleFormat = SubtitleFormat.entries[index]) } },
                    icon = MusicIcons.Sync,
                )
                SettingsSwitchItem(
                    title = stringResource(R.string.downloads_settings_auto_subtitles),
                    summary = stringResource(R.string.downloads_settings_auto_subtitles_summary),
                    icon = MusicIcons.ClosedCaption,
                    checked = preferences.writesAutoSubtitles,
                    onCheckedChange = { writes -> viewModel.updatePreferences { it.copy(writesAutoSubtitles = writes) } },
                )
                SettingsSwitchItem(
                    title = stringResource(R.string.downloads_settings_embed_subtitles),
                    summary = stringResource(R.string.downloads_settings_embed_subtitles_summary),
                    icon = MusicIcons.Subtitles,
                    checked = !choices.subtitlesAsFile,
                    onCheckedChange = { embeds -> viewModel.updateChoices { it.copy(subtitlesAsFile = !embeds) } },
                )
                // Only embedded subtitles are kept or not beside the video: saved as files, they are nothing but.
                SettingsSwitchItem(
                    title = stringResource(R.string.downloads_settings_keep_subtitle_files),
                    icon = MusicIcons.Save,
                    checked = preferences.keepsSubtitleFiles,
                    onCheckedChange = { keeps -> viewModel.updatePreferences { it.copy(keepsSubtitleFiles = keeps) } },
                    enabled = !choices.subtitlesAsFile,
                )
            }

            SettingsSectionDivider()

            SettingsSection(stringResource(R.string.downloads_settings_transcription_title)) {
                // Tried first; the other after it, where it is on with its key given too.
                SettingsChoiceItem(
                    title = stringResource(R.string.downloads_settings_transcription_service_title),
                    options = TranscriptionService.entries.map { it.title },
                    selectedIndex = preferences.transcriptionService.ordinal,
                    onSelect = { index -> viewModel.updatePreferences { it.copy(transcriptionService = TranscriptionService.entries[index]) } },
                    icon = MusicIcons.RecordVoiceOver,
                )
                TranscriptionService.entries.forEach { service ->
                    // Whether its key works and what of its allowance is left, as far as the service tells - else that it has one.
                    val usage = when (service) {
                        TranscriptionService.SUPADATA -> supadataKeyCheck?.let { keyCheckSummary(it) }
                        TranscriptionService.GROQ -> groqRequests?.let { requestsSummary(it.at()) }
                    }
                    // On with no key yet, it asks for one: there is nothing to transcribe with until it has it.
                    SettingsSwitchSplitItem(
                        title = stringResource(service.keyTitleRes),
                        summary = when {
                            preferences.keyFor(service).isBlank() -> keysSummary
                            usage != null -> usage
                            else -> keySet
                        },
                        icon = MusicIcons.Key,
                        checked = preferences.isTranscribingWith(service),
                        onCheckedChange = { on ->
                            if (on && preferences.keyFor(service).isBlank()) editingKey = service else viewModel.updatePreferences { it.transcribingWith(service, on) }
                        },
                        onClick = { editingKey = service },
                    )
                }
            }
        }

        if (isEditingLanguages) {
            SubtitleLanguagesDialog(
                languages = preferences.subtitleLanguages,
                onSave = { languages ->
                    viewModel.updatePreferences { it.copy(subtitleLanguages = languages.trim()) }
                    isEditingLanguages = false
                },
                onDismiss = { isEditingLanguages = false },
            )
        }

        editingKey?.let { service ->
            val refusedLabel = stringResource(R.string.downloads_settings_key_refused)
            val unreachableLabel = stringResource(R.string.downloads_settings_key_unreachable, service.title)
            SettingsTextFieldDialog(
                title = stringResource(service.keyTitleRes),
                icon = MusicIcons.Key,
                description = keysSummary,
                initialValue = preferences.keyFor(service),
                // Tried against the service before it is kept - an empty key, taking the service off, needs no trying.
                check = { key ->
                    if (key.isBlank()) {
                        null
                    } else {
                        when (viewModel.checkKey(service, key.trim())) {
                            KeyCheck.Refused -> refusedLabel
                            KeyCheck.Unreachable -> unreachableLabel
                            else -> null
                        }
                    }
                },
                onConfirm = { key ->
                    // A key given is a service on.
                    viewModel.updatePreferences { it.withKey(service, key.trim()).transcribingWith(service, key.isNotBlank()) }
                    editingKey = null
                },
                onDismiss = { editingKey = null },
            )
        }
    }
}

/** What Supadata said of its key: "98,800 of 100,000 credits left · Pro" where it works - its own count, this billing period. */
@Composable
private fun keyCheckSummary(check: KeyCheck): String = when (check) {
    KeyCheck.Checking -> stringResource(R.string.downloads_settings_key_checking)
    KeyCheck.Refused -> stringResource(R.string.downloads_settings_key_refused)
    KeyCheck.Unreachable -> stringResource(R.string.downloads_settings_key_unreachable, TranscriptionService.SUPADATA.title)
    is KeyCheck.Works -> when (val usage = check.usage) {
        is ServiceUsage.Credits -> {
            val number = NumberFormat.getIntegerInstance()
            val left = stringResource(
                R.string.downloads_settings_supadata_usage,
                number.format((usage.max - usage.used).coerceAtLeast(0)),
                number.format(usage.max),
            )
            listOf(left, usage.plan).filter(String::isNotBlank).joinToString(InfoSeparator)
        }
        is ServiceUsage.DailyRequests -> requestsSummary(usage.at())
    }
}

/** "1,970 of 2,000 requests left today" - as Groq's last answer counted them. */
@Composable
private fun requestsSummary(requests: ServiceUsage.DailyRequests): String {
    val number = NumberFormat.getIntegerInstance()
    return stringResource(R.string.downloads_settings_groq_usage, number.format(requests.left), number.format(requests.limit))
}

private val TranscriptionService.keyTitleRes: Int
    get() = when (this) {
        TranscriptionService.SUPADATA -> R.string.downloads_settings_supadata_title
        TranscriptionService.GROQ -> R.string.downloads_settings_groq_title
    }
