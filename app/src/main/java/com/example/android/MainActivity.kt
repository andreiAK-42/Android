package com.example.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

        val btnShowPic: Button = findViewById(R.id.button_with_image)
        btnShowPic.setOnClickListener {
            val picLink = "https://i.pinimg.com/originals/a8/bf/f1/a8bff19fe2b4672ea84796d901fd4eea.jpg"
            val intent = Intent(this, PicActivity::class.java)
            intent.putExtra("picLink", picLink)
            startActivity(intent)
        }

    }


}