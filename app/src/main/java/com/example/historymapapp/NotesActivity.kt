package com.example.historymapapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NotesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notes_activity)

        findViewById<ImageButton>(R.id.button_back).setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recycler_notes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = NotesAdapter(
            emptyList(),
            onNoteClick = { note ->
                val intent = Intent(this, NoteEditorActivity::class.java).apply {
                    putExtra("note_id", note.id)
                    putExtra("note_title", note.title)
                    putExtra("note_content", note.content)
                }
                startActivity(intent)
            },
            onDeleteClick = { note ->
                AlertDialog.Builder(this)
                    .setTitle("Delete note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteNote(note.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

        )
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.button_add_note).setOnClickListener {
            startActivity(Intent(this, NoteEditorActivity::class.java))
        }

        loadNotes()
    }

    private fun loadNotes() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        Firebase.database("https://historymapapp-2e0cb-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("notes").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notes = mutableListOf<Note>()
                    for (child in snapshot.children) {
                        child.getValue(Note::class.java)?.let { notes.add(it) }
                    }
                    adapter.updateData(notes.sortedByDescending { it.lastEdited })
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun deleteNote(noteId: String) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        Firebase.database("https://historymapapp-2e0cb-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("notes").child(userId).child(noteId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Błąd przy usuwaniu", Toast.LENGTH_SHORT).show()
            }
    }

}