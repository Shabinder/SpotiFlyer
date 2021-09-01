cmake_minimum_required(VERSION 3.4.1)

set(ffmpeg_dir ${CMAKE_SOURCE_DIR}/../ffmpeg-android-maker/output)
set(ffmpeg_libs ${ffmpeg_dir}/lib/${ANDROID_ABI})

include_directories(${ffmpeg_dir}/include/${ANDROID_ABI})

set(
        # List variable name
        ffmpeg_libs_names
        # Values in the list
        avutil avformat avcodec avresample swresample
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
        spotiflyer-converter
        # Type of a library
        SHARED
        # All cpp files to compile
        src/main/cpp/main.cpp
        #        src/main/cpp/media_file_builder.cpp
        #        src/main/cpp/media_file_builder_jni.cpp
        #        src/main/cpp/frame_loader_context.cpp
        #        src/main/cpp/frame_loader_context_jni.cpp
        #        src/main/cpp/frame_extractor.cpp
        #        src/main/cpp/utils.cpp
)

target_link_libraries(
        # Library to link
        spotiflyer-converter
        # List of libraries to link against:
        # Library for writing messages in LogCat
        log
        # Library for processing Bitmap objects
        jnigraphics
        # FFmpeg libraries
        ${ffmpeg_libs_names}
)