package com.example.medicitas

import android.app.Application
import com.google.firebase.FirebaseApp

class MedicitasApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}