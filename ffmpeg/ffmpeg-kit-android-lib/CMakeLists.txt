cmake_minimum_required(VERSION 3.4.1)

set(ffmpeg_dir ${CMAKE_SOURCE_DIR}/../ffmpeg-android-maker/output)
set(ffmpeg_libs ${ffmpeg_dir}/lib/${ANDROID_ABI})

include_directories(${ffmpeg_dir}/include/${ANDROID_ABI})

set(
        # List variable name
        ffmpeg_libs_names
        # Values in the list
        avutil avformat avcodec swresample avdevice avfilter swscale
)

foreach (ffmpeg_lib_name ${ffmpeg_libs_names})
    add_library(
            ${ffmpeg_lib_name}
            SHARED
            IMPORTED
    )
    set_target_properties(
            ${ffmpeg_lib_name}
            PROPERTIES
            IMPORTED_LOCATION
            ${ffmpeg_libs}/lib${ffmpeg_lib_name}.so
    )
endforeach ()

add_library(
        # Name for a library to build
        spotiflyer-ffmpeg
        # Type of a library
        SHARED
        # All cpp files to compile
        # mobile-ffmpeg
       src/main/cpp/doc_examples_transcode_aac.c

        # ffmpeg-kit
#        src/main/cpp/ffmpegkit.c
#        src/main/cpp/ffprobekit.c
#        src/main/cpp/ffmpegkit_exception.c
#        src/main/cpp/fftools_cmdutils.c
#        src/main/cpp/fftools_ffmpeg.c
#        src/main/cpp/fftools_ffprobe.c
#        src/main/cpp/fftools_ffmpeg_opt.c
#        src/main/cpp/fftools_ffmpeg_hw.c
#        src/main/cpp/fftools_ffmpeg_filter.c
#        src/main/cpp/saf_wrapper.c
)

target_link_libraries(
        # Library to link
        spotiflyer-ffmpeg
        # List of libraries to link against:
        # Library for writing messages in LogCat
        log
        # Library for processing Bitmap objects
        jnigraphics
        # FFmpeg libraries
        ${ffmpeg_libs_names}
)