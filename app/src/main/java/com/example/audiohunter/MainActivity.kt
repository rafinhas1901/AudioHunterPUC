package com.example.audiohunter

import android.Manifest
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
import com.example.audiohunter.utils.AudioHunterService
import java.lang.Math.log10
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var screen: ActivityMainBinding// Cria uma variável da nossa tela
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1
    private val referencia = 2e-5
    private val SAMPLE_RATE = 44100
    private val CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        screen = ActivityMainBinding.inflate(layoutInflater) // Transforma XML em KT
        setContentView(screen.root) // Coloca na tela

        screen.tvSoundPressureLevel.text = "${String.format("%.2f", calculateSPL())} dB"
//        val recordAudio = Intent(this, AudioHunterService::class.java) // Inicio o servico de captura de audio (audio capturado a cada 10s)
//        startService(recordAudio)
    }


    override fun onRestart() {
        super.onRestart()
        val decibel = calculateSPL().roundToInt().toString()
        Log.d("ON_RESTART", decibel)
        screen.tvSoundPressureLevel.text = "${String.format("%.2f", calculateSPL())} dB"
    }

    override fun onResume() {
        super.onResume()
        val decibel = calculateSPL().roundToInt().toString()
        Log.d("ON_RESUME", decibel)
        screen.tvSoundPressureLevel.text = "${String.format("%.2f", calculateSPL())} dB"
    }
    private fun calculateSPL(): Double {
        // Configura a gravação de áudio

        val audioBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestRecordAudioPermission()
        }
        val audioRecord = AudioRecord(MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, audioBufferSize)
        audioRecord.startRecording()


        // Captura 1 segundo de áudio
        val audioSamples = ShortArray(SAMPLE_RATE)
        var readSize = 0
        while (readSize < SAMPLE_RATE) {
            readSize += audioRecord.read(audioSamples, readSize, SAMPLE_RATE - readSize)
        }

        // Calcula o nível de pressão sonora médio (SPL)
        var rms = 0.0
        for (sample in audioSamples) {
            rms += sample * sample.toDouble()
        }
        rms = Math.sqrt(rms / audioSamples.size)
        val db = 20 * log10(rms / referencia) - 94

        // Libera recursos de gravação de áudio
        audioRecord.stop()
        audioRecord.release()

        // Retorna o valor do SPL em decibéis (dB)
        return db
    }

    private fun AppCompatActivity.requestRecordAudioPermission() {
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Tratar exceção aqui, se necessário
        }
    }
    // Método para manipular a resposta do usuário às solicitações de permissão
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // A permissão foi concedida. Você pode iniciar a gravação de áudio aqui.
            } else {
                // A permissão foi negada. Você pode exibir uma mensagem para o usuário aqui.
            }
        }
    }
}