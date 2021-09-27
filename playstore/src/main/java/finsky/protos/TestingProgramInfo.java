// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.TestingProgramInfo}
 */
public  final class TestingProgramInfo extends
    com.google.protobuf.GeneratedMessageLite<
        TestingProgramInfo, TestingProgramInfo.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.TestingProgramInfo)
    TestingProgramInfoOrBuilder {
  private TestingProgramInfo() {
    email_ = "";
    displayName_ = "";
  }
  private int bitField0_;
  public static final int SUBSCRIBED_FIELD_NUMBER = 2;
  private boolean subscribed_;
  /**
   * <code>optional bool subscribed = 2;</code>
   * @return Whether the subscribed field is set.
   */
  @java.lang.Override
  public boolean hasSubscribed() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional bool subscribed = 2;</code>
   * @return The subscribed.
   */
  @java.lang.Override
  public boolean getSubscribed() {
    return subscribed_;
  }
  /**
   * <code>optional bool subscribed = 2;</code>
   * @param value The subscribed to set.
   */
  private void setSubscribed(boolean value) {
    bitField0_ |= 0x00000001;
    subscribed_ = value;
  }
  /**
   * <code>optional bool subscribed = 2;</code>
   */
  private void clearSubscribed() {
    bitField0_ = (bitField0_ & ~0x00000001);
    subscribed_ = false;
  }

  public static final int SUBSCRIBEDANDINSTALLED_FIELD_NUMBER = 3;
  private boolean subscribedAndInstalled_;
  /**
   * <code>optional bool subscribedAndInstalled = 3;</code>
   * @return Whether the subscribedAndInstalled field is set.
   */
  @java.lang.Override
  public boolean hasSubscribedAndInstalled() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional bool subscribedAndInstalled = 3;</code>
   * @return The subscribedAndInstalled.
   */
  @java.lang.Override
  public boolean getSubscribedAndInstalled() {
    return subscribedAndInstalled_;
  }
  /**
   * <code>optional bool subscribedAndInstalled = 3;</code>
   * @param value The subscribedAndInstalled to set.
   */
  private void setSubscribedAndInstalled(boolean value) {
    bitField0_ |= 0x00000002;
    subscribedAndInstalled_ = value;
  }
  /**
   * <code>optional bool subscribedAndInstalled = 3;</code>
   */
  private void clearSubscribedAndInstalled() {
    bitField0_ = (bitField0_ & ~0x00000002);
    subscribedAndInstalled_ = false;
  }

  public static final int EMAIL_FIELD_NUMBER = 5;
  private java.lang.String email_;
  /**
   * <code>optional string email = 5;</code>
   * @return Whether the email field is set.
   */
  @java.lang.Override
  public boolean hasEmail() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <code>optional string email = 5;</code>
   * @return The email.
   */
  @java.lang.Override
  public java.lang.String getEmail() {
    return email_;
  }
  /**
   * <code>optional string email = 5;</code>
   * @return The bytes for email.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getEmailBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(email_);
  }
  /**
   * <code>optional string email = 5;</code>
   * @param value The email to set.
   */
  private void setEmail(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000004;
    email_ = value;
  }
  /**
   * <code>optional string email = 5;</code>
   */
  private void clearEmail() {
    bitField0_ = (bitField0_ & ~0x00000004);
    email_ = getDefaultInstance().getEmail();
  }
  /**
   * <code>optional string email = 5;</code>
   * @param value The bytes for email to set.
   */
  private void setEmailBytes(
      com.google.protobuf.ByteString value) {
    email_ = value.toStringUtf8();
    bitField0_ |= 0x00000004;
  }

  public static final int DISPLAYNAME_FIELD_NUMBER = 7;
  private java.lang.String displayName_;
  /**
   * <code>optional string displayName = 7;</code>
   * @return Whether the displayName field is set.
   */
  @java.lang.Override
  public boolean hasDisplayName() {
    return ((bitField0_ & 0x00000008) != 0);
  }
  /**
   * <code>optional string displayName = 7;</code>
   * @return The displayName.
   */
  @java.lang.Override
  public java.lang.String getDisplayName() {
    return displayName_;
  }
  /**
   * <code>optional string displayName = 7;</code>
   * @return The bytes for displayName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getDisplayNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(displayName_);
  }
  /**
   * <code>optional string displayName = 7;</code>
   * @param value The displayName to set.
   */
  private void setDisplayName(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000008;
    displayName_ = value;
  }
  /**
   * <code>optional string displayName = 7;</code>
   */
  private void clearDisplayName() {
    bitField0_ = (bitField0_ & ~0x00000008);
    displayName_ = getDefaultInstance().getDisplayName();
  }
  /**
   * <code>optional string displayName = 7;</code>
   * @param value The bytes for displayName to set.
   */
  private void setDisplayNameBytes(
      com.google.protobuf.ByteString value) {
    displayName_ = value.toStringUtf8();
    bitField0_ |= 0x00000008;
  }

  public static final int IMAGE_FIELD_NUMBER = 6;
  private finsky.protos.Image image_;
  /**
   * <code>optional .finsky.protos.Image image = 6;</code>
   */
  @java.lang.Override
  public boolean hasImage() {
    return ((bitField0_ & 0x00000010) != 0);
  }
  /**
   * <code>optional .finsky.protos.Image image = 6;</code>
   */
  @java.lang.Override
  public finsky.protos.Image getImage() {
    return image_ == null ? finsky.protos.Image.getDefaultInstance() : image_;
  }
  /**
   * <code>optional .finsky.protos.Image image = 6;</code>
   */
  private void setImage(finsky.protos.Image value) {
    value.getClass();
  image_ = value;
    bitField0_ |= 0x00000010;
    }
  /**
   * <code>optional .finsky.protos.Image image = 6;</code>
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
    bitField0_ |= 0x00000010;
  }
  /**
   * <code>optional .finsky.protos.Image image = 6;</code>
   */
  private void clearImage() {  image_ = null;
    bitField0_ = (bitField0_ & ~0x00000010);
  }

  public static finsky.protos.TestingProgramInfo parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.TestingProgramInfo parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.TestingProgramInfo parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.TestingProgramInfo parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.TestingProgramInfo parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.TestingProgramInfo parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.TestingProgramInfo parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.TestingProgramInfo parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.TestingProgramInfo parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.TestingProgramInfo parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.TestingProgramInfo parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.TestingProgramInfo parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.TestingProgramInfo prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.TestingProgramInfo}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.TestingProgramInfo, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.TestingProgramInfo)
      finsky.protos.TestingProgramInfoOrBuilder {
    // Construct using finsky.protos.TestingProgramInfo.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional bool subscribed = 2;</code>
     * @return Whether the subscribed field is set.
     */
    @java.lang.Override
    public boolean hasSubscribed() {
      return instance.hasSubscribed();
    }
    /**
     * <code>optional bool subscribed = 2;</code>
     * @return The subscribed.
     */
    @java.lang.Override
    public boolean getSubscribed() {
      return instance.getSubscribed();
    }
    /**
     * <code>optional bool subscribed = 2;</code>
     * @param value The subscribed to set.
     * @return This builder for chaining.
     */
    public Builder setSubscribed(boolean value) {
      copyOnWrite();
      instance.setSubscribed(value);
      return this;
    }
    /**
     * <code>optional bool subscribed = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearSubscribed() {
      copyOnWrite();
      instance.clearSubscribed();
      return this;
    }

    /**
     * <code>optional bool subscribedAndInstalled = 3;</code>
     * @return Whether the subscribedAndInstalled field is set.
     */
    @java.lang.Override
    public boolean hasSubscribedAndInstalled() {
      return instance.hasSubscribedAndInstalled();
    }
    /**
     * <code>optional bool subscribedAndInstalled = 3;</code>
     * @return The subscribedAndInstalled.
     */
    @java.lang.Override
    public boolean getSubscribedAndInstalled() {
      return instance.getSubscribedAndInstalled();
    }
    /**
     * <code>optional bool subscribedAndInstalled = 3;</code>
     * @param value The subscribedAndInstalled to set.
     * @return This builder for chaining.
     */
    public Builder setSubscribedAndInstalled(boolean value) {
      copyOnWrite();
      instance.setSubscribedAndInstalled(value);
      return this;
    }
    /**
     * <code>optional bool subscribedAndInstalled = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearSubscribedAndInstalled() {
      copyOnWrite();
      instance.clearSubscribedAndInstalled();
      return this;
    }

    /**
     * <code>optional string email = 5;</code>
     * @return Whether the email field is set.
     */
    @java.lang.Override
    public boolean hasEmail() {
      return instance.hasEmail();
    }
    /**
     * <code>optional string email = 5;</code>
     * @return The email.
     */
    @java.lang.Override
    public java.lang.String getEmail() {
      return instance.getEmail();
    }
    /**
     * <code>optional string email = 5;</code>
     * @return The bytes for email.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getEmailBytes() {
      return instance.getEmailBytes();
    }
    /**
     * <code>optional string email = 5;</code>
     * @param value The email to set.
     * @return This builder for chaining.
     */
    public Builder setEmail(
        java.lang.String value) {
      copyOnWrite();
      instance.setEmail(value);
      return this;
    }
    /**
     * <code>optional string email = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearEmail() {
      copyOnWrite();
      instance.clearEmail();
      return this;
    }
    /**
     * <code>optional string email = 5;</code>
     * @param value The bytes for email to set.
     * @return This builder for chaining.
     */
    public Builder setEmailBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setEmailBytes(value);
      return this;
    }

    /**
     * <code>optional string displayName = 7;</code>
     * @return Whether the displayName field is set.
     */
    @java.lang.Override
    public boolean hasDisplayName() {
      return instance.hasDisplayName();
    }
    /**
     * <code>optional string displayName = 7;</code>
     * @return The displayName.
     */
    @java.lang.Override
    public java.lang.String getDisplayName() {
      return instance.getDisplayName();
    }
    /**
     * <code>optional string displayName = 7;</code>
     * @return The bytes for displayName.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getDisplayNameBytes() {
      return instance.getDisplayNameBytes();
    }
    /**
     * <code>optional string displayName = 7;</code>
     * @param value The displayName to set.
     * @return This builder for chaining.
     */
    public Builder setDisplayName(
        java.lang.String value) {
      copyOnWrite();
      instance.setDisplayName(value);
      return this;
    }
    /**
     * <code>optional string displayName = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearDisplayName() {
      copyOnWrite();
      instance.clearDisplayName();
      return this;
    }
    /**
     * <code>optional string displayName = 7;</code>
     * @param value The bytes for displayName to set.
     * @return This builder for chaining.
     */
    public Builder setDisplayNameBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setDisplayNameBytes(value);
      return this;
    }

    /**
     * <code>optional .finsky.protos.Image image = 6;</code>
     */
    @java.lang.Override
    public boolean hasImage() {
      return instance.hasImage();
    }
    /**
     * <code>optional .finsky.protos.Image image = 6;</code>
     */
    @java.lang.Override
    public finsky.protos.Image getImage() {
      return instance.getImage();
    }
    /**
     * <code>optional .finsky.protos.Image image = 6;</code>
     */
    public Builder setImage(finsky.protos.Image value) {
      copyOnWrite();
      instance.setImage(value);
      return this;
      }
    /**
     * <code>optional .finsky.protos.Image image = 6;</code>
     */
    public Builder setImage(
        finsky.protos.Image.Builder builderForValue) {
      copyOnWrite();
      instance.setImage(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .finsky.protos.Image image = 6;</code>
     */
    public Builder mergeImage(finsky.protos.Image value) {
      copyOnWrite();
      instance.mergeImage(value);
      return this;
    }
    /**
     * <code>optional .finsky.protos.Image image = 6;</code>
     */
    public Builder clearImage() {  copyOnWrite();
      instance.clearImage();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.TestingProgramInfo)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.TestingProgramInfo();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "subscribed_",
            "subscribedAndInstalled_",
            "email_",
            "image_",
            "displayName_",
          };
          java.lang.String info =
              "\u0001\u0005\u0000\u0001\u0002\u0007\u0005\u0000\u0000\u0000\u0002\u1007\u0000\u0003" +
              "\u1007\u0001\u0005\u1008\u0002\u0006\u1009\u0004\u0007\u1008\u0003";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.TestingProgramInfo> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.TestingProgramInfo.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.TestingProgramInfo>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.TestingProgramInfo)
  private static final finsky.protos.TestingProgramInfo DEFAULT_INSTANCE;
  static {
    TestingProgramInfo defaultInstance = new TestingProgramInfo();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      TestingProgramInfo.class, defaultInstance);
  }

  public static finsky.protos.TestingProgramInfo getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<TestingProgramInfo> PARSER;

  public static com.google.protobuf.Parser<TestingProgramInfo> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

