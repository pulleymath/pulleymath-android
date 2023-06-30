package com.freewheelin.pulley.revision2021.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityVideoPlayerBinding
import com.freewheelin.pulley.databinding.ExoPlaybackControlViewBinding
import com.freewheelin.pulley.revision2021.model.response.AffiliatedSolution
import com.freewheelin.pulley.revision2021.viewmodel.VideoPlayerViewModel
import com.freewheelin.pulley.legacy.utils.getSerializable
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource

class VideoPlayerActivity : AppCompatActivity() {

    private val binding: ActivityVideoPlayerBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_video_player, null, false)
    }

    private val viewModel: VideoPlayerViewModel by viewModels()

    companion object {
        val URI_STRING = "URI_STRING"
        val SOLUTION = "AFF_SOLUTION"

        fun getIntent(context: Context, uriString: String, item: AffiliatedSolution): Intent {
            val intent = Intent(context, VideoPlayerActivity::class.java)
            intent.putExtra(URI_STRING, uriString)
            intent.putExtra(SOLUTION, item)
            return intent
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setFullscreen()
        makeMediaLog()
        initVideoPlayer()
    }

    override fun onBackPressed() {
        viewModel.finishMediaLog {
            super.onBackPressed()
        }
    }

    private fun makeMediaLog() {
//        val solution = getSerializable(this@VideoPlayerActivity, SOLUTION, AffiliatedSolution::class.java)
        val solution = intent.getSerializableExtra(SOLUTION) as? AffiliatedSolution ?: return
        viewModel.let {
            it.currentMedia = solution
            it.makeMediaLog()
        }
    }
    private fun setFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }
    lateinit var player: ExoPlayer
    private fun initVideoPlayer() {
        val videoUriString = intent.getStringExtra(URI_STRING) ?: return
        if (!videoUriString.startsWith("http")) {
            DaebakToast.show(this, "[DEBUG] 잘못된 URI입니다 : $videoUriString")
            finish()
            return
        }
        val exoPlayerView = binding.exoPlayerView

        exoPlayerView.findViewById<View>(R.id.back_btn).setOnClickListener {
            onBackPressed()
        }

        val mediaItem = MediaItem.fromUri(Uri.parse(videoUriString))
        val factory = DefaultDataSource.Factory(this)
        val progressiveMediaSource = ProgressiveMediaSource.Factory(factory)

        player = ExoPlayer.Builder(this, progressiveMediaSource)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
        exoPlayerView.player = player

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}