package com.example.android

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.Locale


class MainActivity : AppCompatActivity(), OnItemClickListener  {
    private lateinit var recyclerView: RecyclerView
    private var adapter: ContactAdapter? = null
    private val PREFS_FILE = "Filter"
    private val PREF_NAME = "FilterString"
    var settings: SharedPreferences? = null
    var prefEditor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getFilter()

        Timber.plant(Timber.DebugTree())

        val etSearch: EditText = findViewById(R.id.et_search)
        recyclerView = findViewById(R.id.rView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ContactAdapter(this)
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter?.filterList(s.toString())
            }
        })

        lifecycleScope.launch {
            val contacts: List<Contact> = loadContacts()
            adapter!!.submitList(contacts)
            adapter!!.originalContacts = contacts
        }
    }
    fun getFilter() {
        val nameView = findViewById<TextView>(R.id.et_search)
        val name: String? = settings!!.getString(PREF_NAME, "Абонент")
        nameView.text = name
    }

    override fun onPause() {
        super.onPause()

        val name: String = findViewById<TextView>(R.id.et_search).getText().toString()
        prefEditor = settings!!.edit()
        prefEditor!!.putString(PREF_NAME, name)
        prefEditor!!.apply()
    }

    override fun onItemClick(number: String?) {
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.CALL_PHONE),
                1
            )
        } else {
            startActivity(Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + number)))
        }

    }

    private suspend fun loadContacts(): List<Contact> {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&export=download")
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val gson = Gson()
                    val type = object : TypeToken<List<Contact>>() {}.type
                    gson.fromJson<List<Contact>>(response.body!!.string(), type)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка загрузки контактов")
                emptyList()
            }
        }
    }
}


class ContactAdapter(private val listener: OnItemClickListener) : ListAdapter<Contact, ContactAdapter.ViewHolder>(ContactDiffCallback()) {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textPhone: TextView = itemView.findViewById(R.id.textPhone)
        val textType: TextView = itemView.findViewById(R.id.textType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rview_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.textName.text = contact.name
        holder.textPhone.text = contact.phone
        holder.textType.text = contact.type
        holder.itemView.setOnClickListener { listener.onItemClick(contact.phone) }
    }

    fun filterList(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalContacts
        } else {
            originalContacts.filter { contact ->
                contact.name.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
                        contact.phone.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
                        contact.type.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
            }
        }
        submitList(filteredList)
    }
    public var originalContacts: List<Contact> = emptyList()
}

class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.phone == newItem.phone
    }

    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem == newItem
    }
}

interface OnItemClickListener {
    fun onItemClick(number: String?)
}

data class Contact(
    val name: String,
    val phone: String,
    val type: String
)