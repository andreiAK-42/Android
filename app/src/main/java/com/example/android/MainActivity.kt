package com.example.android

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val TAG1 = "Flickr cats"
        val TAG2 = "FlickrOK cats"
        val internetButton: Button = findViewById(R.id.btnHTTP)
        val coolerInternetButton: Button = findViewById(R.id.btnOkHTTP)

        internetButton.setOnClickListener{
            var url = URL("https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=ff49fcd4d4a08aa6aafb6ea3de826464&tags=cat&format=json&nojsoncallback=1")
            Thread {
                val connection1 = url.openConnection() as HttpURLConnection
                try {
                    val data = connection1.inputStream.bufferedReader().readText()
                    connection1.disconnect()
                    Log.d(TAG1, data)
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }

        coolerInternetButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=ff49fcd4d4a08aa6aafb6ea3de826464&tags=cat&format=json&nojsoncallback=1")
                    .build()

                try {
                    val response: Response = client.newCall(request).execute()
                    val responseData = response.body?.string()

                    Log.i(TAG2, responseData ?: "No response")
                } catch (e: Exception) {
                    Log.e(TAG2, "Error: ${e.message}")
                }
            }
        }
    }
}