package com.quranengine.embeddedhost

import android.content.Context
import android.content.Intent
import com.quranengine.ui.theme.AppearanceMode

object QuranHostContract {
    private const val EXTRA_INITIAL_PAGE = "com.quranengine.embeddedhost.extra.INITIAL_PAGE"
    private const val EXTRA_FORCE_DARK_MODE = "com.quranengine.embeddedhost.extra.FORCE_DARK_MODE"
    private const val EXTRA_SHOW_CLOSE_BUTTON = "com.quranengine.embeddedhost.extra.SHOW_CLOSE_BUTTON"
    private const val EXTRA_LAST_PAGE = "com.quranengine.embeddedhost.extra.LAST_PAGE"

    fun createIntent(
        context: Context,
        initialPage: Int? = null,
        forceDarkMode: Boolean? = null,
        showCloseButton: Boolean = false,
    ): Intent =
        Intent(context, QuranHostActivity::class.java).apply {
            if (initialPage != null) {
                putExtra(EXTRA_INITIAL_PAGE, initialPage)
            }
            if (forceDarkMode != null) {
                putExtra(EXTRA_FORCE_DARK_MODE, forceDarkMode)
            }
            putExtra(EXTRA_SHOW_CLOSE_BUTTON, showCloseButton)
        }

    internal fun initialPage(intent: Intent?): Int? =
        intent?.takeIf { it.hasExtra(EXTRA_INITIAL_PAGE) }?.getIntExtra(EXTRA_INITIAL_PAGE, 1)

    internal fun forcedAppearanceMode(intent: Intent?): AppearanceMode? =
        intent?.takeIf { it.hasExtra(EXTRA_FORCE_DARK_MODE) }?.let { source ->
            if (source.getBooleanExtra(EXTRA_FORCE_DARK_MODE, false)) AppearanceMode.DARK else AppearanceMode.LIGHT
        }

    internal fun showCloseButton(intent: Intent?): Boolean =
        intent?.getBooleanExtra(EXTRA_SHOW_CLOSE_BUTTON, false) ?: false

    internal fun resultIntent(lastPage: Int?): Intent =
        Intent().apply {
            if (lastPage != null) {
                putExtra(EXTRA_LAST_PAGE, lastPage)
            }
        }

    fun lastPage(resultIntent: Intent?): Int? =
        resultIntent?.takeIf { it.hasExtra(EXTRA_LAST_PAGE) }?.getIntExtra(EXTRA_LAST_PAGE, 1)
}
