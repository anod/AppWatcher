#!/bin/sh

protoc messages.proto --java_out=lite:../java/
