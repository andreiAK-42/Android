package com.example.android

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonSetBlackColorText: Button = findViewById(R.id.button_setBlackColorText)
        buttonSetBlackColorText.setOnClickListener {
            findViewById<TextView>(R.id.text_mainText).setTextColor(Color.BLACK)
        }

        val buttonSetRedColorText: Button = findViewById(R.id.button_setRedColorText)
        buttonSetRedColorText.setOnClickListener {
            findViewById<TextView>(R.id.text_mainText).setTextColor(Color.RED)
        }

        val buttonSetSize8sp: Button = findViewById(R.id.button_setSize8sp)
        buttonSetSize8sp.setOnClickListener {
            findViewById<TextView>(R.id.text_mainText).setTextSize(8f)
        }

        val buttonSetSize24sp: Button = findViewById(R.id.button_setSize24sp)
        buttonSetSize24sp.setOnClickListener {
            findViewById<TextView>(R.id.text_mainText).setTextSize(24f)
        }

        val buttonSetWhiteColorBackgroundText: Button = findViewById(R.id.button_setWhiteColorBackgroundText)
        buttonSetWhiteColorBackgroundText.setOnClickListener {
            findViewById<TextView>(R.id.text_mainText).setBackgroundColor(Color.WHITE)
        }

        val buttonSetYellowColorBackgroundText: Button = findViewById(R.id.button_setYellowColorBackgroundText)
        buttonSetYellowColorBackgroundText.setOnClickListener {
            findViewById<TextView>(R.id.text_mainText).setBackgroundColor(Color.YELLOW)
        }
    }
}