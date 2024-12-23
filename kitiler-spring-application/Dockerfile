ARG GDAL_VERSION=3.9.1

FROM ghcr.io/osgeo/gdal:ubuntu-full-$GDAL_VERSION

ARG STACK_ID=io.buildpacks.stacks.jammy

ENV LD_LIBRARY_PATH /usr/lib/x86_64-linux-gnu/jni
ENV GDAL_DISABLE_READDIR_ON_OPEN=EMPTY_DIR

LABEL io.buildpacks.stack.id=$STACK_ID
