package com.music.app.feature.player

import android.view.HapticFeedbackConstants
import android.view.View
import com.music.app.core.designsystem.effect.performMusicHaptic

/** The light tick ArchiveTune's player controls answer a press with. */
@Suppress("DEPRECATION")
internal fun View.performContextClick() {
    performMusicHaptic(HapticFeedbackConstants.CONTEXT_CLICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
}
