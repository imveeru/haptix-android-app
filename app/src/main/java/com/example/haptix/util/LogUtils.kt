package com.example.haptix.util

import android.util.Log

/**
 * Utility class for logging
 */
object LogUtils {
    private const val TAG = "Haptix"
    
    fun d(message: String) {
        Log.d(TAG, message)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    fun i(message: String) {
        Log.i(TAG, message)
    }
    
    fun w(message: String, throwable: Throwable? = null) {
        Log.w(TAG, message, throwable)
    }
}



