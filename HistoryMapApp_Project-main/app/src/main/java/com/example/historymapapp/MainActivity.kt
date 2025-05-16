package com.example.historymapapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import org.osmdroid.views.CustomZoomButtonsController

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var searchBar: EditText
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

        val btnFilter = findViewById<ImageButton>(R.id.btn_filter)
        val btnFavorite = findViewById<ImageButton>(R.id.btn_favorite)
        val btnNotes = findViewById<ImageButton>(R.id.btn_notes)
        val btnRecent = findViewById<ImageButton>(R.id.btn_recent)
        val btnAbout = findViewById<ImageButton>(R.id.btn_about)
        val btnSettings = findViewById<ImageButton>(R.id.btn_settings)
        val btnZoomIn = findViewById<ImageButton>(R.id.btn_zoom_in)
        val btnZoomOut = findViewById<ImageButton>(R.id.btn_zoom_out)

        // Firebase Authentication
        auth = Firebase.auth

        // Firebase Realtime Database
        database = Firebase.database("https://historymapapp-2e0cb-default-rtdb.europe-west1.firebasedatabase.app/").reference

        // Anonymous logging
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Log.d("FirebaseAuth", "Zalogowano anonimowo. UID: ${user?.uid}")
                        Toast.makeText(this, "Zalogowano anonimowo!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("FirebaseAuth", "Błąd logowania anonimowego", task.exception)
                        Toast.makeText(this, "Błąd logowania!", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            val user = auth.currentUser
            Log.d("FirebaseAuth", "Użytkownik już zalogowany. UID: ${user?.uid}")
        }

        // Open Street Map
        Configuration.getInstance()
            .load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        map = findViewById(R.id.map)

        // Przyciski na mapie

        // Przycisk - filtrowanie według epoki
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

        // Przycisk - przybliżenie mapy
        btnZoomIn.setOnClickListener {
            map.controller.zoomIn()
        }

        // Przycisk - oddalanie mapy
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

        if (intent?.getBooleanExtra("from_splash", true) == true) {
            val cachedMarkers = AppCache.getCachedMarkers(this)
            cachedMarkers.forEach { (lat, lon, event) ->
                addMarker(lat, lon, event.title, event.description,
                    event.imageURL, event.wikiURL, event.type, event.epoch)
            }
        }

        getMarkersFromFirestore()

        // Obsługa wyszukiwarki
        searchBar = findViewById(R.id.search_bar)
        searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                searchForEvent(query)
                searchBar.clearFocus()
                hideKeyboard()
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
                    searchBar.clearFocus()
                    hideKeyboard()
                    searchBar.performClick()
                    return@setOnTouchListener true
                }
            }

            false
        }

        // Przycisk - przejście do ulubionych
        btnFavorite.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

        // Przycisk - przejście do notatek
        btnNotes.setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java)
            startActivity(intent)
        }

        // Przcycisk - przejście do ostatnio oglądanych
        btnRecent.setOnClickListener {
            val intent = Intent(this, RecentActivity::class.java)
            startActivity(intent)
        }

        // Przycisk - przejście do informacji o aplikacji
        btnAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        // Przycisk - przejścoe do ustawień
        btnSettings.setOnClickListener {
            Toast.makeText(this, "Ustawienia", Toast.LENGTH_SHORT).show()
            // TODO: otwórz ustawienia
        }

        // Obsługa przekierowania z FavoritesActivity
        intent?.let {
            if (it.hasExtra("latitude") && it.hasExtra("longitude")) {
                val lat = it.getDoubleExtra("latitude", 0.0)
                val lon = it.getDoubleExtra("longitude", 0.0)
                val title = it.getStringExtra("title") ?: ""

                map.postDelayed({
                    focusOnMarker(lat, lon)

                    markerList.firstOrNull { marker -> marker.title == title }?.let { marker ->
                        val event = marker.relatedObject as? EventData
                        if (event != null) {

                            addToRecentEvents(event, marker.position.latitude, marker.position.longitude)

                            val bottomSheet = PlaceBottomSheet(
                                event.title,
                                event.description,
                                event.imageURL,
                                event.wikiURL,
                                marker.position.latitude,
                                marker.position.longitude,
                                onFavoriteChanged = { isFavorite ->
                                    if (isFavorite) {
                                        addToFavorites(event, marker.position.latitude, marker.position.longitude)
                                    } else {
                                        removeFromFavorites(event.title)
                                    }
                                }
                            )
                            bottomSheet.show(supportFragmentManager, "PlaceBottomSheet")
                        }
                    }
                }, 500)
            }
        }

    }

    // Funkcja pobierająca dane z Firestore
    private fun getMarkersFromFirestore() {

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

                    addToRecentEvents(event, clickedMarker.position.latitude, clickedMarker.position.longitude)

                    val bottomSheet = PlaceBottomSheet(
                        event.title,
                        event.description,
                        event.imageURL,
                        event.wikiURL,
                        clickedMarker.position.latitude,
                        clickedMarker.position.longitude,
                        onFavoriteChanged = { isFavorite ->
                            if (isFavorite) {
                                addToFavorites(event, clickedMarker.position.latitude, clickedMarker.position.longitude)
                            } else {
                                removeFromFavorites(event.title)
                            }
                        }
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

    private fun addToFavorites(event: EventData, lat: Double, lon: Double) {
        val userId = auth.currentUser?.uid ?: return
        val favorite = FavoriteEvent(
            userId = userId,
            eventId = event.title,
            title = event.title,
            description = event.description,
            imageURL = event.imageURL,
            wikiURL = event.wikiURL,
            type = event.type,
            epoch = event.epoch,
            latitude = lat,
            longitude = lon
        )

        database.child("favorites").child(userId).child(event.title).setValue(favorite)
            .addOnSuccessListener {
                Toast.makeText(this, "Dodano do ulubionych", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Błąd podczas dodawania", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromFavorites(eventTitle: String) {
        val userId = auth.currentUser?.uid ?: return
        database.child("favorites").child(userId).child(eventTitle).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Usunięto z ulubionych", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Błąd podczas usuwania", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToRecentEvents(event: EventData, lat: Double, lon: Double) {
        val userId = auth.currentUser?.uid ?: return
        val recentEvent = RecentEvent(
            userId = userId,
            eventId = event.title,
            title = event.title,
            description = event.description,
            imageURL = event.imageURL,
            wikiURL = event.wikiURL,
            type = event.type,
            epoch = event.epoch,
            latitude = lat,
            longitude = lon,
            timestamp = System.currentTimeMillis()
        )

        database.child("recent_events").child(userId).child(event.title).setValue(recentEvent)
            .addOnSuccessListener {
                // Ogranicz do 5 rekordów
                database.child("recent_events").child(userId)
                    .orderByChild("timestamp")
                    .limitToFirst(1)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.children.count() > 5) {
                                snapshot.children.firstOrNull()?.ref?.removeValue()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Błąd przy ograniczaniu historii", error.toException())
                        }
                    })
            }
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
            searchBar.clearFocus()
            hideKeyboard()

            val event = foundMarker.relatedObject as? EventData
            if (event != null) {

                addToRecentEvents(event, foundMarker.position.latitude, foundMarker.position.longitude)

                val bottomSheet = PlaceBottomSheet(
                    event.title,
                    event.description,
                    event.imageURL,
                    event.wikiURL,
                    foundMarker.position.latitude,
                    foundMarker.position.longitude,
                    onFavoriteChanged = { isFavorite ->
                        if (isFavorite) {
                            addToFavorites(event, foundMarker.position.latitude, foundMarker.position.longitude)
                        } else {
                            removeFromFavorites(event.title)
                        }
                    }
                )
                bottomSheet.show(supportFragmentManager, "PlaceBottomSheet")
            }

        } else {
            Log.d("Search", "Nie znaleziono wydarzenia: $query")
            Toast.makeText(this, "Brak wyników dla: $query", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchBar.windowToken, 0)
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

data class FavoriteEvent(
    val userId: String = "",
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val imageURL: String = "",
    val wikiURL: String = "",
    val type: String = "",
    val epoch: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class RecentEvent(
    val userId: String = "",
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val imageURL: String = "",
    val wikiURL: String = "",
    val type: String = "",
    val epoch: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)