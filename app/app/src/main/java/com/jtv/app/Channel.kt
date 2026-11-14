package com.jtv.app

import org.json.JSONArray
import java.net.URLDecoder

/** One TV/radio channel plus the headers its CDN needs. */
data class Channel(
    val name: String,
    val group: String,
    val logo: String,
    val tvgId: String,
    val url: String,
    val userAgent: String?,
    val referer: String?
) {
    /** True for DASH, so the player picks the right media source factory. */
    val isDash: Boolean get() = url.substringBefore('?').endsWith(".mpd")

    companion object {
        /**
         * Parse channels.json (bundled in assets or fetched remote).
         * Also splits Kodi-style inline headers: `url?|Referer=...&User-Agent=...`
         * which several VN CDNs (e.g. sctvonline.vn) embed in the URL.
         */
        fun parseJson(text: String): List<Channel> {
            val arr = JSONArray(text)
            val out = ArrayList<Channel>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                var url = o.optString("url")
                var ua = o.optString("userAgent").ifBlank { null }
                var ref = o.optString("referer").ifBlank { null }

                // split inline pipe headers: ...index.m3u8?|Referer=...&User-Agent=...
                val pipe = url.indexOf("?|")
                if (pipe >= 0) {
                    val headerPart = url.substring(pipe + 2)
                    url = url.substring(0, pipe)
                    for (kv in headerPart.split('&')) {
                        val eq = kv.indexOf('=')
                        if (eq <= 0) continue
                        val k = kv.substring(0, eq).trim().lowercase()
                        val v = runCatching { URLDecoder.decode(kv.substring(eq + 1), "UTF-8") }
                            .getOrDefault(kv.substring(eq + 1))
                        when (k) {
                            "referer", "referrer" -> if (ref == null) ref = v
                            "user-agent" -> if (ua == null) ua = v
                        }
                    }
                }
                out.add(
                    Channel(
                        name = o.optString("name"),
                        group = o.optString("group").ifBlank { "Khác" },
                        logo = o.optString("logo"),
                        tvgId = o.optString("tvgId"),
                        url = url,
                        userAgent = ua,
                        referer = ref
                    )
                )
            }
            return out
        }
    }
}
