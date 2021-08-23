#!/bin/bash

./../ffmpeg-kit/android.sh \
  --lts \
  --disable-everything \
  --disable-network \
  --disable-autodetect \
  --enable-small \
  --enable-decoder=aac*,ac3*,opus,vorbis \
  --enable-demuxer=mov,m4v,matroska \
  --enable-muxer=mp3,mp4 \
  --enable-protocol=file \
  --enable-encoder=mp3 \
  --enable-filter=aresample \
  --enable-gpl \
  --enable-version3 \
  --enable-cross-compile \
  --enable-pic \
  --enable-jni \
  --enable-optimizations \
  --enable-v4l2-m2m