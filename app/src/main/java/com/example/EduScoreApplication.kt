package com.example

import android.app.Application
import com.example.data.AppData

class EduScoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppData.initDatabase(this)
    }
}
