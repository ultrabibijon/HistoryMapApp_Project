package com.example.historymapapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_activity_editor)

        titleEditText = findViewById(R.id.edit_title)
        contentEditText = findViewById(R.id.edit_content)

        val noteId = intent.getStringExtra("note_id")
        if (noteId != null) {
            titleEditText.setText(intent.getStringExtra("note_title"))
            contentEditText.setText(intent.getStringExtra("note_content"))
        }

        findViewById<Button>(R.id.button_save).setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Tytuł i treść nie mogą być puste", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = noteId ?: UUID.randomUUID().toString()
            saveNote(id, title, content)
        }
    }

    private fun saveNote(id: String, title: String, content: String) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val note = Note(id, title, content, Date())

        Firebase.database("https://historymapapp-2e0cb-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("notes").child(userId).child(id)
            .setValue(note)
            .addOnSuccessListener {
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Błąd podczas zapisu", Toast.LENGTH_SHORT).show()
            }
    }

}