package de.mannodermaus.android13labs

import android.annotation.SuppressLint
import android.app.LocaleManager
import android.os.Bundle
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
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

        setupPhotoPicker()

        setupNotificationPoster()
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

    private fun setupNotificationPoster() {
        binding.content.buttonPostNotification.setOnClickListener {
            val channelId = "example"

            // Ensure channel's existence
            val manager = NotificationManagerCompat.from(this)
            if (manager.getNotificationChannel(channelId) == null) {
                manager.createNotificationChannel(
                    NotificationChannelCompat.Builder(channelId, IMPORTANCE_DEFAULT)
                        .setName("Example")
                        .build()
                )
            }

            manager.notify(
                /* id = */ 1234,
                /* notification = */ NotificationCompat.Builder(this, channelId)
                    .setContentTitle("Notification Title")
                    .setContentText("Notification Text")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build()
            )
        }
    }
}
