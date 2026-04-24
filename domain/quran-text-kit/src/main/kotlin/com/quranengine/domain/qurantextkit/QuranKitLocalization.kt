package com.quranengine.domain.qurantextkit

import com.quranengine.core.localization.Language
import com.quranengine.core.localization.Localizer
import com.quranengine.core.localization.NumberFormatters
import com.quranengine.core.localization.Table
import com.quranengine.core.localization.format
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Hizb
import com.quranengine.model.qurankit.Juz
import com.quranengine.model.qurankit.Page
import com.quranengine.model.qurankit.Quarter
import com.quranengine.model.qurankit.Sura

// ---------------------------------------------------------------------------
// AyahNumber extensions
// ---------------------------------------------------------------------------

fun AyahNumber.localizedAyahNumber(localizer: Localizer): String =
    localizer.lFormat("quran_ayah", table = Table.ANDROID, arguments = arrayOf(ayah))

fun AyahNumber.localizedName(localizer: Localizer): String {
    val suraName = sura.localizedName(localizer)
    return "$suraName, ${localizedAyahNumber(localizer)}"
}

fun AyahNumber.localizedNameWithSuraNumber(localizer: Localizer): String {
    val localizedSura = sura.localizedName(localizer, withNumber = true)
    return "$localizedSura - ${localizedAyahNumber(localizer)}"
}

// ---------------------------------------------------------------------------
// Juz extensions
// ---------------------------------------------------------------------------

fun Juz.localizedName(localizer: Localizer): String {
    val localized = localizer.lFormat(
        "juz2_description",
        table = Table.ANDROID,
        arguments = arrayOf(NumberFormatters.shared.format(juzNumber)),
    )
    return if (localized == "juz2_description") {
        "Juz ${NumberFormatters.shared.format(juzNumber)}"
    } else {
        localized
    }
}

// ---------------------------------------------------------------------------
// Page extensions
// ---------------------------------------------------------------------------

fun Page.localizedName(localizer: Localizer): String {
    val pageLabel = localizer.lAndroid("quran_page")
    val localizedNumber = NumberFormatters.shared.format(pageNumber)
    return if (pageLabel == "quran_page") {
        "Page $localizedNumber"
    } else {
        "$pageLabel $localizedNumber"
    }
}

fun Page.localizedNumber(): String =
    NumberFormatters.shared.format(pageNumber)

fun Page.localizedQuarterName(localizer: Localizer): String {
    val juzDescription = startJuz.localizedName(localizer)
    val q = quarter
    return if (q != null) {
        listOf(juzDescription, q.localizedName(localizer)).joinToString(", ")
    } else {
        juzDescription
    }
}

// ---------------------------------------------------------------------------
// Hizb extensions
// ---------------------------------------------------------------------------

fun Hizb.localizedName(localizer: Localizer): String {
    val hizbLabel = localizer.lAndroid("quran_hizb")
    val localizedNumber = NumberFormatters.shared.format(hizbNumber)
    return if (hizbLabel == "quran_hizb") {
        "Hizb $localizedNumber"
    } else {
        "$hizbLabel $localizedNumber"
    }
}

// ---------------------------------------------------------------------------
// Quarter extensions
// ---------------------------------------------------------------------------

fun Quarter.localizedName(localizer: Localizer): String {
    val rub = quarterNumber - 1
    val remainder = rub % 4

    val fraction: String? = when (remainder) {
        1 -> localizer.lAndroid("quran_rob3")
        2 -> localizer.lAndroid("quran_nos")
        3 -> localizer.lAndroid("quran_talt_arb3")
        else -> null
    }?.let { localized ->
        quarterFractionFallback(localized, remainder)
    }

    val hizbString = hizb.localizedName(localizer)
    return listOfNotNull(fraction, hizbString).joinToString(" ")
}

// ---------------------------------------------------------------------------
// Sura extensions
// ---------------------------------------------------------------------------

fun Sura.localizedSuraNumber(): String =
    NumberFormatters.shared.format(suraNumber)

fun Sura.localizedName(
    localizer: Localizer,
    withPrefix: Boolean = false,
    withNumber: Boolean = false,
    language: Language? = null,
): String {
    var suraName = localizer.l("sura_names[${suraNumber - 1}]", table = Table.SURAS, language = language)
    if (withPrefix) {
        suraName = localizer.lFormat("quran_sura_title", table = Table.ANDROID, language = language, arguments = arrayOf(suraName))
    }
    if (withNumber) {
        suraName = "${localizedSuraNumber()}. $suraName"
    }
    return suraName
}

fun Sura.englishName(): String = ENGLISH_SURA_NAMES[suraNumber - 1]

private fun quarterFractionFallback(localized: String, remainder: Int): String =
    if (localized.startsWith("quran_")) {
        when (remainder) {
            1 -> "Quarter"
            2 -> "Half"
            3 -> "Three Quarters"
            else -> localized
        }
    } else {
        localized
    }

fun Sura.arabicSuraName(): String {
    val codePoint = DECORATED_SURAS_CODE_POINTS[suraNumber - 1]
    return String(Character.toChars(codePoint))
}

