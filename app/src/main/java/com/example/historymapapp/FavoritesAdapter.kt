package com.example.historymapapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FavoritesAdapter(
    private var favorites: List<FavoriteEvent>,
    private val onItemClick: (FavoriteEvent) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_title)
        val epoch: TextView = view.findViewById(R.id.text_epoch)
        val image: ImageView = view.findViewById(R.id.image_event)
        val favoriteButton: ImageButton = view.findViewById(R.id.button_delete_favorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favorite = favorites[position]
        holder.title.text = favorite.title
        holder.epoch.text = favorite.epoch

        Glide.with(holder.itemView.context)
            .load(favorite.imageURL)
            .into(holder.image)

        holder.favoriteButton.setOnClickListener {
            removeFavorite(favorite)
        }

        holder.itemView.setOnClickListener {
            onItemClick(favorite)
        }
    }

    override fun getItemCount() = favorites.size

    fun updateData(newFavorites: List<FavoriteEvent>) {
        this.favorites = newFavorites
        notifyDataSetChanged()
    }

    private fun removeFavorite(favorite: FavoriteEvent) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        Firebase.database("https://historymapapp-2e0cb-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference
            .child("favorites")
            .child(userId)
            .child(favorite.eventId)
            .removeValue()
            .addOnSuccessListener {
                val updatedList = favorites.toMutableList().apply { remove(favorite) }
                updateData(updatedList)
            }
    }
}