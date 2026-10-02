package com.quranengine.core.audioplayer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Saved ids must match the iOS raw values (shared preference keys). */
class DelayTest {
    @Test
    fun `verse delay ids and multipliers match iOS`() {
        assertThat(VerseDelay.entries.map { it.id to it.multiplier }).containsExactly(
            0 to 0.0, 1 to 0.25, 2 to 0.5, 3 to 0.75, 4 to 1.0, 5 to 2.0,
        ).inOrder()
        assertThat(VerseDelay.fromId(4)).isEqualTo(VerseDelay.FULL)
        assertThat(VerseDelay.fromId(99)).isNull()
    }

    @Test
    fun `repetition delay ids and seconds match iOS`() {
        assertThat(RepetitionDelay.entries.map { it.id to it.seconds }).containsExactly(
            0 to 0.0, 1 to 1.0, 2 to 2.0, 3 to 3.0, 4 to 5.0, 5 to 10.0,
        ).inOrder()
    }

    @Test
    fun `request carries no delay by default`() {
        val request = AudioRequest(emptyList(), null, Runs.ONE, Runs.ONE)
        assertThat(request.verseDelay).isEqualTo(VerseDelay.NONE)
        assertThat(request.repetitionDelay).isEqualTo(RepetitionDelay.NONE)
    }
}
