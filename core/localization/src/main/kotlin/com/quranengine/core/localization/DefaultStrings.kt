package com.quranengine.core.localization

/**
 * Built-in English strings for keys the engine formats at runtime.
 *
 * Values mirror `Core/Localization/Resources/en.lproj/Localizable.strings` in
 * quran-ios. Without them [MapLocalizer] echoes the key back and raw identifiers
 * such as `translation.text.footnote-number` leak into rendered translations.
 */
val defaultLocalizerStrings: Map<Pair<Table, Language>, Map<String, String>> = mapOf(
    (Table.LOCALIZABLE to Language.ENGLISH) to mapOf(
        "translation.text.ayah-number" to "%d:%d",
        "translation.text.footnote-number" to "[%d]",
        "translation.text.footnote-title" to "Footnote #%d",
        "translation.text.read-more" to "Read more",
        "translation.text.see-referenced-verse" to "See ayah %d.",
        "error.translation.text-not-available" to "No translation available for this Ayah.",
        "error.translation.text-retrieval" to
            "Oops! Something went wrong. We couldn't retrieve the translation.",
    ),
)
