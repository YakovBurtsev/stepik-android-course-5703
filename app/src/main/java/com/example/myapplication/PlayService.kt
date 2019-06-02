package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder

class PlayService : Service() {

    private var player: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        player?.stop()

        val url = intent!!.extras!!.getString("mp3")

        player = MediaPlayer()
        player?.setDataSource(this, Uri.parse(url))
        player?.setOnPreparedListener { p -> p.start() }
        player?.prepareAsync()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}