package com.example.audiohunter

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
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
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore

// Variáveis para notificações
const val CHANNEL_ID = "channelID"
const val CHANNEL_NAME = "channelName"
const val NOTIFICATION_ID = 0
class MainActivity : AppCompatActivity() {
    //private var valoresDb = arrayOf("")
    private lateinit var screen: ActivityMainBinding // Cria uma variável da nossa tela
    private var isRunning = true
    // Lista de permissões a serem solicitadas
    private var dbValues = doubleArrayOf()
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.RECORD_AUDIO
    )
    private val audioCaptureService = AudioCaptureService()
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        screen = ActivityMainBinding.inflate(layoutInflater) // Transforma XML em KT
        setContentView(screen.root) // Coloca na tela

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

            // Permissões já concedidas
            // TODO: Executar a lógica da atividade
            screen.panicButton.setOnClickListener {
                sendLocation()
            }

            // Inicia o AudioCaptureThread

            audioCaptureService.start()
            Log.d("AH_DEBUG", audioCaptureService.db.toString())
            Thread {
                while (isRunning) {
                    runOnUiThread {
                        if (audioCaptureService.db > 80.0)
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
                            dbValues += (String.format("%.2f", audioCaptureService.db)).toDouble()
                            sendDbAverage()

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
        else {
            // Permissões ainda não concedidas
            // Solicita as permissões
            screen.tvSoundPressureLevel.text = "Libere as permissões nas configurações"
            requestPermissions(PERMISSIONS, 0)
        }
    }

    fun calculateAverage(): Double {
        var average = 0.0
        for (i in dbValues) {
            average += i
        }
        return (average/dbValues.size)
    }
    fun sendDbAverage() {
        if (dbValues.size >= 6) {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("sound_captures").document()
            val locationData = hashMapOf(
                "dbAverage" to (String.format("%.2f", calculateAverage())).toDouble()
            )
            docRef.set(locationData)
                .addOnSuccessListener {
                    Log.d("TESTE_AG", "Media DB enviada com sucesso!")
                }
                .addOnFailureListener {
                    Log.e("TESTE_AG", "Erro ao enviar media DB", it)
                }
        }
    }

    fun sendLocation() {
        // Obter a localização atual do dispositivo
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Enviar a localização para o Firestore
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("warnings").document()
                    Log.d("TESTE_AH", location.latitude.toString())
                    val locationData = hashMapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,
                        "db" to "${String.format("%.2f", audioCaptureService.db)}"
                    )
                    docRef.set(locationData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Localização enviada com sucesso!")
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Erro ao enviar localização", it)
                        }
                } else {
                    Log.e(TAG, "Localização não encontrada")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao obter localização", e)
            }
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
    // Método chamado quando o usuário concede ou nega as permissões solicitadas
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                // Verifica se as permissões foram concedidas
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Permissões concedidas
                    // TODO: Executar a lógica da atividade
                    // Reestarta a activity
                    val intent = intent
                    finish()
                    startActivity(intent)
                } else {
                    // Permissões negadas
                    // TODO: Tratar a negação das permissões
                }
                return
            }
            else -> {
                // Outro pedido de permissão
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}