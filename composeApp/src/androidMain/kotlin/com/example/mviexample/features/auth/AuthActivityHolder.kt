package com.example.mviexample.features.auth

import android.app.Activity

/**
 * Holds the currently resumed [Activity] so the Credential Manager (Google Sign-In)
 * can launch its UI from a non-UI layer. Set from [android.app.Activity.onResume]
 * and cleared from [android.app.Activity.onPause].
 */
object AuthActivityHolder {
    @Volatile
    var current: Activity? = null
}