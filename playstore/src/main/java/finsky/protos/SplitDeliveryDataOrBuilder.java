// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

public interface SplitDeliveryDataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.SplitDeliveryData)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>optional string name = 1;</code>
   * @return Whether the name field is set.
   */
  boolean hasName();
  /**
   * <code>optional string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>optional string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>optional int64 downloadSize = 2;</code>
   * @return Whether the downloadSize field is set.
   */
  boolean hasDownloadSize();
  /**
   * <code>optional int64 downloadSize = 2;</code>
   * @return The downloadSize.
   */
  long getDownloadSize();

  /**
   * <code>optional int64 compressedSize = 3;</code>
   * @return Whether the compressedSize field is set.
   */
  boolean hasCompressedSize();
  /**
   * <code>optional int64 compressedSize = 3;</code>
   * @return The compressedSize.
   */
  long getCompressedSize();

  /**
   * <code>optional string sha1 = 4;</code>
   * @return Whether the sha1 field is set.
   */
  boolean hasSha1();
  /**
   * <code>optional string sha1 = 4;</code>
   * @return The sha1.
   */
  java.lang.String getSha1();
  /**
   * <code>optional string sha1 = 4;</code>
   * @return The bytes for sha1.
   */
  com.google.protobuf.ByteString
      getSha1Bytes();

  /**
   * <code>optional string downloadUrl = 5;</code>
   * @return Whether the downloadUrl field is set.
   */
  boolean hasDownloadUrl();
  /**
   * <code>optional string downloadUrl = 5;</code>
   * @return The downloadUrl.
   */
  java.lang.String getDownloadUrl();
  /**
   * <code>optional string downloadUrl = 5;</code>
   * @return The bytes for downloadUrl.
   */
  com.google.protobuf.ByteString
      getDownloadUrlBytes();

  /**
   * <code>optional string compressedDownloadUrl = 6;</code>
   * @return Whether the compressedDownloadUrl field is set.
   */
  boolean hasCompressedDownloadUrl();
  /**
   * <code>optional string compressedDownloadUrl = 6;</code>
   * @return The compressedDownloadUrl.
   */
  java.lang.String getCompressedDownloadUrl();
  /**
   * <code>optional string compressedDownloadUrl = 6;</code>
   * @return The bytes for compressedDownloadUrl.
   */
  com.google.protobuf.ByteString
      getCompressedDownloadUrlBytes();

  /**
   * <code>optional .finsky.protos.AndroidAppPatchData patchData = 7;</code>
   * @return Whether the patchData field is set.
   */
  boolean hasPatchData();
  /**
   * <code>optional .finsky.protos.AndroidAppPatchData patchData = 7;</code>
   * @return The patchData.
   */
  finsky.protos.AndroidAppPatchData getPatchData();

  /**
   * <code>optional .finsky.protos.CompressedAppData compressedAppData = 8;</code>
   * @return Whether the compressedAppData field is set.
   */
  boolean hasCompressedAppData();
  /**
   * <code>optional .finsky.protos.CompressedAppData compressedAppData = 8;</code>
   * @return The compressedAppData.
   */
  finsky.protos.CompressedAppData getCompressedAppData();

  /**
   * <code>optional string sha256 = 9;</code>
   * @return Whether the sha256 field is set.
   */
  boolean hasSha256();
  /**
   * <code>optional string sha256 = 9;</code>
   * @return The sha256.
   */
  java.lang.String getSha256();
  /**
   * <code>optional string sha256 = 9;</code>
   * @return The bytes for sha256.
   */
  com.google.protobuf.ByteString
      getSha256Bytes();
}