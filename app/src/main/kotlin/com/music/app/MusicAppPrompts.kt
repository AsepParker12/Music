package com.music.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.datastore.UpdateSettings
import com.music.app.core.navigation.AppPrompts
import com.music.app.feature.feedback.CrashReportPrompt
import com.music.app.feature.feedback.CrashReports
import com.music.app.feature.update.ui.UpdateGate
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/** The prompts every activity raises: an update found, while its prompt is turned on, and the offer to report a crash. */
class MusicAppPrompts @Inject constructor(
    private val updateSettings: UpdateSettings,
    private val crashReports: CrashReports,
) : AppPrompts {

    @Composable
    override fun Content() {
        val isUpdateAutoPromptEnabled by remember { updateSettings.autoPrompt.flow }
            .collectAsStateWithLifecycle(updateSettings.autoPrompt.value)
        UpdateGate(isAutoPromptEnabled = isUpdateAutoPromptEnabled)
        CrashReportPrompt(crashReports)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppPromptsModule {
    @Binds
    abstract fun bindAppPrompts(prompts: MusicAppPrompts): AppPrompts
}
