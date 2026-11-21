package com.music.app.feature.settings.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.Screen
import com.music.app.feature.settings.R
import com.music.app.core.designsystem.component.MusicMark
import com.music.app.core.designsystem.component.settings.SettingsAvatar
import com.music.app.feature.settings.component.SettingsHeader
import com.music.app.core.designsystem.component.settings.SettingsLeadingIcon
import com.music.app.core.designsystem.component.settings.SettingsLinkItem
import com.music.app.core.designsystem.component.settings.SettingsList
import com.music.app.feature.settings.component.SettingsLoad
import com.music.app.feature.settings.component.SettingsLoadStatus
import com.music.app.core.designsystem.component.settings.SettingsNavigationItem
import com.music.app.core.designsystem.component.settings.SettingsSection
import com.music.app.core.designsystem.component.settings.SettingsSectionDivider
import com.music.app.feature.update.github.GitHub
import com.music.app.feature.update.installedBuild
import com.music.app.core.designsystem.icon.MusicIcons

/** A page of the project's on the web, as About links to it. */
private class ProjectLink(val labelRes: Int, val url: String, val icon: @Composable () -> Painter)

private val ProjectLinks = listOf(
    ProjectLink(R.string.about_link_github, GitHub.REPOSITORY_URL) { painterResource(R.drawable.ic_github) },
    ProjectLink(R.string.about_link_releases, GitHub.RELEASES_URL) { rememberVectorPainter(MusicIcons.NewReleases) },
    ProjectLink(R.string.about_link_issues, GitHub.ISSUES_URL) { rememberVectorPainter(MusicIcons.BugReport) },
)

/** Someone Music is by, as About lists them. */
private class Person(val name: String, val avatarUrl: String, val roleRes: Int, val profileUrl: String)

private val LeadDeveloper = Person(GitHub.OWNER, GitHub.AVATAR_URL, R.string.about_role_lead_developer, GitHub.PROFILE_URL)

/**
 * What Music is and who it is by: the installed build, the project's pages and its libraries' licenses, its
 * developer, and its contributors - kept from last time, and asked for again
 * whenever the device is online.
 */
data object AboutScreen : Screen {
    override val titleRes: Int get() = R.string.about_title

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: AboutViewModel = hiltViewModel()
        val contributors by viewModel.contributors.load.collectAsStateWithLifecycle()
        val installedBuild = LocalContext.current.installedBuild()

        SettingsList {
            // Which version, which kind of build, and which of the release's APKs is installed.
            SettingsHeader(
                mark = { MusicMark(modifier = Modifier.fillMaxSize()) },
                title = stringResource(R.string.about_app_name),
                details = listOfNotNull(
                    installedBuild.versionName,
                    stringResource(if (installedBuild.isDebug) R.string.about_build_debug else R.string.about_build_release),
                    installedBuild.variant?.label,
                ).joinToString(" · "),
            )

            SettingsSection(stringResource(R.string.about_project)) {
                ProjectLinks.forEach { link ->
                    SettingsLinkItem(
                        title = stringResource(link.labelRes),
                        summary = link.url.withoutScheme(),
                        url = link.url,
                        leadingContent = { SettingsLeadingIcon(link.icon()) },
                    )
                }
                SettingsNavigationItem(
                    title = stringResource(R.string.about_licenses),
                    summary = stringResource(R.string.about_licenses_summary),
                    icon = MusicIcons.Description,
                    onClick = { navigator.go(LicensesScreen) },
                )
            }

            SettingsSectionDivider()

            SettingsSection(stringResource(R.string.about_lead_developer)) {
                PersonItem(LeadDeveloper)
            }

            SettingsSectionDivider()

            SettingsSection(stringResource(R.string.about_contributors)) {
                SettingsLoadStatus(contributors, onRetry = viewModel.contributors::retry)
                (contributors as? SettingsLoad.Loaded)?.items?.forEach { contributor ->
                    SettingsLinkItem(
                        title = contributor.login,
                        summary = contributor.profileUrl.withoutScheme(),
                        url = contributor.profileUrl,
                        leadingContent = { SettingsAvatar(contributor.avatarUrl) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonItem(person: Person) {
    SettingsLinkItem(
        title = person.name,
        summary = stringResource(person.roleRes),
        url = person.profileUrl,
        leadingContent = { SettingsAvatar(person.avatarUrl) },
    )
}

/** "github.com/AsepParker12/Music": a link as a row shows it. */
private fun String.withoutScheme(): String = removePrefix("https://")
