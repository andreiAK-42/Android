package com.example.android

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val TAG = "Flickr cats"
        val internetButton: Button = findViewById(R.id.btnHTTP)
        internetButton.setOnClickListener{
            var url = URL("https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=ff49fcd4d4a08aa6aafb6ea3de826464&tags=cat&format=json&nojsoncallback=1")
            Thread {
                val connection1 = url.openConnection() as HttpURLConnection
                try {
                    val data = connection1.inputStream.bufferedReader().readText()
                    connection1.disconnect()
                    Log.d(TAG, data)
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
}