package com.example.historymapapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RecentActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recent_activity)

        val backButton: ImageButton = findViewById(R.id.button_back)
        backButton.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recycler_recent)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RecentAdapter(mutableListOf()) { recentEvent ->
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("latitude", recentEvent.latitude)
                putExtra("longitude", recentEvent.longitude)
                putExtra("title", recentEvent.title)

            }
            startActivity(intent)
            finish()
        }
        recyclerView.adapter = adapter

        val cachedRecent = AppCache.getCachedRecentEvents(this)
        if (cachedRecent.isNotEmpty()) {
            adapter.updateData(cachedRecent.reversed())
        }

        loadRecentEvents()
    }

    private fun loadRecentEvents() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        Firebase.database("https://historymapapp-2e0cb-default-rtdb.europe-west1.firebasedatabase.app/").reference
            .child("recent_events")
            .child(userId)
            .orderByChild("timestamp")
            .limitToLast(5)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val events = mutableListOf<RecentEvent>()
                    snapshot.children.forEach { child ->
                        child.getValue(RecentEvent::class.java)?.let { events.add(it) }
                    }
                    adapter.updateData(events.reversed())
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RecentActivity, "Błąd ładowania historii", Toast.LENGTH_SHORT).show()
                }
            })
    }
}