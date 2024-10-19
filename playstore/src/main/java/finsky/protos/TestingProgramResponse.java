// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: messages.proto
// Protobuf Java Version: 4.28.2

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.TestingProgramResponse}
 */
public  final class TestingProgramResponse extends
    com.google.protobuf.GeneratedMessageLite<
        TestingProgramResponse, TestingProgramResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.TestingProgramResponse)
    TestingProgramResponseOrBuilder {
  private TestingProgramResponse() {
  }
  private int bitField0_;
  public static final int RESULT_FIELD_NUMBER = 2;
  private finsky.protos.TestingProgramResult result_;
  /**
   * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
   */
  @java.lang.Override
  public boolean hasResult() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
   */
  @java.lang.Override
  public finsky.protos.TestingProgramResult getResult() {
    return result_ == null ? finsky.protos.TestingProgramResult.getDefaultInstance() : result_;
  }
  /**
   * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
   */
  private void setResult(finsky.protos.TestingProgramResult value) {
    value.getClass();
  result_ = value;
    bitField0_ |= 0x00000001;
    }
  /**
   * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeResult(finsky.protos.TestingProgramResult value) {
    value.getClass();
  if (result_ != null &&
        result_ != finsky.protos.TestingProgramResult.getDefaultInstance()) {
      result_ =
        finsky.protos.TestingProgramResult.newBuilder(result_).mergeFrom(value).buildPartial();
    } else {
      result_ = value;
    }
    bitField0_ |= 0x00000001;
  }
  /**
   * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
   */
  private void clearResult() {  result_ = null;
    bitField0_ = (bitField0_ & ~0x00000001);
  }

  public static finsky.protos.TestingProgramResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.TestingProgramResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.TestingProgramResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.TestingProgramResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.TestingProgramResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.TestingProgramResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.TestingProgramResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.TestingProgramResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static finsky.protos.TestingProgramResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }

  public static finsky.protos.TestingProgramResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.TestingProgramResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.TestingProgramResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.TestingProgramResponse prototype) {
    return DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.TestingProgramResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.TestingProgramResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.TestingProgramResponse)
      finsky.protos.TestingProgramResponseOrBuilder {
    // Construct using finsky.protos.TestingProgramResponse.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
     */
    @java.lang.Override
    public boolean hasResult() {
      return instance.hasResult();
    }
    /**
     * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
     */
    @java.lang.Override
    public finsky.protos.TestingProgramResult getResult() {
      return instance.getResult();
    }
    /**
     * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
     */
    public Builder setResult(finsky.protos.TestingProgramResult value) {
      copyOnWrite();
      instance.setResult(value);
      return this;
      }
    /**
     * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
     */
    public Builder setResult(
        finsky.protos.TestingProgramResult.Builder builderForValue) {
      copyOnWrite();
      instance.setResult(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
     */
    public Builder mergeResult(finsky.protos.TestingProgramResult value) {
      copyOnWrite();
      instance.mergeResult(value);
      return this;
    }
    /**
     * <code>optional .finsky.protos.TestingProgramResult result = 2;</code>
     */
    public Builder clearResult() {  copyOnWrite();
      instance.clearResult();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.TestingProgramResponse)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.TestingProgramResponse();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "result_",
          };
          java.lang.String info =
              "\u0001\u0001\u0000\u0001\u0002\u0002\u0001\u0000\u0000\u0000\u0002\u1009\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.TestingProgramResponse> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.TestingProgramResponse.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.TestingProgramResponse>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.TestingProgramResponse)
  private static final finsky.protos.TestingProgramResponse DEFAULT_INSTANCE;
  static {
    TestingProgramResponse defaultInstance = new TestingProgramResponse();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      TestingProgramResponse.class, defaultInstance);
  }

  public static finsky.protos.TestingProgramResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<TestingProgramResponse> PARSER;

  public static com.google.protobuf.Parser<TestingProgramResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

