// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

public interface RatedOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.Rated)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>optional string label = 1;</code>
   * @return Whether the label field is set.
   */
  boolean hasLabel();
  /**
   * <code>optional string label = 1;</code>
   * @return The label.
   */
  java.lang.String getLabel();
  /**
   * <code>optional string label = 1;</code>
   * @return The bytes for label.
   */
  com.google.protobuf.ByteString
      getLabelBytes();

  /**
   * <code>optional .finsky.protos.Image image = 2;</code>
   * @return Whether the image field is set.
   */
  boolean hasImage();
  /**
   * <code>optional .finsky.protos.Image image = 2;</code>
   * @return The image.
   */
  finsky.protos.Image getImage();

  /**
   * <code>optional string learnMoreHtmlLink = 4;</code>
   * @return Whether the learnMoreHtmlLink field is set.
   */
  boolean hasLearnMoreHtmlLink();
  /**
   * <code>optional string learnMoreHtmlLink = 4;</code>
   * @return The learnMoreHtmlLink.
   */
  java.lang.String getLearnMoreHtmlLink();
  /**
   * <code>optional string learnMoreHtmlLink = 4;</code>
   * @return The bytes for learnMoreHtmlLink.
   */
  com.google.protobuf.ByteString
      getLearnMoreHtmlLinkBytes();
}
