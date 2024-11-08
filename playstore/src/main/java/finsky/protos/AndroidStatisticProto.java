// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: messages.proto
// Protobuf Java Version: 4.28.2

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.AndroidStatisticProto}
 */
public  final class AndroidStatisticProto extends
    com.google.protobuf.GeneratedMessageLite<
        AndroidStatisticProto, AndroidStatisticProto.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.AndroidStatisticProto)
    AndroidStatisticProtoOrBuilder {
  private AndroidStatisticProto() {
    tag_ = "";
  }
  private int bitField0_;
  public static final int TAG_FIELD_NUMBER = 1;
  private java.lang.String tag_;
  /**
   * <code>optional string tag = 1;</code>
   * @return Whether the tag field is set.
   */
  @java.lang.Override
  public boolean hasTag() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional string tag = 1;</code>
   * @return The tag.
   */
  @java.lang.Override
  public java.lang.String getTag() {
    return tag_;
  }
  /**
   * <code>optional string tag = 1;</code>
   * @return The bytes for tag.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getTagBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(tag_);
  }
  /**
   * <code>optional string tag = 1;</code>
   * @param value The tag to set.
   */
  private void setTag(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000001;
    tag_ = value;
  }
  /**
   * <code>optional string tag = 1;</code>
   */
  private void clearTag() {
    bitField0_ = (bitField0_ & ~0x00000001);
    tag_ = getDefaultInstance().getTag();
  }
  /**
   * <code>optional string tag = 1;</code>
   * @param value The bytes for tag to set.
   */
  private void setTagBytes(
      com.google.protobuf.ByteString value) {
    tag_ = value.toStringUtf8();
    bitField0_ |= 0x00000001;
  }

  public static final int COUNT_FIELD_NUMBER = 2;
  private int count_;
  /**
   * <code>optional int32 count = 2;</code>
   * @return Whether the count field is set.
   */
  @java.lang.Override
  public boolean hasCount() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional int32 count = 2;</code>
   * @return The count.
   */
  @java.lang.Override
  public int getCount() {
    return count_;
  }
  /**
   * <code>optional int32 count = 2;</code>
   * @param value The count to set.
   */
  private void setCount(int value) {
    bitField0_ |= 0x00000002;
    count_ = value;
  }
  /**
   * <code>optional int32 count = 2;</code>
   */
  private void clearCount() {
    bitField0_ = (bitField0_ & ~0x00000002);
    count_ = 0;
  }

  public static final int SUM_FIELD_NUMBER = 3;
  private float sum_;
  /**
   * <code>optional float sum = 3;</code>
   * @return Whether the sum field is set.
   */
  @java.lang.Override
  public boolean hasSum() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <code>optional float sum = 3;</code>
   * @return The sum.
   */
  @java.lang.Override
  public float getSum() {
    return sum_;
  }
  /**
   * <code>optional float sum = 3;</code>
   * @param value The sum to set.
   */
  private void setSum(float value) {
    bitField0_ |= 0x00000004;
    sum_ = value;
  }
  /**
   * <code>optional float sum = 3;</code>
   */
  private void clearSum() {
    bitField0_ = (bitField0_ & ~0x00000004);
    sum_ = 0F;
  }

  public static finsky.protos.AndroidStatisticProto parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.AndroidStatisticProto parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.AndroidStatisticProto parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.AndroidStatisticProto parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.AndroidStatisticProto parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.AndroidStatisticProto parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.AndroidStatisticProto parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.AndroidStatisticProto parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static finsky.protos.AndroidStatisticProto parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }

  public static finsky.protos.AndroidStatisticProto parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.AndroidStatisticProto parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.AndroidStatisticProto parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.AndroidStatisticProto prototype) {
    return DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.AndroidStatisticProto}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.AndroidStatisticProto, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.AndroidStatisticProto)
      finsky.protos.AndroidStatisticProtoOrBuilder {
    // Construct using finsky.protos.AndroidStatisticProto.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional string tag = 1;</code>
     * @return Whether the tag field is set.
     */
    @java.lang.Override
    public boolean hasTag() {
      return instance.hasTag();
    }
    /**
     * <code>optional string tag = 1;</code>
     * @return The tag.
     */
    @java.lang.Override
    public java.lang.String getTag() {
      return instance.getTag();
    }
    /**
     * <code>optional string tag = 1;</code>
     * @return The bytes for tag.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getTagBytes() {
      return instance.getTagBytes();
    }
    /**
     * <code>optional string tag = 1;</code>
     * @param value The tag to set.
     * @return This builder for chaining.
     */
    public Builder setTag(
        java.lang.String value) {
      copyOnWrite();
      instance.setTag(value);
      return this;
    }
    /**
     * <code>optional string tag = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearTag() {
      copyOnWrite();
      instance.clearTag();
      return this;
    }
    /**
     * <code>optional string tag = 1;</code>
     * @param value The bytes for tag to set.
     * @return This builder for chaining.
     */
    public Builder setTagBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setTagBytes(value);
      return this;
    }

    /**
     * <code>optional int32 count = 2;</code>
     * @return Whether the count field is set.
     */
    @java.lang.Override
    public boolean hasCount() {
      return instance.hasCount();
    }
    /**
     * <code>optional int32 count = 2;</code>
     * @return The count.
     */
    @java.lang.Override
    public int getCount() {
      return instance.getCount();
    }
    /**
     * <code>optional int32 count = 2;</code>
     * @param value The count to set.
     * @return This builder for chaining.
     */
    public Builder setCount(int value) {
      copyOnWrite();
      instance.setCount(value);
      return this;
    }
    /**
     * <code>optional int32 count = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearCount() {
      copyOnWrite();
      instance.clearCount();
      return this;
    }

    /**
     * <code>optional float sum = 3;</code>
     * @return Whether the sum field is set.
     */
    @java.lang.Override
    public boolean hasSum() {
      return instance.hasSum();
    }
    /**
     * <code>optional float sum = 3;</code>
     * @return The sum.
     */
    @java.lang.Override
    public float getSum() {
      return instance.getSum();
    }
    /**
     * <code>optional float sum = 3;</code>
     * @param value The sum to set.
     * @return This builder for chaining.
     */
    public Builder setSum(float value) {
      copyOnWrite();
      instance.setSum(value);
      return this;
    }
    /**
     * <code>optional float sum = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearSum() {
      copyOnWrite();
      instance.clearSum();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.AndroidStatisticProto)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.AndroidStatisticProto();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "tag_",
            "count_",
            "sum_",
          };
          java.lang.String info =
              "\u0001\u0003\u0000\u0001\u0001\u0003\u0003\u0000\u0000\u0000\u0001\u1008\u0000\u0002" +
              "\u1004\u0001\u0003\u1001\u0002";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.AndroidStatisticProto> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.AndroidStatisticProto.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.AndroidStatisticProto>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.AndroidStatisticProto)
  private static final finsky.protos.AndroidStatisticProto DEFAULT_INSTANCE;
  static {
    AndroidStatisticProto defaultInstance = new AndroidStatisticProto();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      AndroidStatisticProto.class, defaultInstance);
  }

  public static finsky.protos.AndroidStatisticProto getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<AndroidStatisticProto> PARSER;

  public static com.google.protobuf.Parser<AndroidStatisticProto> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
