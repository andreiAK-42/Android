package com.example.android

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import timber.log.Timber
import java.io.InputStreamReader
import java.net.URL


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), OnItemClickListener {
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Thread {
            Timber.plant(Timber.DebugTree())
            val urlString =
                "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=ff49fcd4d4a08aa6aafb6ea3de826464&tags=cat&format=json&nojsoncallback=1" // Замените на ваш URL
            val url = URL(urlString)
            val connection = url.openConnection()
            val reader = InputStreamReader(connection.getInputStream())

            val gson = GsonBuilder().create()
            val jsonElement = JsonParser.parseReader(reader)

            val data: Wrapper = gson.fromJson(jsonElement, Wrapper::class.java)

            for (i in 0 until data.photos.photo.size step 5) {
                val gson = Gson()

                val jsonString = gson.toJson(data.photos.photo[i])
                Timber.d("Photo: ${jsonString}")
            }

            runOnUiThread {
                val recyclerView = findViewById<RecyclerView>(R.id.rView)
                recyclerView.layoutManager = GridLayoutManager(this, 2)
                recyclerView.adapter = FlickrAdapter(data.photos.photo, this)
            }
        }.start()
    }


    override fun onItemClick(imageUrl: String) {
        val intent = Intent(this, PicViewer::class.java)
        intent.putExtra("picLink", imageUrl)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val imageUrl = it.getStringExtra("imageUrl")
                    val isFavorite = it.getBooleanExtra("isFavorite", false)

                    if (isFavorite && imageUrl != null) {
                        val snackbar = Snackbar.make(findViewById(android.R.id.content), "Картинка добавлена в избранное", Snackbar.LENGTH_LONG)
                        snackbar.setAction("Открыть") {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl))
                            startActivity(browserIntent)
                        }
                        snackbar.show()
                    }
                }
            }
        }
    }

}

class FlickrAdapter(private val photos: List<Photo>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<FlickrAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val picture: ImageView = itemView.findViewById(R.id.picture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rview_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photos[position]
        val imageUrl = "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_q.jpg"

        Glide.with(holder.itemView).load(imageUrl).into(holder.picture)
        val PICK_IMAGE_REQUEST: Int = 1

        holder.itemView.setOnClickListener {
            listener.onItemClick(imageUrl)
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }
}

interface OnItemClickListener {
    fun onItemClick(imageUrl: String)
}

data class Photo (
    val id: String,
    val owner: String,
    val secret: String,
    val server: String,
    val farm: Int,
    val title: String,
    val ispublic: Int,
    val isfriend: Int,
    val isfamily: Int
)
data class PhotoPage (
    val page: Int,
    val pages: Int,
    val perpage: Int,
    val total: Int,
    val photo: MutableList<Photo>
)
data class Wrapper (val photos: PhotoPage)