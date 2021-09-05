package nl.bravobit.ffmpeg;

class Log {

    private static String TAG = FFmpeg.class.getSimpleName();
    private static boolean DEBUG = false;

    public static void setDebug(boolean debug) {
        Log.DEBUG = debug;
    }

    public static void setTag(String tag) {
        Log.TAG = tag;
    }

    static void d(Object obj) {
        if (DEBUG) {
            android.util.Log.d(TAG, obj != null ? obj.toString() : "");
        }
    }

    static void e(Object obj) {
        if (DEBUG) {
            android.util.Log.e(TAG, obj != null ? obj.toString() : "");
        }
    }

    static void w(Object obj) {
        if (DEBUG) {
            android.util.Log.w(TAG, obj != null ? obj.toString() : "");
        }
    }

    static void i(Object obj) {
        if (DEBUG) {
            android.util.Log.i(TAG, obj != null ? obj.toString() : "");
        }
    }

    static void v(Object obj) {
        if (DEBUG) {
            android.util.Log.v(TAG, obj != null ? obj.toString() : "");
        }
    }

    static void e(Object obj, Throwable throwable) {
        if (DEBUG) {
            android.util.Log.e(TAG, obj != null ? obj.toString() : "", throwable);
        }
    }

    static void e(Throwable throwable) {
        if (DEBUG) {
            android.util.Log.e(TAG, "", throwable);
        }
    }

}
