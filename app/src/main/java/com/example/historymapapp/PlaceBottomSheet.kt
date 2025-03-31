package com.example.historymapapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlaceBottomSheet(
    private val title: String,
    private val description: String,
    private val lat: Double,
    private val lon: Double
) : BottomSheetDialogFragment() {

    private lateinit var behavior: BottomSheetBehavior<View> // Poprawione: lateinit, żeby można było później przypisać

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
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                it.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corner_bg)
                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.requestLayout()

                behavior = BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_COLLAPSED
                    peekHeight = 242 // Mały stan (tylko nazwa)
                    halfExpandedRatio = 0.5f // Średni stan (50% ekranu)
                    isFitToContents = false // Musi być false, żeby działał halfExpandedRatio
                    isHideable = true // Można schować
                    skipCollapsed = false // Nie pomijamy małego stanu

                    // Obsługa zmian stanów i animacji
                    addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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
        return dialog
    }
}
