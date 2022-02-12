package de.mannodermaus.android13labs

import android.annotation.SuppressLint
import android.app.LocaleManager
import android.os.Bundle
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.WindowCompat
import com.google.android.material.radiobutton.MaterialRadioButton
import de.mannodermaus.android13labs.databinding.ActivityMainBinding
import java.util.*

private val supportedLocales = listOf(
    Locale.forLanguageTag("en-US"),
    Locale.forLanguageTag("ja-JP"),
)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupLanguagePicker()
    }

    // Only works on API 33+; AndroidX version is supposedly on its way
    @SuppressLint("NewApi")
    private fun setupLanguagePicker() {
        with(binding.content.radioGroup) {
            // Clear previous content
            removeAllViews()

            // Find initial selection via new LocaleManager
            val localeManager = getSystemService<LocaleManager>()!!
            val currentLocale = localeManager.applicationLocales[0] ?: Locale.getDefault()

            // Add button for each supported language
            supportedLocales.forEachIndexed { index, locale ->
                addView(MaterialRadioButton(context).apply {
                    id = index
                    text = locale.toString()
                    isChecked = currentLocale.language == locale.language
                })
            }

            // Add listener to update language preference via LocaleManager
            // To reset to system default, set it to 'LocaleList.getEmptyLocaleList()'
            setOnCheckedChangeListener { _, i ->
                localeManager.applicationLocales = LocaleList(supportedLocales[i])
            }
        }
    }
}
