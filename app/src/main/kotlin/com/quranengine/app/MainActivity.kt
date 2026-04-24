package com.quranengine.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.quranengine.features.appstructure.AppStructureScreen
import com.quranengine.ui.theme.QuranTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeStyle by viewModel.themeStyle.collectAsState()
            val appearanceMode by viewModel.appearanceMode.collectAsState()

            QuranTheme(
                themeStyle = themeStyle,
                appearanceMode = appearanceMode,
            ) {
                AppStructureScreen()
            }
        }
    }
}
