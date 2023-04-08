package com.example.audiohunter.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.audiohunter.MainActivity
import java.util.*
import kotlin.concurrent.timerTask

class AudioHunterService : Service() {

    private var timer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        timer = Timer()
        timer?.scheduleAtFixedRate(timerTask {
            Log.d("AH_DEBUG","ESTOU VIVO")
            //val mainActivity = MainActivity()
            //mainActivity.calculateSPL()
        }, 0, 10000) // 10 segundos em milissegundos
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        timer = null
    }
}