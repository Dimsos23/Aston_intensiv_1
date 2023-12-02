package com.example.aston_intensive_1


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import com.example.aston_intensive_1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var musicService: MusicService
    private var isPlaying = false
    private var serviceStarted = false
    private var mBound: Boolean = false
    private lateinit var serviceIntent: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        serviceIntent = Intent(this, MusicService::class.java)

        binding.apply {
            playPauseButton.setOnClickListener {
                if (isPlaying) {
                    binding.playPauseButton.text = resources.getString(R.string.button_play)
                    musicService.pauseTrack()
                } else {
                    if (!serviceStarted) {
                        binding.playPauseButton.text = resources.getString(R.string.button_pause)
                        Intent(applicationContext, MusicService::class.java).also {
                            it.action = MusicService.Actions.START.toString()
                            startService(it)
                            serviceStarted = true
                        }
                    } else {
                        musicService.startTrack()
                        binding.playPauseButton.text = resources.getString(R.string.button_pause)
                    }
                }
                isPlaying = !isPlaying
            }
            nextImageButton.setOnClickListener {
                if (serviceStarted) {
                    musicService.nextTrack()
                    playPauseButton.text = resources.getString(R.string.button_pause)
                    isPlaying = true
                } else {
                    Intent(applicationContext, MusicService::class.java).also {
                        it.action = MusicService.Actions.NEXT.toString()
                        startService(it)
                        playPauseButton.text = resources.getString(R.string.button_pause)
                        isPlaying = true
                        serviceStarted = true
                    }
                }
            }

            previousImageButton.setOnClickListener {
                if (serviceStarted) {
                    musicService.previousTrack()
                    playPauseButton.text = resources.getString(R.string.button_pause)
                    isPlaying = true
                } else {
                    Intent(applicationContext, MusicService::class.java).also {
                        it.action = MusicService.Actions.PREVIOUS.toString()
                        startService(it)
                        playPauseButton.text = resources.getString(R.string.button_pause)
                        isPlaying = true
                        serviceStarted = true
                    }
                }
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MyBinder
            musicService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        stopService(serviceIntent)
    }

}
