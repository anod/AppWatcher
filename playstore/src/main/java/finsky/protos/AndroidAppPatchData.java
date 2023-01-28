// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.AndroidAppPatchData}
 */
public  final class AndroidAppPatchData extends
    com.google.protobuf.GeneratedMessageLite<
        AndroidAppPatchData, AndroidAppPatchData.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.AndroidAppPatchData)
    AndroidAppPatchDataOrBuilder {
  private AndroidAppPatchData() {
    baseSha1_ = "";
    downloadUrl_ = "";
    patchFormat_ = 1;
  }
  private int bitField0_;
  public static final int BASEVERSIONCODE_FIELD_NUMBER = 1;
  private int baseVersionCode_;
  /**
   * <code>optional int32 baseVersionCode = 1;</code>
   * @return Whether the baseVersionCode field is set.
   */
  @java.lang.Override
  public boolean hasBaseVersionCode() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional int32 baseVersionCode = 1;</code>
   * @return The baseVersionCode.
   */
  @java.lang.Override
  public int getBaseVersionCode() {
    return baseVersionCode_;
  }
  /**
   * <code>optional int32 baseVersionCode = 1;</code>
   * @param value The baseVersionCode to set.
   */
  private void setBaseVersionCode(int value) {
    bitField0_ |= 0x00000001;
    baseVersionCode_ = value;
  }
  /**
   * <code>optional int32 baseVersionCode = 1;</code>
   */
  private void clearBaseVersionCode() {
    bitField0_ = (bitField0_ & ~0x00000001);
    baseVersionCode_ = 0;
  }

  public static final int BASESHA1_FIELD_NUMBER = 2;
  private java.lang.String baseSha1_;
  /**
   * <code>optional string baseSha1 = 2;</code>
   * @return Whether the baseSha1 field is set.
   */
  @java.lang.Override
  public boolean hasBaseSha1() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional string baseSha1 = 2;</code>
   * @return The baseSha1.
   */
  @java.lang.Override
  public java.lang.String getBaseSha1() {
    return baseSha1_;
  }
  /**
   * <code>optional string baseSha1 = 2;</code>
   * @return The bytes for baseSha1.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getBaseSha1Bytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(baseSha1_);
  }
  /**
   * <code>optional string baseSha1 = 2;</code>
   * @param value The baseSha1 to set.
   */
  private void setBaseSha1(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000002;
    baseSha1_ = value;
  }
  /**
   * <code>optional string baseSha1 = 2;</code>
   */
  private void clearBaseSha1() {
    bitField0_ = (bitField0_ & ~0x00000002);
    baseSha1_ = getDefaultInstance().getBaseSha1();
  }
  /**
   * <code>optional string baseSha1 = 2;</code>
   * @param value The bytes for baseSha1 to set.
   */
  private void setBaseSha1Bytes(
      com.google.protobuf.ByteString value) {
    baseSha1_ = value.toStringUtf8();
    bitField0_ |= 0x00000002;
  }

  public static final int DOWNLOADURL_FIELD_NUMBER = 3;
  private java.lang.String downloadUrl_;
  /**
   * <code>optional string downloadUrl = 3;</code>
   * @return Whether the downloadUrl field is set.
   */
  @java.lang.Override
  public boolean hasDownloadUrl() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <code>optional string downloadUrl = 3;</code>
   * @return The downloadUrl.
   */
  @java.lang.Override
  public java.lang.String getDownloadUrl() {
    return downloadUrl_;
  }
  /**
   * <code>optional string downloadUrl = 3;</code>
   * @return The bytes for downloadUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getDownloadUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(downloadUrl_);
  }
  /**
   * <code>optional string downloadUrl = 3;</code>
   * @param value The downloadUrl to set.
   */
  private void setDownloadUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000004;
    downloadUrl_ = value;
  }
  /**
   * <code>optional string downloadUrl = 3;</code>
   */
  private void clearDownloadUrl() {
    bitField0_ = (bitField0_ & ~0x00000004);
    downloadUrl_ = getDefaultInstance().getDownloadUrl();
  }
  /**
   * <code>optional string downloadUrl = 3;</code>
   * @param value The bytes for downloadUrl to set.
   */
  private void setDownloadUrlBytes(
      com.google.protobuf.ByteString value) {
    downloadUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000004;
  }

  public static final int PATCHFORMAT_FIELD_NUMBER = 4;
  private int patchFormat_;
  /**
   * <code>optional int32 patchFormat = 4 [default = 1];</code>
   * @return Whether the patchFormat field is set.
   */
  @java.lang.Override
  public boolean hasPatchFormat() {
    return ((bitField0_ & 0x00000008) != 0);
  }
  /**
   * <code>optional int32 patchFormat = 4 [default = 1];</code>
   * @return The patchFormat.
   */
  @java.lang.Override
  public int getPatchFormat() {
    return patchFormat_;
  }
  /**
   * <code>optional int32 patchFormat = 4 [default = 1];</code>
   * @param value The patchFormat to set.
   */
  private void setPatchFormat(int value) {
    bitField0_ |= 0x00000008;
    patchFormat_ = value;
  }
  /**
   * <code>optional int32 patchFormat = 4 [default = 1];</code>
   */
  private void clearPatchFormat() {
    bitField0_ = (bitField0_ & ~0x00000008);
    patchFormat_ = 1;
  }

  public static final int MAXPATCHSIZE_FIELD_NUMBER = 5;
  private long maxPatchSize_;
  /**
   * <code>optional int64 maxPatchSize = 5;</code>
   * @return Whether the maxPatchSize field is set.
   */
  @java.lang.Override
  public boolean hasMaxPatchSize() {
    return ((bitField0_ & 0x00000010) != 0);
  }
  /**
   * <code>optional int64 maxPatchSize = 5;</code>
   * @return The maxPatchSize.
   */
  @java.lang.Override
  public long getMaxPatchSize() {
    return maxPatchSize_;
  }
  /**
   * <code>optional int64 maxPatchSize = 5;</code>
   * @param value The maxPatchSize to set.
   */
  private void setMaxPatchSize(long value) {
    bitField0_ |= 0x00000010;
    maxPatchSize_ = value;
  }
  /**
   * <code>optional int64 maxPatchSize = 5;</code>
   */
  private void clearMaxPatchSize() {
    bitField0_ = (bitField0_ & ~0x00000010);
    maxPatchSize_ = 0L;
  }

  public static finsky.protos.AndroidAppPatchData parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.AndroidAppPatchData parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.AndroidAppPatchData parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.AndroidAppPatchData parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.AndroidAppPatchData parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.AndroidAppPatchData parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.AndroidAppPatchData parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.AndroidAppPatchData parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.AndroidAppPatchData parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.AndroidAppPatchData parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.AndroidAppPatchData parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.AndroidAppPatchData parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.AndroidAppPatchData prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.AndroidAppPatchData}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.AndroidAppPatchData, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.AndroidAppPatchData)
      finsky.protos.AndroidAppPatchDataOrBuilder {
    // Construct using finsky.protos.AndroidAppPatchData.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional int32 baseVersionCode = 1;</code>
     * @return Whether the baseVersionCode field is set.
     */
    @java.lang.Override
    public boolean hasBaseVersionCode() {
      return instance.hasBaseVersionCode();
    }
    /**
     * <code>optional int32 baseVersionCode = 1;</code>
     * @return The baseVersionCode.
     */
    @java.lang.Override
    public int getBaseVersionCode() {
      return instance.getBaseVersionCode();
    }
    /**
     * <code>optional int32 baseVersionCode = 1;</code>
     * @param value The baseVersionCode to set.
     * @return This builder for chaining.
     */
    public Builder setBaseVersionCode(int value) {
      copyOnWrite();
      instance.setBaseVersionCode(value);
      return this;
    }
    /**
     * <code>optional int32 baseVersionCode = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearBaseVersionCode() {
      copyOnWrite();
      instance.clearBaseVersionCode();
      return this;
    }

    /**
     * <code>optional string baseSha1 = 2;</code>
     * @return Whether the baseSha1 field is set.
     */
    @java.lang.Override
    public boolean hasBaseSha1() {
      return instance.hasBaseSha1();
    }
    /**
     * <code>optional string baseSha1 = 2;</code>
     * @return The baseSha1.
     */
    @java.lang.Override
    public java.lang.String getBaseSha1() {
      return instance.getBaseSha1();
    }
    /**
     * <code>optional string baseSha1 = 2;</code>
     * @return The bytes for baseSha1.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getBaseSha1Bytes() {
      return instance.getBaseSha1Bytes();
    }
    /**
     * <code>optional string baseSha1 = 2;</code>
     * @param value The baseSha1 to set.
     * @return This builder for chaining.
     */
    public Builder setBaseSha1(
        java.lang.String value) {
      copyOnWrite();
      instance.setBaseSha1(value);
      return this;
    }
    /**
     * <code>optional string baseSha1 = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearBaseSha1() {
      copyOnWrite();
      instance.clearBaseSha1();
      return this;
    }
    /**
     * <code>optional string baseSha1 = 2;</code>
     * @param value The bytes for baseSha1 to set.
     * @return This builder for chaining.
     */
    public Builder setBaseSha1Bytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setBaseSha1Bytes(value);
      return this;
    }

    /**
     * <code>optional string downloadUrl = 3;</code>
     * @return Whether the downloadUrl field is set.
     */
    @java.lang.Override
    public boolean hasDownloadUrl() {
      return instance.hasDownloadUrl();
    }
    /**
     * <code>optional string downloadUrl = 3;</code>
     * @return The downloadUrl.
     */
    @java.lang.Override
    public java.lang.String getDownloadUrl() {
      return instance.getDownloadUrl();
    }
    /**
     * <code>optional string downloadUrl = 3;</code>
     * @return The bytes for downloadUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getDownloadUrlBytes() {
      return instance.getDownloadUrlBytes();
    }
    /**
     * <code>optional string downloadUrl = 3;</code>
     * @param value The downloadUrl to set.
     * @return This builder for chaining.
     */
    public Builder setDownloadUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setDownloadUrl(value);
      return this;
    }
    /**
     * <code>optional string downloadUrl = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearDownloadUrl() {
      copyOnWrite();
      instance.clearDownloadUrl();
      return this;
    }
    /**
     * <code>optional string downloadUrl = 3;</code>
     * @param value The bytes for downloadUrl to set.
     * @return This builder for chaining.
     */
    public Builder setDownloadUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setDownloadUrlBytes(value);
      return this;
    }

    /**
     * <code>optional int32 patchFormat = 4 [default = 1];</code>
     * @return Whether the patchFormat field is set.
     */
    @java.lang.Override
    public boolean hasPatchFormat() {
      return instance.hasPatchFormat();
    }
    /**
     * <code>optional int32 patchFormat = 4 [default = 1];</code>
     * @return The patchFormat.
     */
    @java.lang.Override
    public int getPatchFormat() {
      return instance.getPatchFormat();
    }
    /**
     * <code>optional int32 patchFormat = 4 [default = 1];</code>
     * @param value The patchFormat to set.
     * @return This builder for chaining.
     */
    public Builder setPatchFormat(int value) {
      copyOnWrite();
      instance.setPatchFormat(value);
      return this;
    }
    /**
     * <code>optional int32 patchFormat = 4 [default = 1];</code>
     * @return This builder for chaining.
     */
    public Builder clearPatchFormat() {
      copyOnWrite();
      instance.clearPatchFormat();
      return this;
    }

    /**
     * <code>optional int64 maxPatchSize = 5;</code>
     * @return Whether the maxPatchSize field is set.
     */
    @java.lang.Override
    public boolean hasMaxPatchSize() {
      return instance.hasMaxPatchSize();
    }
    /**
     * <code>optional int64 maxPatchSize = 5;</code>
     * @return The maxPatchSize.
     */
    @java.lang.Override
    public long getMaxPatchSize() {
      return instance.getMaxPatchSize();
    }
    /**
     * <code>optional int64 maxPatchSize = 5;</code>
     * @param value The maxPatchSize to set.
     * @return This builder for chaining.
     */
    public Builder setMaxPatchSize(long value) {
      copyOnWrite();
      instance.setMaxPatchSize(value);
      return this;
    }
    /**
     * <code>optional int64 maxPatchSize = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearMaxPatchSize() {
      copyOnWrite();
      instance.clearMaxPatchSize();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.AndroidAppPatchData)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.AndroidAppPatchData();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "baseVersionCode_",
            "baseSha1_",
            "downloadUrl_",
            "patchFormat_",
            "maxPatchSize_",
          };
          java.lang.String info =
              "\u0001\u0005\u0000\u0001\u0001\u0005\u0005\u0000\u0000\u0000\u0001\u1004\u0000\u0002" +
              "\u1008\u0001\u0003\u1008\u0002\u0004\u1004\u0003\u0005\u1002\u0004";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.AndroidAppPatchData> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.AndroidAppPatchData.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.AndroidAppPatchData>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.AndroidAppPatchData)
  private static final finsky.protos.AndroidAppPatchData DEFAULT_INSTANCE;
  static {
    AndroidAppPatchData defaultInstance = new AndroidAppPatchData();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      AndroidAppPatchData.class, defaultInstance);
  }

  public static finsky.protos.AndroidAppPatchData getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<AndroidAppPatchData> PARSER;

  public static com.google.protobuf.Parser<AndroidAppPatchData> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
