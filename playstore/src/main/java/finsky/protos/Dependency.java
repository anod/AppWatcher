// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.Dependency}
 */
public  final class Dependency extends
    com.google.protobuf.GeneratedMessageLite<
        Dependency, Dependency.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.Dependency)
    DependencyOrBuilder {
  private Dependency() {
    packageName_ = "";
  }
  private int bitField0_;
  public static final int PACKAGENAME_FIELD_NUMBER = 1;
  private java.lang.String packageName_;
  /**
   * <code>optional string packageName = 1;</code>
   * @return Whether the packageName field is set.
   */
  @java.lang.Override
  public boolean hasPackageName() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional string packageName = 1;</code>
   * @return The packageName.
   */
  @java.lang.Override
  public java.lang.String getPackageName() {
    return packageName_;
  }
  /**
   * <code>optional string packageName = 1;</code>
   * @return The bytes for packageName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getPackageNameBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(packageName_);
  }
  /**
   * <code>optional string packageName = 1;</code>
   * @param value The packageName to set.
   */
  private void setPackageName(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000001;
    packageName_ = value;
  }
  /**
   * <code>optional string packageName = 1;</code>
   */
  private void clearPackageName() {
    bitField0_ = (bitField0_ & ~0x00000001);
    packageName_ = getDefaultInstance().getPackageName();
  }
  /**
   * <code>optional string packageName = 1;</code>
   * @param value The bytes for packageName to set.
   */
  private void setPackageNameBytes(
      com.google.protobuf.ByteString value) {
    packageName_ = value.toStringUtf8();
    bitField0_ |= 0x00000001;
  }

  public static final int VERSION_FIELD_NUMBER = 2;
  private int version_;
  /**
   * <code>optional int32 version = 2;</code>
   * @return Whether the version field is set.
   */
  @java.lang.Override
  public boolean hasVersion() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional int32 version = 2;</code>
   * @return The version.
   */
  @java.lang.Override
  public int getVersion() {
    return version_;
  }
  /**
   * <code>optional int32 version = 2;</code>
   * @param value The version to set.
   */
  private void setVersion(int value) {
    bitField0_ |= 0x00000002;
    version_ = value;
  }
  /**
   * <code>optional int32 version = 2;</code>
   */
  private void clearVersion() {
    bitField0_ = (bitField0_ & ~0x00000002);
    version_ = 0;
  }

  public static final int UNKNOWN4_FIELD_NUMBER = 4;
  private int unknown4_;
  /**
   * <code>optional int32 unknown4 = 4;</code>
   * @return Whether the unknown4 field is set.
   */
  @java.lang.Override
  public boolean hasUnknown4() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <code>optional int32 unknown4 = 4;</code>
   * @return The unknown4.
   */
  @java.lang.Override
  public int getUnknown4() {
    return unknown4_;
  }
  /**
   * <code>optional int32 unknown4 = 4;</code>
   * @param value The unknown4 to set.
   */
  private void setUnknown4(int value) {
    bitField0_ |= 0x00000004;
    unknown4_ = value;
  }
  /**
   * <code>optional int32 unknown4 = 4;</code>
   */
  private void clearUnknown4() {
    bitField0_ = (bitField0_ & ~0x00000004);
    unknown4_ = 0;
  }

  public static finsky.protos.Dependency parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Dependency parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Dependency parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Dependency parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Dependency parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Dependency parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Dependency parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Dependency parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.Dependency parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Dependency parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.Dependency parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Dependency parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.Dependency prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.Dependency}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.Dependency, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.Dependency)
      finsky.protos.DependencyOrBuilder {
    // Construct using finsky.protos.Dependency.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional string packageName = 1;</code>
     * @return Whether the packageName field is set.
     */
    @java.lang.Override
    public boolean hasPackageName() {
      return instance.hasPackageName();
    }
    /**
     * <code>optional string packageName = 1;</code>
     * @return The packageName.
     */
    @java.lang.Override
    public java.lang.String getPackageName() {
      return instance.getPackageName();
    }
    /**
     * <code>optional string packageName = 1;</code>
     * @return The bytes for packageName.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getPackageNameBytes() {
      return instance.getPackageNameBytes();
    }
    /**
     * <code>optional string packageName = 1;</code>
     * @param value The packageName to set.
     * @return This builder for chaining.
     */
    public Builder setPackageName(
        java.lang.String value) {
      copyOnWrite();
      instance.setPackageName(value);
      return this;
    }
    /**
     * <code>optional string packageName = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearPackageName() {
      copyOnWrite();
      instance.clearPackageName();
      return this;
    }
    /**
     * <code>optional string packageName = 1;</code>
     * @param value The bytes for packageName to set.
     * @return This builder for chaining.
     */
    public Builder setPackageNameBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPackageNameBytes(value);
      return this;
    }

    /**
     * <code>optional int32 version = 2;</code>
     * @return Whether the version field is set.
     */
    @java.lang.Override
    public boolean hasVersion() {
      return instance.hasVersion();
    }
    /**
     * <code>optional int32 version = 2;</code>
     * @return The version.
     */
    @java.lang.Override
    public int getVersion() {
      return instance.getVersion();
    }
    /**
     * <code>optional int32 version = 2;</code>
     * @param value The version to set.
     * @return This builder for chaining.
     */
    public Builder setVersion(int value) {
      copyOnWrite();
      instance.setVersion(value);
      return this;
    }
    /**
     * <code>optional int32 version = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearVersion() {
      copyOnWrite();
      instance.clearVersion();
      return this;
    }

    /**
     * <code>optional int32 unknown4 = 4;</code>
     * @return Whether the unknown4 field is set.
     */
    @java.lang.Override
    public boolean hasUnknown4() {
      return instance.hasUnknown4();
    }
    /**
     * <code>optional int32 unknown4 = 4;</code>
     * @return The unknown4.
     */
    @java.lang.Override
    public int getUnknown4() {
      return instance.getUnknown4();
    }
    /**
     * <code>optional int32 unknown4 = 4;</code>
     * @param value The unknown4 to set.
     * @return This builder for chaining.
     */
    public Builder setUnknown4(int value) {
      copyOnWrite();
      instance.setUnknown4(value);
      return this;
    }
    /**
     * <code>optional int32 unknown4 = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearUnknown4() {
      copyOnWrite();
      instance.clearUnknown4();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.Dependency)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.Dependency();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "packageName_",
            "version_",
            "unknown4_",
          };
          java.lang.String info =
              "\u0001\u0003\u0000\u0001\u0001\u0004\u0003\u0000\u0000\u0000\u0001\u1008\u0000\u0002" +
              "\u1004\u0001\u0004\u1004\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.Dependency> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.Dependency.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.Dependency>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.Dependency)
  private static final finsky.protos.Dependency DEFAULT_INSTANCE;
  static {
    Dependency defaultInstance = new Dependency();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Dependency.class, defaultInstance);
  }

  public static finsky.protos.Dependency getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Dependency> PARSER;

  public static com.google.protobuf.Parser<Dependency> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
