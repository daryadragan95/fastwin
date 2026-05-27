package com.striklewin.apps.data.web

interface WebConfigRepository {
    suspend fun getWebViewUrl(): String?
}
