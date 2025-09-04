package com.convex.mnotifysdk.config

import android.content.Context
import android.util.Log
import com.convex.mnotifysdk.model.MnotifyConfig
import org.json.JSONObject

fun Context.readMNotifyConfig(): MnotifyConfig? {
    val json = GeneratedConfig.JSON ?: return null // safe null check

    return try {
        val jsonObject = JSONObject(json)
        Log.i("Mnotify", "Extracted DATA: $json")

        MnotifyConfig(
            projectId = jsonObject.optString("projectId"),
            packageName = jsonObject.optString("packageName"),
            apiKey = jsonObject.optString("apiKey"),
            authDomain = jsonObject.optString("authDomain"),
            databaseURL = jsonObject.optString("databaseURL"),
            storageBucket = jsonObject.optString("storageBucket"),
            messagingSenderId = jsonObject.optString("messagingSenderId"),
            appId = jsonObject.optString("appId"),
            measurementId = jsonObject.optString("measurementId"),
            fcmAppId = jsonObject.optString("fcmAppId"),
            fcmProjectId = jsonObject.optString("fcmProjectId"),
            fcmApiKey = jsonObject.optString("fcmApiKey"),
            fcmSenderId = jsonObject.optString("fcmSenderId"),
        )
    } catch (e: Exception) {
        Log.e("Mnotify", "Failed to parse config: ${e.message}")
        null
    }
}