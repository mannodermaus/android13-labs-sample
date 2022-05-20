package de.mannodermaus.android13labs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BuildCompat

// https://gist.github.com/ianhanniballake/42d8bbf37e6050dd6869229de6606f11

/**
 * Use this [ActivityResultContract] to seamlessly switch between
 * the new [MediaStore.ACTION_PICK_IMAGES] and [Intent.ACTION_GET_CONTENT]
 * based on the availability of the Photo Picker.
 *
 * Use [PickMultipleImages] if you'd like the user to be able to select multiple
 * photos/videos.
 *
 * Input: the mimeType you'd like to receive. This should generally be
 * either `image/\*` or `video/\*` for requesting only images or only videos
 * or can be `\*\/\*` to support both types.
 *
 * Output: the Uri of the chosen image or `null` if no image was selected (i.e.,
 * the user cancelled the request).
 *
 * ```
 * private val pickImage = registerForActivityResult(PickImage()) { photoUri ->
 *   if (photoUri != null) {
 *     processSelectedUri(photoUri)
 *   }
 * }
 * ```
 */
class PickImage : ActivityResultContracts.GetContent() {
    @SuppressLint("UnsafeOptInUsageError")
    override fun createIntent(context: Context, input: String): Intent {
        // Check to see if the ACTION_PICK_IMAGES intent is available
        return if (BuildCompat.isAtLeastT()) {
            Intent(MediaStore.ACTION_PICK_IMAGES)
        } else {
            // For backward compatibility with previous API levels
            super.createIntent(context, input).apply {
                if (input == "*/*") {
                    // Ensure that only images and videos are selectable
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }
            }
        }
    }
}

/**
 * Use this [ActivityResultContract] to seamlessly switch between
 * the new [MediaStore.ACTION_PICK_IMAGES] and [Intent.ACTION_GET_CONTENT]
 * based on the availability of the Photo Picker.
 *
 * Use [PickImage] if you'd like the user to be able to select multiple
 * photos/videos.
 *
 * Input: the mimeType you'd like to receive. This should generally be
 * either `image/\*` or `video/\*` for requesting only images or only videos
 * or can be `\*\/\*` to support both types.
 *
 * Output: a list of Uris representing the chosen images. This list will be empty
 * if the user did not select any image (i.e., the user cancelled the request).
 *
 * ```
 * private val pickImages = registerForActivityResult(PickMultipleImages()) { photoUris ->
 *   processSelectedUris(photoUris)
 * }
 * ```
 */
@BuildCompat.PrereleaseSdkCheck
class PickMultipleImages(
    private val maxImageCount: Int = if (BuildCompat.isAtLeastT()) {
        MediaStore.getPickImagesMaxLimit()
    } else {
        Integer.MAX_VALUE
    }
): ActivityResultContracts.GetMultipleContents() {
    override fun createIntent(context: Context, input: String): Intent {
        // Check to see if the ACTION_PICK_IMAGES intent is available
        if (BuildCompat.isAtLeastT()) {
            return Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                if (input != "*/*") {
                    type = input
                }
                require(maxImageCount > 0) {
                    "Max Image Count must be at least 1"
                }
                if (maxImageCount > 1) {
                    require(maxImageCount <= MediaStore.getPickImagesMaxLimit()) {
                        "Max Image Count must be at most MediaStore.getPickImagesMaxLimit()"
                    }
                    putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxImageCount)
                }
            }
        } else {
            // To maintain compatibility for previous releases
            return super.createIntent(context, input).apply {
                if (input == "*/*") {
                    // Ensure that only images and videos are selectable
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                }
            }
        }
    }
}
