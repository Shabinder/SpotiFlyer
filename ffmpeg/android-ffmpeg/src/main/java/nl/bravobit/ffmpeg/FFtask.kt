package nl.bravobit.ffmpeg

interface FFtask {
    /**
     * Sends 'q' to the ff binary running process asynchronously
     */
    fun sendQuitSignal()

    /**
     * Checks if process is completed
     * @return `true` if a process is running
     */
    val isProcessCompleted: Boolean

    /**
     * Kill given running process
     *
     * @return true if process is killed successfully
     */
    fun killRunningProcess(): Boolean
}