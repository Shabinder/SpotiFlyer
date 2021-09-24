package nl.bravobit.ffmpeg

interface ResponseHandler {
    /**
     * on Start
     */
    fun onStart()

    /**
     * on Finish
     */
    fun onFinish()
}