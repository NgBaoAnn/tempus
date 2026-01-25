package com.projectapp.tempus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
//import android.content.Intent
import android.util.Log
import com.projectapp.tempus.util.TimerEventBus

class TimerActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TimerActionReceiver"
        const val ACTION_PAUSE = "com.projectapp.tempus.TIMER_PAUSE"
        const val ACTION_RESUME = "com.projectapp.tempus.TIMER_RESUME"
        const val ACTION_STOP = "com.projectapp.tempus.TIMER_STOP"
        
        // Broadcasts to send to TimerFragment
        const val BROADCAST_PAUSE_TIMER = "com.projectapp.tempus.BROADCAST_PAUSE_TIMER"
        const val BROADCAST_RESUME_TIMER = "com.projectapp.tempus.BROADCAST_RESUME_TIMER"
        const val BROADCAST_STOP_TIMER = "com.projectapp.tempus.BROADCAST_STOP_TIMER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: action=${intent.action}")
        when (intent.action) {
            ACTION_PAUSE -> {
                Log.d(TAG, "Emitting PAUSE event")
                TimerEventBus.tryEmit(TimerEventBus.TimerEvent.PAUSE)
            }
            ACTION_RESUME -> {
                Log.d(TAG, "Emitting RESUME event")
                TimerEventBus.tryEmit(TimerEventBus.TimerEvent.RESUME)
            }
            ACTION_STOP -> {
                Log.d(TAG, "Emitting STOP event")
                TimerEventBus.tryEmit(TimerEventBus.TimerEvent.STOP)
            }
        }
    }
}
