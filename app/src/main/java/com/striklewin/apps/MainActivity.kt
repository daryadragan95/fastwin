package com.striklewin.apps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.striklewin.apps.core.persistence.GamePreferencesRepository
import com.striklewin.apps.data.web.FirestoreWebConfigRepository
import com.striklewin.apps.feature.game.presentation.GameViewModel
import com.striklewin.apps.feature.game.ui.GameScreen
import com.striklewin.apps.ui.theme.DribbleMasterTheme
import com.striklewin.apps.ui.viewmodel.WebGateViewModel
import com.striklewin.apps.ui.web.AdvancedWebViewScreen

class MainActivity : ComponentActivity() {

    private val webGateViewModel: WebGateViewModel by viewModels {
        WebGateViewModel.factory(FirestoreWebConfigRepository(), applicationContext)
    }

    private val gameViewModel: GameViewModel by viewModels {
        GameViewModel.factory(GamePreferencesRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DribbleMasterTheme {
                AppContent(
                    webGateViewModel = webGateViewModel,
                    gameViewModel = gameViewModel
                )
            }
        }
    }
}

@Composable
private fun AppContent(
    webGateViewModel: WebGateViewModel,
    gameViewModel: GameViewModel
) {
    val webGateState by webGateViewModel.appState.collectAsState()

    when (val state = webGateState) {
        WebGateViewModel.AppState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is WebGateViewModel.AppState.WebView -> {
            AdvancedWebViewScreen(initialUrl = state.url)
        }

        WebGateViewModel.AppState.NormalApp -> {
            val uiState by gameViewModel.uiState.collectAsState()
            GameScreen(
                uiState = uiState,
                hapticEvents = gameViewModel.hapticEvents,
                onStartClicked = gameViewModel::onStartClicked,
                onPauseClicked = gameViewModel::onPauseClicked,
                onRestartClicked = gameViewModel::onRestartClicked,
                onPlayerDragged = gameViewModel::onPlayerDragged
            )
        }
    }
}
