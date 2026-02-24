package com.example.logistic_app

import android.app.Application
import com.example.logistic_app.utils.CloudinaryHelper
import com.google.firebase.FirebaseApp
import org.osmdroid.config.Configuration
import java.io.File

class LogisticApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        CloudinaryHelper.init(this)

        // Initialize OSMDroid configuration
        Configuration.getInstance().apply {
            userAgentValue = packageName
            val tileCache = File(cacheDir, "osmdroid")
            if (!tileCache.exists()) {
                tileCache.mkdirs()
            }
            osmdroidTileCache = tileCache
        }
    }
}
