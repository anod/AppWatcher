// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

public interface CompressedAppDataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.CompressedAppData)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>optional int64 type = 1;</code>
   * @return Whether the type field is set.
   */
  boolean hasType();
  /**
   * <code>optional int64 type = 1;</code>
   * @return The type.
   */
  long getType();

  /**
   * <code>optional int64 size = 2;</code>
   * @return Whether the size field is set.
   */
  boolean hasSize();
  /**
   * <code>optional int64 size = 2;</code>
   * @return The size.
   */
  long getSize();

  /**
   * <code>optional string downloadUrl = 3;</code>
   * @return Whether the downloadUrl field is set.
   */
  boolean hasDownloadUrl();
  /**
   * <code>optional string downloadUrl = 3;</code>
   * @return The downloadUrl.
   */
  java.lang.String getDownloadUrl();
  /**
   * <code>optional string downloadUrl = 3;</code>
   * @return The bytes for downloadUrl.
   */
  com.google.protobuf.ByteString
      getDownloadUrlBytes();
}
