package com.gabriel.cal

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa Firebase
        FirebaseApp.initializeApp(this)
    }
}
