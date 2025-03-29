package com.example.historymapapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlaceBottomSheet(private val title: String, private val description: String) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Znajdujemy widoki w BottomSheet
        val titleTextView = view.findViewById<TextView>(R.id.place_title)
        val descriptionTextView = view.findViewById<TextView>(R.id.place_description)

        // Ustawiamy tekst
        titleTextView.text = title
        descriptionTextView.text = description
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(true) // Możliwość zamknięcia
        }
    }
}
