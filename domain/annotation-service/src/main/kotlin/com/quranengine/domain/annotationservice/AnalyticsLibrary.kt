package com.quranengine.domain.annotationservice

interface AnalyticsLibrary {
    fun logEvent(name: String, value: String)
}
