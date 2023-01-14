// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

public interface AndroidAppDeliveryDataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.AndroidAppDeliveryData)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>optional int64 downloadSize = 1;</code>
   * @return Whether the downloadSize field is set.
   */
  boolean hasDownloadSize();
  /**
   * <code>optional int64 downloadSize = 1;</code>
   * @return The downloadSize.
   */
  long getDownloadSize();

  /**
   * <code>optional string sha1 = 2;</code>
   * @return Whether the sha1 field is set.
   */
  boolean hasSha1();
  /**
   * <code>optional string sha1 = 2;</code>
   * @return The sha1.
   */
  java.lang.String getSha1();
  /**
   * <code>optional string sha1 = 2;</code>
   * @return The bytes for sha1.
   */
  com.google.protobuf.ByteString
      getSha1Bytes();

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

  /**
   * <code>repeated .finsky.protos.AppFileMetadata additionalFile = 4;</code>
   */
  java.util.List<finsky.protos.AppFileMetadata> 
      getAdditionalFileList();
  /**
   * <code>repeated .finsky.protos.AppFileMetadata additionalFile = 4;</code>
   */
  finsky.protos.AppFileMetadata getAdditionalFile(int index);
  /**
   * <code>repeated .finsky.protos.AppFileMetadata additionalFile = 4;</code>
   */
  int getAdditionalFileCount();

  /**
   * <code>repeated .finsky.protos.HttpCookie downloadAuthCookie = 5;</code>
   */
  java.util.List<finsky.protos.HttpCookie> 
      getDownloadAuthCookieList();
  /**
   * <code>repeated .finsky.protos.HttpCookie downloadAuthCookie = 5;</code>
   */
  finsky.protos.HttpCookie getDownloadAuthCookie(int index);
  /**
   * <code>repeated .finsky.protos.HttpCookie downloadAuthCookie = 5;</code>
   */
  int getDownloadAuthCookieCount();

  /**
   * <code>optional bool forwardLocked = 6;</code>
   * @return Whether the forwardLocked field is set.
   */
  boolean hasForwardLocked();
  /**
   * <code>optional bool forwardLocked = 6;</code>
   * @return The forwardLocked.
   */
  boolean getForwardLocked();

  /**
   * <code>optional int64 refundTimeout = 7;</code>
   * @return Whether the refundTimeout field is set.
   */
  boolean hasRefundTimeout();
  /**
   * <code>optional int64 refundTimeout = 7;</code>
   * @return The refundTimeout.
   */
  long getRefundTimeout();

  /**
   * <code>optional bool serverInitiated = 8 [default = true];</code>
   * @return Whether the serverInitiated field is set.
   */
  boolean hasServerInitiated();
  /**
   * <code>optional bool serverInitiated = 8 [default = true];</code>
   * @return The serverInitiated.
   */
  boolean getServerInitiated();

  /**
   * <code>optional int64 postInstallRefundWindowMillis = 9;</code>
   * @return Whether the postInstallRefundWindowMillis field is set.
   */
  boolean hasPostInstallRefundWindowMillis();
  /**
   * <code>optional int64 postInstallRefundWindowMillis = 9;</code>
   * @return The postInstallRefundWindowMillis.
   */
  long getPostInstallRefundWindowMillis();

  /**
   * <code>optional bool immediateStartNeeded = 10;</code>
   * @return Whether the immediateStartNeeded field is set.
   */
  boolean hasImmediateStartNeeded();
  /**
   * <code>optional bool immediateStartNeeded = 10;</code>
   * @return The immediateStartNeeded.
   */
  boolean getImmediateStartNeeded();

  /**
   * <code>optional .finsky.protos.AndroidAppPatchData patchData = 11;</code>
   * @return Whether the patchData field is set.
   */
  boolean hasPatchData();
  /**
   * <code>optional .finsky.protos.AndroidAppPatchData patchData = 11;</code>
   * @return The patchData.
   */
  finsky.protos.AndroidAppPatchData getPatchData();

  /**
   * <code>optional .finsky.protos.EncryptionParams encryptionParams = 12;</code>
   * @return Whether the encryptionParams field is set.
   */
  boolean hasEncryptionParams();
  /**
   * <code>optional .finsky.protos.EncryptionParams encryptionParams = 12;</code>
   * @return The encryptionParams.
   */
  finsky.protos.EncryptionParams getEncryptionParams();

  /**
   * <code>optional string compressedDownloadUrl = 13;</code>
   * @return Whether the compressedDownloadUrl field is set.
   */
  boolean hasCompressedDownloadUrl();
  /**
   * <code>optional string compressedDownloadUrl = 13;</code>
   * @return The compressedDownloadUrl.
   */
  java.lang.String getCompressedDownloadUrl();
  /**
   * <code>optional string compressedDownloadUrl = 13;</code>
   * @return The bytes for compressedDownloadUrl.
   */
  com.google.protobuf.ByteString
      getCompressedDownloadUrlBytes();

  /**
   * <code>optional int64 compressedSize = 14;</code>
   * @return Whether the compressedSize field is set.
   */
  boolean hasCompressedSize();
  /**
   * <code>optional int64 compressedSize = 14;</code>
   * @return The compressedSize.
   */
  long getCompressedSize();

  /**
   * <code>repeated .finsky.protos.SplitDeliveryData splitDeliveryData = 15;</code>
   */
  java.util.List<finsky.protos.SplitDeliveryData> 
      getSplitDeliveryDataList();
  /**
   * <code>repeated .finsky.protos.SplitDeliveryData splitDeliveryData = 15;</code>
   */
  finsky.protos.SplitDeliveryData getSplitDeliveryData(int index);
  /**
   * <code>repeated .finsky.protos.SplitDeliveryData splitDeliveryData = 15;</code>
   */
  int getSplitDeliveryDataCount();

  /**
   * <code>optional int32 installLocation = 16;</code>
   * @return Whether the installLocation field is set.
   */
  boolean hasInstallLocation();
  /**
   * <code>optional int32 installLocation = 16;</code>
   * @return The installLocation.
   */
  int getInstallLocation();

  /**
   * <code>optional int64 type = 17;</code>
   * @return Whether the type field is set.
   */
  boolean hasType();
  /**
   * <code>optional int64 type = 17;</code>
   * @return The type.
   */
  long getType();

  /**
   * <code>optional .finsky.protos.CompressedAppData compressedAppData = 18;</code>
   * @return Whether the compressedAppData field is set.
   */
  boolean hasCompressedAppData();
  /**
   * <code>optional .finsky.protos.CompressedAppData compressedAppData = 18;</code>
   * @return The compressedAppData.
   */
  finsky.protos.CompressedAppData getCompressedAppData();

  /**
   * <code>optional string sha256 = 19;</code>
   * @return Whether the sha256 field is set.
   */
  boolean hasSha256();
  /**
   * <code>optional string sha256 = 19;</code>
   * @return The sha256.
   */
  java.lang.String getSha256();
  /**
   * <code>optional string sha256 = 19;</code>
   * @return The bytes for sha256.
   */
  com.google.protobuf.ByteString
      getSha256Bytes();
}
