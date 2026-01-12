package com.example.android_sdk

import android.content.Context
import android.os.Build
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

object CrashReporter {
    private const val TAG = "CrashReporter"
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private var initialized = false
    private var appContext: Context? = null
    private var previousHandler: Thread.UncaughtExceptionHandler? = null
    private var baseUrl: String = "http://10.0.2.2:5000"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .writeTimeout(2, TimeUnit.SECONDS)
        .build()

    fun init(context: Context, apiBaseUrl: String = "http://10.0.2.2:5000") {
        if (initialized) return

        initialized = true
        appContext = context.applicationContext
        baseUrl = apiBaseUrl.trimEnd('/')
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val payload = buildPayload(throwable, isFatal = true, threadName = thread.name)
                sendCrash(payload, isFatal = true)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed while handling crash", t)
            } finally {
                previousHandler?.uncaughtException(thread, throwable)
            }
        }

        Log.i(TAG, "Initialized. baseUrl=$baseUrl")
    }

    fun logException(throwable: Throwable) {
        if (!initialized) {
            Log.w(TAG, "Not initialized")
            return
        }
        val payload = buildPayload(throwable, isFatal = false, threadName = Thread.currentThread().name)
        sendCrash(payload, isFatal = false)
    }

    private fun buildPayload(throwable: Throwable, isFatal: Boolean, threadName: String): JSONObject {
        val ctx = appContext

        val eventId = UUID.randomUUID().toString()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())

        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        val appPackage = ctx?.packageName ?: "unknown"
        val device = "${Build.MANUFACTURER} ${Build.MODEL}"
        val androidVersion = Build.VERSION.RELEASE ?: "unknown"

        return JSONObject().apply {
            put("event_id", eventId)
            put("message", throwable.message ?: throwable.javaClass.name)
            put("stacktrace", stackTrace)
            put("fatal", isFatal)
            put("thread", threadName)
            put("package", appPackage)
            put("device", device)
            put("android_version", androidVersion)
            put("timestamp_client", timestamp)
        }
    }

    private fun sendCrash(payload: JSONObject, isFatal: Boolean) {
        val url = "$baseUrl/crashes"
        val body = payload.toString().toRequestBody(JSON)
        val request = Request.Builder().url(url).post(body).build()

        if (isFatal) {
            // IMPORTANT: don't do network on the crashing (main) thread.
            // Send on background thread and wait briefly so it can finish before process dies.
            val t = Thread {
                try {
                    client.newCall(request).execute().use { response ->
                        Log.e(TAG, "Crash sent (fatal). code=${response.code}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Crash send failed (fatal)", e)
                }
            }
            t.start()
            try {
                t.join(1500)
            } catch (_: InterruptedException) {
            }
        } else {
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    Log.e(TAG, "Crash send failed (nonfatal)", e)
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.use {
                        Log.e(TAG, "Crash sent (nonfatal). code=${it.code}")
                    }
                }
            })
        }

        Log.e(TAG, "Crash captured payload=$payload")
    }
}
