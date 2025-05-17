package com.example.historymapapp

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.historymapapp.databinding.SettingsActivityBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Pobierz zapisany motyw PRZED super.onCreate
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt("theme_mode", 0)
        when (themeMode) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ustaw RadioButton na podstawie zapisanych ustawień
        when (themeMode) {
            0 -> binding.radioThemeSystem.isChecked = true
            1 -> binding.radioThemeLight.isChecked = true
            2 -> binding.radioThemeDark.isChecked = true
        }

        // Obsługa zmiany motywu
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val editor = sharedPreferences.edit()
            when (checkedId) {
                binding.radioThemeSystem.id -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    editor.putInt("theme_mode", 0)
                }
                binding.radioThemeLight.id -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    editor.putInt("theme_mode", 1)
                }
                binding.radioThemeDark.id -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    editor.putInt("theme_mode", 2)
                }
            }
            editor.apply()
            // Odśwież aktywność żeby natychmiast zastosować motyw
            recreate()
        }

        // Obsługa powrotu
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}
