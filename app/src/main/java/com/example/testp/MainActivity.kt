package com.example.testp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.testp.services.CountdownTimerService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val INTENT_COUNTDOWN_MINUTES = "intent_countdown_minutes"
    }

    lateinit var timerBrodReceiver: TimerStatusReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        onClicks()
    }

    private fun init() {
        timerBrodReceiver = TimerStatusReceiver()
    }

    private fun onClicks() {
        btnTimer.setOnClickListener {
            if (isValidInput()) {
                val minutes = edtTime.text.toString().toLong()
                startTimer(minutes)
            } else {
                Toast.makeText(this, "Enter minutes", Toast.LENGTH_SHORT).show()
            }
        }

        btnTimerStop.setOnClickListener {
            stopTimer()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(timerBrodReceiver, IntentFilter(CountdownTimerService.TIME_INFO));
    }

    private fun startTimer(minutes: Long) {
        val intent = Intent(this, CountdownTimerService::class.java)
        intent.putExtra(INTENT_COUNTDOWN_MINUTES, minutes)
        startService(intent)
    }

    private fun stopTimer() {
        val intent = Intent(this, CountdownTimerService::class.java)
        stopService(intent)
    }

    private fun isValidInput(): Boolean {
        return edtTime.text.toString().isNotEmpty();
    }


    inner class TimerStatusReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == CountdownTimerService.TIME_INFO) {
                if (intent.hasExtra("VALUE")) {
                    tvCountdownTimer.text = intent.getStringExtra("VALUE")
                }
            }
        }
    }

}