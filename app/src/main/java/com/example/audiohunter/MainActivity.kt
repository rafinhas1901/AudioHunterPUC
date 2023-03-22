package com.example.audiohunter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    private lateinit var screen: ActivityMainBinding// Cria uma variável da nossa tela

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        screen = ActivityMainBinding.inflate(layoutInflater) // Transforma XML em KT
        setContentView(screen.root) // Coloca na tela


        screen.tvSoundPressureLevel.text = "67.58 dB"
    }
}