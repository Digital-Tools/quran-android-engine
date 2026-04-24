package com.quranengine.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.quranengine.ui.theme.QuranTheme

data class ErrorState(
    val title: String = "Error",
    val message: String,
    val retryAction: (() -> Unit)? = null,
)

@Composable
fun ErrorAlertDialog(
    error: ErrorState,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = error.title) },
        text = { Text(text = error.message) },
        confirmButton = {
            if (error.retryAction != null) {
                TextButton(onClick = {
                    error.retryAction.invoke()
                    onDismiss()
                }) {
                    Text("Retry", color = QuranTheme.appIdentity)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = QuranTheme.appIdentity)
            }
        },
    )
}
