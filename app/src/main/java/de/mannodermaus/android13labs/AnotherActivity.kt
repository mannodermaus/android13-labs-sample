package de.mannodermaus.android13labs

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.mannodermaus.android13labs.databinding.ActivityAnotherBinding

class AnotherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnotherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityAnotherBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)

        val callback = onBackPressedDispatcher.addCallback(this, enabled = false) {
            showConfirmDialog()
        }

        binding.inputField.doAfterTextChanged { text ->
            callback.isEnabled = !text.isNullOrEmpty()
        }
    }

    private fun showConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm")
            .setMessage("Are you sure you want to go back?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No") { d, _ -> d.dismiss() }
            .show()
    }
}
