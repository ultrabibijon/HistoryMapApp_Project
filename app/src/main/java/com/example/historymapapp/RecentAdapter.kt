package com.example.historymapapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecentAdapter(
    private var recentEvents: List<RecentEvent>,
    private val onItemClick: (RecentEvent) -> Unit
) : RecyclerView.Adapter<RecentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_title)
        val epoch: TextView = view.findViewById(R.id.text_epoch)
        val image: ImageView = view.findViewById(R.id.image_event)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = recentEvents[position]
        holder.title.text = event.title
        holder.epoch.text = event.epoch

        Glide.with(holder.itemView.context)
            .load(event.imageURL)
            .into(holder.image)

        holder.itemView.setOnClickListener { onItemClick(event) }
    }

    override fun getItemCount() = recentEvents.size

    fun updateData(newEvents: List<RecentEvent>) {
        this.recentEvents = newEvents
        notifyDataSetChanged()
    }
}