package com.quranengine.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quranengine.model.quranannotations.LastPage
import com.quranengine.model.qurankit.Page
import com.quranengine.model.qurankit.Sura
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.ui.components.NoorAccessory
import com.quranengine.ui.components.NoorBasicSection
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.components.NoorSection
import com.quranengine.ui.theme.QuranTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Last pages section
            if (lastPages.isNotEmpty()) {
                item {
                    NoorSection(
                        items = lastPages,
                        title = "Recent Pages",
                    ) { lastPage ->
                        NoorListItem(
                            title = "Page ${lastPage.page.pageNumber}",
                            subtitle = formatDate(lastPage.modifiedOn),
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

private val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    .withZone(ZoneId.systemDefault())

private fun formatDate(instant: java.time.Instant): String =
    dateFormatter.format(instant)
