// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

public interface AppFileMetadataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.AppFileMetadata)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>optional int32 fileType = 1;</code>
   * @return Whether the fileType field is set.
   */
  boolean hasFileType();
  /**
   * <code>optional int32 fileType = 1;</code>
   * @return The fileType.
   */
  int getFileType();

  /**
   * <code>optional int32 versionCode = 2;</code>
   * @return Whether the versionCode field is set.
   */
  boolean hasVersionCode();
  /**
   * <code>optional int32 versionCode = 2;</code>
   * @return The versionCode.
   */
  int getVersionCode();

  /**
   * <code>optional int64 size = 3;</code>
   * @return Whether the size field is set.
   */
  boolean hasSize();
  /**
   * <code>optional int64 size = 3;</code>
   * @return The size.
   */
  long getSize();

  /**
   * <code>optional string downloadUrl = 4;</code>
   * @return Whether the downloadUrl field is set.
   */
  boolean hasDownloadUrl();
  /**
   * <code>optional string downloadUrl = 4;</code>
   * @return The downloadUrl.
   */
  java.lang.String getDownloadUrl();
  /**
   * <code>optional string downloadUrl = 4;</code>
   * @return The bytes for downloadUrl.
   */
  com.google.protobuf.ByteString
      getDownloadUrlBytes();

  /**
   * <code>optional .finsky.protos.AndroidAppPatchData patchData = 5;</code>
   * @return Whether the patchData field is set.
   */
  boolean hasPatchData();
  /**
   * <code>optional .finsky.protos.AndroidAppPatchData patchData = 5;</code>
   * @return The patchData.
   */
  finsky.protos.AndroidAppPatchData getPatchData();

  /**
   * <code>optional int64 compressedSize = 6;</code>
   * @return Whether the compressedSize field is set.
   */
  boolean hasCompressedSize();
  /**
   * <code>optional int64 compressedSize = 6;</code>
   * @return The compressedSize.
   */
  long getCompressedSize();

  /**
   * <code>optional string compressedDownloadUrl = 7;</code>
   * @return Whether the compressedDownloadUrl field is set.
   */
  boolean hasCompressedDownloadUrl();
  /**
   * <code>optional string compressedDownloadUrl = 7;</code>
   * @return The compressedDownloadUrl.
   */
  java.lang.String getCompressedDownloadUrl();
  /**
   * <code>optional string compressedDownloadUrl = 7;</code>
   * @return The bytes for compressedDownloadUrl.
   */
  com.google.protobuf.ByteString
      getCompressedDownloadUrlBytes();

  /**
   * <code>optional string sha1 = 8;</code>
   * @return Whether the sha1 field is set.
   */
  boolean hasSha1();
  /**
   * <code>optional string sha1 = 8;</code>
   * @return The sha1.
   */
  java.lang.String getSha1();
  /**
   * <code>optional string sha1 = 8;</code>
   * @return The bytes for sha1.
   */
  com.google.protobuf.ByteString
      getSha1Bytes();
}