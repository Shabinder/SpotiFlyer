#!/usr/bin/env bash

# CD to script location
cd "$(dirname "$0")" || echo "cd to $(dirname "$0") Failed"

# Copy ffmpeg executables for all targets
for target in arm64-v8a armeabi-v7a x86 x86_64
do
  mkdir -p ./android-ffmpeg/src/main/jniLibs/$target/
  cp ./ffmpeg-android-maker/build/ffmpeg/$target/bin/ffmpeg  ./android-ffmpeg/src/main/jniLibs/$target/lib..ffmpeg..so
done
