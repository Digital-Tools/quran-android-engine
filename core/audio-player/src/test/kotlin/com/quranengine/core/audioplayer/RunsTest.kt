package com.quranengine.core.audioplayer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RunsTest {
    @Test
    fun `of maps preset counts to the preset values`() {
        assertThat(Runs.of(1)).isEqualTo(Runs.ONE)
        assertThat(Runs.of(3)).isEqualTo(Runs.THREE)
        assertThat(Runs.of(Int.MAX_VALUE)).isEqualTo(Runs.INDEFINITE)
    }

    @Test
    fun `of keeps custom counts and clamps below one`() {
        assertThat(Runs.of(40).maxRuns).isEqualTo(40)
        assertThat(Runs.of(40)).isNotEqualTo(Runs.INDEFINITE)
        assertThat(Runs.of(0)).isEqualTo(Runs.ONE)
    }
}
