package com.quranengine.domain.reciterservice

import com.quranengine.core.localization.Localizer
import com.quranengine.core.localization.Table
import com.quranengine.model.quranaudio.Reciter

fun Reciter.localizedName(localizer: Localizer): String {
    val localized = localizer.l(nameKey, table = Table.READERS)
    return if (localized == nameKey) {
        ENGLISH_RECITER_NAMES[nameKey] ?: humanizedName()
    } else {
        localized
    }
}

private fun Reciter.humanizedName(): String =
    nameKey
        .removePrefix("qari_")
        .removeSuffix("_gapless")
        .removeSuffix("_gapped")
        .split('_')
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { character ->
                if (character.isLowerCase()) character.titlecase() else character.toString()
            }
        }

private val ENGLISH_RECITER_NAMES = mapOf(
    "qari_abdulbaset_mujawwad_gapless" to "Abd Al-Basit Mujawwad",
    "qari_afasy_cali_gapless" to "Mishari Al-Afasy (California)",
    "qari_afasy_gapless" to "Mishary Al-Afasy",
    "qari_minshawi_mujawwad" to "Minshawy Mujawwad (gapped)",
    "qari_shatri_gapless" to "Abu Bakr Ash-Shatri",
    "qari_sudais_gapless" to "Abdurrahman As-Sudais",
    "qari_muaiqly_gapless" to "Maher Al Muaiqly",
    "qari_saad_al_ghamidi_gapless" to "Saad Al-Ghamdi",
)
