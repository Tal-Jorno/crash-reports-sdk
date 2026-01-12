package com.example.android_sdk

import android.content.Context
import android.util.Log

object CrashReporter {

    private var initialized = false

    fun init(context: Context) {
        initialized = true
        Log.d("CrashReporter", "Initialized")
    }

    fun logException(throwable: Throwable) {
        if (!initialized) {
            Log.w("CrashReporter", "Not initialized")
            return
        }
        Log.e("CrashReporter", "Exception caught", throwable)
    }
}
