package com.quranengine.features.quranview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

/** A footnote extracted from a translation, addressed by its position in the verse. */
data class TranslationFootnote(
    val index: Int,
    val text: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationFootnoteSheet(
    footnote: TranslationFootnote,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = QuranTheme.colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Footnote #${footnote.index + 1}",
                style = MaterialTheme.typography.titleMedium,
                color = QuranTheme.colors.text,
            )
            Text(
                text = footnote.text.trim('[', ']'),
                style = MaterialTheme.typography.bodyMedium,
                color = QuranTheme.colors.text,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
