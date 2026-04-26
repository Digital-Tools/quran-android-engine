package com.quranengine.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.quranengine.features.appstructure.AppStructureScreen
import com.quranengine.features.appstructure.DeepLinkHandler
import com.quranengine.ui.theme.QuranTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    @Inject lateinit var deepLinkHandler: DeepLinkHandler

    private val deepLinkEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialDeepLinkUri = intent?.dataString
        setContent {
            val themeStyle by viewModel.themeStyle.collectAsState()
            val appearanceMode by viewModel.appearanceMode.collectAsState()

            QuranTheme(
                themeStyle = themeStyle,
                appearanceMode = appearanceMode,
            ) {
                AppStructureScreen(
                    deepLinkHandler = deepLinkHandler,
                    initialDeepLinkUri = initialDeepLinkUri,
                    incomingDeepLinks = deepLinkEvents.asSharedFlow(),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.dataString?.let(deepLinkEvents::tryEmit)
    }
}
