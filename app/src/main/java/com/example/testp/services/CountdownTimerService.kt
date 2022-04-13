package com.example.testp.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import android.content.Intent
import android.graphics.Color

import android.os.CountDownTimer


import androidx.core.app.NotificationCompat

import android.os.Build

import android.os.IBinder
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.example.testp.MainActivity
import com.example.testp.MainActivity.Companion.INTENT_COUNTDOWN_MINUTES
import com.example.testp.R
import java.util.concurrent.TimeUnit
import android.app.NotificationManager
import android.app.PendingIntent


class CountdownTimerService : Service() {


    private var timer: CounterClass? = null


    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("WrongConstant")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val totalMinutes = intent?.getLongExtra(INTENT_COUNTDOWN_MINUTES, -1)

        if (totalMinutes != null && totalMinutes != -1L) {

            val totalInMillis = totalMinutes * 60 * 1000
            timer = CounterClass(totalInMillis, 1000)
            timer!!.start()

            val notification = getNotification(getFormattedTime(totalInMillis));

            startForeground(NOTIFICATION_ID, notification)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


    @SuppressLint("WrongConstant")
    private fun getNotification(timerText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this, 0,
                notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK
            )
        }
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                ""
            }

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText(timerText)
            .setContentIntent(pendingIntent).build()
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private fun updateTimerNotification(timerText: String) {
        val notification: Notification = getNotification(timerText)
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_ID, notification)
    }

    inner class CounterClass(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            val hms = getFormattedTime(millisUntilFinished)
            println(hms)
            val timerInfoIntent = Intent(TIME_INFO)
            timerInfoIntent.putExtra("VALUE", hms)

            updateTimerNotification(hms)
            LocalBroadcastManager.getInstance(this@CountdownTimerService)
                .sendBroadcast(timerInfoIntent)
        }

        override fun onFinish() {
            // do nothing
        }
    }


    private fun getFormattedTime(millisUntilFinished: Long): String {
        val hms = String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(
                millisUntilFinished
            ),
            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    millisUntilFinished
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    millisUntilFinished
                )
            )
        )
        return "Current timer $hms"
    }

    companion object {
        const val NOTIFICATION_ID = 101
        const val TIME_INFO = "time_info"
    }
}