package com.example.myapplication

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import java.util.*

class PlayService : Service() {

    private var player: MediaPlayer? = null
    private var notificationBuilder: NotificationCompat.Builder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notificationId = 333
        val stopAction = "stop"

        if (intent?.action == stopAction) {
            player?.stop()
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationId)
            stopSelf()
            return START_NOT_STICKY
        }

        player?.stop()

        val url = intent!!.extras!!.getString("mp3")
        player = MediaPlayer()
        player?.setDataSource(this, Uri.parse(url))
        player?.setOnPreparedListener { p ->
            if (p != player)
                return@setOnPreparedListener
            p.start()

            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    if (!p.isPlaying) {
                        timer.cancel()
                        return
                    }
                    notificationBuilder?.setContentText("${p.currentPosition / 1000} sek / ${p.duration / 1000}")
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                        notificationId,
                        notificationBuilder?.build()
                    )
                }
            }, 1000, 1000)
        }
        player?.prepareAsync()

        val notificationIntent = Intent(
            this,
            MainActivity::class.java
        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val iStop = Intent(this, PlayService::class.java).setAction(stopAction)
        val piStop = PendingIntent.getService(this, 0, iStop, PendingIntent.FLAG_CANCEL_CURRENT)

        notificationBuilder = NotificationCompat.Builder(this, "1")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MP3")
            .setContentText("")
            .setContentIntent(PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT)
            )
            .addAction(R.mipmap.ic_launcher, "Stop", piStop)
            .setAutoCancel(true)
            .setOngoing(false)

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(notificationId, notificationBuilder?.build())

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