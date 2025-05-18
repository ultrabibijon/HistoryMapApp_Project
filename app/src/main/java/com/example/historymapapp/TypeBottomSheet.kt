package com.example.historymapapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TypeBottomSheet(
    private val preselectedTypes: Set<String>,
    private val onTypesSelected: (Set<String>) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var behavior: BottomSheetBehavior<View>
    private val selectedTypes = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchBattle = view.findViewById<SwitchCompat>(R.id.switch_battle)
        val switchPolitical = view.findViewById<SwitchCompat>(R.id.switch_political)
        val switchTerrorism = view.findViewById<SwitchCompat>(R.id.switch_terrorism)
        val switchDisaster = view.findViewById<SwitchCompat>(R.id.switch_disaster)
        val switchSport = view.findViewById<SwitchCompat>(R.id.switch_sport)
        val switchScience = view.findViewById<SwitchCompat>(R.id.switch_science)

        // Ustawienie początkowego stanu switchy
        switchBattle.isChecked = preselectedTypes.contains("battle")
        switchPolitical.isChecked = preselectedTypes.contains("political")
        switchTerrorism.isChecked = preselectedTypes.contains("terrorism")
        switchDisaster.isChecked = preselectedTypes.contains("disaster")
        switchSport.isChecked = preselectedTypes.contains("sport")
        switchScience.isChecked = preselectedTypes.contains("science")

        // Listenery dla switchy
        switchBattle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedTypes.add("battle") else selectedTypes.remove("battle")
            onTypesSelected(selectedTypes)
        }

        switchPolitical.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedTypes.add("political") else selectedTypes.remove("political")
            onTypesSelected(selectedTypes)
        }

        switchTerrorism.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedTypes.add("terrorism") else selectedTypes.remove("terrorism")
            onTypesSelected(selectedTypes)
        }

        switchDisaster.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedTypes.add("disaster") else selectedTypes.remove("disaster")
            onTypesSelected(selectedTypes)
        }

        switchSport.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedTypes.add("sport") else selectedTypes.remove("sport")
            onTypesSelected(selectedTypes)
        }

        switchScience.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedTypes.add("science") else selectedTypes.remove("science")
            onTypesSelected(selectedTypes)
        }

        // Inicjalizacja wybranych typów
        selectedTypes.addAll(preselectedTypes)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                it.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bottom_sheet_corner_bg)
                behavior = BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    isHideable = true
                    skipCollapsed = true
                }
            }
        }

        return dialog
    }
}