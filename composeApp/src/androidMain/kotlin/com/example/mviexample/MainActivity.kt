package com.example.mviexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mviexample.features.auth.AuthActivityHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(authRepository = (application as MviApplication).authRepository)
        }
    }

    override fun onResume() {
        super.onResume()
        AuthActivityHolder.current = this
    }

    override fun onPause() {
        super.onPause()
        AuthActivityHolder.current = null
    }
}