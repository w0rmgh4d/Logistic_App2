package com.example.logistic_app

import android.app.Application
import com.example.logistic_app.utils.CloudinaryHelper
import com.google.firebase.FirebaseApp

class LogisticApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        CloudinaryHelper.init(this)
    }
}
