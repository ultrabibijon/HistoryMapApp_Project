package com.example.historymapapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.historymapapp.databinding.ActivityTutorialBinding
import com.google.android.material.tabs.TabLayoutMediator

class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding
    private lateinit var adapter: TutorialAdapter

    private val slides = listOf(
        TutorialSlide(
            title       = "Wyszukuj wydarzenia",
            description = "Znajdź dowolne wydarzenie w pasku wyszukiwania.",
            imageRes    = R.drawable.ic_search
        ),
        TutorialSlide(
            title       = "Filtruj według rodzaju",
            description = "Bitwy, Polityka, Katastrofy, Sport, Nauka…",
            imageRes    = R.drawable.ic_filter
        ),
        TutorialSlide(
            title       = "Filtruj według epoki",
            description = "Starożytność, średniowiecze, historia nowożytna.",
            imageRes    = R.drawable.ic_ancient_epoch
        ),
        TutorialSlide(
            title       = "Dodawaj do ulubionych",
            description = "Zapisuj najciekawsze miejsca i wracaj do nich później.",
            imageRes    = R.drawable.ic_favorite_empty
        ),
        TutorialSlide(
            title       = "Notatki",
            description = "Twórz własne notatki do wydarzeń.",
            imageRes    = R.drawable.ic_notes
        ),
        TutorialSlide(
            title       = "Ostatnio przeglądane",
            description = "Szybko wracaj do ostatnio odwiedzonych miejsc.",
            imageRes    = R.drawable.ic_history
        ),
        TutorialSlide(
            title       = "Dostosuj motyw",
            description = "Wybierz jasny, ciemny lub systemowy motyw.",
            imageRes    = R.drawable.ic_settings
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewPager + adapter
        adapter = TutorialAdapter(slides)
        binding.viewPager.adapter = adapter

        // (Opcjonalne) Kropki pod ViewPagerem
       // TabLayoutMediator(binding.tabDots, binding.viewPager) { _, _ -> }.attach()

        // Skip
        binding.skipButton.setOnClickListener { finishTutorial() }

        // Dalej / Start
        binding.nextButton.setOnClickListener {
            val next = binding.viewPager.currentItem + 1
            if (next < slides.size) {
                binding.viewPager.currentItem = next
            } else {
                finishTutorial()
            }
        }

        // Zmiana tekstu przycisku na ostatnim slajdzie
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val resId = if (position == slides.lastIndex) R.string.start else R.string.next
                binding.nextButton.text = getString(resId)
            }
        })
    }

    private fun finishTutorial() {

        // (jeśli kiedyś chcesz zapamiętać „tylko raz”, tu zapisz tutorial_shown=true)

        val intent = Intent(this, MainActivity::class.java)
            .putExtra("skipTutorial", true)   // ❷ powiedz MainActivity: „nie odpalaj mnie znowu”
        startActivity(intent)
        finish()
    }

}
