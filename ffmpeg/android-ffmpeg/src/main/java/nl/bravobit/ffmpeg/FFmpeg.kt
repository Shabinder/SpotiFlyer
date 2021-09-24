package nl.bravobit.ffmpeg

import android.content.Context
import nl.bravobit.ffmpeg.Log.setDebug
import nl.bravobit.ffmpeg.Util.isDebug
import nl.bravobit.ffmpeg.FileUtils.getFFmpeg
import nl.bravobit.ffmpeg.Log.e
import nl.bravobit.ffmpeg.Log.d
import android.os.AsyncTask
import java.lang.IllegalArgumentException

class FFmpeg private constructor(private val context: FFbinaryContextProvider) : FFbinaryInterface {
    private var timeout = Long.MAX_VALUE

    init {
        setDebug(isDebug(context.provide()))
    }

    override fun isSupported(): Boolean {
        // get ffmpeg file
        val ffmpeg = getFFmpeg(context.provide())

        // check if ffmpeg can be executed
        if (!ffmpeg.canExecute()) {
            // try to make executable
            e("ffmpeg cannot execute")
            return false
        }
        d("ffmpeg is ready!")
        return true
    }

    override fun execute(
        environmentVars: Map<String, String>,
        cmd: Array<String>,
        ffmpegExecuteResponseHandler: FFcommandExecuteResponseHandler
    ): FFtask {
        return if (cmd.isNotEmpty()) {
            val command = arrayOfNulls<String>(cmd.size + 1)
            command[0] = getFFmpeg(context.provide()).absolutePath
            System.arraycopy(cmd, 0, command, 1, cmd.size)
            val task = FFcommandExecuteAsyncTask(
                command,
                environmentVars,
                timeout,
                ffmpegExecuteResponseHandler
            )
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            task
        } else {
            throw IllegalArgumentException("shell command cannot be empty")
        }
    }

    override fun execute(
        cmd: Array<String>,
        ffmpegExecuteResponseHandler: FFcommandExecuteResponseHandler
    ): FFtask {
        return execute(emptyMap(), cmd, ffmpegExecuteResponseHandler)
    }

    override fun isCommandRunning(task: FFtask): Boolean {
        return !task.isProcessCompleted
    }

    override fun killRunningProcesses(task: FFtask): Boolean {
        return task.killRunningProcess()
    }

    override fun setTimeout(timeout: Long) {
        if (timeout >= MINIMUM_TIMEOUT) {
            this.timeout = timeout
        }
    }

    companion object {
        private const val MINIMUM_TIMEOUT = (10 * 1000).toLong()
        private var instance: FFmpeg? = null

        fun getInstance(context: Context): FFmpeg {
            return instance ?: FFmpeg { context }.also {
                instance = it
            }
        }
    }
}