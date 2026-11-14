package com.jtv.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Loads channels. Strategy:
 *  1. Try remote URL (so you can update the list without shipping an APK).
 *  2. Fall back to bundled assets/channels.json.
 *
 * Put your own playlist URL in REMOTE_URL — do NOT depend on a third-party repo
 * (that was exactly why DL Tivi broke). Host the JSON in your own GitHub repo.
 */
object ChannelRepo {

    private const val REMOTE_URL =
        "https://raw.githubusercontent.com/canthienlinh/J-Tivi/main/playlist/channels.json"

    private val http = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun load(ctx: Context): List<Channel> = withContext(Dispatchers.IO) {
        remote()?.let { return@withContext it }
        bundled(ctx)
    }

    private fun remote(): List<Channel>? {
        if (REMOTE_URL.isBlank()) return null
        return runCatching {
            http.newCall(Request.Builder().url(REMOTE_URL).build()).execute().use { r ->
                if (!r.isSuccessful) return null
                val body = r.body?.string() ?: return null
                Channel.parseJson(body).ifEmpty { null }
            }
        }.getOrNull()
    }

    private fun bundled(ctx: Context): List<Channel> =
        runCatching {
            ctx.assets.open("channels.json").bufferedReader().use { it.readText() }
                .let { Channel.parseJson(it) }
        }.getOrDefault(emptyList())
}
