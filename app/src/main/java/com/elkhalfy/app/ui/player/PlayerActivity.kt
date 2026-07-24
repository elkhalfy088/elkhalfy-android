package com.elkhalfy.app.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bumptech.glide.Glide
import com.elkhalfy.app.R
import com.elkhalfy.app.databinding.ActivityPlayerBinding
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NAME = "name"
        const val EXTRA_ICON = "icon"
        const val EXTRA_URL = "url"
        const val EXTRA_IS_LIVE = "is_live"
        const val EXTRA_SUB = "sub"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var ctrlVisible = false
    private val hideDelay = 4000L
    private var isLive = false
    private var isMuted = false
    private var currentUrl = ""
    private var channelName = ""

    private val hideCtrlRunnable = Runnable { hideControls() }

    private val seekUpdateRunnable = object : Runnable {
        override fun run() {
            updateSeekBar()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        channelName = intent.getStringExtra(EXTRA_NAME) ?: ""
        currentUrl = intent.getStringExtra(EXTRA_URL) ?: ""
        isLive = intent.getBooleanExtra(EXTRA_IS_LIVE, true)
        val icon = intent.getStringExtra(EXTRA_ICON) ?: ""

        setupUI(channelName, icon)
        setupPlayer()
        setupListeners()
    }

    private fun setupUI(name: String, icon: String) {
        binding.tvChannelName.text = name
        binding.tvLivePill.visibility = if (isLive) View.VISIBLE else View.GONE
        binding.ctrlCenter.visibility = if (isLive) View.GONE else View.VISIBLE
        binding.seekContainer.visibility = if (isLive) View.GONE else View.VISIBLE
    }

    private fun setupPlayer() {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                if (isLive) 5000 else 15000,
                if (isLive) 30000 else 60000,
                if (isLive) 2000 else 5000,
                if (isLive) 5000 else 10000
            ).build()

        player = ExoPlayer.Builder(this).setLoadControl(loadControl).build().also { p ->
            binding.playerView.player = p
            p.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> {
                            binding.bufOverlay.visibility = View.VISIBLE
                            binding.errOverlay.visibility = View.GONE
                        }
                        Player.STATE_READY -> {
                            binding.bufOverlay.visibility = View.GONE
                            binding.errOverlay.visibility = View.GONE
                            updatePlayPauseIcon()
                        }
                        Player.STATE_ENDED -> {
                            binding.bufOverlay.visibility = View.GONE
                        }
                        Player.STATE_IDLE -> {}
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayPauseIcon()
                    if (!isLive) {
                        if (isPlaying) handler.post(seekUpdateRunnable)
                        else handler.removeCallbacks(seekUpdateRunnable)
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    binding.bufOverlay.visibility = View.GONE
                    binding.errOverlay.visibility = View.VISIBLE
                    binding.tvErrMsg.text = "تعذر تشغيل الفيديو: ${error.message ?: ""}"
                }
            })
            loadMedia(currentUrl)
        }
        showControls()
    }

    private fun loadMedia(url: String) {
        val p = player ?: return
        binding.bufOverlay.visibility = View.VISIBLE
        binding.errOverlay.visibility = View.GONE
        p.stop()

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(15000).setReadTimeoutMs(20000)
            .setAllowCrossProtocolRedirects(true)

        val mediaSource = when {
            url.contains(".m3u8") || url.contains("/live/") -> {
                val factory = HlsMediaSource.Factory(dataSourceFactory)
                factory.createMediaSource(MediaItem.fromUri(url))
            }
            else -> {
                val factory = ProgressiveMediaSource.Factory(dataSourceFactory)
                factory.createMediaSource(MediaItem.fromUri(url))
            }
        }
        p.setMediaSource(mediaSource)
        p.prepare()
        p.playWhenReady = true
    }

    private fun setupListeners() {
        binding.playerView.setOnClickListener { toggleControls() }
        binding.btnBack.setOnClickListener { finish() }

        binding.btnPlayPause.setOnClickListener {
            player?.let { p ->
                if (p.isPlaying) p.pause() else p.play()
            }
            scheduleHide()
        }
        binding.btnSkipBack.setOnClickListener {
            player?.seekTo(maxOf(0L, (player?.currentPosition ?: 0L) - 10000))
            showOsd(-10)
            scheduleHide()
        }
        binding.btnSkipFwd.setOnClickListener {
            player?.let { p -> p.seekTo(minOf(p.duration, p.currentPosition + 10000)) }
            showOsd(+10)
            scheduleHide()
        }
        binding.btnMute.setOnClickListener {
            isMuted = !isMuted
            player?.volume = if (isMuted) 0f else 1f
            binding.btnMute.setImageResource(if (isMuted) R.drawable.ic_volume_off else R.drawable.ic_volume)
            scheduleHide()
        }
        binding.btnFit.setOnClickListener {
            binding.playerView.resizeMode = when (binding.playerView.resizeMode) {
                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT ->
                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                else -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
            scheduleHide()
        }
        binding.btnRetry.setOnClickListener { loadMedia(currentUrl) }
        binding.btnQuality.setOnClickListener { scheduleHide() }

        if (!isLive) {
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val p = player ?: return
                        val dur = p.duration.takeIf { it > 0 } ?: return
                        binding.tvCurTime.text = formatTime(progress.toLong() * dur / 100L)
                    }
                }
                override fun onStartTrackingTouch(sb: SeekBar?) { handler.removeCallbacks(seekUpdateRunnable) }
                override fun onStopTrackingTouch(sb: SeekBar?) {
                    val p = player ?: return
                    val dur = p.duration.takeIf { it > 0 } ?: return
                    p.seekTo(sb!!.progress.toLong() * dur / 100L)
                    handler.post(seekUpdateRunnable)
                    scheduleHide()
                }
            })
        }
    }

    private fun updatePlayPauseIcon() {
        val isPlaying = player?.isPlaying == true
        binding.btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }

    private fun updateSeekBar() {
        val p = player ?: return
        val dur = p.duration.takeIf { it > 0 } ?: return
        val pos = p.currentPosition
        binding.seekBar.progress = (pos * 100L / dur).toInt()
        binding.tvCurTime.text = formatTime(pos)
        binding.tvDurTime.text = formatTime(dur)
    }

    private fun showOsd(seconds: Int) {
        val label = if (seconds > 0) "+${seconds}s" else "${seconds}s"
        binding.tvOsd.text = label
        binding.tvOsd.visibility = View.VISIBLE
        handler.removeCallbacksAndMessages("osd")
        handler.postDelayed({ binding.tvOsd.visibility = View.GONE }, 1200)
    }

    private fun toggleControls() {
        if (ctrlVisible) hideControls() else showControls()
    }

    private fun showControls() {
        ctrlVisible = true
        binding.ctrlOverlay.visibility = View.VISIBLE
        scheduleHide()
    }

    private fun hideControls() {
        ctrlVisible = false
        binding.ctrlOverlay.visibility = View.INVISIBLE
    }

    private fun scheduleHide() {
        handler.removeCallbacks(hideCtrlRunnable)
        handler.postDelayed(hideCtrlRunnable, hideDelay)
    }

    private fun formatTime(ms: Long): String {
        if (ms <= 0) return "0:00"
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        else String.format(Locale.US, "%d:%02d", m, s)
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        return when (keyCode) {
            android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            android.view.KeyEvent.KEYCODE_DPAD_CENTER -> {
                player?.let { if (it.isPlaying) it.pause() else it.play() }
                showControls(); true
            }
            android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (!isLive) { player?.let { it.seekTo(minOf(it.duration, it.currentPosition + 10000)) } }
                showControls(); true
            }
            android.view.KeyEvent.KEYCODE_MEDIA_REWIND,
            android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (!isLive) { player?.seekTo(maxOf(0L, (player?.currentPosition ?: 0L) - 10000)) }
                showControls(); true
            }
            android.view.KeyEvent.KEYCODE_BACK -> { finish(); true }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
