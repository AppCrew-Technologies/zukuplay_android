package dev.anilbeesetti.nextplayer.core.common

import android.content.Context
import android.util.Log
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized error handling to prevent app crashes and provide better error reporting
 */
@Singleton
class ErrorHandler @Inject constructor() {
    
    companion object {
        private const val TAG = "ErrorHandler"
    }
    
    /**
     * Safely execute a function with error handling
     */
    inline fun <T> safeExecute(
        operation: String,
        defaultValue: T,
        crossinline block: () -> T
    ): T {
        return try {
            block()
        } catch (e: Exception) {
            Timber.e(e, "Error in operation: $operation")
            defaultValue
        }
    }
    
    /**
     * Safely execute a function that returns nullable
     */
    inline fun <T> safeExecuteNullable(
        operation: String,
        crossinline block: () -> T?
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            Timber.e(e, "Error in operation: $operation")
            null
        }
    }
    
    /**
     * Safely execute a function that returns Unit
     */
    inline fun safeExecuteVoid(
        operation: String,
        crossinline block: () -> Unit
    ) {
        try {
            block()
        } catch (e: Exception) {
            Timber.e(e, "Error in operation: $operation")
        }
    }
    
    /**
     * Handle critical errors that could cause app crashes
     */
    fun handleCriticalError(
        error: Throwable,
        context: String,
        additionalInfo: String? = null
    ) {
        val errorMessage = buildString {
            append("Critical error in: $context")
            if (!additionalInfo.isNullOrBlank()) {
                append(" - $additionalInfo")
            }
        }
        
        Timber.e(error, errorMessage)
        Log.e(TAG, errorMessage, error)
        
        // Here you could add crash reporting service integration
        // Firebase Crashlytics, Bugsnag, etc.
    }
    
    /**
     * Handle non-critical errors
     */
    fun handleNonCriticalError(
        error: Throwable,
        context: String,
        additionalInfo: String? = null
    ) {
        val errorMessage = buildString {
            append("Non-critical error in: $context")
            if (!additionalInfo.isNullOrBlank()) {
                append(" - $additionalInfo")
            }
        }
        
        Timber.w(error, errorMessage)
        Log.w(TAG, errorMessage, error)
    }
    
    /**
     * Check if an error is recoverable
     */
    fun isRecoverableError(error: Throwable): Boolean {
        return when (error) {
            is OutOfMemoryError -> false
            is StackOverflowError -> false
            is SecurityException -> true
            is IllegalArgumentException -> true
            is IllegalStateException -> true
            else -> true
        }
    }
}
