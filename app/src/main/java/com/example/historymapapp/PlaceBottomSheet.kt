package com.example.historymapapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlaceBottomSheet(private val title: String,
                       private val description: String,
                       private val lat: Double,
                       private val lon: Double,
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetStyle) // Ustaw styl
    }
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
        return BottomSheetDialog(requireContext(), theme).apply {
            setOnShowListener { dialog ->
                val bottomSheet = (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED // Startowy stan
                    behavior.peekHeight = 400 // Wysokość po otwarciu

                    // Rozwijanie do pełnego ekranu po przesunięciu w górę
                    behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                                dismiss() // Zamknięcie po całkowitym schowaniu
                            }
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            // Tu można dodać animacje lub zmiany UI
                        }
                    })
                }
            }
        }
    }
}
