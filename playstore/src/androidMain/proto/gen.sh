#!/bin/sh
rm -rf ../java/finsky/protos
protoc messages.proto --java_out=lite:../java/
