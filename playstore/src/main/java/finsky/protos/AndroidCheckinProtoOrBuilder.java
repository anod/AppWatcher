// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: messages.proto
// Protobuf Java Version: 4.28.2

package finsky.protos;

public interface AndroidCheckinProtoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.AndroidCheckinProto)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>optional .finsky.protos.AndroidBuildProto build = 1;</code>
   * @return Whether the build field is set.
   */
  boolean hasBuild();
  /**
   * <code>optional .finsky.protos.AndroidBuildProto build = 1;</code>
   * @return The build.
   */
  finsky.protos.AndroidBuildProto getBuild();

  /**
   * <code>optional int64 lastCheckinMsec = 2;</code>
   * @return Whether the lastCheckinMsec field is set.
   */
  boolean hasLastCheckinMsec();
  /**
   * <code>optional int64 lastCheckinMsec = 2;</code>
   * @return The lastCheckinMsec.
   */
  long getLastCheckinMsec();

  /**
   * <code>repeated .finsky.protos.AndroidEventProto event = 3;</code>
   */
  java.util.List<finsky.protos.AndroidEventProto> 
      getEventList();
  /**
   * <code>repeated .finsky.protos.AndroidEventProto event = 3;</code>
   */
  finsky.protos.AndroidEventProto getEvent(int index);
  /**
   * <code>repeated .finsky.protos.AndroidEventProto event = 3;</code>
   */
  int getEventCount();

  /**
   * <code>repeated .finsky.protos.AndroidStatisticProto stat = 4;</code>
   */
  java.util.List<finsky.protos.AndroidStatisticProto> 
      getStatList();
  /**
   * <code>repeated .finsky.protos.AndroidStatisticProto stat = 4;</code>
   */
  finsky.protos.AndroidStatisticProto getStat(int index);
  /**
   * <code>repeated .finsky.protos.AndroidStatisticProto stat = 4;</code>
   */
  int getStatCount();

  /**
   * <code>repeated string requestedGroup = 5;</code>
   * @return A list containing the requestedGroup.
   */
  java.util.List<java.lang.String>
      getRequestedGroupList();
  /**
   * <code>repeated string requestedGroup = 5;</code>
   * @return The count of requestedGroup.
   */
  int getRequestedGroupCount();
  /**
   * <code>repeated string requestedGroup = 5;</code>
   * @param index The index of the element to return.
   * @return The requestedGroup at the given index.
   */
  java.lang.String getRequestedGroup(int index);
  /**
   * <code>repeated string requestedGroup = 5;</code>
   * @param index The index of the element to return.
   * @return The requestedGroup at the given index.
   */
  com.google.protobuf.ByteString
      getRequestedGroupBytes(int index);

  /**
   * <code>optional string cellOperator = 6;</code>
   * @return Whether the cellOperator field is set.
   */
  boolean hasCellOperator();
  /**
   * <code>optional string cellOperator = 6;</code>
   * @return The cellOperator.
   */
  java.lang.String getCellOperator();
  /**
   * <code>optional string cellOperator = 6;</code>
   * @return The bytes for cellOperator.
   */
  com.google.protobuf.ByteString
      getCellOperatorBytes();

  /**
   * <code>optional string simOperator = 7;</code>
   * @return Whether the simOperator field is set.
   */
  boolean hasSimOperator();
  /**
   * <code>optional string simOperator = 7;</code>
   * @return The simOperator.
   */
  java.lang.String getSimOperator();
  /**
   * <code>optional string simOperator = 7;</code>
   * @return The bytes for simOperator.
   */
  com.google.protobuf.ByteString
      getSimOperatorBytes();

  /**
   * <code>optional string roaming = 8;</code>
   * @return Whether the roaming field is set.
   */
  boolean hasRoaming();
  /**
   * <code>optional string roaming = 8;</code>
   * @return The roaming.
   */
  java.lang.String getRoaming();
  /**
   * <code>optional string roaming = 8;</code>
   * @return The bytes for roaming.
   */
  com.google.protobuf.ByteString
      getRoamingBytes();

  /**
   * <code>optional int32 userNumber = 9;</code>
   * @return Whether the userNumber field is set.
   */
  boolean hasUserNumber();
  /**
   * <code>optional int32 userNumber = 9;</code>
   * @return The userNumber.
   */
  int getUserNumber();
}
