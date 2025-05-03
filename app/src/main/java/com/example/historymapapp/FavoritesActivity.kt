package com.example.historymapapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FavoritesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adapter: FavoritesAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.favorites_activity)

        auth = Firebase.auth
        database = Firebase.database("https://historymapapp-2e0cb-default-rtdb.europe-west1.firebasedatabase.app/").reference

        val backButton: ImageButton = findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recycler_favorites)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FavoritesAdapter(mutableListOf()) { favorite ->
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("latitude", favorite.latitude)
                putExtra("longitude", favorite.longitude)
                putExtra("title", favorite.title)
            }
            startActivity(intent)
            finish()
        }
        recyclerView.adapter = adapter

        loadFavorites()
    }

    private fun loadFavorites() {
        val userId = auth.currentUser?.uid ?: return
        database.child("favorites").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorites = mutableListOf<FavoriteEvent>()
                    for (child in snapshot.children) {
                        child.getValue(FavoriteEvent::class.java)?.let { favorites.add(it) }
                    }
                    adapter.updateData(favorites)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@FavoritesActivity, "Błąd ładowania ulubionych", Toast.LENGTH_SHORT).show()
                }
            })
    }
}