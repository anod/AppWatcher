// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

public interface EncryptionParamsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.EncryptionParams)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>optional int32 version = 1;</code>
   * @return Whether the version field is set.
   */
  boolean hasVersion();
  /**
   * <code>optional int32 version = 1;</code>
   * @return The version.
   */
  int getVersion();

  /**
   * <code>optional string encryptionKey = 2;</code>
   * @return Whether the encryptionKey field is set.
   */
  boolean hasEncryptionKey();
  /**
   * <code>optional string encryptionKey = 2;</code>
   * @return The encryptionKey.
   */
  java.lang.String getEncryptionKey();
  /**
   * <code>optional string encryptionKey = 2;</code>
   * @return The bytes for encryptionKey.
   */
  com.google.protobuf.ByteString
      getEncryptionKeyBytes();

  /**
   * <code>optional string hMacKey = 3;</code>
   * @return Whether the hMacKey field is set.
   */
  boolean hasHMacKey();
  /**
   * <code>optional string hMacKey = 3;</code>
   * @return The hMacKey.
   */
  java.lang.String getHMacKey();
  /**
   * <code>optional string hMacKey = 3;</code>
   * @return The bytes for hMacKey.
   */
  com.google.protobuf.ByteString
      getHMacKeyBytes();
}