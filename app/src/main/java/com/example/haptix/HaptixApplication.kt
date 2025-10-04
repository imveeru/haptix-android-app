package com.example.haptix

import android.app.Application
import com.example.haptix.util.LogUtils

/**
 * Application class for Haptix
 */
class HaptixApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        LogUtils.d("Haptix application started")
    }
}
