package com.example.historymapapp

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import android.widget.Toast
import org.osmdroid.views.CustomZoomButtonsController

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private val db = Firebase.firestore
    private val markerList = mutableListOf<Marker>()
    private var selectedEpochsGlobal: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnFavorite = findViewById<ImageButton>(R.id.btn_favorite)
        val btnNotes = findViewById<ImageButton>(R.id.btn_notes)
        val btnRecent = findViewById<ImageButton>(R.id.btn_recent)
        val btnAbout = findViewById<ImageButton>(R.id.btn_about)
        val btnSettings = findViewById<ImageButton>(R.id.btn_settings)
        val btnZoomIn = findViewById<ImageButton>(R.id.btn_zoom_in)
        val btnZoomOut = findViewById<ImageButton>(R.id.btn_zoom_out)

        // Open Street Map
        Configuration.getInstance()
            .load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        map = findViewById(R.id.map)

        val btnFilter = findViewById<ImageButton>(R.id.btn_filter)
        btnFilter.setOnClickListener {
            val filterBottomSheet = EpochBottomSheet(
                preselectedEpochs = selectedEpochsGlobal,
                onEpochSelected = { selected ->
                    selectedEpochsGlobal = selected
                    filterMarkersByEpoch(selected)
                }
            )
            filterBottomSheet.show(supportFragmentManager, "EpochFilterBottomSheet")
        }

        btnZoomIn.setOnClickListener {
            map.controller.zoomIn()
        }

        btnZoomOut.setOnClickListener {
            map.controller.zoomOut()
        }

        map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(10.0)
            controller.setCenter(GeoPoint(52.2298, 21.0122))
            minZoomLevel = 4.0
            maxZoomLevel = 15.0

            isHorizontalMapRepetitionEnabled = true
            isVerticalMapRepetitionEnabled = false
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

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
            val drawableEnd = 2

            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawable = searchBar.compoundDrawables[drawableEnd]
                if (drawable != null &&
                    event.rawX >= (searchBar.right - drawable.bounds.width() - searchBar.paddingEnd)
                ) {
                    searchBar.text.clear()
                    searchBar.performClick()
                    return@setOnTouchListener true
                }
            }

            false
        }

        btnFavorite.setOnClickListener {
            Toast.makeText(this, "Dodano do ulubionych", Toast.LENGTH_SHORT).show()
            // TODO: dodaj logikę ulubionych
        }

        btnNotes.setOnClickListener {
            Toast.makeText(this, "Notatki", Toast.LENGTH_SHORT).show()
            // TODO: otwórz notatki
        }

        btnRecent.setOnClickListener {
            Toast.makeText(this, "Ostatnio oglądane", Toast.LENGTH_SHORT).show()
            // TODO: pokaż historię
        }

        btnAbout.setOnClickListener {
            Toast.makeText(this, "O aplikacji", Toast.LENGTH_SHORT).show()
            // TODO: otwórz info
        }

        btnSettings.setOnClickListener {
            Toast.makeText(this, "Ustawienia", Toast.LENGTH_SHORT).show()
            // TODO: otwórz ustawienia
        }

    }

    // Funkcja pobierająca dane z Firestore
    private fun getMarkersFromFirestore() {
        map.overlays.clear()
        markerList.clear()
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
                    val type = document.getString("Type of Event") ?: "other"
                    val epoch = document.getString("Epoch") ?: "other"

                    addMarker(lat, lon, title, description, imageURL, wikiURL, type, epoch)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    private fun addMarker(
        lat: Double,
        lon: Double,
        title: String,
        description: String,
        imageURL: String,
        wikiURL: String,
        type: String,
        epoch: String) {
        if (!this::map.isInitialized) {
            Log.e("MainActivity", "Map is not initialized yet!")
            return
        }

        val iconRes = when (type.lowercase()) {
            "battle" -> R.drawable.ic_marker_battle
            "political" -> R.drawable.ic_marker_political
            "terrorism" -> R.drawable.ic_marker_terrorism
            "industrial disaster", "nature disaster" -> R.drawable.ic_marker_disaster
            "sport" -> R.drawable.ic_marker_sport
            "science" -> R.drawable.ic_marker_science
            else -> null
        }

        val marker = Marker(map).apply {
            position = GeoPoint(lat, lon)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = iconRes?.let { ContextCompat.getDrawable(this@MainActivity, it) }

            this.title = title
            this.subDescription = description
            //infoWindow = null

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

            relatedObject = EventData(title, description, imageURL, wikiURL, type, epoch)
        }

        map.overlays.add(marker)
        markerList.add(marker)
    }

    private fun filterMarkersByEpoch(selectedEpochs: Set<String>) {
        map.overlays.clear()

        for (marker in markerList) {
            val event = marker.relatedObject as? EventData
            if (event != null) {
                if (selectedEpochs.isEmpty() || selectedEpochs.contains(event.epoch)) {
                    map.overlays.add(marker)
                }
            }
        }
        map.invalidate()
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
            Toast.makeText(this, "Brak wyników dla: $query", Toast.LENGTH_SHORT).show()
        }
    }

}

data class EventData(
    val title: String,
    val description: String,
    val imageURL: String,
    val wikiURL: String,
    val type: String,
    val epoch: String
)
