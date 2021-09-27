// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.Rated}
 */
public  final class Rated extends
    com.google.protobuf.GeneratedMessageLite<
        Rated, Rated.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.Rated)
    RatedOrBuilder {
  private Rated() {
    label_ = "";
    learnMoreHtmlLink_ = "";
  }
  private int bitField0_;
  public static final int LABEL_FIELD_NUMBER = 1;
  private java.lang.String label_;
  /**
   * <code>optional string label = 1;</code>
   * @return Whether the label field is set.
   */
  @java.lang.Override
  public boolean hasLabel() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional string label = 1;</code>
   * @return The label.
   */
  @java.lang.Override
  public java.lang.String getLabel() {
    return label_;
  }
  /**
   * <code>optional string label = 1;</code>
   * @return The bytes for label.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getLabelBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(label_);
  }
  /**
   * <code>optional string label = 1;</code>
   * @param value The label to set.
   */
  private void setLabel(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000001;
    label_ = value;
  }
  /**
   * <code>optional string label = 1;</code>
   */
  private void clearLabel() {
    bitField0_ = (bitField0_ & ~0x00000001);
    label_ = getDefaultInstance().getLabel();
  }
  /**
   * <code>optional string label = 1;</code>
   * @param value The bytes for label to set.
   */
  private void setLabelBytes(
      com.google.protobuf.ByteString value) {
    label_ = value.toStringUtf8();
    bitField0_ |= 0x00000001;
  }

  public static final int IMAGE_FIELD_NUMBER = 2;
  private finsky.protos.Image image_;
  /**
   * <code>optional .finsky.protos.Image image = 2;</code>
   */
  @java.lang.Override
  public boolean hasImage() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional .finsky.protos.Image image = 2;</code>
   */
  @java.lang.Override
  public finsky.protos.Image getImage() {
    return image_ == null ? finsky.protos.Image.getDefaultInstance() : image_;
  }
  /**
   * <code>optional .finsky.protos.Image image = 2;</code>
   */
  private void setImage(finsky.protos.Image value) {
    value.getClass();
  image_ = value;
    bitField0_ |= 0x00000002;
    }
  /**
   * <code>optional .finsky.protos.Image image = 2;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeImage(finsky.protos.Image value) {
    value.getClass();
  if (image_ != null &&
        image_ != finsky.protos.Image.getDefaultInstance()) {
      image_ =
        finsky.protos.Image.newBuilder(image_).mergeFrom(value).buildPartial();
    } else {
      image_ = value;
    }
    bitField0_ |= 0x00000002;
  }
  /**
   * <code>optional .finsky.protos.Image image = 2;</code>
   */
  private void clearImage() {  image_ = null;
    bitField0_ = (bitField0_ & ~0x00000002);
  }

  public static final int LEARNMOREHTMLLINK_FIELD_NUMBER = 4;
  private java.lang.String learnMoreHtmlLink_;
  /**
   * <code>optional string learnMoreHtmlLink = 4;</code>
   * @return Whether the learnMoreHtmlLink field is set.
   */
  @java.lang.Override
  public boolean hasLearnMoreHtmlLink() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <code>optional string learnMoreHtmlLink = 4;</code>
   * @return The learnMoreHtmlLink.
   */
  @java.lang.Override
  public java.lang.String getLearnMoreHtmlLink() {
    return learnMoreHtmlLink_;
  }
  /**
   * <code>optional string learnMoreHtmlLink = 4;</code>
   * @return The bytes for learnMoreHtmlLink.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getLearnMoreHtmlLinkBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(learnMoreHtmlLink_);
  }
  /**
   * <code>optional string learnMoreHtmlLink = 4;</code>
   * @param value The learnMoreHtmlLink to set.
   */
  private void setLearnMoreHtmlLink(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000004;
    learnMoreHtmlLink_ = value;
  }
  /**
   * <code>optional string learnMoreHtmlLink = 4;</code>
   */
  private void clearLearnMoreHtmlLink() {
    bitField0_ = (bitField0_ & ~0x00000004);
    learnMoreHtmlLink_ = getDefaultInstance().getLearnMoreHtmlLink();
  }
  /**
   * <code>optional string learnMoreHtmlLink = 4;</code>
   * @param value The bytes for learnMoreHtmlLink to set.
   */
  private void setLearnMoreHtmlLinkBytes(
      com.google.protobuf.ByteString value) {
    learnMoreHtmlLink_ = value.toStringUtf8();
    bitField0_ |= 0x00000004;
  }

  public static finsky.protos.Rated parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Rated parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Rated parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Rated parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Rated parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Rated parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Rated parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Rated parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.Rated parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Rated parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.Rated parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Rated parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.Rated prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.Rated}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.Rated, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.Rated)
      finsky.protos.RatedOrBuilder {
    // Construct using finsky.protos.Rated.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional string label = 1;</code>
     * @return Whether the label field is set.
     */
    @java.lang.Override
    public boolean hasLabel() {
      return instance.hasLabel();
    }
    /**
     * <code>optional string label = 1;</code>
     * @return The label.
     */
    @java.lang.Override
    public java.lang.String getLabel() {
      return instance.getLabel();
    }
    /**
     * <code>optional string label = 1;</code>
     * @return The bytes for label.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getLabelBytes() {
      return instance.getLabelBytes();
    }
    /**
     * <code>optional string label = 1;</code>
     * @param value The label to set.
     * @return This builder for chaining.
     */
    public Builder setLabel(
        java.lang.String value) {
      copyOnWrite();
      instance.setLabel(value);
      return this;
    }
    /**
     * <code>optional string label = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearLabel() {
      copyOnWrite();
      instance.clearLabel();
      return this;
    }
    /**
     * <code>optional string label = 1;</code>
     * @param value The bytes for label to set.
     * @return This builder for chaining.
     */
    public Builder setLabelBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setLabelBytes(value);
      return this;
    }

    /**
     * <code>optional .finsky.protos.Image image = 2;</code>
     */
    @java.lang.Override
    public boolean hasImage() {
      return instance.hasImage();
    }
    /**
     * <code>optional .finsky.protos.Image image = 2;</code>
     */
    @java.lang.Override
    public finsky.protos.Image getImage() {
      return instance.getImage();
    }
    /**
     * <code>optional .finsky.protos.Image image = 2;</code>
     */
    public Builder setImage(finsky.protos.Image value) {
      copyOnWrite();
      instance.setImage(value);
      return this;
      }
    /**
     * <code>optional .finsky.protos.Image image = 2;</code>
     */
    public Builder setImage(
        finsky.protos.Image.Builder builderForValue) {
      copyOnWrite();
      instance.setImage(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .finsky.protos.Image image = 2;</code>
     */
    public Builder mergeImage(finsky.protos.Image value) {
      copyOnWrite();
      instance.mergeImage(value);
      return this;
    }
    /**
     * <code>optional .finsky.protos.Image image = 2;</code>
     */
    public Builder clearImage() {  copyOnWrite();
      instance.clearImage();
      return this;
    }

    /**
     * <code>optional string learnMoreHtmlLink = 4;</code>
     * @return Whether the learnMoreHtmlLink field is set.
     */
    @java.lang.Override
    public boolean hasLearnMoreHtmlLink() {
      return instance.hasLearnMoreHtmlLink();
    }
    /**
     * <code>optional string learnMoreHtmlLink = 4;</code>
     * @return The learnMoreHtmlLink.
     */
    @java.lang.Override
    public java.lang.String getLearnMoreHtmlLink() {
      return instance.getLearnMoreHtmlLink();
    }
    /**
     * <code>optional string learnMoreHtmlLink = 4;</code>
     * @return The bytes for learnMoreHtmlLink.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getLearnMoreHtmlLinkBytes() {
      return instance.getLearnMoreHtmlLinkBytes();
    }
    /**
     * <code>optional string learnMoreHtmlLink = 4;</code>
     * @param value The learnMoreHtmlLink to set.
     * @return This builder for chaining.
     */
    public Builder setLearnMoreHtmlLink(
        java.lang.String value) {
      copyOnWrite();
      instance.setLearnMoreHtmlLink(value);
      return this;
    }
    /**
     * <code>optional string learnMoreHtmlLink = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearLearnMoreHtmlLink() {
      copyOnWrite();
      instance.clearLearnMoreHtmlLink();
      return this;
    }
    /**
     * <code>optional string learnMoreHtmlLink = 4;</code>
     * @param value The bytes for learnMoreHtmlLink to set.
     * @return This builder for chaining.
     */
    public Builder setLearnMoreHtmlLinkBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setLearnMoreHtmlLinkBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.Rated)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.Rated();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "label_",
            "image_",
            "learnMoreHtmlLink_",
          };
          java.lang.String info =
              "\u0001\u0003\u0000\u0001\u0001\u0004\u0003\u0000\u0000\u0000\u0001\u1008\u0000\u0002" +
              "\u1009\u0001\u0004\u1008\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.Rated> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.Rated.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.Rated>(
                      DEFAULT_INSTANCE);
              PARSER = parser;
            }
          }
        }
        return parser;
    }
    case GET_MEMOIZED_IS_INITIALIZED: {
      return (byte) 1;
    }
    case SET_MEMOIZED_IS_INITIALIZED: {
      return null;
    }
    }
    throw new UnsupportedOperationException();
  }


  // @@protoc_insertion_point(class_scope:finsky.protos.Rated)
  private static final finsky.protos.Rated DEFAULT_INSTANCE;
  static {
    Rated defaultInstance = new Rated();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Rated.class, defaultInstance);
  }

  public static finsky.protos.Rated getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Rated> PARSER;

  public static com.google.protobuf.Parser<Rated> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

