// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.ListResponse}
 */
public  final class ListResponse extends
    com.google.protobuf.GeneratedMessageLite<
        ListResponse, ListResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.ListResponse)
    ListResponseOrBuilder {
  private ListResponse() {
    bucket_ = emptyProtobufList();
    doc_ = emptyProtobufList();
  }
  public static final int BUCKET_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.ProtobufList<finsky.protos.Bucket> bucket_;
  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  @java.lang.Override
  public java.util.List<finsky.protos.Bucket> getBucketList() {
    return bucket_;
  }
  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  public java.util.List<? extends finsky.protos.BucketOrBuilder> 
      getBucketOrBuilderList() {
    return bucket_;
  }
  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  @java.lang.Override
  public int getBucketCount() {
    return bucket_.size();
  }
  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  @java.lang.Override
  public finsky.protos.Bucket getBucket(int index) {
    return bucket_.get(index);
  }
  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  public finsky.protos.BucketOrBuilder getBucketOrBuilder(
      int index) {
    return bucket_.get(index);
  }
  private void ensureBucketIsMutable() {
    com.google.protobuf.Internal.ProtobufList<finsky.protos.Bucket> tmp = bucket_;
    if (!tmp.isModifiable()) {
      bucket_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  private void setBucket(
      int index, finsky.protos.Bucket value) {
    value.getClass();
  ensureBucketIsMutable();
    bucket_.set(index, value);
  }
  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  private void addBucket(finsky.protos.Bucket value) {
    value.getClass();
  ensureBucketIsMutable();
    bucket_.add(value);
  }
  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  private void addBucket(
      int index, finsky.protos.Bucket value) {
    value.getClass();
  ensureBucketIsMutable();
    bucket_.add(index, value);
  }
  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  private void addAllBucket(
      java.lang.Iterable<? extends finsky.protos.Bucket> values) {
    ensureBucketIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, bucket_);
  }
  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  private void clearBucket() {
    bucket_ = emptyProtobufList();
  }
  /**
   * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
   */
  private void removeBucket(int index) {
    ensureBucketIsMutable();
    bucket_.remove(index);
  }

  public static final int DOC_FIELD_NUMBER = 2;
  private com.google.protobuf.Internal.ProtobufList<finsky.protos.DocV2> doc_;
  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  @java.lang.Override
  public java.util.List<finsky.protos.DocV2> getDocList() {
    return doc_;
  }
  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  public java.util.List<? extends finsky.protos.DocV2OrBuilder> 
      getDocOrBuilderList() {
    return doc_;
  }
  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  @java.lang.Override
  public int getDocCount() {
    return doc_.size();
  }
  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  @java.lang.Override
  public finsky.protos.DocV2 getDoc(int index) {
    return doc_.get(index);
  }
  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  public finsky.protos.DocV2OrBuilder getDocOrBuilder(
      int index) {
    return doc_.get(index);
  }
  private void ensureDocIsMutable() {
    com.google.protobuf.Internal.ProtobufList<finsky.protos.DocV2> tmp = doc_;
    if (!tmp.isModifiable()) {
      doc_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  private void setDoc(
      int index, finsky.protos.DocV2 value) {
    value.getClass();
  ensureDocIsMutable();
    doc_.set(index, value);
  }
  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  private void addDoc(finsky.protos.DocV2 value) {
    value.getClass();
  ensureDocIsMutable();
    doc_.add(value);
  }
  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  private void addDoc(
      int index, finsky.protos.DocV2 value) {
    value.getClass();
  ensureDocIsMutable();
    doc_.add(index, value);
  }
  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  private void addAllDoc(
      java.lang.Iterable<? extends finsky.protos.DocV2> values) {
    ensureDocIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, doc_);
  }
  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  private void clearDoc() {
    doc_ = emptyProtobufList();
  }
  /**
   * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
   */
  private void removeDoc(int index) {
    ensureDocIsMutable();
    doc_.remove(index);
  }

  public static finsky.protos.ListResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.ListResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.ListResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.ListResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.ListResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.ListResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.ListResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.ListResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.ListResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.ListResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.ListResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.ListResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.ListResponse prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.ListResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.ListResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.ListResponse)
      finsky.protos.ListResponseOrBuilder {
    // Construct using finsky.protos.ListResponse.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    @java.lang.Override
    public java.util.List<finsky.protos.Bucket> getBucketList() {
      return java.util.Collections.unmodifiableList(
          instance.getBucketList());
    }
    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    @java.lang.Override
    public int getBucketCount() {
      return instance.getBucketCount();
    }/**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    @java.lang.Override
    public finsky.protos.Bucket getBucket(int index) {
      return instance.getBucket(index);
    }
    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    public Builder setBucket(
        int index, finsky.protos.Bucket value) {
      copyOnWrite();
      instance.setBucket(index, value);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    public Builder setBucket(
        int index, finsky.protos.Bucket.Builder builderForValue) {
      copyOnWrite();
      instance.setBucket(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    public Builder addBucket(finsky.protos.Bucket value) {
      copyOnWrite();
      instance.addBucket(value);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    public Builder addBucket(
        int index, finsky.protos.Bucket value) {
      copyOnWrite();
      instance.addBucket(index, value);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    public Builder addBucket(
        finsky.protos.Bucket.Builder builderForValue) {
      copyOnWrite();
      instance.addBucket(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    public Builder addBucket(
        int index, finsky.protos.Bucket.Builder builderForValue) {
      copyOnWrite();
      instance.addBucket(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    public Builder addAllBucket(
        java.lang.Iterable<? extends finsky.protos.Bucket> values) {
      copyOnWrite();
      instance.addAllBucket(values);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    public Builder clearBucket() {
      copyOnWrite();
      instance.clearBucket();
      return this;
    }
    /**
     * <code>repeated .finsky.protos.Bucket bucket = 1;</code>
     */
    public Builder removeBucket(int index) {
      copyOnWrite();
      instance.removeBucket(index);
      return this;
    }

    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    @java.lang.Override
    public java.util.List<finsky.protos.DocV2> getDocList() {
      return java.util.Collections.unmodifiableList(
          instance.getDocList());
    }
    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    @java.lang.Override
    public int getDocCount() {
      return instance.getDocCount();
    }/**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    @java.lang.Override
    public finsky.protos.DocV2 getDoc(int index) {
      return instance.getDoc(index);
    }
    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    public Builder setDoc(
        int index, finsky.protos.DocV2 value) {
      copyOnWrite();
      instance.setDoc(index, value);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    public Builder setDoc(
        int index, finsky.protos.DocV2.Builder builderForValue) {
      copyOnWrite();
      instance.setDoc(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    public Builder addDoc(finsky.protos.DocV2 value) {
      copyOnWrite();
      instance.addDoc(value);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    public Builder addDoc(
        int index, finsky.protos.DocV2 value) {
      copyOnWrite();
      instance.addDoc(index, value);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    public Builder addDoc(
        finsky.protos.DocV2.Builder builderForValue) {
      copyOnWrite();
      instance.addDoc(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    public Builder addDoc(
        int index, finsky.protos.DocV2.Builder builderForValue) {
      copyOnWrite();
      instance.addDoc(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    public Builder addAllDoc(
        java.lang.Iterable<? extends finsky.protos.DocV2> values) {
      copyOnWrite();
      instance.addAllDoc(values);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    public Builder clearDoc() {
      copyOnWrite();
      instance.clearDoc();
      return this;
    }
    /**
     * <code>repeated .finsky.protos.DocV2 doc = 2;</code>
     */
    public Builder removeDoc(int index) {
      copyOnWrite();
      instance.removeDoc(index);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.ListResponse)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.ListResponse();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bucket_",
            finsky.protos.Bucket.class,
            "doc_",
            finsky.protos.DocV2.class,
          };
          java.lang.String info =
              "\u0001\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0002\u0000\u0001\u001b\u0002\u001b" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.ListResponse> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.ListResponse.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.ListResponse>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.ListResponse)
  private static final finsky.protos.ListResponse DEFAULT_INSTANCE;
  static {
    ListResponse defaultInstance = new ListResponse();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ListResponse.class, defaultInstance);
  }

  public static finsky.protos.ListResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ListResponse> PARSER;

  public static com.google.protobuf.Parser<ListResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
