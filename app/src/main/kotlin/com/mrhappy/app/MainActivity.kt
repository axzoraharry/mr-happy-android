package com.mrhappy.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var chatAdapter: ChatAdapter
    private val client = OkHttpClient()
    // TODO: replace <HOST> with the actual IP or hostname of the Mr Happy server
    private val serverUrl = "http://<HOST>:8080/api/chat"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<RecyclerView>(R.id.chatRecycler)
        recycler.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter()
        recycler.adapter = chatAdapter

        val input = findViewById<EditText>(R.id.inputMessage)
        val send = findViewById<Button>(R.id.sendButton)
        send.setOnClickListener { sendMessage(input.text.toString()) }
    }

    private fun sendMessage(message: String) {
        if (message.isBlank()) return
        chatAdapter.addMessage("You", message)
        val json = JSONObject().put("message", message).toString()
        val body = RequestBody.create(MediaType.get("application/json"), json)
        val request = Request.Builder()
            .url(serverUrl)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { chatAdapter.addMessage("Error", e.localizedMessage ?: "Network error") }
            }
            override fun onResponse(call: Call, response: Response) {
                val resp = response.body?.string() ?: ""
                runOnUiThread { chatAdapter.addMessage("Mr Happy", resp) }
            }
        })
        // clear input after sending
        input.text.clear()
    }
}
