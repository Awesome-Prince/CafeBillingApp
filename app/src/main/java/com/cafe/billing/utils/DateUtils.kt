package com.cafe.billing.utils

import java.text.SimpleDateFormat
import java.util.*

// ============================================================
// DATE UTILITIES
// Shared formatting helpers used across all screens.
// ============================================================

object DateUtils {

    private val fullFormatter   = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateFormatter   = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormatter   = SimpleDateFormat("hh:mm a", Locale.getDefault())

    /** e.g. "25 Dec 2024, 02:30 PM" */
    fun formatFull(timestamp: Long): String =
        fullFormatter.format(Date(timestamp))

    /** e.g. "25 Dec 2024" */
    fun formatDate(timestamp: Long): String =
        dateFormatter.format(Date(timestamp))

    /** e.g. "02:30 PM" */
    fun formatTime(timestamp: Long): String =
        timeFormatter.format(Date(timestamp))

    /** e.g. "₹1,250.00" */
    fun formatCurrency(amount: Double): String =
        "₹${"%.2f".format(amount)}"
}
