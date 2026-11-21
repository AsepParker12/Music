package com.music.app.feature.explorer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.music.app.core.designsystem.component.MusicPathBar
import com.music.app.core.model.FolderNode

/** Where the open folder lies: the top, then every folder down to it - [folders], the open one last. */
@Composable
internal fun FolderPathBar(folders: List<FolderNode>, onOpen: (path: String?) -> Unit, modifier: Modifier = Modifier) {
    MusicPathBar(
        levels = listOf(stringResource(R.string.explorer_all_folders)) + folders.map { folderDisplayName(it) },
        onOpen = { level -> onOpen(folders.getOrNull(level - 1)?.path) },
        modifier = modifier,
    )
}
