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
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val firestore = Firebase.firestore
    private val database = Firebase.database("https://historymapapp-2e0cb-default-rtdb.europe-west1.firebasedatabase.app/")
    private val userId = Firebase.auth.currentUser?.uid
    private val tasksCompleted = AtomicInteger(0)
    private val totalTasks = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        preloadData()
    }

    private fun preloadData() {
        loadFirestoreMarkers()
        if (userId != null) {
            loadFavorites()
            loadRecentEvents()
        } else {
            tasksCompleted.addAndGet(2)
        }
    }

    private fun onTaskFinished() {
        if (tasksCompleted.incrementAndGet() == totalTasks) {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("from_splash", true)
            })
            finish()
        }, 1000)
    }

    private fun loadFirestoreMarkers() {
        firestore.collection("historical_events").get()
            .addOnSuccessListener { documents ->
                val markers = documents.map { doc ->
                    Triple(
                        doc.getDouble("Latitude") ?: 0.0,
                        doc.getDouble("Longitude") ?: 0.0,
                        EventData(
                            title = doc.getString("Name of Incident") ?: "",
                            description = doc.getString("Describe Event") ?: "",
                            imageURL = doc.getString("imageURL") ?: "",
                            wikiURL = doc.getString("wikiURL") ?: "",
                            type = doc.getString("Type of Event") ?: "other",
                            epoch = doc.getString("Epoch") ?: "other"
                        )
                    )
                }
                AppCache.saveMarkers(this, markers)
                onTaskFinished()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading map data", Toast.LENGTH_SHORT).show()
                onTaskFinished()
            }
    }

    private fun loadFavorites() {
        database.reference.child("favorites").child(userId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorites = snapshot.children.mapNotNull {
                        it.getValue(FavoriteEvent::class.java)
                    }
                    AppCache.saveFavorites(this@SplashActivity, favorites)
                    onTaskFinished()
                }

                override fun onCancelled(error: DatabaseError) {
                    onTaskFinished()
                }
            })
    }

    private fun loadRecentEvents() {
        database.reference.child("recent_events").child(userId!!)
            .orderByChild("timestamp").limitToLast(5)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val recentEvents = snapshot.children.mapNotNull {
                        it.getValue(RecentEvent::class.java)
                    }
                    AppCache.saveRecentEvents(this@SplashActivity, recentEvents)
                    onTaskFinished()
                }

                override fun onCancelled(error: DatabaseError) {
                    onTaskFinished()
                }
            })
    }
}