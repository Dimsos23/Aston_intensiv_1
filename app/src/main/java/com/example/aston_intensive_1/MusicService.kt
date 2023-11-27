package com.example.aston_intensive_1

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.concurrent.Executors

class MusicService : Service() {
    private var mediaPlayer = MediaPlayer()
    private var trackList = listOf(R.raw.dune, R.raw.battletoads, R.raw.beavis_and_butt_head)
    private var currentTrackIndex = 0
    private var checkPause = false

    companion object {
        const val NOTIFICATION_ID = 1
    }

    enum class Actions {
        START, PAUSE, NEXT, PREVIOUS
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return MyBinder()
    }

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(this, "service created", Toast.LENGTH_SHORT).show()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener { nextTrack() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        startForeground(NOTIFICATION_ID, getNotification())
        Executors.newSingleThreadExecutor().execute {
            try {
                when (intent?.action) {
                    Actions.START.toString() -> startTrack()
                    Actions.PAUSE.toString() -> pauseTrack()
                    Actions.NEXT.toString() -> nextTrack()
                    Actions.PREVIOUS.toString() -> previousTrack()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        return START_NOT_STICKY
    }

    fun startTrack() {
        if (!checkPause) {
            prepareTrack()
            mediaPlayer.start()
        } else {
            mediaPlayer.start()
            checkPause = false
        }
    }

    fun pauseTrack() {
        mediaPlayer.pause()
        checkPause = true
    }

    fun nextTrack() {
        checkPause = false
        currentTrackIndex++
        prepareTrack()
        mediaPlayer.start()
    }

    fun previousTrack() {
        checkPause = false
        currentTrackIndex--
        prepareTrack()
        mediaPlayer.start()
    }

    private fun prepareTrack() {
        mediaPlayer.reset()
        if (currentTrackIndex >= trackList.size) {
            currentTrackIndex = 0
        } else if (currentTrackIndex < 0) {
            currentTrackIndex = trackList.size - 1
        }
        val trackResId = trackList[currentTrackIndex]
        val trackUri = Uri.parse("android.resource://${packageName}/$trackResId")

        mediaPlayer.let {
            it.setDataSource(applicationContext, trackUri)
            it.prepare()
        }
    }

    private fun getNotification(): Notification {
        val notification = NotificationCompat.Builder(this, "running chanel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("MediaPlayer is active")
        return notification.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }
}
