package com.music.app.feature.vault

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.app.core.navigation.LocalNavigator
import com.music.app.core.navigation.Screen
import com.music.app.core.vault.VaultState
import com.music.app.feature.vault.R

/** Changing the Private Folder's PIN, from inside it: the current PIN again, before a new one is chosen. */
internal data object VaultChangePinScreen : Screen {
    override val isPrivate: Boolean get() = true

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: VaultAccessViewModel = hiltViewModel()
        val vault by viewModel.state.collectAsStateWithLifecycle()

        VaultStepScaffold(title = stringResource(PinPurpose.CHANGE.titleRes), needsOpenVault = true) {
            val current = vault as? VaultState.Created ?: return@VaultStepScaffold
            PinEntry(
                title = stringResource(R.string.vault_current_pin),
                vault = current,
                onGranted = { navigator.go(VaultChoosePinScreen(PinPurpose.CHANGE)) },
            )
        }
    }
}
