package com.example.historymapapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import java.util.concurrent.atomic.AtomicInteger


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val firestore = Firebase.firestore
    private val database = Firebase.database("https://historymapapp-2e0cb-default-rtdb.europe-west1.firebasedatabase.app/")
    private val userId = Firebase.auth.currentUser?.uid
    private val tasksCompleted = AtomicInteger(0)
    private val totalTasks = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.splash_activity)

        // Inicjalizacja MapView
        mapView = MapView(this)
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)

        preloadData()
    }

    private fun preloadData() {
        loadFirestoreMarkers()
        loadFavorites()
        loadRecentEvents()
    }

    private fun onTaskFinished() {
        if (tasksCompleted.incrementAndGet() == totalTasks) {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 1000)
        }
    }

    private fun loadFirestoreMarkers() {
        firestore.collection("historical_events")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val lat = document.getDouble("Latitude") ?: 0.0
                    val lon = document.getDouble("Longitude") ?: 0.0
                    val title = document.getString("Name of Incident") ?: "Nieznane miejsce"
                    val description = document.getString("Describe Event") ?: "Brak opisu"
                    val imageURL = document.getString("imageURL") ?: ""
                    val wikiURL = document.getString("wikiURL") ?: ""
                    val type = document.getString("Type of Event") ?: "other"
                    val epoch = document.getString("Epoch") ?: "other"
                }
                onTaskFinished()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Błąd ładowania markerów", Toast.LENGTH_SHORT).show()
                onTaskFinished()
            }
    }

    private fun loadFavorites() {
        if (userId == null) {
            onTaskFinished(); return
        }

        database.reference.child("favorites").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val favorite = child.getValue(FavoriteEvent::class.java)
                    }
                    onTaskFinished()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SplashActivity, "Błąd ładowania ulubionych", Toast.LENGTH_SHORT).show()
                    onTaskFinished()
                }
            })
    }

    private fun loadRecentEvents() {
        if (userId == null) {
            onTaskFinished(); return
        }

        database.reference.child("recent_events").child(userId)
            .orderByChild("timestamp").limitToLast(5)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val event = child.getValue(RecentEvent::class.java)
                    }
                    onTaskFinished()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SplashActivity, "Błąd ładowania historii", Toast.LENGTH_SHORT).show()
                    onTaskFinished()
                }
            })
    }
}
