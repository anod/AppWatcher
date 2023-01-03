rm ..\java\finsky\protos -r -force
protoc messages.proto --java_out=lite:..\java\