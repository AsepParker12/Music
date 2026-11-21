package com.music.app

import com.music.app.core.common.di.ApplicationScope
import com.music.app.core.datastore.LibrarySettings
import com.music.app.core.designsystem.theme.AppFastScrollTouchArea
import com.music.app.core.model.FastScrollTouchArea
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * The stored fast scroll touch area, as every list's fast scroller grabs its thumb with it.
 *
 * Starts from the stored value, already in memory, so the first list shown already grabs as wide as
 * the user chose.
 */
@Singleton
class MusicFastScrollTouchArea @Inject constructor(
    @ApplicationScope scope: CoroutineScope,
    librarySettings: LibrarySettings,
) : AppFastScrollTouchArea {

    override val touchArea: StateFlow<FastScrollTouchArea> = librarySettings.fastScrollTouchArea.flow
        .stateIn(scope, SharingStarted.Eagerly, librarySettings.fastScrollTouchArea.value)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FastScrollTouchAreaModule {
    @Binds
    abstract fun bindAppFastScrollTouchArea(touchArea: MusicFastScrollTouchArea): AppFastScrollTouchArea
}
