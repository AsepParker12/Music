package com.music.app.feature.settings.about

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.app.core.common.network.NetworkMonitor
import com.music.app.feature.settings.remote.RemoteList
import com.music.app.feature.update.github.Contributor
import com.music.app.feature.update.github.Contributors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** About's state - ArchiveTune's `AboutViewModel`: the repository's contributors - see [RemoteList]. */
@HiltViewModel
class AboutViewModel @Inject constructor(
    @ApplicationContext context: Context,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val contributors: RemoteList<Contributor> = RemoteList(
        scope = viewModelScope,
        networkMonitor = networkMonitor,
        cached = { Contributors.cached(context) },
        fetch = { force -> Contributors.all(context, force) },
    )
}
