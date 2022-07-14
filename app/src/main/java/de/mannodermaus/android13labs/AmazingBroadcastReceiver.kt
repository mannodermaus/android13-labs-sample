package de.mannodermaus.android13labs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AmazingBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        println("AmazingBroadcastReceiver received something! $intent")
    }
}
