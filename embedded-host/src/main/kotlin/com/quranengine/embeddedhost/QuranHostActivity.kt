package com.quranengine.embeddedhost

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.quranengine.app.MainViewModel
import com.quranengine.data.annotation.persistence.LastPagePersistence
import com.quranengine.features.appstructure.DeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuranHostActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject lateinit var deepLinkHandler: DeepLinkHandler
    @Inject lateinit var lastPagePersistence: LastPagePersistence

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        observeLastPage()

        val initialDeepLinkUri = QuranHostContract.initialPage(intent)?.let { page ->
            "quranengine://page/$page"
        } ?: intent?.dataString

        setContent {
            QuranHostScreen(
                viewModel = viewModel,
                deepLinkHandler = deepLinkHandler,
                initialDeepLinkUri = initialDeepLinkUri,
                forcedAppearanceMode = QuranHostContract.forcedAppearanceMode(intent),
                showCloseButton = QuranHostContract.showCloseButton(intent),
                onClose = ::finish,
            )
        }
    }

    private fun observeLastPage() {
        setActivityResult(lastPage = null)
        lifecycleScope.launch {
            lastPagePersistence.lastPages().collectLatest { pages ->
                val lastPage = pages.maxByOrNull { it.modifiedOn }?.page
                setActivityResult(lastPage)
            }
        }
    }

    private fun setActivityResult(lastPage: Int?) {
        setResult(Activity.RESULT_OK, QuranHostContract.resultIntent(lastPage))
    }
}
