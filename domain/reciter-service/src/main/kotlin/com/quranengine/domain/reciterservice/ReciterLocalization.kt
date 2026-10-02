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
    "qari_abdulaziz_zahrani_gapless" to "Abdulaziz Az-Zahrani",
    "qari_abdulbaset_gapless" to "Abd Al-Basit",
    "qari_abdulbaset_mujawwad_gapless" to "Abd Al-Basit Mujawwad",
    "qari_abdullah_matroud_gapless" to "Abdullah Matroud",
    "qari_abdulmuhsin_qasim_gapless" to "AbdulMuhsin al Qasim",
    "qari_abdulrahman_alshahat_gapless" to "Abdulrahman Al-Shahat",
    "qari_abdurrashid_sufi_gapless" to "Abdur-Rashid Sufi",
    "qari_afasy_cali_gapless" to "Mishari Al-Afasy (California)",
    "qari_afasy_gapless" to "Mishary Al-Afasy",
    "qari_ahmad_nauina_gapless" to "Ahmad Nauina",
    "qari_akram_al_alaqmi" to "Akram Al-Alaqmi",
    "qari_ali_hajjaj_alsouasi_gapless" to "Ali Hajjaj Alsouasi",
    "qari_ali_jaber_gapless" to "Ali Jaber",
    "qari_alijon_qari_gapless" to "Alijon Qari",
    "qari_alzain_ahmad_gapless" to "Alzain Mohammad Ahmad",
    "qari_ayman_suwaid_gapless" to "Dr. Ayman Suwaid",
    "qari_ayyoub_gapless" to "Muhammad Ayyoub",
    "qari_aziz_alili_gapless" to "Aziz Alili",
    "qari_badr_al_turki_gapless" to "Badr Al-Turki",
    "qari_bandar_baleela_gapless" to "Bandar Baleela",
    "qari_basfar_gapless" to "Abdullah Basfar",
    "qari_fares_abbad_gapless" to "Fares Abbad",
    "qari_farman_shawani_gapless" to "Farman Shawani",
    "qari_hady_toure_gapless" to "Hady Toure",
    "qari_hani_rifai_gapless" to "Hani Ar-Rifai",
    "qari_husary_gapless" to "Husary",
    "qari_husary_iza3a_gapless" to "Husary Broadcast",
    "qari_husary_muallim_gapless" to "Husary (Muallim)",
    "qari_husary_mujawwad_gapless" to "Husary (Mujawwad)",
    "qari_ibrahim_alakhdar_gapless" to "Ibrahim Al-Akhdar",
    "qari_idrees_abkar_gapless" to "Idrees Abkar",
    "qari_juhany_gapless" to "Abdullah al Juhany",
    "qari_khalid_jalil_gapless" to "Khalid Al-Jalil",
    "qari_khalid_qahtani_gapless" to "Khalid Al-Qahtani",
    "qari_mahmoud_ali_albana_gapless" to "Mahmoud Ali al Bana",
    "qari_minshawi_mujawwad_gapless" to "Minshawy Mujawwad",
    "qari_minshawi_murattal_gapless" to "Minshawi Murattal",
    "qari_mishari_walk_gapless" to "Mishari with Ibrahim Walk (English)",
    "qari_mohammad_altablawi_gapless" to "Mohammad al Tablawy",
    "qari_mostafa_ismaeel_gapless" to "Mostafa Ismaeel",
    "qari_muaiqly_gapless" to "Maher Al Muaiqly",
    "qari_nabil_rifa3i_gapless" to "Nabil Ar-Rifai",
    "qari_noreen_siddiq_gapless" to "Noreen Siddiq",
    "qari_peshawa_qadir_al_qurdi_gapless" to "Peshewa Qadir al-Kurdi",
    "qari_qatami_gapless" to "Nasser Al Qatami",
    "qari_raad_al_kurdi_gapless" to "Raad Al-Kurdi",
    "qari_saad_al_ghamidi_gapless" to "Saad Al-Ghamdi",
    "qari_sahl_yaseen_gapless" to "Sahl Yaseen",
    "qari_salah_budair_gapless" to "Salah Budair",
    "qari_salah_bukhatir_gapless" to "Salah Bukhatir",
    "qari_shatri_gapless" to "Abu Bakr Ash-Shatri",
    "qari_shuraym_gapless" to "Saood Ash-Shuraym",
    "qari_sudais_gapless" to "Abdurrahman As-Sudais",
    "qari_tawfeeq_as_sawaigh_gapless" to "Tawfeeq as-Sawaigh",
    "qari_wadee3_alyamani_gapless" to "Wadee' Al-Yamani",
    "qari_walk_gapless" to "Ibrahim Walk (English)",
    "qari_yasser_dussary_gapless" to "Yasser Ad-Dussary",
)
