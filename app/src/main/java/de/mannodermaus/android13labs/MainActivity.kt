package de.mannodermaus.android13labs

import android.Manifest
import android.annotation.SuppressLint
import android.app.LocaleManager
import android.content.Intent
import android.content.IntentFilter
import android.graphics.text.LineBreakConfig.LINE_BREAK_WORD_STYLE_NONE
import android.graphics.text.LineBreakConfig.LINE_BREAK_WORD_STYLE_PHRASE
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.content.getSystemService
import androidx.core.os.BuildCompat
import androidx.core.view.WindowCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import de.mannodermaus.android13labs.databinding.ActivityMainBinding
import java.util.*

private val supportedLocales = listOf(
    Locale.forLanguageTag("en-US"),
    Locale.forLanguageTag("ja-JP"),
)

private const val notificationChannel = "example"

@SuppressLint("UnsafeOptInUsageError", "ObsoleteSdkInt")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.content.checkboxPhraseWrapping.setOnCheckedChangeListener { _, enabled ->
            updateLoremIpsumTextWrapping(enabled)
        }

        setupLanguagePicker()

        setupPhotoPicker()

        binding.content.buttonPostNotification.setOnClickListener {
            tryShowNotification()
        }

        binding.content.buttonRevokePermission.setOnClickListener { revokeNotificationPermission() }

        // Foreground service management
        binding.content.buttonStartService.setOnClickListener { startFGService() }
        binding.content.buttonStopService.setOnClickListener { stopFGService() }
    }

    private val broadcastReceiver = AmazingBroadcastReceiver()
    private val broadcastIntentFilter = IntentFilter(Intent.ACTION_TIME_TICK)

    override fun onResume() {
        super.onResume()

        if (BuildCompat.isAtLeastT()) {
            registerReceiver(broadcastReceiver, broadcastIntentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(broadcastReceiver, broadcastIntentFilter)
        }
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(broadcastReceiver)
    }

    private fun updateLoremIpsumTextWrapping(enabled: Boolean) {
        if (BuildCompat.isAtLeastT()) {
            // Grab current line break config and update the word style
            binding.content.textLoremIpsum.lineBreakWordStyle = if (enabled) {
                LINE_BREAK_WORD_STYLE_PHRASE
            } else {
                LINE_BREAK_WORD_STYLE_NONE
            }
        }
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

    private val pickPhotoContract = registerForActivityResult(PickImage()) { uri ->
        println("Photo was picked: $uri")
    }

    private fun tryShowNotification() {
        // Ensure channel's existence
        val manager = NotificationManagerCompat.from(this)
        if (manager.getNotificationChannel(notificationChannel) == null) {
            manager.createNotificationChannel(
                NotificationChannelCompat.Builder(notificationChannel, IMPORTANCE_DEFAULT)
                    .setName("Example")
                    .build()
            )
        }

        println("Try to post a notification. enabled=${manager.areNotificationsEnabled()}")
        if (manager.areNotificationsEnabled()) {
            showNotification()
        } else {
            permissionContract.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun revokeNotificationPermission() {
        if (BuildCompat.isAtLeastT()) {
            try {
                println("Revoking notification permission on kill...")
                revokeSelfPermissionOnKill(Manifest.permission.POST_NOTIFICATIONS)
            } catch (e: SecurityException) {
                println("No need to revoke permission, I didn't even have it in the first place")
            }
        }
    }

    private fun showNotification() {
        NotificationManagerCompat.from(this).notify(
            /* id = */ 1234,
            /* notification = */ NotificationCompat.Builder(this, notificationChannel)
                .setContentTitle("Notification Title")
                .setContentText("Notification Text")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        )
    }

    private val permissionContract = registerForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            println("Notification Permission was granted!")
            showNotification()
        } else {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (showRationale) {
                // First denial; explain why it would be good to have the permission
                println("Notification Permission was denied once")
            } else {
                // Second denial, basically "Never ask again"
                println("Notification Permission was denied repeatedly ('never ask again')")

                MaterialAlertDialogBuilder(this)
                    .setTitle("OMG how could you!?")
                    .setMessage("Notifications are permanently disabled - please switch them on in Settings!")
                    .setPositiveButton("Take me there") { _, _ -> launchNotificationSettings() }
                    .setNegativeButton("No thanks") { _, _ -> }
                    .show()
            }
        }
    }

    private fun launchNotificationSettings() {
        // Depending on OS version, launch the notification settings in a different way
        Intent().apply {
            when {
                Build.VERSION.SDK_INT >= 26 -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                Build.VERSION.SDK_INT >= 21 -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", packageName)
                    putExtra("app_uid", applicationInfo.uid)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:$packageName")
                }
            }

            startActivity(this)
        }
    }

    private fun startFGService() {
        startForegroundService(applicationContext, Intent(this, AmazingService::class.java))
    }

    private fun stopFGService() {
        stopService(Intent(this, AmazingService::class.java))
    }
}
