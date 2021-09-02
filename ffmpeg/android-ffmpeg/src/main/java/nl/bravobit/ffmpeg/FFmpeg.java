package nl.bravobit.ffmpeg;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.Map;

public class FFmpeg implements FFbinaryInterface {

    private final FFbinaryContextProvider context;

    private static final long MINIMUM_TIMEOUT = 10 * 1000;
    private long timeout = Long.MAX_VALUE;

    private static FFmpeg instance = null;

    private FFmpeg(FFbinaryContextProvider context) {
        this.context = context;
        Log.setDebug(Util.isDebug(this.context.provide()));
    }

    public static FFmpeg getInstance(final Context context) {
        if (instance == null) {
            instance = new FFmpeg(new FFbinaryContextProvider() {
                @Override
                public Context provide() {
                    return context;
                }
            });
        }
        return instance;
    }

    @Override
    public boolean isSupported() {

        // get ffmpeg file
        File ffmpeg = FileUtils.getFFmpeg(context.provide());

        // check if ffmpeg can be executed
        if (!ffmpeg.canExecute()) {
            // try to make executable
            Log.e("ffmpeg cannot execute");
            return false;
        }

        Log.d("ffmpeg is ready!");

        return true;
    }

    @Override
    public FFtask execute(Map<String, String> environvenmentVars, String[] cmd, FFcommandExecuteResponseHandler ffmpegExecuteResponseHandler) {
        if (cmd.length != 0) {
            final String[] command = new String[cmd.length + 1];
            command[0] = FileUtils.getFFmpeg(context.provide()).getAbsolutePath();
            System.arraycopy(cmd, 0, command, 1, cmd.length);
            FFcommandExecuteAsyncTask task = new FFcommandExecuteAsyncTask(command, environvenmentVars, timeout, ffmpegExecuteResponseHandler);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return task;
        } else {
            throw new IllegalArgumentException("shell command cannot be empty");
        }
    }

    @Override
    public FFtask execute(String[] cmd, FFcommandExecuteResponseHandler ffmpegExecuteResponseHandler) {
        return execute(null, cmd, ffmpegExecuteResponseHandler);
    }

    @Override
    public boolean isCommandRunning(FFtask task) {
        return task != null && !task.isProcessCompleted();
    }

    @Override
    public boolean killRunningProcesses(FFtask task) {
        return task != null && task.killRunningProcess();
    }

    @Override
    public void setTimeout(long timeout) {
        if (timeout >= MINIMUM_TIMEOUT) {
            this.timeout = timeout;
        }
    }
}
