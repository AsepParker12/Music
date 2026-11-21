package com.music.app.core.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import com.music.app.core.model.FastScrollTouchArea
import kotlinx.coroutines.flow.StateFlow

/** The [FastScrollTouchArea] every fast scroller below [MusicTheme] grabs its thumb with. */
val LocalFastScrollTouchArea = compositionLocalOf<FastScrollTouchArea> {
    error("No FastScrollTouchArea provided: fast scrollers must be drawn inside MusicTheme")
}

/**
 * The app's current [FastScrollTouchArea], for whichever activity themes itself.
 *
 * The setting is stored in `:core:datastore`, which this module cannot see, so the app hands it over -
 * the same split as [AppCoverStyle].
 */
interface AppFastScrollTouchArea {
    val touchArea: StateFlow<FastScrollTouchArea>
}
