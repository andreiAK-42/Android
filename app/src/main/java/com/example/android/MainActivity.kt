package com.example.android

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import timber.log.Timber
import java.io.InputStreamReader
import java.net.URL


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
}

class FlickrAdapter(private val photos: List<Photo>, private val context: Context) :
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

        // Загрузка изображения с помощью Glide
        Glide.with(context).load(imageUrl).into(holder.picture)

        holder.itemView.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("imageUrl", imageUrl)
            clipboard.setPrimaryClip(clip)

            Log.i("RecyclerView", "Ссылка скопирована в буфер обмена: $imageUrl")
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }
}



class Photo {
    val id: String
    val owner: String
    val secret: String
    val server: String
    val farm: Int
    val title: String
    val ispublic: Int
    val isfriend: Int
    val isfamily: Int

    constructor(
        id: String, owner: String, secret: String, server: String, farm: Int,
        title: String, ispublic: Int, isfriend: Int, isfamily: Int
    ) {
        this.id = id
        this.owner = owner
        this.secret = secret
        this.server = server
        this.farm = farm
        this.title = title
        this.ispublic = ispublic
        this.isfriend = isfriend
        this.isfamily = isfamily
    }

}
class PhotoPage {
    val page: Int
    val pages: Int
    val perpage: Int
    val total: Int
    val photo: MutableList<Photo>

    constructor(list: MutableList<Photo>, total: Int, perpage: Int, page: Int, pages: Int) {
        this.page = page
        this.pages = pages
        this.perpage = perpage
        this.total = total
        this.photo = list
    }
}
class Wrapper {
    val photos: PhotoPage

    constructor(pages: PhotoPage) {
        this.photos = pages
    }
}