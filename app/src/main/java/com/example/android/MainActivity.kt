package com.example.android


import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import timber.log.Timber
import java.util.Locale


class MainActivity : AppCompatActivity(), OnItemClickListener  {
    private lateinit var recyclerView: RecyclerView
    private var adapter: ContactAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Timber.plant(Timber.DebugTree())

        val etSearch: EditText = findViewById(R.id.et_search)
        recyclerView = findViewById(R.id.rView)

        etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter?.filterList(s.toString())
            }
        })

        CoroutineScope(Dispatchers.Main).launch {
            loadContacts()
        }
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

     suspend fun loadContacts() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&export=download")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.e(e, "Ошибка загрузки данных")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val type = object : TypeToken<List<Contact>>() {}.type
                    val contacts = gson.fromJson<List<Contact>>(response.body!!.string(), type)

                    CoroutineScope(Dispatchers.Main).launch {
                        adapter = ContactAdapter(contacts, this@MainActivity)
                        adapter!!.originalContacts = contacts
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                        Timber.d("Контакты загружены: ${contacts.size}")
                    }
                } else {
                    Timber.e("Ошибка загрузки данных: ${response.code}")
                }
            }
        })
    }

}
class ContactAdapter(private var contacts: List<Contact>, private val listener: OnItemClickListener) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
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
        val contact = contacts[position]
        holder.textName.text = contact.name
        holder.textPhone.text = contact.phone
        holder.textType.text = contact.type

        holder.itemView.setOnClickListener{
            listener.onItemClick(contact.phone)
        }
    }

    fun filterList(query: String) {
        if (!originalContacts.isNullOrEmpty()) {
            contacts = if (query.isEmpty()) {
                originalContacts
            } else {
                originalContacts.filter { contact ->
                    contact.name.lowercase(Locale.getDefault())
                        .contains(query.lowercase(Locale.getDefault())) ||
                            contact.phone.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault())) ||
                            contact.type.lowercase(Locale.getDefault())
                                .contains(query.lowercase(Locale.getDefault()))
                }
            }
            notifyDataSetChanged()
        }
    }
    override fun getItemCount(): Int = contacts.size
    public var originalContacts = contacts
}

interface OnItemClickListener {
    fun onItemClick(number: String?)
}

data class Contact(
    val name: String,
    val phone: String,
    val type: String
)