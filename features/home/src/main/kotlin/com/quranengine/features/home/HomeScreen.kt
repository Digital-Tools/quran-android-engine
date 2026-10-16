package com.quranengine.features.home

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.quranengine.model.quranannotations.LastPage
import com.quranengine.model.qurankit.Page
import com.quranengine.model.qurankit.Sura
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.domain.qurantextkit.plainArabicName
import com.quranengine.ui.components.NoorAccessory
import com.quranengine.ui.components.NoorBasicSection
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.components.NoorSection
import com.quranengine.ui.theme.QuranTheme
import java.time.Instant

@Composable
fun HomeScreen(
    viewType: HomeViewType,
    sortOrder: SurahSortOrder,
    lastPages: List<LastPage>,
    suras: List<Sura>,
    quarters: List<QuarterItem>,
    onViewTypeChange: (HomeViewType) -> Unit,
    onToggleSortOrder: () -> Unit,
    onSelectPage: (Page) -> Unit,
    onSelectSura: (Sura) -> Unit,
    onSelectQuarter: (QuarterItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = viewType.ordinal,
            containerColor = QuranTheme.colors.background,
            contentColor = QuranTheme.colors.text,
        ) {
            Tab(
                selected = viewType == HomeViewType.SURAS,
                onClick = { onViewTypeChange(HomeViewType.SURAS) },
                text = { Text("Suras") },
            )
            Tab(
                selected = viewType == HomeViewType.JUZS,
                onClick = { onViewTypeChange(HomeViewType.JUZS) },
                text = { Text("Juz") },
            )
        }

        // Recent pages load after the sura list, and the list keeps "Juz 1" anchored when a
        // row is inserted above it, so the section would sit just out of view.
        val listState = rememberLazyListState()
        val hasLastPages = lastPages.isNotEmpty()
        LaunchedEffect(hasLastPages) {
            if (hasLastPages && listState.firstVisibleItemIndex == 1 && listState.firstVisibleItemScrollOffset == 0) {
                listState.scrollToItem(0)
            }
        }

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            // Last pages section
            if (lastPages.isNotEmpty()) {
                item(key = "recent-pages") {
                    NoorSection(
                        items = lastPages,
                        title = "Recent Pages",
                    ) { lastPage ->
                        val sura = lastPage.page.startSura
                        NoorListItem(
                            title = "${sura.englishName()}  ${sura.plainArabicName()}",
                            subtitle = "Juz ${lastPage.page.startJuz.juzNumber} · ${relativeTime(lastPage.modifiedOn)}",
                            accessory = NoorAccessory.TextAccessory("Page ${lastPage.page.pageNumber}"),
                            image = {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = QuranTheme.colors.secondaryText,
                                )
                            },
                            onClick = { onSelectPage(lastPage.page) },
                        )
                    }
                }
            }

            // Content based on view type
            when (viewType) {
                HomeViewType.SURAS -> {
                    val sortedSuras = when (sortOrder) {
                        SurahSortOrder.ASCENDING -> suras
                        SurahSortOrder.DESCENDING -> suras.reversed()
                    }
                    val grouped = sortedSuras.groupBy { it.page.startJuz }
                    grouped.forEach { (juz, surasInJuz) ->
                        item(key = "juz-sura-${juz.juzNumber}") {
                            NoorSection(
                                items = surasInJuz,
                                title = "Juz ${juz.juzNumber}",
                            ) { sura ->
                                NoorListItem(
                                    title = "${sura.suraNumber}. ${sura.englishName()}",
                                    subtitle = if (sura.isMakki) "Makki" else "Madani",
                                    accessory = NoorAccessory.TextAccessory(
                                        "Page ${sura.page.pageNumber}"
                                    ),
                                    onClick = { onSelectSura(sura) },
                                )
                            }
                        }
                    }
                }

                HomeViewType.JUZS -> {
                    val grouped = quarters.groupBy { it.localizedJuzName }
                    grouped.forEach { (juzName, quartersInJuz) ->
                        item(key = "juz-quarter-$juzName") {
                            NoorSection(
                                items = quartersInJuz,
                                title = juzName,
                            ) { quarterItem ->
                                NoorListItem(
                                    title = quarterItem.localizedName,
                                    subtitle = quarterItem.pageDescription,
                                    onClick = { onSelectQuarter(quarterItem) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** "Just now", "5 minutes ago", "Yesterday", or the date for older pages, like iOS `timeAgo()`. */
private fun relativeTime(instant: Instant): String {
    val now = System.currentTimeMillis()
    if (now - instant.toEpochMilli() < DateUtils.MINUTE_IN_MILLIS) return "Just now"
    return DateUtils.getRelativeTimeSpanString(instant.toEpochMilli(), now, DateUtils.MINUTE_IN_MILLIS).toString()
}
