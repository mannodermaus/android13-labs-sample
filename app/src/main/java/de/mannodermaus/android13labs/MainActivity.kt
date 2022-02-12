package de.mannodermaus.android13labs

import android.annotation.SuppressLint
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.LocaleList
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.os.BuildCompat
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

        setupPhotoPicker()
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

    private fun setupPhotoPicker() {
        binding.content.buttonPickPhoto.setOnClickListener {
            pickPhotoContract.launch("images/*")
        }
    }

    // Using https://gist.github.com/ianhanniballake/42d8bbf37e6050dd6869229de6606f11
    private val pickPhotoContract = registerForActivityResult(PickImage()) { uri ->
        println("Photo was picked: $uri")
    }
}
