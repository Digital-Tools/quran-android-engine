package com.quranengine.core.preferences

public class PreferenceKey<Type>(
    public val key: String,
    public val defaultValue: Type,
)
