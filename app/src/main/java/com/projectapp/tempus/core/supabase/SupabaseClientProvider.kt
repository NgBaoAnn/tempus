package com.projectapp.tempus.core.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.websocket.WebSocketDeflateExtension.Companion.install

object SupabaseClientProvider {
    // TODO: thay bằng URL/KEY của project team
    private const val SUPABASE_URL = "https://hrkqiydqhpjahhzdwbag.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imhya3FpeWRxaHBqYWhoemR3YmFnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjYwMjk5MzMsImV4cCI6MjA4MTYwNTkzM30.oZVCINy-KZTis8VlUh5sy27NjR_uyiG0Cv97_MuP68o"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth) {
            // Tự động lưu session vào máy, không cần viết code lưu id hay token nữa
            sessionManager = SettingsSessionManager()
        }
        install(Postgrest)
    }
}
