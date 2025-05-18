package com.example.historymapapp

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.historymapapp.databinding.SettingsActivityBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: SettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

        // Initialize adapter with theme setting
        val settingsList = listOf(
            SettingsAdapter.SettingItem(
                id = 1,
                title = getString(R.string.theme_settings),
                type = SettingsAdapter.SettingType.THEME_SELECTION
            )
        )

        adapter = SettingsAdapter(
            settings = settingsList,
            sharedPreferences = sharedPreferences,
            onThemeChanged = { recreate() } // Refresh activity when theme changes
        )

        binding.recyclerSettings.apply {
            layoutManager = LinearLayoutManager(this@SettingsActivity)
            adapter = this@SettingsActivity.adapter
            setHasFixedSize(true)
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}