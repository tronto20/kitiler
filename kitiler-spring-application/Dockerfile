ARG GDAL_VERSION=3.10.1

FROM ghcr.io/osgeo/gdal:ubuntu-full-$GDAL_VERSION

ARG JNI=/usr/lib/x86_64-linux-gnu/jni
ENV LD_LIBRARY_PATH=$JNI
ENV GDAL_DISABLE_READDIR_ON_OPEN=EMPTY_DIR

ARG STACK_ID=io.buildpacks.stacks.noble.tiny
LABEL io.buildpacks.stack.id=$STACK_ID
