package com.example.viralclicker

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Delay AdMob init to avoid blocking app startup
        Handler(Looper.getMainLooper()).postDelayed({
            Thread { MobileAds.initialize(this) }.start()
        }, 2000)
    }
}
