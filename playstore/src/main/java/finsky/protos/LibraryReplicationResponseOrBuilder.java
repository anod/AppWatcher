// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

public interface LibraryReplicationResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.LibraryReplicationResponse)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>repeated .finsky.protos.LibraryUpdateProto.LibraryUpdate update = 1;</code>
   */
  java.util.List<finsky.protos.LibraryUpdateProto.LibraryUpdate> 
      getUpdateList();
  /**
   * <code>repeated .finsky.protos.LibraryUpdateProto.LibraryUpdate update = 1;</code>
   */
  finsky.protos.LibraryUpdateProto.LibraryUpdate getUpdate(int index);
  /**
   * <code>repeated .finsky.protos.LibraryUpdateProto.LibraryUpdate update = 1;</code>
   */
  int getUpdateCount();

  /**
   * <code>repeated string autoAcquireFreeAppIfHigherVersionAvailableTag = 2;</code>
   * @return A list containing the autoAcquireFreeAppIfHigherVersionAvailableTag.
   */
  java.util.List<java.lang.String>
      getAutoAcquireFreeAppIfHigherVersionAvailableTagList();
  /**
   * <code>repeated string autoAcquireFreeAppIfHigherVersionAvailableTag = 2;</code>
   * @return The count of autoAcquireFreeAppIfHigherVersionAvailableTag.
   */
  int getAutoAcquireFreeAppIfHigherVersionAvailableTagCount();
  /**
   * <code>repeated string autoAcquireFreeAppIfHigherVersionAvailableTag = 2;</code>
   * @param index The index of the element to return.
   * @return The autoAcquireFreeAppIfHigherVersionAvailableTag at the given index.
   */
  java.lang.String getAutoAcquireFreeAppIfHigherVersionAvailableTag(int index);
  /**
   * <code>repeated string autoAcquireFreeAppIfHigherVersionAvailableTag = 2;</code>
   * @param index The index of the element to return.
   * @return The autoAcquireFreeAppIfHigherVersionAvailableTag at the given index.
   */
  com.google.protobuf.ByteString
      getAutoAcquireFreeAppIfHigherVersionAvailableTagBytes(int index);
}
