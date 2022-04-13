package com.example.testp.services

import android.annotation.SuppressLint
import android.app.Notification
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import android.content.Intent

import android.os.CountDownTimer


import androidx.core.app.NotificationCompat

import android.app.PendingIntent
import android.app.Service

import android.os.IBinder
import androidx.annotation.Nullable
import com.example.testp.MainActivity
import com.example.testp.MainActivity.Companion.INTENT_COUNTDOWN_MINUTES
import com.example.testp.R
import java.util.concurrent.TimeUnit


class CountdownTimerService : Service() {
    private var timer: CounterClass? = null


    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("WrongConstant")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val totalMinutes = intent?.getLongExtra(INTENT_COUNTDOWN_MINUTES,-1)
        if (totalMinutes != null && totalMinutes != -1L) {

            val totalInMillis = totalMinutes * 60 * 1000
            timer = CounterClass(totalInMillis , 1000)
            timer!!.start()

            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0,
                notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK
            )
            val notification: Notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Timer is Running")
                .setContentIntent(pendingIntent).build()
            startForeground(101, notification)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timer!!.cancel()
        super.onDestroy()
        val timerInfoIntent = Intent(TIME_INFO)
        timerInfoIntent.putExtra("VALUE", "Stopped")
        LocalBroadcastManager.getInstance(this@CountdownTimerService).sendBroadcast(timerInfoIntent)
    }

    inner class CounterClass(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
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
            println(hms)
            val timerInfoIntent = Intent(TIME_INFO)
            timerInfoIntent.putExtra("VALUE", hms)
            LocalBroadcastManager.getInstance(this@CountdownTimerService)
                .sendBroadcast(timerInfoIntent)
        }

        override fun onFinish() {
            val timerInfoIntent = Intent(TIME_INFO)
            timerInfoIntent.putExtra("VALUE", "Completed")
            LocalBroadcastManager.getInstance(this@CountdownTimerService)
                .sendBroadcast(timerInfoIntent)
        }
    }

    companion object {
        const val TIME_INFO = "time_info"
    }
}