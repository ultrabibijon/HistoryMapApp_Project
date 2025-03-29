package com.example.historymapapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker


class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Open Street Map
        Configuration.getInstance()
            .load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(10.0)
        map.controller.setCenter(GeoPoint(52.2298, 21.0122)) // Warszawa

        getMarkersFromFirestore()

    }

    // Funkcja pobierająca dane z Firestore
    private fun getMarkersFromFirestore() {
        // Kolekcja "locations"
        db.collection("historical_events")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val lat = document.getDouble("Latitude") ?: 0.0
                    val lon = document.getDouble("Longitude") ?: 0.0
                    val title = document.getString("Name of Incident") ?: "Nieznane miejsce"
                    val description = document.getString("Describe Event") ?: "Brak opisu"

                    addMarker(lat, lon, title, description)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    private fun addMarker(lat: Double, lon: Double, title: String, description: String) {
        if (!this::map.isInitialized) {
            Log.e("MainActivity", "Map is not initialized yet!")
            return
        }

        val marker = Marker(map).apply {
            position = GeoPoint(lat, lon)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
            setOnMarkerClickListener { _, _ ->
                // Po kliknięciu otwieramy BottomSheet
                val bottomSheet = PlaceBottomSheet(title, description, lat, lon)
                bottomSheet.show(supportFragmentManager, "PlaceBottomSheet")
                focusOnMarker(lat, lon)
                true
            }
        }
        map.overlays.add(marker)
    }

    // Funkcja do przybliżenia mapy na dany punkt
    private fun focusOnMarker(lat: Double, lon: Double) {
        val geoPoint = GeoPoint(lat, lon)
        map.controller.animateTo(geoPoint)
        map.controller.setZoom(15.0) // Przybliżenie na wybrany punkt
    }

}
