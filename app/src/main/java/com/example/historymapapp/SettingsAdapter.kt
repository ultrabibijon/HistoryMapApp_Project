package com.example.historymapapp

import android.content.SharedPreferences
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.isVisible
import androidx.core.content.edit

class SettingsAdapter(
    private val settings: List<SettingItem>,
    private val sharedPreferences: SharedPreferences,
    private val onThemeChanged: () -> Unit
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    data class SettingItem(
        val id: Int,
        val title: String,
        val type: SettingType
    )

    enum class SettingType {
        THEME_SELECTION
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardView: CardView = view.findViewById(R.id.card_view)
        private val icon: ImageView = view.findViewById(R.id.image_icon)
        private val title: TextView = view.findViewById(R.id.text_title)
        private val value: TextView = view.findViewById(R.id.text_value)
        private val expandButton: ImageButton = view.findViewById(R.id.button_expand)
        private val optionsLayout: LinearLayout = view.findViewById(R.id.theme_options_layout)
        private val radioGroup: RadioGroup = view.findViewById(R.id.theme_radio_group)

        fun bind(item: SettingItem) {
            when (item.type) {
                SettingType.THEME_SELECTION -> bindThemeSetting()
            }
        }

        private fun bindThemeSetting() {
            title.text = itemView.context.getString(R.string.theme_settings)
            icon.setImageResource(R.drawable.ic_dark_mode)

            val themeMode = sharedPreferences.getInt("theme_mode", 0)
            val themeText = when (themeMode) {
                1 -> itemView.context.getString(R.string.theme_light)
                2 -> itemView.context.getString(R.string.theme_dark)
                else -> itemView.context.getString(R.string.theme_system)
            }
            value.text = themeText

            radioGroup.check(
                when (themeMode) {
                    1 -> R.id.radio_theme_light
                    2 -> R.id.radio_theme_dark
                    else -> R.id.radio_theme_system
                }
            )

            expandButton.setOnClickListener {
                val isExpanded = optionsLayout.isVisible
                TransitionManager.beginDelayedTransition(cardView)
                optionsLayout.visibility = if (isExpanded) View.GONE else View.VISIBLE
                expandButton.setImageResource(
                    if (isExpanded) R.drawable.ic_expand_more
                    else R.drawable.ic_expand_less
                )
            }

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                val (mode, text) = when (checkedId) {
                    R.id.radio_theme_light -> Pair(1, itemView.context.getString(R.string.theme_light))
                    R.id.radio_theme_dark -> Pair(2, itemView.context.getString(R.string.theme_dark))
                    else -> Pair(0, itemView.context.getString(R.string.theme_system))
                }
                value.text = text
                saveThemePreference(mode)
                onThemeChanged()
            }
        }

        private fun saveThemePreference(themeMode: Int) {
            sharedPreferences.edit {
                putInt("theme_mode", themeMode)
            }

            AppCompatDelegate.setDefaultNightMode(
                when (themeMode) {
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    2 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setting_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(settings[position])
    }

    override fun getItemCount() = settings.size
}