package com.music.app.feature.settings.updates

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.app.core.common.network.NetworkMonitor
import com.music.app.core.datastore.UpdateSettings
import com.music.app.feature.settings.remote.RemoteList
import com.music.app.feature.update.github.Release
import com.music.app.feature.update.github.Releases
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Every release on the chosen channel, newest first, with its notes - see [RemoteList]. */
@HiltViewModel
class ChangelogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    updateSettings: UpdateSettings,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val channel = updateSettings.channel.value

    val releases: RemoteList<Release> = RemoteList(
        scope = viewModelScope,
        networkMonitor = networkMonitor,
        cached = { Releases.cached(context, channel).ifEmpty { null } },
        fetch = { force -> Releases.all(context, channel, force) },
    )
}
