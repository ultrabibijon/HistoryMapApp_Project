package com.example.historymapapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)

        findViewById<ImageButton>(R.id.button_back).setOnClickListener {
            finish()
        }

        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            findViewById<TextView>(R.id.app_version).text = "Wersja $version"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        findViewById<TextView>(R.id.privacy_policy).setOnClickListener {

            val privacyText = """
# Polityka Prywatności HistoryMap

**Ostatnia aktualizacja: 07.05.2025**

## 1. Informacje ogólne

Aplikacja HistoryMap ("My", "Nasza") szanuje Twoją prywatność. Niniejsza polityka wyjaśnia jakie dane zbieramy i jak je wykorzystujemy.

## 2. Gromadzone dane

### 2.1 Dane użytkownika
- Podczas korzystania z aplikacji w trybie anonimowym zbieramy podstawowe informacje o urządzeniu (model, wersja systemu)

### 2.2 Dane użytkowania
- Lokalizacje wyświetlanych markerów
- Wybrane filtry i preferencje
- Czas spędzony w aplikacji

## 3. Jak wykorzystujemy dane

- Udoskonalanie działania aplikacji
- Personalizacja wyświetlanych treści
- Analiza popularności różnych wydarzeń historycznych
- Bezpieczne przechowywanie Twoich ulubionych i historii przeglądania

## 4. Udostępnianie danych

Nie sprzedajemy ani nie udostępniamy Twoich danych osobom trzecim, z wyjątkiem:
- Dostawców usług (Firebase, Google Cloud) niezbędnych do działania aplikacji
- Wymogów prawnych (na żądanie organów ścigania)

## 5. Bezpieczeństwo danych

Stosujemy środki bezpieczeństwa:
- Szyfrowanie transmisji (SSL)
- Bezpieczne serwery Firebase
- Ograniczony dostęp do danych

## 6. Twoje prawa

Masz prawo do:
- Dostępu do swoich danych
- Sprostowania danych
- Usunięcia konta i danych
- Wniesienia sprzeciwu wobec przetwarzania

## 7. Pliki cookies i śledzenie

Aplikacja używa:
- Lokalnego przechowywania danych
- Cookies Firebase do autentykacji

## 8. Zmiany w polityce

Wszelkie zmiany będą publikowane w tej sekcji. Kontynuując korzystanie z aplikacji po zmianach, akceptujesz nową politykę.

## 9. Kontakt

W sprawach prywatności prosimy o kontakt:
- Email: vpapanov@st.swps.edu.pl

---

**Dziękujemy za korzystanie z HistoryMap!**
""".trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Polityka Prywatności")
                .setMessage(privacyText)
                .setPositiveButton("Rozumiem") { dialog, _ -> dialog.dismiss() }
                .create()
                .apply {
                    setOnShowListener {
                        getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                            setTextColor(Color.BLACK)
                        }
                    }
                    show()
                }

        }

        setupLibraryLinks()
    }

    private fun setupLibraryLinks() {
        val links = mapOf(
            R.id.link_osmdroid to "https://github.com/osmdroid/osmdroid",
            R.id.link_glide to "https://github.com/bumptech/glide",
            R.id.link_gson to "https://github.com/google/gson",
            R.id.link_firebase to "https://firebase.google.com/"
        )

        links.forEach { (id, url) ->
            findViewById<TextView>(id).setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Nie można otworzyć linku", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}