package com.codingblocks.cbonlineapp.activities

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.codingblocks.cbonlineapp.R
import com.codingblocks.cbonlineapp.adapters.TabLayoutAdapter
import com.codingblocks.cbonlineapp.fragments.NotesFragment
import com.codingblocks.cbonlineapp.fragments.VideoDoubtFragment
import com.codingblocks.cbonlineapp.utils.MediaUtils
import com.codingblocks.cbonlineapp.utils.MyVideoControls
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.exomedia_default_controls_mobile.view.*
import org.jetbrains.anko.AnkoLogger


class VideoPlayerActivity : AppCompatActivity(), OnPreparedListener, AnkoLogger,YouTubePlayer.OnFullscreenListener {
    override fun onFullscreen(p0: Boolean) {

    }

    private var pos: Long? = 0
    private lateinit var youtubePlayerInit: YouTubePlayer.OnInitializedListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.codingblocks.cbonlineapp.R.layout.activity_video_player)
        rootLayout.layoutTransition
                .enableTransitionType(LayoutTransition.CHANGING)
        val controls = MyVideoControls(this)
        videoView.setControls(controls)
        (videoView.videoControls as MyVideoControls).let {
            it.fullscreenBtn.setOnClickListener {
                val i = Intent(this, VideoPlayerFullScreenActivity::class.java)
                i.putExtra("FOLDER_NAME", videoView.videoUri.toString())
                i.putExtra("CURRENT_POSITION", videoView.currentPosition)
                startActivityForResult(i, 1)
            }

        }

    }

    private fun setupViewPager(attemptId: String, contentId: String) {
        val adapter = TabLayoutAdapter(supportFragmentManager)
        adapter.add(VideoDoubtFragment.newInstance(attemptId, contentId), "Doubts")
        adapter.add(NotesFragment(), "Notes")

        player_viewpager.adapter = adapter
        player_tabs.setupWithViewPager(player_viewpager)
        player_viewpager.offscreenPageLimit = 2
    }


    override fun onStart() {
        super.onStart()
        val url = intent.getStringExtra("FOLDER_NAME")
        val youtubeUrl = intent.getStringExtra("videoUrl")
        val attemptId = intent.getStringExtra("attemptId")
        val contentId = intent.getStringExtra("contentId")


        if (youtubeUrl != null) {
            displayYoutubeVideo.view?.visibility = View.VISIBLE
            setupYoutubePlayer(youtubeUrl)
        } else {
            displayYoutubeVideo.view?.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            setupVideoView(url)
        }
        setupViewPager(attemptId,contentId)
    }

    private fun setupYoutubePlayer(youtubeUrl: String) {
        youtubePlayerInit = object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
            }

            override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, youtubePlayerInstance: YouTubePlayer?, p2: Boolean) {
                if (!p2) {
                    youtubePlayerInstance?.loadVideo(youtubeUrl.substring(32))
                }
            }
        }
        val youTubePlayerSupportFragment = supportFragmentManager.findFragmentById(R.id.displayYoutubeVideo) as YouTubePlayerSupportFragment?
        youTubePlayerSupportFragment!!.initialize(MyCourseActivity.YOUTUBE_API_KEY, youtubePlayerInit)


    }

    private fun setupVideoView(url: String) {
        videoView.setOnPreparedListener(this)
        videoView.setVideoURI(MediaUtils.getCourseVideoUri(url, this))
        videoView.setOnCompletionListener {
            finish()
        }
    }

    override fun onPrepared() {
        videoView.start()
        videoView.seekTo(pos ?: 0)
    }

    override fun onPause() {
        super.onPause()
        videoView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == -1) {
            pos = data?.getLongExtra("CURRENT_POSITION", 0)
        }
    }
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }


}