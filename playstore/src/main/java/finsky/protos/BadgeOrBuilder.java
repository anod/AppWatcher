// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: messages.proto
// Protobuf Java Version: 4.28.2

package finsky.protos;

public interface BadgeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.Badge)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>optional string major = 1;</code>
   * @return Whether the major field is set.
   */
  boolean hasMajor();
  /**
   * <code>optional string major = 1;</code>
   * @return The major.
   */
  java.lang.String getMajor();
  /**
   * <code>optional string major = 1;</code>
   * @return The bytes for major.
   */
  com.google.protobuf.ByteString
      getMajorBytes();

  /**
   * <code>optional .finsky.protos.Common.Image image = 2;</code>
   * @return Whether the image field is set.
   */
  boolean hasImage();
  /**
   * <code>optional .finsky.protos.Common.Image image = 2;</code>
   * @return The image.
   */
  finsky.protos.Common.Image getImage();

  /**
   * <code>optional string minor = 3;</code>
   * @return Whether the minor field is set.
   */
  boolean hasMinor();
  /**
   * <code>optional string minor = 3;</code>
   * @return The minor.
   */
  java.lang.String getMinor();
  /**
   * <code>optional string minor = 3;</code>
   * @return The bytes for minor.
   */
  com.google.protobuf.ByteString
      getMinorBytes();

  /**
   * <code>optional string minorHtml = 4;</code>
   * @return Whether the minorHtml field is set.
   */
  boolean hasMinorHtml();
  /**
   * <code>optional string minorHtml = 4;</code>
   * @return The minorHtml.
   */
  java.lang.String getMinorHtml();
  /**
   * <code>optional string minorHtml = 4;</code>
   * @return The bytes for minorHtml.
   */
  com.google.protobuf.ByteString
      getMinorHtmlBytes();

  /**
   * <pre>
   * optional SubBadge subBadge = 6;
   * </pre>
   *
   * <code>optional .finsky.protos.StreamLink link = 7;</code>
   * @return Whether the link field is set.
   */
  boolean hasLink();
  /**
   * <pre>
   * optional SubBadge subBadge = 6;
   * </pre>
   *
   * <code>optional .finsky.protos.StreamLink link = 7;</code>
   * @return The link.
   */
  finsky.protos.StreamLink getLink();

  /**
   * <code>optional string description = 8;</code>
   * @return Whether the description field is set.
   */
  boolean hasDescription();
  /**
   * <code>optional string description = 8;</code>
   * @return The description.
   */
  java.lang.String getDescription();
  /**
   * <code>optional string description = 8;</code>
   * @return The bytes for description.
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <code>optional .finsky.protos.SubStream stream = 12;</code>
   * @return Whether the stream field is set.
   */
  boolean hasStream();
  /**
   * <code>optional .finsky.protos.SubStream stream = 12;</code>
   * @return The stream.
   */
  finsky.protos.SubStream getStream();
}
