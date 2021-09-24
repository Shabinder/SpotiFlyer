package nl.bravobit.ffmpeg

import android.util.Log

internal object Log {

    private var TAG = FFmpeg::class.java.simpleName
    private var DEBUG = false

    @JvmStatic
    fun setDebug(debug: Boolean) {
        DEBUG = debug
    }

    fun setTag(tag: String) {
        TAG = tag
    }

    @JvmStatic
    fun d(obj: Any?) {
        if (DEBUG) {
            Log.d(TAG, obj?.toString() ?: "")
        }
    }

    @JvmStatic
    fun e(obj: Any?) {
        if (DEBUG) {
            Log.e(TAG, obj?.toString() ?: "")
        }
    }

    @JvmStatic
    fun w(obj: Any?) {
        if (DEBUG) {
            Log.w(TAG, obj?.toString() ?: "")
        }
    }

    @JvmStatic
    fun i(obj: Any?) {
        if (DEBUG) {
            Log.i(TAG, obj?.toString() ?: "")
        }
    }

    @JvmStatic
    fun v(obj: Any?) {
        if (DEBUG) {
            Log.v(TAG, obj?.toString() ?: "")
        }
    }

    @JvmStatic
    fun e(obj: Any?, throwable: Throwable?) {
        if (DEBUG) {
            Log.e(TAG, obj?.toString() ?: "", throwable)
        }
    }

    @JvmStatic
    fun e(throwable: Throwable?) {
        if (DEBUG) {
            Log.e(TAG, "", throwable)
        }
    }
}