// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.DeliveryResponse}
 */
public  final class DeliveryResponse extends
    com.google.protobuf.GeneratedMessageLite<
        DeliveryResponse, DeliveryResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.DeliveryResponse)
    DeliveryResponseOrBuilder {
  private DeliveryResponse() {
    status_ = 1;
  }
  private int bitField0_;
  public static final int STATUS_FIELD_NUMBER = 1;
  private int status_;
  /**
   * <code>optional int32 status = 1 [default = 1];</code>
   * @return Whether the status field is set.
   */
  @java.lang.Override
  public boolean hasStatus() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional int32 status = 1 [default = 1];</code>
   * @return The status.
   */
  @java.lang.Override
  public int getStatus() {
    return status_;
  }
  /**
   * <code>optional int32 status = 1 [default = 1];</code>
   * @param value The status to set.
   */
  private void setStatus(int value) {
    bitField0_ |= 0x00000001;
    status_ = value;
  }
  /**
   * <code>optional int32 status = 1 [default = 1];</code>
   */
  private void clearStatus() {
    bitField0_ = (bitField0_ & ~0x00000001);
    status_ = 1;
  }

  public static final int APPDELIVERYDATA_FIELD_NUMBER = 2;
  private finsky.protos.AndroidAppDeliveryData appDeliveryData_;
  /**
   * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
   */
  @java.lang.Override
  public boolean hasAppDeliveryData() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
   */
  @java.lang.Override
  public finsky.protos.AndroidAppDeliveryData getAppDeliveryData() {
    return appDeliveryData_ == null ? finsky.protos.AndroidAppDeliveryData.getDefaultInstance() : appDeliveryData_;
  }
  /**
   * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
   */
  private void setAppDeliveryData(finsky.protos.AndroidAppDeliveryData value) {
    value.getClass();
  appDeliveryData_ = value;
    bitField0_ |= 0x00000002;
    }
  /**
   * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeAppDeliveryData(finsky.protos.AndroidAppDeliveryData value) {
    value.getClass();
  if (appDeliveryData_ != null &&
        appDeliveryData_ != finsky.protos.AndroidAppDeliveryData.getDefaultInstance()) {
      appDeliveryData_ =
        finsky.protos.AndroidAppDeliveryData.newBuilder(appDeliveryData_).mergeFrom(value).buildPartial();
    } else {
      appDeliveryData_ = value;
    }
    bitField0_ |= 0x00000002;
  }
  /**
   * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
   */
  private void clearAppDeliveryData() {  appDeliveryData_ = null;
    bitField0_ = (bitField0_ & ~0x00000002);
  }

  public static finsky.protos.DeliveryResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.DeliveryResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.DeliveryResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.DeliveryResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.DeliveryResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.DeliveryResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.DeliveryResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.DeliveryResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.DeliveryResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.DeliveryResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.DeliveryResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.DeliveryResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.DeliveryResponse prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.DeliveryResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.DeliveryResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.DeliveryResponse)
      finsky.protos.DeliveryResponseOrBuilder {
    // Construct using finsky.protos.DeliveryResponse.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional int32 status = 1 [default = 1];</code>
     * @return Whether the status field is set.
     */
    @java.lang.Override
    public boolean hasStatus() {
      return instance.hasStatus();
    }
    /**
     * <code>optional int32 status = 1 [default = 1];</code>
     * @return The status.
     */
    @java.lang.Override
    public int getStatus() {
      return instance.getStatus();
    }
    /**
     * <code>optional int32 status = 1 [default = 1];</code>
     * @param value The status to set.
     * @return This builder for chaining.
     */
    public Builder setStatus(int value) {
      copyOnWrite();
      instance.setStatus(value);
      return this;
    }
    /**
     * <code>optional int32 status = 1 [default = 1];</code>
     * @return This builder for chaining.
     */
    public Builder clearStatus() {
      copyOnWrite();
      instance.clearStatus();
      return this;
    }

    /**
     * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
     */
    @java.lang.Override
    public boolean hasAppDeliveryData() {
      return instance.hasAppDeliveryData();
    }
    /**
     * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
     */
    @java.lang.Override
    public finsky.protos.AndroidAppDeliveryData getAppDeliveryData() {
      return instance.getAppDeliveryData();
    }
    /**
     * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
     */
    public Builder setAppDeliveryData(finsky.protos.AndroidAppDeliveryData value) {
      copyOnWrite();
      instance.setAppDeliveryData(value);
      return this;
      }
    /**
     * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
     */
    public Builder setAppDeliveryData(
        finsky.protos.AndroidAppDeliveryData.Builder builderForValue) {
      copyOnWrite();
      instance.setAppDeliveryData(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
     */
    public Builder mergeAppDeliveryData(finsky.protos.AndroidAppDeliveryData value) {
      copyOnWrite();
      instance.mergeAppDeliveryData(value);
      return this;
    }
    /**
     * <code>optional .finsky.protos.AndroidAppDeliveryData appDeliveryData = 2;</code>
     */
    public Builder clearAppDeliveryData() {  copyOnWrite();
      instance.clearAppDeliveryData();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.DeliveryResponse)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.DeliveryResponse();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "status_",
            "appDeliveryData_",
          };
          java.lang.String info =
              "\u0001\u0002\u0000\u0001\u0001\u0002\u0002\u0000\u0000\u0000\u0001\u1004\u0000\u0002" +
              "\u1009\u0001";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.DeliveryResponse> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.DeliveryResponse.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.DeliveryResponse>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.DeliveryResponse)
  private static final finsky.protos.DeliveryResponse DEFAULT_INSTANCE;
  static {
    DeliveryResponse defaultInstance = new DeliveryResponse();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      DeliveryResponse.class, defaultInstance);
  }

  public static finsky.protos.DeliveryResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<DeliveryResponse> PARSER;

  public static com.google.protobuf.Parser<DeliveryResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

