package com.music.app.feature.library.options

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.music.app.core.designsystem.R as DesignR
import com.music.app.core.designsystem.component.actionButton
import com.music.app.core.designsystem.component.dialog.MusicDialog
import com.music.app.core.designsystem.component.dialog.MusicDialogOption
import com.music.app.core.model.Artist
import com.music.app.feature.library.R

/** Which of the [artists] a track or album is credited to Go to artist opens, as they are credited. */
@Composable
internal fun ArtistPickerDialog(
    artists: List<Artist>,
    onDismiss: () -> Unit,
    onArtistSelected: (Artist) -> Unit,
) {
    // Read here rather than inside the buttons: they are built outside composition.
    val cancelLabel = stringResource(DesignR.string.confirmation_dialog_cancel)
    MusicDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.options_artist_details),
        buttons = { actionButton(label = cancelLabel, onClick = onDismiss) },
    ) {
        Column(modifier = Modifier.selectableGroup()) {
            artists.forEach { artist ->
                MusicDialogOption(label = artist.name, selected = false, onClick = { onArtistSelected(artist) })
            }
        }
    }
}
