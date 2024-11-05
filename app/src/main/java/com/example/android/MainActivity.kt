package com.example.android


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {
    private lateinit var rView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: ContactAdapter
    private lateinit var contacts: List<Contact>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val searchButton: Button = findViewById(R.id.btn_search)
        val searchEditText: EditText = findViewById(R.id.et_search)
        lateinit var privateContactList: List<Contact>
        lateinit var MyAdapter: ContactAdapter
        val recyclerView: RecyclerView = findViewById(R.id.rView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            val contacts = getContacts()
            privateContactList = contacts
            withContext(Dispatchers.Main){
                MyAdapter = ContactAdapter(contacts)
                recyclerView.adapter = MyAdapter
            }
        }

        searchButton.setOnClickListener{
            val filter = searchEditText.text.toString()
            Timber.d("Search started")
            val filtered = filtered(privateContactList, filter)
            MyAdapter.updateContacts(filtered)
        }

       /* CoroutineScope(Dispatchers.Main).launch {
            Timber.plant(Timber.DebugTree())

            val contacts = getContacts()

            rView = findViewById(R.id.rView)
            rView.layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ContactAdapter(contacts)
            rView.adapter = adapter

            etSearch = findViewById(R.id.et_search)
            val btnSearch = findViewById<Button>(R.id.btn_search)
            btnSearch.setOnClickListener {
                searchContacts()
            }
        }*/
    }

    private fun filtered(inputContact: List<Contact>, filter: String): List<Contact>{
        Timber.d("Trying filter")
        if (filter.isEmpty()) {
            inputContact
            Timber.d("Nothing to filter")
        } else {
            inputContact.filter {
                it.name.contains(filter, ignoreCase = true) ||
                        it.phone.contains(filter,ignoreCase = true) ||
                        it.type.contains(filter, ignoreCase = true)
            }
            Timber.d("Something to filter")
        }
        return inputContact
    }
    private fun searchContacts() {
        val searchText = etSearch.text.toString().trim()
        val filteredContacts = if (searchText.isEmpty()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(searchText, ignoreCase = true) ||
                        contact.phone.contains(searchText, ignoreCase = true) ||
                        contact.type.contains(searchText, ignoreCase = true)
            }
        }
        adapter.updateContacts(filteredContacts)
    }

    private suspend fun getContacts() : List<Contact> {
        val wrapper: List<Contact> = emptyList()
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&=download")
            .build()
        val response = client.newCall(request).execute()
        val body = response.body()?.string()
        if (body != null) {
            val gson = GsonBuilder().create()
            val wrapper: List<Contact> = gson.fromJson(body, Array<Contact>::class.java).toList()
            Timber.d("Русский язык")
            val Contactlist: List<Contact> = wrapper
            wrapper.forEach { contact ->
                Timber.d("Name: ${contact.name}, Phone: ${contact.phone}, Type: ${contact.type}")
            }

        }
        return withContext(Dispatchers.IO) {
            wrapper
        }
    }
}
class ContactAdapter(private var contacts: List<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textPhone: TextView = itemView.findViewById(R.id.textPhone)
        val textType: TextView = itemView.findViewById(R.id.textType)
    }

    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.rview_item, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.textName.text = contact.name
        holder.textPhone.text = contact.phone
        holder.textType.text = contact.type
    }

    override fun getItemCount(): Int {
        return contacts.size
    }
}



data class Contact(
    val name: String,
    val phone: String,
    val type: String
)