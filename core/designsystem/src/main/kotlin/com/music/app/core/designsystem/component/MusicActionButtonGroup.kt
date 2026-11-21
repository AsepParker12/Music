package com.music.app.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.music.app.core.designsystem.R
import com.music.app.core.designsystem.icon.MusicIcons
import com.music.app.core.designsystem.theme.MusicComponentStyle
import com.music.app.core.designsystem.theme.buttonPressShapes

/**
 * The buttons at the foot of every dialog and sheet - Cancel, OK, Save and the like.
 *
 * Holding one only tightens its corners: no button widens into its neighbour, so the row's buttons
 * never move under the user's finger.
 *
 * In an `AlertDialog` the whole group goes in `confirmButton`, with `dismissButton` left out: Material
 * lays those two slots out as unrelated buttons, and this keeps them spaced as one row.
 *
 * Unlike [MusicIconButtonGroup] the minimum touch target is left alone: a text button is already wider
 * than it, so it only adds height, which never reaches the painted container.
 */
@Composable
fun MusicActionButtonGroup(
    modifier: Modifier = Modifier,
    content: ButtonGroupScope.() -> Unit,
) {
    ButtonGroup(
        // Never reached by two or three short labels. Required by the group all the same.
        overflowIndicator = { menuState ->
            MusicIconButton(
                onClick = menuState::show,
                icon = MusicIcons.MoreVert,
                contentDescription = stringResource(R.string.top_bar_more_actions),
            )
        },
        modifier = modifier,
        // At the end of whatever width it is given - a sheet's or a screen's whole width - as a dialog's are.
        horizontalArrangement = Arrangement.spacedBy(MusicComponentStyle.ItemSpacing, Alignment.End),
        content = content,
    )
}

/**
 * One button in a [MusicActionButtonGroup], whose corners tighten while it is held. Declared in order:
 * the way out first, the action last.
 */
fun ButtonGroupScope.actionButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) = customItem(
    buttonGroupContent = {
        TextButton(
            onClick = onClick,
            shapes = buttonPressShapes(),
            enabled = enabled,
        ) {
            Text(label)
        }
    },
    menuContent = { menuState ->
        DropdownMenuItem(
            text = { Text(label) },
            onClick = {
                menuState.dismiss()
                onClick()
            },
            enabled = enabled,
        )
    },
)
