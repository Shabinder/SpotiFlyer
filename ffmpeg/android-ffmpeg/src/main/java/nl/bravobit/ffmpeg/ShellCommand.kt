package nl.bravobit.ffmpeg

internal class ShellCommand {
    fun run(commandString: Array<String?>, environment: Map<String?, String?>?): Process? {
        var process: Process? = null
        try {
            val processBuilder = ProcessBuilder(*commandString)
            if (environment != null) {
                processBuilder.environment().putAll(environment)
            }
            process = processBuilder.start()
        } catch (t: Throwable) {
            Log.e("Exception while trying to run: " + commandString.contentToString(), t)
        }
        return process
    }
}