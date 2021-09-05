package nl.bravobit.ffmpeg;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Util {

    static boolean isDebug(Context context) {
        return (context.getApplicationContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    static String convertInputStreamToString(InputStream inputStream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String str;
            StringBuilder sb = new StringBuilder();
            while ((str = r.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e("error converting input stream to string", e);
        }
        return null;
    }

    static void destroyProcess(Process process) {
        if (process != null) {
            try {
                process.destroy();
            } catch (Exception e) {
                Log.e("progress destroy error", e);
            }
        }
    }

    static boolean killAsync(AsyncTask asyncTask) {
        return asyncTask != null && !asyncTask.isCancelled() && asyncTask.cancel(true);
    }

    static boolean isProcessCompleted(Process process) {
        try {
            if (process == null) return true;
            process.exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            // do nothing
        }
        return false;
    }

    public interface ObservePredicate {
        Boolean isReadyToProceed();
    }

    static FFbinaryObserver observeOnce(final ObservePredicate predicate, final Runnable run, final int timeout) {
        final android.os.Handler observer = new android.os.Handler();


        final FFbinaryObserver observeAction = new FFbinaryObserver() {
            private boolean canceled = false;
            private int timeElapsed = 0;

            @Override
            public void run() {
                if (timeElapsed + 40 > timeout) cancel();
                timeElapsed += 40;

                if (canceled) return;

                boolean readyToProceed = false;
                try {
                    readyToProceed = predicate.isReadyToProceed();
                } catch (Exception e) {
                    Log.v("Observing " + e.getMessage());
                    observer.postDelayed(this, 40);
                    return;
                }

                if (readyToProceed) {
                    Log.v("Observed");
                    run.run();
                } else {
                    Log.v("Observing");
                    observer.postDelayed(this, 40);
                }
            }

            @Override
            public void cancel() {
                canceled = true;
            }
        };

        observer.post(observeAction);

        return observeAction;
    }
}
