package com.quranengine.domain.quranaudiokit

import com.quranengine.core.localization.Localizer
import com.quranengine.core.localization.Table
import com.quranengine.model.quranaudio.AudioEnd

fun AudioEnd.localizedName(localizer: Localizer): String =
    when (this) {
        AudioEnd.JUZ -> localizer.lAndroid("quran_juz2")
        AudioEnd.SURA -> localizer.l("surah")
        AudioEnd.PAGE -> localizer.lAndroid("quran_page")
        AudioEnd.QURAN -> localizer.l("quran_alquran")
    }
