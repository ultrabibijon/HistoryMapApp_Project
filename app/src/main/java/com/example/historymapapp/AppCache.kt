package com.example.historymapapp

import androidx.core.content.edit
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppCache {
    private const val PREFS_NAME = "app_cache"

    // MARKERS
    fun saveMarkers(context: Context, markers: List<Triple<Double, Double, EventData>>) {
        val cachedMarkers = markers.map { (lat, lon, event) ->
            CachedMarker(lat, lon, event)
        }
        val json = Gson().toJson(cachedMarkers)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString("cached_markers", json) }
    }

    fun getCachedMarkers(context: Context): List<Triple<Double, Double, EventData>> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("cached_markers", "[]") ?: "[]"
        val type = object : TypeToken<List<CachedMarker>>() {}.type
        val cached = Gson().fromJson<List<CachedMarker>>(json, type)
        return cached.map { Triple(it.latitude, it.longitude, it.eventData) }
    }

    // FAVORITES
    fun saveFavorites(context: Context, favorites: List<FavoriteEvent>) {
        val json = Gson().toJson(favorites)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString("cached_favorites", json) }
    }

    fun getCachedFavorites(context: Context): List<FavoriteEvent> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("cached_favorites", "[]") ?: "[]"
        val type = object : TypeToken<List<FavoriteEvent>>() {}.type
        return Gson().fromJson(json, type)
    }

    // RECENT EVENTS
    fun saveRecentEvents(context: Context, events: List<RecentEvent>) {
        val json = Gson().toJson(events)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString("cached_recent", json) }
    }

    fun getCachedRecentEvents(context: Context): List<RecentEvent> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("cached_recent", "[]") ?: "[]"
        val type = object : TypeToken<List<RecentEvent>>() {}.type
        return Gson().fromJson(json, type)
    }
}

data class CachedMarker(
    val latitude: Double,
    val longitude: Double,
    val eventData: EventData
)