// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: messages.proto
// Protobuf Java Version: 4.28.2

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.CategoryInfo}
 */
public  final class CategoryInfo extends
    com.google.protobuf.GeneratedMessageLite<
        CategoryInfo, CategoryInfo.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.CategoryInfo)
    CategoryInfoOrBuilder {
  private CategoryInfo() {
    appType_ = "";
    appCategory_ = "";
  }
  private int bitField0_;
  public static final int APPTYPE_FIELD_NUMBER = 1;
  private java.lang.String appType_;
  /**
   * <code>optional string appType = 1;</code>
   * @return Whether the appType field is set.
   */
  @java.lang.Override
  public boolean hasAppType() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional string appType = 1;</code>
   * @return The appType.
   */
  @java.lang.Override
  public java.lang.String getAppType() {
    return appType_;
  }
  /**
   * <code>optional string appType = 1;</code>
   * @return The bytes for appType.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getAppTypeBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(appType_);
  }
  /**
   * <code>optional string appType = 1;</code>
   * @param value The appType to set.
   */
  private void setAppType(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000001;
    appType_ = value;
  }
  /**
   * <code>optional string appType = 1;</code>
   */
  private void clearAppType() {
    bitField0_ = (bitField0_ & ~0x00000001);
    appType_ = getDefaultInstance().getAppType();
  }
  /**
   * <code>optional string appType = 1;</code>
   * @param value The bytes for appType to set.
   */
  private void setAppTypeBytes(
      com.google.protobuf.ByteString value) {
    appType_ = value.toStringUtf8();
    bitField0_ |= 0x00000001;
  }

  public static final int APPCATEGORY_FIELD_NUMBER = 2;
  private java.lang.String appCategory_;
  /**
   * <code>optional string appCategory = 2;</code>
   * @return Whether the appCategory field is set.
   */
  @java.lang.Override
  public boolean hasAppCategory() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional string appCategory = 2;</code>
   * @return The appCategory.
   */
  @java.lang.Override
  public java.lang.String getAppCategory() {
    return appCategory_;
  }
  /**
   * <code>optional string appCategory = 2;</code>
   * @return The bytes for appCategory.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getAppCategoryBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(appCategory_);
  }
  /**
   * <code>optional string appCategory = 2;</code>
   * @param value The appCategory to set.
   */
  private void setAppCategory(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000002;
    appCategory_ = value;
  }
  /**
   * <code>optional string appCategory = 2;</code>
   */
  private void clearAppCategory() {
    bitField0_ = (bitField0_ & ~0x00000002);
    appCategory_ = getDefaultInstance().getAppCategory();
  }
  /**
   * <code>optional string appCategory = 2;</code>
   * @param value The bytes for appCategory to set.
   */
  private void setAppCategoryBytes(
      com.google.protobuf.ByteString value) {
    appCategory_ = value.toStringUtf8();
    bitField0_ |= 0x00000002;
  }

  public static finsky.protos.CategoryInfo parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.CategoryInfo parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.CategoryInfo parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.CategoryInfo parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.CategoryInfo parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.CategoryInfo parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.CategoryInfo parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.CategoryInfo parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static finsky.protos.CategoryInfo parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }

  public static finsky.protos.CategoryInfo parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.CategoryInfo parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.CategoryInfo parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.CategoryInfo prototype) {
    return DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.CategoryInfo}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.CategoryInfo, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.CategoryInfo)
      finsky.protos.CategoryInfoOrBuilder {
    // Construct using finsky.protos.CategoryInfo.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional string appType = 1;</code>
     * @return Whether the appType field is set.
     */
    @java.lang.Override
    public boolean hasAppType() {
      return instance.hasAppType();
    }
    /**
     * <code>optional string appType = 1;</code>
     * @return The appType.
     */
    @java.lang.Override
    public java.lang.String getAppType() {
      return instance.getAppType();
    }
    /**
     * <code>optional string appType = 1;</code>
     * @return The bytes for appType.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getAppTypeBytes() {
      return instance.getAppTypeBytes();
    }
    /**
     * <code>optional string appType = 1;</code>
     * @param value The appType to set.
     * @return This builder for chaining.
     */
    public Builder setAppType(
        java.lang.String value) {
      copyOnWrite();
      instance.setAppType(value);
      return this;
    }
    /**
     * <code>optional string appType = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearAppType() {
      copyOnWrite();
      instance.clearAppType();
      return this;
    }
    /**
     * <code>optional string appType = 1;</code>
     * @param value The bytes for appType to set.
     * @return This builder for chaining.
     */
    public Builder setAppTypeBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setAppTypeBytes(value);
      return this;
    }

    /**
     * <code>optional string appCategory = 2;</code>
     * @return Whether the appCategory field is set.
     */
    @java.lang.Override
    public boolean hasAppCategory() {
      return instance.hasAppCategory();
    }
    /**
     * <code>optional string appCategory = 2;</code>
     * @return The appCategory.
     */
    @java.lang.Override
    public java.lang.String getAppCategory() {
      return instance.getAppCategory();
    }
    /**
     * <code>optional string appCategory = 2;</code>
     * @return The bytes for appCategory.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getAppCategoryBytes() {
      return instance.getAppCategoryBytes();
    }
    /**
     * <code>optional string appCategory = 2;</code>
     * @param value The appCategory to set.
     * @return This builder for chaining.
     */
    public Builder setAppCategory(
        java.lang.String value) {
      copyOnWrite();
      instance.setAppCategory(value);
      return this;
    }
    /**
     * <code>optional string appCategory = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearAppCategory() {
      copyOnWrite();
      instance.clearAppCategory();
      return this;
    }
    /**
     * <code>optional string appCategory = 2;</code>
     * @param value The bytes for appCategory to set.
     * @return This builder for chaining.
     */
    public Builder setAppCategoryBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setAppCategoryBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.CategoryInfo)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.CategoryInfo();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "appType_",
            "appCategory_",
          };
          java.lang.String info =
              "\u0001\u0002\u0000\u0001\u0001\u0002\u0002\u0000\u0000\u0000\u0001\u1008\u0000\u0002" +
              "\u1008\u0001";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.CategoryInfo> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.CategoryInfo.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.CategoryInfo>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.CategoryInfo)
  private static final finsky.protos.CategoryInfo DEFAULT_INSTANCE;
  static {
    CategoryInfo defaultInstance = new CategoryInfo();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      CategoryInfo.class, defaultInstance);
  }

  public static finsky.protos.CategoryInfo getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<CategoryInfo> PARSER;

  public static com.google.protobuf.Parser<CategoryInfo> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

