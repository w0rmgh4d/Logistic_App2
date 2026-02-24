package com.example.logistic_app.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryHelper {
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        try {
            // Check if already initialized by attempting a 'get'
            MediaManager.get()
            isInitialized = true
        } catch (e: Exception) {
            val config = mapOf(
                "cloud_name" to "dm61xnbqz",
                "api_key" to "118988279184219",
                "api_secret" to "xhAodgtVe-D0FR-MSwI4krRukew"
            )
            MediaManager.init(context, config)
            isInitialized = true
        }
    }

    suspend fun uploadImage(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        try {
            MediaManager.get().upload(uri)
                // Using the specific unsigned preset from your web admin
                .unsigned("AFP-Logistics")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val url = resultData?.get("secure_url") as? String ?: ""
                        continuation.resume(url)
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        continuation.resumeWithException(Exception(error?.description ?: "Upload Failed"))
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}
