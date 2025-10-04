package com.example.haptix.util

import android.util.Log

/**
 * Simple logging utility for the Haptix app
 */
object Logger {
    private const val TAG = "Haptix"
    
    fun d(message: String) {
        Log.d(TAG, message)
    }
    
    fun i(message: String) {
        Log.i(TAG, message)
    }
    
    fun w(message: String) {
        Log.w(TAG, message)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}



