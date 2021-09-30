package nl.bravobit.ffmpeg;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("deprecation")
class FFcommandExecuteAsyncTask extends AsyncTask<Void, String, CommandResult> implements FFtask {

    private final String[] cmd;
    private final Map<String, String> environment;
    private final StringBuilder outputStringBuilder = new StringBuilder();
    private final FFcommandExecuteResponseHandler ffmpegExecuteResponseHandler;
    private final ShellCommand shellCommand;
    private final long timeout;
    private long startTime;
    private Process process;
    private String output = "";
    private boolean quitPending;

    FFcommandExecuteAsyncTask(String[] cmd, Map<String, String> environment, long timeout, FFcommandExecuteResponseHandler ffmpegExecuteResponseHandler) {
        this.cmd = cmd;
        this.timeout = timeout;
        this.environment = environment;
        this.ffmpegExecuteResponseHandler = ffmpegExecuteResponseHandler;
        this.shellCommand = new ShellCommand();
    }

    @Override
    protected void onPreExecute() {
        startTime = System.currentTimeMillis();
        if (ffmpegExecuteResponseHandler != null) {
            ffmpegExecuteResponseHandler.onStart();
        }
    }

    @Override
    protected CommandResult doInBackground(Void... params) {
        CommandResult ret = CommandResult.getDummyFailureResponse();
        try {
            process = shellCommand.run(cmd, environment);
            if (process == null) {
                return CommandResult.getDummyFailureResponse();
            }
            Log.d("Running publishing updates method");
            checkAndUpdateProcess();
            ret = CommandResult.getOutputFromProcess(process);
            outputStringBuilder.append(ret.output);
        } catch (TimeoutException e) {
            Log.e("FFmpeg binary timed out", e);
            ret = new CommandResult(false, e.getMessage());
            outputStringBuilder.append(ret.output);
        } catch (Exception e) {
            Log.e("Error running FFmpeg binary", e);
        } finally {
            Util.destroyProcess(process);
        }
        output = outputStringBuilder.toString();
        return ret;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (values != null && values[0] != null && ffmpegExecuteResponseHandler != null) {
            ffmpegExecuteResponseHandler.onProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(CommandResult commandResult) {
        if (ffmpegExecuteResponseHandler != null) {
            if (commandResult.success) {
                ffmpegExecuteResponseHandler.onSuccess(output);
            } else {
                ffmpegExecuteResponseHandler.onFailure(output);
            }
            ffmpegExecuteResponseHandler.onFinish();
        }
    }

    private void checkAndUpdateProcess() throws TimeoutException, InterruptedException {
        while (!Util.isProcessCompleted(process)) {

            // checking if process is completed
            if (Util.isProcessCompleted(process)) {
                return;
            }

            // Handling timeout
            if (timeout != Long.MAX_VALUE && System.currentTimeMillis() > startTime + timeout) {
                throw new TimeoutException("FFmpeg binary timed out");
            }

            try {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    if (isCancelled()) {
                        process.destroy();
                        process.waitFor();
                        return;
                    }

                    if (quitPending) {
                        sendQ();
                        process = null;
                        return;
                    }

                    outputStringBuilder.append(line); outputStringBuilder.append("\n");
                    publishProgress(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isProcessCompleted() {
        return Util.isProcessCompleted(process);
    }

    @Override
    public boolean killRunningProcess() {
        return Util.killAsync(this);
    }

    @Override
    public void sendQuitSignal() {
        quitPending = true;
    }

    private void sendQ() {
        OutputStream outputStream = process.getOutputStream();
        try {
            outputStream.write("q\n".getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}