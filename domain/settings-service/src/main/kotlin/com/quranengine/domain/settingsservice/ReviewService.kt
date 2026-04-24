package com.quranengine.domain.settingsservice

import android.app.Activity
import java.util.concurrent.TimeUnit

/**
 * Checks review eligibility (7 days since install + 10 opens) and triggers
 * the review flow via [ReviewRequester].
 */
class ReviewService(
    private val persistence: ReviewPersistence,
    private val analytics: AnalyticsLibrary,
    private val reviewRequester: ReviewRequester,
) {
    fun checkForReview(activity: Activity) {
        var appOpenedCounter = persistence.appOpenedCounter

        if (appOpenedCounter == 0) {
            persistence.appInstalledDate = System.currentTimeMillis()
        } else {
            val requestReviewDate = persistence.requestReviewDate

            if (requestReviewDate == null) {
                val installedDate = persistence.appInstalledDate
                val now = System.currentTimeMillis()
                val daysSinceInstall = TimeUnit.MILLISECONDS.toDays(now - installedDate)

                if (daysSinceInstall >= 7 && appOpenedCounter >= 10) {
                    reviewRequester.requestReview(activity)
                    persistence.requestReviewDate = now
                    analytics.logEvent("RequestReviewAutomatic", "true")
                }
            }
        }

        appOpenedCounter += 1
        persistence.appOpenedCounter = appOpenedCounter
    }
}

/**
 * Abstraction for requesting an in-app review.
 * Implement with Google Play In-App Review API (ReviewManagerFactory).
 */
interface ReviewRequester {
    fun requestReview(activity: Activity)
}

interface AnalyticsLibrary {
    fun logEvent(name: String, value: String)
}
