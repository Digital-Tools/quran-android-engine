package com.quranengine.features.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurantext.SearchResult
import com.quranengine.model.qurantext.SearchResults
import com.quranengine.ui.components.LoadingView
import com.quranengine.ui.components.NoorBasicSection
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.components.NoorSection
import com.quranengine.ui.theme.QuranTheme

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    initialQuery: String? = null,
    onNavigateToAyah: (AyahNumber) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchTerm by viewModel.searchTerm.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val autocompletions by viewModel.autocompletions.collectAsState()

    LaunchedEffect(initialQuery) {
        initialQuery
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(viewModel::search)
    }

    Column(modifier = modifier.fillMaxSize()) {
        SearchBar(
            query = searchTerm,
            onQueryChange = { viewModel.searchTerm.value = it },
            onSearch = { viewModel.search(searchTerm) },
            onClear = { viewModel.clearSearch() },
        )

        if (searchTerm.isNotEmpty() && autocompletions.isNotEmpty() && uiState !is SearchUiState.Results) {
            AutocompleteDropdown(
                suggestions = autocompletions,
                onSuggestionClick = { viewModel.search(it) },
            )
        }

        when (val state = uiState) {
            is SearchUiState.Entry -> EntryContent(
                recents = state.recents,
                populars = state.populars,
                onTermClick = { viewModel.selectRecent(it) },
                onClearRecents = { viewModel.clearRecents() },
            )
            is SearchUiState.Searching -> LoadingView()
            is SearchUiState.Results -> ResultsContent(
                results = state.results,
                onResultClick = onNavigateToAyah,
            )
            is SearchUiState.NoResults -> NoResultsContent(term = state.term)
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = QuranTheme.colors.secondaryBackground,
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search",
                    color = QuranTheme.colors.secondaryText,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = QuranTheme.colors.secondaryText,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = QuranTheme.colors.secondaryText,
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun AutocompleteDropdown(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        color = QuranTheme.colors.secondaryBackground,
        shadowElevation = 4.dp,
    ) {
        Column {
            suggestions.take(5).forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = QuranTheme.colors.secondaryText,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = QuranTheme.colors.text,
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryContent(
    recents: List<String>,
    populars: List<String>,
    onTermClick: (String) -> Unit,
    onClearRecents: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (recents.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                NoorSection(
                    items = recents,
                    title = "Recent Searches",
                ) { term ->
                    NoorListItem(
                        title = term,
                        onClick = { onTermClick(term) },
                    )
                }
            }
            item {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.bodySmall,
                    color = QuranTheme.appIdentity,
                    modifier = Modifier
                        .clickable { onClearRecents() }
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            NoorSection(
                items = populars,
                title = "Popular Searches",
            ) { term ->
                NoorListItem(
                    title = term,
                    onClick = { onTermClick(term) },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ResultsContent(
    results: List<SearchResults>,
    onResultClick: (AyahNumber) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        results.forEach { group ->
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                NoorBasicSection(title = group.source.name) {
                    group.items.forEachIndexed { index, result ->
                        SearchResultItem(
                            result = result,
                            onClick = { onResultClick(result.ayah) },
                        )
                        if (index < group.items.lastIndex) {
                            com.quranengine.ui.components.NoorDivider()
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val highlightColor = QuranTheme.appIdentity
    val annotatedText = buildAnnotatedString {
        var cursor = 0
        for (range in result.ranges.sortedBy { it.first }) {
            if (cursor < range.first) {
                append(result.text.substring(cursor, range.first))
            }
            withStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold)) {
                append(result.text.substring(range.first, range.last + 1))
            }
            cursor = range.last + 1
        }
        if (cursor < result.text.length) {
            append(result.text.substring(cursor))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyLarge,
            color = QuranTheme.colors.text,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${result.ayah.sura.suraNumber}:${result.ayah.ayah}",
            style = MaterialTheme.typography.bodySmall,
            color = QuranTheme.colors.secondaryText,
        )
    }
}

@Composable
private fun NoResultsContent(
    term: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No results found for \"$term\"",
            style = MaterialTheme.typography.bodyLarge,
            color = QuranTheme.colors.secondaryText,
        )
    }
}
