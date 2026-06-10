package com.striklewin.apps.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.edit
import com.striklewin.apps.data.web.WebConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class WebGateViewModel(
    private val webConfigRepository: WebConfigRepository,
    context: Context
) : ViewModel() {
    private val appContext = context.applicationContext

    sealed interface AppState {
        data object Loading : AppState
        data class WebView(val url: String) : AppState
        data object NormalApp : AppState
    }

    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        checkWebViewUrl()
    }

    private fun checkWebViewUrl() {
        viewModelScope.launch {
            val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val webViewPrefs = appContext.getSharedPreferences(WEBVIEW_CACHE_PREFS_NAME, Context.MODE_PRIVATE)
            val cachedUrl = prefs.getString(CACHED_URL, null)
            val cachedFinalUrl = webViewPrefs.getString(CACHED_FINAL_URL, null)
            val fallbackUrl = cachedFinalUrl.takeIf { !it.isNullOrBlank() }
                ?: cachedUrl.takeIf { !it.isNullOrBlank() }

            runCatching {
                withTimeoutOrNull(10_000L) {
                    webConfigRepository.getWebViewUrl()
                }
            }.fold(
                onSuccess = { url ->
                    when {
                        !url.isNullOrBlank() -> {
                            prefs.edit { putString(CACHED_URL, url) }
                            _appState.value = AppState.WebView(url)
                        }
                        else -> {
                            _appState.value = if (!fallbackUrl.isNullOrBlank()) {
                                AppState.WebView(fallbackUrl)
                            } else {
                                AppState.NormalApp
                            }
                        }
                    }
                },
                onFailure = {
                    _appState.value = if (!fallbackUrl.isNullOrBlank()) {
                        AppState.WebView(fallbackUrl)
                    } else {
                        AppState.NormalApp
                    }
                }
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "webview_prefs"
        private const val CACHED_URL = "cached_url"
        private const val WEBVIEW_CACHE_PREFS_NAME = "webview_cache"
        private const val CACHED_FINAL_URL = "cached_final_url"

        fun factory(
            webConfigRepository: WebConfigRepository,
            context: Context
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(WebGateViewModel::class.java)) {
                        "Unknown ViewModel class: ${modelClass.name}"
                    }
                    return WebGateViewModel(webConfigRepository, context) as T
                }
            }
        }
    }
}
