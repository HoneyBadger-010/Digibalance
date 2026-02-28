package com.CuriosityLabs.digibalance

import android.app.Application
import com.CuriosityLabs.digibalance.BuildConfig
import com.CuriosityLabs.digibalance.service.SessionSyncWorker
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

class DigiBalanceApplication : Application() {

    companion object {
        lateinit var supabaseClient: SupabaseClient
            private set
    }

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize Supabase client
            val supabaseUrl = BuildConfig.SUPABASE_URL.ifEmpty { "https://placeholder.supabase.co" }
            val supabaseKey = BuildConfig.SUPABASE_ANON_KEY.ifEmpty { "placeholder-key" }
            
            supabaseClient = createSupabaseClient(
                supabaseUrl = supabaseUrl,
                supabaseKey = supabaseKey
            ) {
                install(Auth)
                install(Postgrest)
                install(Realtime)

                httpEngine = HttpClient(Android).engine
            }

            // Schedule periodic usage stats sync for students
            com.CuriosityLabs.digibalance.service.UsageStatsSyncWorker.schedule(this)
            
            // Schedule periodic session sync to Room database (every 5 minutes)
            SessionSyncWorker.schedule(this)

            // NOTE: RuleSyncService realtime hookup is intentionally deferred here to
            // avoid startup failures until Supabase schema and filters are finalized.
            // Realtime sync is still available to be started explicitly from the app.
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e("DigiBalanceApp", "Failed to initialize Supabase: ${e.message}", e)
        }
    }
}

