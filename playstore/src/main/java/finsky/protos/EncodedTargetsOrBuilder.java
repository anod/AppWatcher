// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

public interface EncodedTargetsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.EncodedTargets)
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
   * <code>repeated int64 supportedTarget = 2;</code>
   * @return A list containing the supportedTarget.
   */
  java.util.List<java.lang.Long> getSupportedTargetList();
  /**
   * <code>repeated int64 supportedTarget = 2;</code>
   * @return The count of supportedTarget.
   */
  int getSupportedTargetCount();
  /**
   * <code>repeated int64 supportedTarget = 2;</code>
   * @param index The index of the element to return.
   * @return The supportedTarget at the given index.
   */
  long getSupportedTarget(int index);

  /**
   * <code>repeated int64 otherTarget = 3;</code>
   * @return A list containing the otherTarget.
   */
  java.util.List<java.lang.Long> getOtherTargetList();
  /**
   * <code>repeated int64 otherTarget = 3;</code>
   * @return The count of otherTarget.
   */
  int getOtherTargetCount();
  /**
   * <code>repeated int64 otherTarget = 3;</code>
   * @param index The index of the element to return.
   * @return The otherTarget at the given index.
   */
  long getOtherTarget(int index);
}
