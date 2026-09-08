package com.example.mviexample.features.auth

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

/** Seconds between the Unix epoch (1970-01-01) and Cocoa's reference date (2001-01-01). */
private const val UNIX_EPOCH_TO_COCOA_REFERENCE = 978_307_200.0

/** Formats an epoch-millis timestamp using the device locale ("medium" date, "short" time). */
actual fun formatEpochMillis(millis: Long): String {
    val formatter = NSDateFormatter()
    formatter.dateStyle = NSDateFormatterMediumStyle
    formatter.timeStyle = NSDateFormatterShortStyle
    formatter.locale = NSLocale.currentLocale
    val date = NSDate(timeIntervalSinceReferenceDate = millis / 1000.0 + UNIX_EPOCH_TO_COCOA_REFERENCE)
    return formatter.stringFromDate(date)
}