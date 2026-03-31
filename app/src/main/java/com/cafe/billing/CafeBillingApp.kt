package com.cafe.billing

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// ============================================================
// APPLICATION CLASS
// @HiltAndroidApp triggers Hilt's code generation and sets up
// the application-level dependency injection container.
// This MUST be declared in AndroidManifest.xml as android:name=".CafeBillingApp"
// ============================================================

@HiltAndroidApp
class CafeBillingApp : Application()
