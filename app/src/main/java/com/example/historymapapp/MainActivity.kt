package com.example.historymapapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private val db = Firebase.firestore
    private val markerList = mutableListOf<Marker>()

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

        map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(10.0)
            controller.setCenter(GeoPoint(52.2298, 21.0122))
            minZoomLevel = 4.0
            maxZoomLevel = 15.0

            isHorizontalMapRepetitionEnabled = true
            isVerticalMapRepetitionEnabled = false

            val worldBounds = BoundingBox(
                85.0,
                180.0,
                -85.0,
                -180.0
            )
            setScrollableAreaLimitDouble(worldBounds)

            setMultiTouchControls(true)

        }

        getMarkersFromFirestore()

        // Obsługa wyszukiwarki
        val searchBar = findViewById<EditText>(R.id.search_bar)
        searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                searchForEvent(query)
                true
            } else {
                false
            }
        }

        @Suppress("ClickableViewAccessibility")
        searchBar.setOnTouchListener { _, event ->
            val drawableEnd = 2 // Index drawableEnd

            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawable = searchBar.compoundDrawables[drawableEnd]
                if (drawable != null &&
                    event.rawX >= (searchBar.right - drawable.bounds.width() - searchBar.paddingEnd)
                ) {
                    searchBar.text.clear()
                    searchBar.performClick() // Inform system o kliknięciu
                    return@setOnTouchListener true
                }
            }

            false
        }

        // Czyszczenie pola wyszukiwania po kliknięciu ikony
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    // Można np. odświeżyć markery lub przywrócić domyślny widok mapy
                }
            }
        })

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
                    val imageURL = document.getString("imageURL") ?: ""
                    val wikiURL = document.getString("wikiURL") ?: ""

                    addMarker(lat, lon, title, description, imageURL, wikiURL)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    private fun addMarker(lat: Double, lon: Double, title: String, description: String, imageURL: String, wikiURL: String) {
        if (!this::map.isInitialized) {
            Log.e("MainActivity", "Map is not initialized yet!")
            return
        }

        val marker = Marker(map).apply {
            position = GeoPoint(lat, lon)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
            this.subDescription = description

            this.setOnMarkerClickListener { clickedMarker, _ ->
                focusOnMarker(clickedMarker.position.latitude, clickedMarker.position.longitude)

                val event = clickedMarker.relatedObject as? EventData
                if (event != null) {
                    val bottomSheet = PlaceBottomSheet(
                        event.title,
                        event.description,
                        event.imageURL,
                        event.wikiURL,
                        clickedMarker.position.latitude,
                        clickedMarker.position.longitude
                    )
                    bottomSheet.show(supportFragmentManager, "PlaceBottomSheet")
                }
                true
            }

            relatedObject = EventData(title, description, imageURL, wikiURL)
        }

        map.overlays.add(marker)
        markerList.add(marker)
    }

    // Funkcja do przybliżenia mapy na dany punkt
    private fun focusOnMarker(lat: Double, lon: Double) {
        val geoPoint = GeoPoint(lat, lon)
        map.controller.animateTo(geoPoint)
        map.controller.setZoom(15.0)
    }

    // Funkcja wyszukiwania wydarzenia po nazwie
    private fun searchForEvent(query: String) {
        val foundMarker = markerList.firstOrNull {
            it.title?.contains(query, ignoreCase = true) == true
        }

        if (foundMarker != null) {
            focusOnMarker(foundMarker.position.latitude, foundMarker.position.longitude)

            val event = foundMarker.relatedObject as? EventData
            if (event != null) {
                val bottomSheet = PlaceBottomSheet(
                    event.title,
                    event.description,
                    event.imageURL,
                    event.wikiURL,
                    foundMarker.position.latitude,
                    foundMarker.position.longitude
                )
                bottomSheet.show(supportFragmentManager, "PlaceBottomSheet")
            }

        } else {
            Log.d("Search", "Nie znaleziono wydarzenia: $query")
        }
    }

}

data class EventData(
    val title: String,
    val description: String,
    val imageURL: String,
    val wikiURL: String
)
