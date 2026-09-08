package com.example.mviexample.features.auth

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * Holds the currently resumed [Activity] so the Credential Manager (Google Sign-In)
 * can launch its UI from a non-UI layer. Set from [android.app.Activity.onResume]
 * and cleared from [android.app.Activity.onPause].
 *
 * The activity is held through a [WeakReference] so a static (singleton) holder never
 * leaks the activity/window. While the activity is resumed the system keeps a strong
 * reference to it, so it is always available for the Credential Manager flow.
 */
object AuthActivityHolder {
    @Volatile
    private var currentRef: WeakReference<Activity>? = null

    var current: Activity?
        get() = currentRef?.get()
        set(value) {
            currentRef = value?.let(::WeakReference)
        }
}
