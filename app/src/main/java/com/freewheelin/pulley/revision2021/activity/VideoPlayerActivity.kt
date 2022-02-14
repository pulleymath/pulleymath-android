package com.freewheelin.pulley.revision2021.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityVideoPlayerBinding
import com.freewheelin.pulley.revision2021.viewmodel.VideoPlayerViewModel
import com.freewheelin.pulley.views.DaebakToast
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

        fun getIntent(context: Context, uriString: String): Intent {
            val intent = Intent(context, VideoPlayerActivity::class.java)
            println("tpehf, put intent - num : ${uriString}")
            intent.putExtra(URI_STRING, uriString)
            return intent
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initVideoPlayer()
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

        // MediaItem을 만들고
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUriString))

        // MediaSource를 만들고
//        val userAgent = Util.getUserAgent(this, this.applicationInfo.name)
        val factory = DefaultDataSource.Factory(this)

//        val factory = DefaultDataSourceFactory(this, userAgent)
//        val progressiveMediaSource = ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem)
        val progressiveMediaSource = ProgressiveMediaSource.Factory(factory)

        player = ExoPlayer.Builder(this, progressiveMediaSource).build()
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