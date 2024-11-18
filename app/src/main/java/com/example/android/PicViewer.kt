package com.example.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class PicViewer : AppCompatActivity() {
    private var imageUrl: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.pic_viewer)

        val picLink = intent.getStringExtra("picLink")

        val fullImageView: ImageView = findViewById(R.id.fullImageView)
        Glide.with(this)
            .load(picLink)
            .into(fullImageView)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val optionsMenu = findViewById<ImageView>(R.id.fullImageView)
        optionsMenu.setOnClickListener {
            imageUrl = picLink;
            val intent = Intent()
            intent.putExtra("imageUrl", imageUrl)
            intent.putExtra("isFavorite", true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}