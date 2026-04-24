package com.quranengine.core.preferences

public class PreferenceTransformer<Raw, T>(
    public val rawToValue: (Raw) -> T,
    public val valueToRaw: (T) -> Raw,
) {
    public companion object {
        /**
         * Creates a transformer for Kotlin enums, mirroring Swift's RawRepresentable extension.
         * [valueOf] should map the raw value to the enum entry (e.g. `MyEnum::valueOf` or a lookup).
         * [toRaw] extracts the raw representation (e.g. `MyEnum::name`).
         */
        public fun <Raw, T : Enum<T>> enumTransformer(
            defaultValue: () -> T,
            valueOf: (Raw) -> T?,
            toRaw: (T) -> Raw,
        ): PreferenceTransformer<Raw, T> = PreferenceTransformer(
            rawToValue = { valueOf(it) ?: defaultValue() },
            valueToRaw = toRaw,
        )
    }
}

public fun <Raw, T> optionalTransformer(
    transformer: PreferenceTransformer<Raw, T>,
): PreferenceTransformer<Raw?, T?> = PreferenceTransformer(
    rawToValue = { it?.let { raw -> transformer.rawToValue(raw) } },
    valueToRaw = { it?.let { value -> transformer.valueToRaw(value) } },
)
