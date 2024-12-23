ARG GDAL_VERSION=3.9.1

FROM ghcr.io/osgeo/gdal:ubuntu-full-$GDAL_VERSION

ENV LD_LIBRARY_PATH /usr/lib/x86_64-linux-gnu/jni
ENV GDAL_DISABLE_READDIR_ON_OPEN=EMPTY_DIR

ARG STACK_ID=io.buildpacks.stacks.jammy.tiny
LABEL io.buildpacks.stack.id=$STACK_ID
