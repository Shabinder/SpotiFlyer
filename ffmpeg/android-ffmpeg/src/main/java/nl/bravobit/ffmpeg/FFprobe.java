package nl.bravobit.ffmpeg;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.Map;

@SuppressWarnings("deprecation")
public class FFprobe implements FFbinaryInterface {

    private final FFbinaryContextProvider context;

    private static final long MINIMUM_TIMEOUT = 10 * 1000;
    private long timeout = Long.MAX_VALUE;

    private static FFprobe instance = null;

    private FFprobe(FFbinaryContextProvider context) {
        this.context = context;
        Log.setDebug(Util.isDebug(this.context.provide()));
    }

    public static FFprobe getInstance(final Context context) {
        if (instance == null) {
            instance = new FFprobe(() -> context);
        }
        return instance;
    }

    @Override
    public boolean isSupported() {
        // get ffprobe file
        File ffprobe = FileUtils.getFFprobe(context.provide());

        // check if ffprobe can be executed
        if (!ffprobe.canExecute()) {
            // try to make executable
            Log.e("ffprobe cannot execute");
            return false;
        }

        Log.d("ffprobe is ready!");

        return true;
    }

    @Override
    public FFtask execute(Map<String, String> environvenmentVars, String[] cmd, FFcommandExecuteResponseHandler ffcommandExecuteResponseHandler) {
        if (cmd.length != 0) {
            final String[] command = new String[cmd.length + 1];
            command[0] = FileUtils.getFFprobe(context.provide()).getAbsolutePath();
            System.arraycopy(cmd, 0, command, 1, cmd.length);
            FFcommandExecuteAsyncTask task = new FFcommandExecuteAsyncTask(command, environvenmentVars, timeout, ffcommandExecuteResponseHandler);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return task;
        } else {
            throw new IllegalArgumentException("shell command cannot be empty");
        }
    }

    @Override
    public FFtask execute(String[] cmd, FFcommandExecuteResponseHandler ffcommandExecuteResponseHandler) {
        return execute(null, cmd, ffcommandExecuteResponseHandler);
    }

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
