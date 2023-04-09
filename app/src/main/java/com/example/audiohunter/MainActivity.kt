package com.example.audiohunter

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.audiohunter.databinding.ActivityMainBinding
import com.example.audiohunter.utils.AudioCaptureService

//"val" é constante, "var" é variavel
    const val CHANNEL_ID = "channelID"
    const val CHANNEL_NAME = "channelName"
    const val NOTIFICATION_ID = 0
class MainActivity : AppCompatActivity() {
    private lateinit var screen: ActivityMainBinding// Cria uma variável da nossa tela
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1
    private var isRunning = true

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        screen = ActivityMainBinding.inflate(layoutInflater) // Transforma XML em KT
        setContentView(screen.root) // Coloca na tela
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_REQUEST_CODE)
            return
        }
            // Inicia o AudioCaptureThread
            val audioCaptureService = AudioCaptureService()
            audioCaptureService.start()
            Log.d("AH_DEBUG", audioCaptureService.db.toString())
            Thread {
                while (isRunning) {
                    runOnUiThread {
                        if (audioCaptureService.db > 10.5)
                        {
                            createNotification()
                            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                                .setContentTitle("Ambiente com muito barulho - ${String.format("%.2f", audioCaptureService.db)} dB")
                                .setContentText("Este ambiente ser prejudicial a saúde.")
                                .setSmallIcon(R.drawable.enabled_mic)
                                .setPriority(NotificationManager.IMPORTANCE_MAX)
                                .setVibrate(longArrayOf(1000, 1000))
                                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                .build()
                            val notificationManager = NotificationManagerCompat.from(this)
                            notificationManager.notify(NOTIFICATION_ID, notification)
                            screen.tvSoundPressureLevel.setTextColor(resources.getColor(R.color.puc_yellow))
                        } else {
                            screen.tvSoundPressureLevel.setTextColor(resources.getColor(R.color.white))
                        }
                        Log.d("AH_SOUND","Valor mic: ${audioCaptureService.db}")
                        Log.d("AH_SOUND","----")
                        screen.tvSoundPressureLevel.text = "${String.format("%.2f", audioCaptureService.db)} dB"
                    }
                    Thread.sleep(2000) // Espera 1 segundo antes de verificar novamente
                }
            }.start()
    }

    //Métodos são "fun"
    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                lightColor = Color.GREEN
                enableLights(true)
                enableVibration(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == 0) { //0 deu bom, -1 não aceitou
            finish()
            startActivity(intent)
        } else {
            screen.tvSoundPressureLevel.textSize = 20F
            screen.tvSoundPressureLevel.text = "Libere a permissão de microfone nas configurações"
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}