package com.example.audiohunter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource.MIC
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.audiohunter.databinding.ActivityMainBinding
import com.example.audiohunter.utils.AudioCaptureService
import com.example.audiohunter.utils.AudioHunterService
import java.lang.Math.log10
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var screen: ActivityMainBinding// Cria uma variável da nossa tela
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1
    private val referencia = 2e-5
    private val SAMPLE_RATE = 44100
    private val CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT
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
                1);
            return
        }

            // Inicia o AudioCaptureThread
            val audioCaptureService = AudioCaptureService()
            audioCaptureService.start()
            Log.d("AH_DEBUG", audioCaptureService.db.toString())
            Thread {
                while (isRunning) {
                    runOnUiThread {
                        if (audioCaptureService.db > 72.5)
                        {
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

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}