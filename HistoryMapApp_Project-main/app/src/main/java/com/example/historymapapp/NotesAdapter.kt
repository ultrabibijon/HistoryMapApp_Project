package com.example.historymapapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(
    private var notes: List<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_title)
        val date: TextView = view.findViewById(R.id.text_date)
        val deleteButton: ImageButton = view.findViewById(R.id.button_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.date.text = dateFormat.format(note.lastEdited)

        holder.itemView.setOnClickListener { onNoteClick(note) }
        holder.deleteButton.setOnClickListener { onDeleteClick(note) }
    }

    override fun getItemCount() = notes.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newNotes: List<Note>) {
        this.notes = newNotes
        notifyDataSetChanged()
    }
}

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val lastEdited: Date = Date(),
)