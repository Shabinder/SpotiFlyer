package nl.bravobit.ffmpeg;

public interface FFcommandExecuteResponseHandler extends ResponseHandler {

    /**
     * on Success
     *
     * @param message complete output of the binary command
     */
    void onSuccess(String message);

    /**
     * on Progress
     *
     * @param message current output of binary command
     */
    void onProgress(String message);

    /**
     * on Failure
     *
     * @param message complete output of the binary command
     */
    void onFailure(String message);

}
