package com.music.app

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

/**
 * The entry gate: Music is a personal app, so the first open on a device asks for an access code -
 * the owner's, or the guest code the owner hands out. Only the codes' SHA-256 hashes live here; a
 * device that has entered one once is never asked again.
 */
object AccessGate {

    private const val PREFS_NAME = "access_gate"
    private const val KEY_UNLOCKED = "unlocked"

    private const val OWNER_CODE_HASH = "3b40902027a00ef78f2e54a4f42f01bafa740467b9dbdd470dc39336c9e6331d"
    private const val GUEST_CODE_HASH = "cbf2980e96884395b7db00d7a0200b551bea26a4706ba0c449ae669abf5c3418"

    fun isUnlocked(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_UNLOCKED, false)

    /** Tries [code] against both hashes; on a match this device stays unlocked from now on. */
    fun tryUnlock(context: Context, code: String): Boolean {
        val hash = sha256(code.trim().uppercase())
        if (hash != OWNER_CODE_HASH && hash != GUEST_CODE_HASH) return false
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_UNLOCKED, true).apply()
        return true
    }

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}

/** The full-screen code prompt shown over everything until the device is unlocked. */
@Composable
fun AccessGateScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(text = stringResource(R.string.access_gate_prompt), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it; showError = false },
                label = { Text(stringResource(R.string.access_gate_label)) },
                singleLine = true,
                isError = showError,
                visualTransformation = PasswordVisualTransformation(),
                supportingText = if (showError) {
                    { Text(stringResource(R.string.access_gate_error)) }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (AccessGate.tryUnlock(context, code)) onUnlocked() else showError = true
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.access_gate_button))
            }
        }
    }
}
