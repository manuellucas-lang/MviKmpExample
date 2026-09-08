package com.example.mviexample.features.auth

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun formatEpochMillis(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))