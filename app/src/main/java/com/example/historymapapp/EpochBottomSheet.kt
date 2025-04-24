package com.example.historymapapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EpochBottomSheet(
    private val preselectedEpochs: Set<String>,
    private val onEpochSelected: (Set<String>) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var behavior: BottomSheetBehavior<View>
    private val selectedEpochs = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.bottom_sheet_epoch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.epoch_title)
        val ancientButton = view.findViewById<ImageButton>(R.id.btn_ancient)
        val middleButton = view.findViewById<ImageButton>(R.id.btn_middle)
        val modernButton = view.findViewById<ImageButton>(R.id.btn_modern)

        titleTextView.text = "Choosing an epoch"

        ancientButton.setOnClickListener {
            toggleEpochSelection("Ancient", ancientButton)
        }

        middleButton.setOnClickListener {
            toggleEpochSelection("Middle", middleButton)
        }

        modernButton.setOnClickListener {
            toggleEpochSelection("Modern", modernButton)
        }

        selectedEpochs.addAll(preselectedEpochs)

        if (selectedEpochs.contains("Ancient")) {
            ancientButton.setBackgroundResource(R.drawable.epoch_button_selected)
        }
        if (selectedEpochs.contains("Middle")) {
            middleButton.setBackgroundResource(R.drawable.epoch_button_selected)
        }
        if (selectedEpochs.contains("Modern")) {
            modernButton.setBackgroundResource(R.drawable.epoch_button_selected)
        }
    }

    private fun toggleEpochSelection(epoch: String, button: ImageButton) {
        if (selectedEpochs.contains(epoch)) {
            selectedEpochs.remove(epoch)
            button.setBackgroundResource(R.drawable.epoch_button_unselected)
        } else {
            selectedEpochs.add(epoch)
            button.setBackgroundResource(R.drawable.epoch_button_selected)
        }

        onEpochSelected(selectedEpochs)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                it.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bottom_sheet_corner_bg)
                val screenHeight = resources.displayMetrics.heightPixels
                val halfScreenHeight = (screenHeight * 0.5).toInt()
                it.layoutParams.height = halfScreenHeight
                it.requestLayout()

                behavior = BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_COLLAPSED
                    peekHeight = halfScreenHeight
                    isHideable = true
                    skipCollapsed = false
                    isFitToContents = true

                    addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                                this@EpochBottomSheet.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            }
                            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                                dismiss()
                            }
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            if (slideOffset > 0.0f) {
                                this@EpochBottomSheet.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            }
                        }
                    })
                }
            }
        }

        return dialog
    }

}
