package nl.bravobit.ffmpeg

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.AsyncTask
import android.os.Handler
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

internal object Util {

    @JvmStatic
    fun isDebug(context: Context): Boolean {
        return context.applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    @JvmStatic
    fun convertInputStreamToString(inputStream: InputStream?): String? {
        try {
            val r = BufferedReader(InputStreamReader(inputStream))
            var str: String?
            val sb = StringBuilder()
            while (r.readLine().also { str = it } != null) {
                sb.append(str)
            }
            return sb.toString()
        } catch (e: IOException) {
            Log.e("error converting input stream to string", e)
        }
        return null
    }

    @JvmStatic
    fun destroyProcess(process: Process?) {
        if (process != null) {
            try {
                process.destroy()
            } catch (e: Exception) {
                Log.e("progress destroy error", e)
            }
        }
    }

    @JvmStatic
    fun killAsync(asyncTask: AsyncTask<*, *, *>?): Boolean {
        return asyncTask != null && !asyncTask.isCancelled && asyncTask.cancel(true)
    }

    @JvmStatic
    fun isProcessCompleted(process: Process?): Boolean {
        try {
            if (process == null) return true
            process.exitValue()
            return true
        } catch (e: IllegalThreadStateException) {
            // do nothing
        }
        return false
    }

    fun observeOnce(predicate: ObservePredicate, run: Runnable, timeout: Int): FFbinaryObserver {
        val observer = Handler()
        val observeAction: FFbinaryObserver = object : FFbinaryObserver {
            private var canceled = false
            private var timeElapsed = 0
            override fun run() {
                if (timeElapsed + 40 > timeout) cancel()
                timeElapsed += 40
                if (canceled) return
                var readyToProceed = false
                readyToProceed = try {
                    predicate.isReadyToProceed
                } catch (e: Exception) {
                    Log.v("Observing " + e.message)
                    observer.postDelayed(this, 40)
                    return
                }
                if (readyToProceed) {
                    Log.v("Observed")
                    run.run()
                } else {
                    Log.v("Observing")
                    observer.postDelayed(this, 40)
                }
            }

            override fun cancel() {
                canceled = true
            }
        }
        observer.post(observeAction)
        return observeAction
    }

    interface ObservePredicate {
        val isReadyToProceed: Boolean
    }
}