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
            findViewById<TextView>(R.id.app_version).text = "Version $version"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        findViewById<TextView>(R.id.privacy_policy).setOnClickListener {

            val privacyText = """
# HistoryMap Privacy Policy

**Last updated: 2025-05-18**

## 1. General Information

The HistoryMap app ("We", "Our") respects your privacy. This policy explains what data we collect and how we use it.

## 2. Collected Data

### 2.1 User Data
- When using the app anonymously, we collect basic device information (model, system version)

### 2.2 Usage Data
- Locations of displayed markers
- Selected filters, preferences, and settings
- Saved notes
- Time spent in the app

## 3. How We Use the Data

- Improving app functionality
- Personalizing displayed content
- Analyzing the popularity of historical events
- Securely storing your favorites and browsing history

## 4. Data Sharing

We do not sell or share your data with third parties, except for:
- Service providers (Firebase, Google Cloud) necessary for app functionality
- Legal requirements (upon request by law enforcement)

## 5. Data Security

We implement security measures:
- Encrypted transmission (SSL)
- Secure Firebase servers
- Limited access to data

## 6. Your Rights

You have the right to:
- Access your data
- Correct your data
- Delete your account and data
- Object to data processing

## 7. Cookies and Tracking

The app uses:
- Local data storage
- Firebase cookies for authentication

## 8. Changes to the Policy

Any changes will be published in this section. By continuing to use the app after updates, you accept the new policy.

## 9. Contact

For privacy-related matters, please contact:
- Email: vpapanov@st.swps.edu.pl

---

**Thank you for using HistoryMap!**
""".trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage(privacyText)
                .setPositiveButton("I understand") { dialog, _ -> dialog.dismiss() }
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
                    Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}