package com.quranengine.domain.qurantextkit

import android.content.res.Configuration
import android.content.res.Resources

object TwoPagesUtils {

    /** Enable two-page mode by default on tablets (non-phone devices). */
    val settingDefaultValue: Boolean
        get() {
            val config = Resources.getSystem().configuration
            val layout = config.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
            return layout >= Configuration.SCREENLAYOUT_SIZE_LARGE
        }

    /** Returns true if the current screen width has enough space for two pages (> 900dp). */
    fun hasEnoughHorizontalSpace(): Boolean {
        val displayMetrics = Resources.getSystem().displayMetrics
        val widthDp = displayMetrics.widthPixels / displayMetrics.density
        return widthDp > 900
    }
}
