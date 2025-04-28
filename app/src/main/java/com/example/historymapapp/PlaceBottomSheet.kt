package com.example.historymapapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.net.toUri

class PlaceBottomSheet(
    private val title: String,
    private val description: String,
    private val imageURL: String,
    private val wikiURL: String,
    private val lat: Double,
    private val lon: Double
) : BottomSheetDialogFragment() {

    private lateinit var behavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.bottom_sheet_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.place_title)
        val descriptionTextView = view.findViewById<TextView>(R.id.place_description)
        val imageView = view.findViewById<ImageView>(R.id.place_image)
        val wikiLabelTextView = view.findViewById<TextView>(R.id.wikipedia_label)
        val wikiTextView = view.findViewById<TextView>(R.id.place_wikipedia)

        titleTextView.text = title
        descriptionTextView.text = description

        Glide.with(requireContext())
            .load(imageURL)
            //.placeholder(R.drawable.placeholder_image)
            .into(imageView)

        wikiTextView.text = wikiURL
        wikiTextView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, wikiURL.toUri())
            startActivity(intent)
        }
        wikiLabelTextView.text = "Learn more about $title:"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                it.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bottom_sheet_corner_bg)
                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.requestLayout()

                behavior = BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_COLLAPSED
                    peekHeight = 242
                    halfExpandedRatio = 0.5f
                    isFitToContents = false
                    isHideable = true
                    skipCollapsed = false

                    addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                                dismiss()
                            }
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        }
                    })
                }
            }
        }
        return dialog
    }
}
