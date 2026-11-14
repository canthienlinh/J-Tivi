package com.jtv.app

import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var titleView: TextView

    private lateinit var channels: ArrayList<ChannelParcel>
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        playerView = findViewById(R.id.playerView)
        titleView = findViewById(R.id.channelTitle)
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        channels = intent.getParcelableArrayListExtra(EXTRA_LIST) ?: arrayListOf()
        index = intent.getIntExtra(EXTRA_INDEX, 0).coerceIn(0, maxOf(0, channels.size - 1))
        if (channels.isEmpty()) { finish(); return }
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
        play(index)
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initPlayer() {
        val p = ExoPlayer.Builder(this).build()
        playerView.player = p
        playerView.keepScreenOn = true
        p.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(
                    this@PlayerActivity,
                    "Lỗi phát: ${error.errorCodeName}. Thử kênh khác.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        player = p
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    private fun play(i: Int) {
        val ch = channels[i]
        index = i
        titleView.text = ch.name
        val p = player ?: return
        p.setMediaSource(buildSource(ch))
        p.prepare()
        p.playWhenReady = true
    }

    /** HTTP datasource carrying the channel's UA + Referer so the CDN accepts us. */
    private fun buildSource(ch: ChannelParcel): MediaSource {
        val ua = ch.userAgent ?: DEFAULT_UA
        val http = DefaultHttpDataSource.Factory()
            .setUserAgent(ua)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(10_000)
            .setReadTimeoutMs(10_000)
        // Only Referer — adding Origin makes some VN CDNs (e.g. SCTV) return 403.
        ch.referer?.let {
            http.setDefaultRequestProperties(mapOf("Referer" to it))
        }

        val item = MediaItem.fromUri(Uri.parse(ch.url))
        return if (ch.isDash) {
            DashMediaSource.Factory(http).createMediaSource(item)
        } else if (ch.url.substringBefore('?').endsWith(".m3u8")) {
            HlsMediaSource.Factory(http).createMediaSource(item)
        } else {
            // progressive / unknown -> default factory decides
            DefaultMediaSourceFactory(http).createMediaSource(item)
        }
    }

    // D-pad / remote: up-down change channel, ok toggles controls
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                play((index - 1 + channels.size) % channels.size); return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                play((index + 1) % channels.size); return true
            }
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                finish(); return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        const val EXTRA_LIST = "list"
        const val EXTRA_INDEX = "index"
        const val DEFAULT_UA =
            "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile Safari/537.36"
    }
}
