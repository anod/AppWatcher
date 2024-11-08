// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: messages.proto
// Protobuf Java Version: 4.28.2

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.UploadDeviceConfigRequest}
 */
public  final class UploadDeviceConfigRequest extends
    com.google.protobuf.GeneratedMessageLite<
        UploadDeviceConfigRequest, UploadDeviceConfigRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.UploadDeviceConfigRequest)
    UploadDeviceConfigRequestOrBuilder {
  private UploadDeviceConfigRequest() {
    manufacturer_ = "";
    gcmRegistrationId_ = "";
  }
  private int bitField0_;
  public static final int DEVICECONFIGURATION_FIELD_NUMBER = 1;
  private finsky.protos.DeviceConfigurationProto deviceConfiguration_;
  /**
   * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
   */
  @java.lang.Override
  public boolean hasDeviceConfiguration() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
   */
  @java.lang.Override
  public finsky.protos.DeviceConfigurationProto getDeviceConfiguration() {
    return deviceConfiguration_ == null ? finsky.protos.DeviceConfigurationProto.getDefaultInstance() : deviceConfiguration_;
  }
  /**
   * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
   */
  private void setDeviceConfiguration(finsky.protos.DeviceConfigurationProto value) {
    value.getClass();
  deviceConfiguration_ = value;
    bitField0_ |= 0x00000001;
    }
  /**
   * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeDeviceConfiguration(finsky.protos.DeviceConfigurationProto value) {
    value.getClass();
  if (deviceConfiguration_ != null &&
        deviceConfiguration_ != finsky.protos.DeviceConfigurationProto.getDefaultInstance()) {
      deviceConfiguration_ =
        finsky.protos.DeviceConfigurationProto.newBuilder(deviceConfiguration_).mergeFrom(value).buildPartial();
    } else {
      deviceConfiguration_ = value;
    }
    bitField0_ |= 0x00000001;
  }
  /**
   * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
   */
  private void clearDeviceConfiguration() {  deviceConfiguration_ = null;
    bitField0_ = (bitField0_ & ~0x00000001);
  }

  public static final int MANUFACTURER_FIELD_NUMBER = 2;
  private java.lang.String manufacturer_;
  /**
   * <code>optional string manufacturer = 2;</code>
   * @return Whether the manufacturer field is set.
   */
  @java.lang.Override
  public boolean hasManufacturer() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional string manufacturer = 2;</code>
   * @return The manufacturer.
   */
  @java.lang.Override
  public java.lang.String getManufacturer() {
    return manufacturer_;
  }
  /**
   * <code>optional string manufacturer = 2;</code>
   * @return The bytes for manufacturer.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getManufacturerBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(manufacturer_);
  }
  /**
   * <code>optional string manufacturer = 2;</code>
   * @param value The manufacturer to set.
   */
  private void setManufacturer(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000002;
    manufacturer_ = value;
  }
  /**
   * <code>optional string manufacturer = 2;</code>
   */
  private void clearManufacturer() {
    bitField0_ = (bitField0_ & ~0x00000002);
    manufacturer_ = getDefaultInstance().getManufacturer();
  }
  /**
   * <code>optional string manufacturer = 2;</code>
   * @param value The bytes for manufacturer to set.
   */
  private void setManufacturerBytes(
      com.google.protobuf.ByteString value) {
    manufacturer_ = value.toStringUtf8();
    bitField0_ |= 0x00000002;
  }

  public static final int GCMREGISTRATIONID_FIELD_NUMBER = 3;
  private java.lang.String gcmRegistrationId_;
  /**
   * <code>optional string gcmRegistrationId = 3;</code>
   * @return Whether the gcmRegistrationId field is set.
   */
  @java.lang.Override
  public boolean hasGcmRegistrationId() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <code>optional string gcmRegistrationId = 3;</code>
   * @return The gcmRegistrationId.
   */
  @java.lang.Override
  public java.lang.String getGcmRegistrationId() {
    return gcmRegistrationId_;
  }
  /**
   * <code>optional string gcmRegistrationId = 3;</code>
   * @return The bytes for gcmRegistrationId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getGcmRegistrationIdBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(gcmRegistrationId_);
  }
  /**
   * <code>optional string gcmRegistrationId = 3;</code>
   * @param value The gcmRegistrationId to set.
   */
  private void setGcmRegistrationId(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000004;
    gcmRegistrationId_ = value;
  }
  /**
   * <code>optional string gcmRegistrationId = 3;</code>
   */
  private void clearGcmRegistrationId() {
    bitField0_ = (bitField0_ & ~0x00000004);
    gcmRegistrationId_ = getDefaultInstance().getGcmRegistrationId();
  }
  /**
   * <code>optional string gcmRegistrationId = 3;</code>
   * @param value The bytes for gcmRegistrationId to set.
   */
  private void setGcmRegistrationIdBytes(
      com.google.protobuf.ByteString value) {
    gcmRegistrationId_ = value.toStringUtf8();
    bitField0_ |= 0x00000004;
  }

  public static finsky.protos.UploadDeviceConfigRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.UploadDeviceConfigRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.UploadDeviceConfigRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.UploadDeviceConfigRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.UploadDeviceConfigRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.UploadDeviceConfigRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.UploadDeviceConfigRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.UploadDeviceConfigRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static finsky.protos.UploadDeviceConfigRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }

  public static finsky.protos.UploadDeviceConfigRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.UploadDeviceConfigRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.UploadDeviceConfigRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.UploadDeviceConfigRequest prototype) {
    return DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.UploadDeviceConfigRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.UploadDeviceConfigRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.UploadDeviceConfigRequest)
      finsky.protos.UploadDeviceConfigRequestOrBuilder {
    // Construct using finsky.protos.UploadDeviceConfigRequest.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
     */
    @java.lang.Override
    public boolean hasDeviceConfiguration() {
      return instance.hasDeviceConfiguration();
    }
    /**
     * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
     */
    @java.lang.Override
    public finsky.protos.DeviceConfigurationProto getDeviceConfiguration() {
      return instance.getDeviceConfiguration();
    }
    /**
     * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
     */
    public Builder setDeviceConfiguration(finsky.protos.DeviceConfigurationProto value) {
      copyOnWrite();
      instance.setDeviceConfiguration(value);
      return this;
      }
    /**
     * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
     */
    public Builder setDeviceConfiguration(
        finsky.protos.DeviceConfigurationProto.Builder builderForValue) {
      copyOnWrite();
      instance.setDeviceConfiguration(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
     */
    public Builder mergeDeviceConfiguration(finsky.protos.DeviceConfigurationProto value) {
      copyOnWrite();
      instance.mergeDeviceConfiguration(value);
      return this;
    }
    /**
     * <code>optional .finsky.protos.DeviceConfigurationProto deviceConfiguration = 1;</code>
     */
    public Builder clearDeviceConfiguration() {  copyOnWrite();
      instance.clearDeviceConfiguration();
      return this;
    }

    /**
     * <code>optional string manufacturer = 2;</code>
     * @return Whether the manufacturer field is set.
     */
    @java.lang.Override
    public boolean hasManufacturer() {
      return instance.hasManufacturer();
    }
    /**
     * <code>optional string manufacturer = 2;</code>
     * @return The manufacturer.
     */
    @java.lang.Override
    public java.lang.String getManufacturer() {
      return instance.getManufacturer();
    }
    /**
     * <code>optional string manufacturer = 2;</code>
     * @return The bytes for manufacturer.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getManufacturerBytes() {
      return instance.getManufacturerBytes();
    }
    /**
     * <code>optional string manufacturer = 2;</code>
     * @param value The manufacturer to set.
     * @return This builder for chaining.
     */
    public Builder setManufacturer(
        java.lang.String value) {
      copyOnWrite();
      instance.setManufacturer(value);
      return this;
    }
    /**
     * <code>optional string manufacturer = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearManufacturer() {
      copyOnWrite();
      instance.clearManufacturer();
      return this;
    }
    /**
     * <code>optional string manufacturer = 2;</code>
     * @param value The bytes for manufacturer to set.
     * @return This builder for chaining.
     */
    public Builder setManufacturerBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setManufacturerBytes(value);
      return this;
    }

    /**
     * <code>optional string gcmRegistrationId = 3;</code>
     * @return Whether the gcmRegistrationId field is set.
     */
    @java.lang.Override
    public boolean hasGcmRegistrationId() {
      return instance.hasGcmRegistrationId();
    }
    /**
     * <code>optional string gcmRegistrationId = 3;</code>
     * @return The gcmRegistrationId.
     */
    @java.lang.Override
    public java.lang.String getGcmRegistrationId() {
      return instance.getGcmRegistrationId();
    }
    /**
     * <code>optional string gcmRegistrationId = 3;</code>
     * @return The bytes for gcmRegistrationId.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getGcmRegistrationIdBytes() {
      return instance.getGcmRegistrationIdBytes();
    }
    /**
     * <code>optional string gcmRegistrationId = 3;</code>
     * @param value The gcmRegistrationId to set.
     * @return This builder for chaining.
     */
    public Builder setGcmRegistrationId(
        java.lang.String value) {
      copyOnWrite();
      instance.setGcmRegistrationId(value);
      return this;
    }
    /**
     * <code>optional string gcmRegistrationId = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearGcmRegistrationId() {
      copyOnWrite();
      instance.clearGcmRegistrationId();
      return this;
    }
    /**
     * <code>optional string gcmRegistrationId = 3;</code>
     * @param value The bytes for gcmRegistrationId to set.
     * @return This builder for chaining.
     */
    public Builder setGcmRegistrationIdBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setGcmRegistrationIdBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.UploadDeviceConfigRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.UploadDeviceConfigRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "deviceConfiguration_",
            "manufacturer_",
            "gcmRegistrationId_",
          };
          java.lang.String info =
              "\u0001\u0003\u0000\u0001\u0001\u0003\u0003\u0000\u0000\u0000\u0001\u1009\u0000\u0002" +
              "\u1008\u0001\u0003\u1008\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.UploadDeviceConfigRequest> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.UploadDeviceConfigRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.UploadDeviceConfigRequest>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.UploadDeviceConfigRequest)
  private static final finsky.protos.UploadDeviceConfigRequest DEFAULT_INSTANCE;
  static {
    UploadDeviceConfigRequest defaultInstance = new UploadDeviceConfigRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      UploadDeviceConfigRequest.class, defaultInstance);
  }

  public static finsky.protos.UploadDeviceConfigRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<UploadDeviceConfigRequest> PARSER;

  public static com.google.protobuf.Parser<UploadDeviceConfigRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
