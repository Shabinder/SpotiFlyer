#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <string.h>
#include <android/log.h>

extern "C" {
    #include <libavformat/avformat.h>
    // #include <libavcodec/avcodec.h>
    JNIEXPORT jint

    JNICALL Java_com_shabinder_spotiflyer_ffmpeg_FFmpeg_testInit(JNIEnv *env, jclass c) {
        __android_log_print(ANDROID_LOG_DEBUG, "FFmpeg", "%s", avcodec_configuration());
        return (jint)
        1;
    }
}

