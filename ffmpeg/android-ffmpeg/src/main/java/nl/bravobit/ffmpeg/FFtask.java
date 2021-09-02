package nl.bravobit.ffmpeg;

public interface FFtask {

    /**
     * Sends 'q' to the ff binary running process asynchronously
     */
    void sendQuitSignal();

    /**
     * Checks if process is completed
     * @return <code>true</code> if a process is running
     */
    boolean isProcessCompleted();

    /**
     * Kill given running process
     *
     * @return true if process is killed successfully
     */
    boolean killRunningProcess();
}