private val DECORATED_SURAS_CODE_POINTS = intArrayOf(
    0xE904, 0xE905, 0xE906, 0xE907, 0xE908, 0xE90B,
    0xE90C, 0xE90D, 0xE90E, 0xE90F, 0xE910, 0xE911,
    0xE912, 0xE913, 0xE914, 0xE915, 0xE916, 0xE917,
    0xE918, 0xE919, 0xE91A, 0xE91B, 0xE91C, 0xE91D,
    0xE91E, 0xE91F, 0xE920, 0xE921, 0xE922, 0xE923,
    0xE924, 0xE925, 0xE926, 0xE92E, 0xE92F, 0xE930,
    0xE931, 0xE909, 0xE90A, 0xE927, 0xE928, 0xE929,
    0xE92A, 0xE92B, 0xE92C, 0xE92D, 0xE932, 0xE902,
    0xE933, 0xE934, 0xE935, 0xE936, 0xE937, 0xE938,
    0xE939, 0xE93A, 0xE93B, 0xE93C, 0xE900, 0xE901,
    0xE941, 0xE942, 0xE943, 0xE944, 0xE945, 0xE946,
    0xE947, 0xE948, 0xE949, 0xE94A, 0xE94B, 0xE94C,
    0xE94D, 0xE94E, 0xE94F, 0xE950, 0xE951, 0xE952,
    0xE93D, 0xE93E, 0xE93F, 0xE940, 0xE953, 0xE954,
    0xE955, 0xE956, 0xE957, 0xE958, 0xE959, 0xE95A,
    0xE95B, 0xE95C, 0xE95D, 0xE95E, 0xE95F, 0xE960,
    0xE961, 0xE962, 0xE963, 0xE964, 0xE965, 0xE966,
    0xE967, 0xE968, 0xE969, 0xE96A, 0xE96B, 0xE96C,
    0xE96D, 0xE96E, 0xE96F, 0xE970, 0xE971, 0xE972,
)

private val ENGLISH_SURA_NAMES = listOf(
    "Al-Fātihah",
    "Al-Baqarah",
    "Āli-ʿImrān",
    "An-Nisāʾ",
    "Al-Māʾidah",
    "Al-Anʿām",
    "Al-Aʿrāf",
    "Al-Anfāl",
    "At-Tawbah",
    "Yūnus",
    "Hūd",
    "Yūsuf",
    "Ar-Raʿd",
    "Ibrāhīm",
    "Al-Ḥijr",
    "An-Naḥl",
    "Al-Isrāʾ",
    "Al-Kahf",
    "Maryam",
    "Ṭā-Hā",
    "Al-Anbiyāʾ",
    "Al-Ḥajj",
    "Al-Muʾminūn",
    "An-Nūr",
    "Al-Furqān",
    "Ash-Shuʿarāʾ",
    "An-Naml",
    "Al-Qaṣaṣ",
    "Al-ʿAnkabūt",
    "Ar-Rūm",
    "Luqmān",
    "As-Sajdah",
    "Al-Aḥzāb",
    "Sabaʾ",
    "Fāṭir",
    "Yā-Sīn",
    "Aṣ-Ṣāffāt",
    "Ṣād",
    "Az-Zumar",
    "Ghāfir",
    "Fuṣṣilat",
    "Ash-Shūrā",
    "Az-Zukhruf",
    "Ad-Dukhān",
    "Al-Jāthiyah",
    "Al-Aḥqāf",
    "Muḥammad",
    "Al-Fatḥ",
    "Al-Ḥujurāt",
    "Qāf",
    "Adh-Dhāriyāt",
    "Aṭ-Ṭūr",
    "An-Najm",
    "Al-Qamar",
    "Ar-Raḥmān",
    "Al-Wāqiʿah",
    "Al-Ḥadīd",
    "Al-Mujādilah",
    "Al-Ḥashr",
    "Al-Mumtaḥanah",
    "Aṣ-Ṣaff",
    "Al-Jumuʿah",
    "Al-Munāfiqūn",
    "At-Taghābun",
    "Aṭ-Ṭalāq",
    "At-Taḥrīm",
    "Al-Mulk",
    "Al-Qalam",
    "Al-Ḥāqqah",
    "Al-Maʿārij",
    "Nūḥ",
    "Al-Jinn",
    "Al-Muzzammil",
    "Al-Muddaththir",
    "Al-Qiyāmah",
    "Al-Insān",
    "Al-Mursalāt",
    "An-Nabaʾ",
    "An-Nāziʿāt",
    "ʿAbasa",
    "At-Takwīr",
    "Al-Infiṭār",
    "Al-Muṭaffifīn",
    "Al-Inshiqāq",
    "Al-Burūj",
    "Aṭ-Ṭāriq",
    "Al-Aʿlā",
    "Al-Ghāshiyah",
    "Al-Fajr",
    "Al-Balad",
    "Ash-Shams",
    "Al-Layl",
    "Aḍ-Ḍuḥā",
    "Ash-Sharḥ",
    "At-Tīn",
    "Al-ʿAlaq",
    "Al-Qadr",
    "Al-Bayyinah",
    "Az-Zalzalah",
    "Al-ʿĀdiyāt",
    "Al-Qāriʿah",
    "At-Takāthur",
    "Al-ʿAṣr",
    "Al-Humazah",
    "Al-Fīl",
    "Quraysh",
    "Al-Māʿūn",
    "Al-Kawthar",
    "Al-Kāfirūn",
    "An-Naṣr",
    "Al-Masad",
    "Al-Ikhlāṣ",
    "Al-Falaq",
    "An-Nās",
)
