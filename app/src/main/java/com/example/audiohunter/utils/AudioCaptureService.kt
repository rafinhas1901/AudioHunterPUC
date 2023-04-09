package com.example.audiohunter.utils

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource.MIC
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.audiohunter.MainActivity
import kotlin.math.log10
import kotlin.properties.Delegates

class AudioCaptureService : Thread() {

    var db = 0.0
    private val referencia = 2e-5
    private val SAMPLE_RATE = 44100
    private val CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT
    private lateinit var audioRecord: AudioRecord

    override fun run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
        val audioBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

        audioRecord = AudioRecord(MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, audioBufferSize)
        audioRecord.startRecording()

        while (true) {
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
            db = 20 * log10(rms / referencia) - 94

            // Mostra o SPL na tela (isso deve ser feito em uma thread separada)

                //screen.tvSoundPressureLevel.text = "${String.format("%.2f", db)} dB"

            Handler(Looper.getMainLooper()).post {
                Log.d("AH_LOG","${String.format("%.2f", db)} dB")
            }
        }
    }

    fun stopRecording() {
        audioRecord.stop()
        audioRecord.release()
    }
}