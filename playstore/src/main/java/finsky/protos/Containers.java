// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.Containers}
 */
public  final class Containers extends
    com.google.protobuf.GeneratedMessageLite<
        Containers, Containers.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.Containers)
    ContainersOrBuilder {
  private Containers() {
  }
  public interface ContainerMetadataOrBuilder extends
      // @@protoc_insertion_point(interface_extends:finsky.protos.Containers.ContainerMetadata)
      com.google.protobuf.MessageLiteOrBuilder {

    /**
     * <code>optional string browseUrl = 1;</code>
     * @return Whether the browseUrl field is set.
     */
    boolean hasBrowseUrl();
    /**
     * <code>optional string browseUrl = 1;</code>
     * @return The browseUrl.
     */
    java.lang.String getBrowseUrl();
    /**
     * <code>optional string browseUrl = 1;</code>
     * @return The bytes for browseUrl.
     */
    com.google.protobuf.ByteString
        getBrowseUrlBytes();

    /**
     * <code>optional string nextPageUrl = 2;</code>
     * @return Whether the nextPageUrl field is set.
     */
    boolean hasNextPageUrl();
    /**
     * <code>optional string nextPageUrl = 2;</code>
     * @return The nextPageUrl.
     */
    java.lang.String getNextPageUrl();
    /**
     * <code>optional string nextPageUrl = 2;</code>
     * @return The bytes for nextPageUrl.
     */
    com.google.protobuf.ByteString
        getNextPageUrlBytes();

    /**
     * <code>optional double relevance = 3;</code>
     * @return Whether the relevance field is set.
     */
    boolean hasRelevance();
    /**
     * <code>optional double relevance = 3;</code>
     * @return The relevance.
     */
    double getRelevance();

    /**
     * <code>optional int64 estimatedResults = 4;</code>
     * @return Whether the estimatedResults field is set.
     */
    boolean hasEstimatedResults();
    /**
     * <code>optional int64 estimatedResults = 4;</code>
     * @return The estimatedResults.
     */
    long getEstimatedResults();

    /**
     * <code>optional string analyticsCookie = 5;</code>
     * @return Whether the analyticsCookie field is set.
     */
    boolean hasAnalyticsCookie();
    /**
     * <code>optional string analyticsCookie = 5;</code>
     * @return The analyticsCookie.
     */
    java.lang.String getAnalyticsCookie();
    /**
     * <code>optional string analyticsCookie = 5;</code>
     * @return The bytes for analyticsCookie.
     */
    com.google.protobuf.ByteString
        getAnalyticsCookieBytes();

    /**
     * <code>optional bool ordered = 6;</code>
     * @return Whether the ordered field is set.
     */
    boolean hasOrdered();
    /**
     * <code>optional bool ordered = 6;</code>
     * @return The ordered.
     */
    boolean getOrdered();
  }
  /**
   * Protobuf type {@code finsky.protos.Containers.ContainerMetadata}
   */
  public  static final class ContainerMetadata extends
      com.google.protobuf.GeneratedMessageLite<
          ContainerMetadata, ContainerMetadata.Builder> implements
      // @@protoc_insertion_point(message_implements:finsky.protos.Containers.ContainerMetadata)
      ContainerMetadataOrBuilder {
    private ContainerMetadata() {
      browseUrl_ = "";
      nextPageUrl_ = "";
      analyticsCookie_ = "";
    }
    private int bitField0_;
    public static final int BROWSEURL_FIELD_NUMBER = 1;
    private java.lang.String browseUrl_;
    /**
     * <code>optional string browseUrl = 1;</code>
     * @return Whether the browseUrl field is set.
     */
    @java.lang.Override
    public boolean hasBrowseUrl() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>optional string browseUrl = 1;</code>
     * @return The browseUrl.
     */
    @java.lang.Override
    public java.lang.String getBrowseUrl() {
      return browseUrl_;
    }
    /**
     * <code>optional string browseUrl = 1;</code>
     * @return The bytes for browseUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getBrowseUrlBytes() {
      return com.google.protobuf.ByteString.copyFromUtf8(browseUrl_);
    }
    /**
     * <code>optional string browseUrl = 1;</code>
     * @param value The browseUrl to set.
     */
    private void setBrowseUrl(
        java.lang.String value) {
      java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000001;
      browseUrl_ = value;
    }
    /**
     * <code>optional string browseUrl = 1;</code>
     */
    private void clearBrowseUrl() {
      bitField0_ = (bitField0_ & ~0x00000001);
      browseUrl_ = getDefaultInstance().getBrowseUrl();
    }
    /**
     * <code>optional string browseUrl = 1;</code>
     * @param value The bytes for browseUrl to set.
     */
    private void setBrowseUrlBytes(
        com.google.protobuf.ByteString value) {
      browseUrl_ = value.toStringUtf8();
      bitField0_ |= 0x00000001;
    }

    public static final int NEXTPAGEURL_FIELD_NUMBER = 2;
    private java.lang.String nextPageUrl_;
    /**
     * <code>optional string nextPageUrl = 2;</code>
     * @return Whether the nextPageUrl field is set.
     */
    @java.lang.Override
    public boolean hasNextPageUrl() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>optional string nextPageUrl = 2;</code>
     * @return The nextPageUrl.
     */
    @java.lang.Override
    public java.lang.String getNextPageUrl() {
      return nextPageUrl_;
    }
    /**
     * <code>optional string nextPageUrl = 2;</code>
     * @return The bytes for nextPageUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getNextPageUrlBytes() {
      return com.google.protobuf.ByteString.copyFromUtf8(nextPageUrl_);
    }
    /**
     * <code>optional string nextPageUrl = 2;</code>
     * @param value The nextPageUrl to set.
     */
    private void setNextPageUrl(
        java.lang.String value) {
      java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000002;
      nextPageUrl_ = value;
    }
    /**
     * <code>optional string nextPageUrl = 2;</code>
     */
    private void clearNextPageUrl() {
      bitField0_ = (bitField0_ & ~0x00000002);
      nextPageUrl_ = getDefaultInstance().getNextPageUrl();
    }
    /**
     * <code>optional string nextPageUrl = 2;</code>
     * @param value The bytes for nextPageUrl to set.
     */
    private void setNextPageUrlBytes(
        com.google.protobuf.ByteString value) {
      nextPageUrl_ = value.toStringUtf8();
      bitField0_ |= 0x00000002;
    }

    public static final int RELEVANCE_FIELD_NUMBER = 3;
    private double relevance_;
    /**
     * <code>optional double relevance = 3;</code>
     * @return Whether the relevance field is set.
     */
    @java.lang.Override
    public boolean hasRelevance() {
      return ((bitField0_ & 0x00000004) != 0);
    }
    /**
     * <code>optional double relevance = 3;</code>
     * @return The relevance.
     */
    @java.lang.Override
    public double getRelevance() {
      return relevance_;
    }
    /**
     * <code>optional double relevance = 3;</code>
     * @param value The relevance to set.
     */
    private void setRelevance(double value) {
      bitField0_ |= 0x00000004;
      relevance_ = value;
    }
    /**
     * <code>optional double relevance = 3;</code>
     */
    private void clearRelevance() {
      bitField0_ = (bitField0_ & ~0x00000004);
      relevance_ = 0D;
    }

    public static final int ESTIMATEDRESULTS_FIELD_NUMBER = 4;
    private long estimatedResults_;
    /**
     * <code>optional int64 estimatedResults = 4;</code>
     * @return Whether the estimatedResults field is set.
     */
    @java.lang.Override
    public boolean hasEstimatedResults() {
      return ((bitField0_ & 0x00000008) != 0);
    }
    /**
     * <code>optional int64 estimatedResults = 4;</code>
     * @return The estimatedResults.
     */
    @java.lang.Override
    public long getEstimatedResults() {
      return estimatedResults_;
    }
    /**
     * <code>optional int64 estimatedResults = 4;</code>
     * @param value The estimatedResults to set.
     */
    private void setEstimatedResults(long value) {
      bitField0_ |= 0x00000008;
      estimatedResults_ = value;
    }
    /**
     * <code>optional int64 estimatedResults = 4;</code>
     */
    private void clearEstimatedResults() {
      bitField0_ = (bitField0_ & ~0x00000008);
      estimatedResults_ = 0L;
    }

    public static final int ANALYTICSCOOKIE_FIELD_NUMBER = 5;
    private java.lang.String analyticsCookie_;
    /**
     * <code>optional string analyticsCookie = 5;</code>
     * @return Whether the analyticsCookie field is set.
     */
    @java.lang.Override
    public boolean hasAnalyticsCookie() {
      return ((bitField0_ & 0x00000010) != 0);
    }
    /**
     * <code>optional string analyticsCookie = 5;</code>
     * @return The analyticsCookie.
     */
    @java.lang.Override
    public java.lang.String getAnalyticsCookie() {
      return analyticsCookie_;
    }
    /**
     * <code>optional string analyticsCookie = 5;</code>
     * @return The bytes for analyticsCookie.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getAnalyticsCookieBytes() {
      return com.google.protobuf.ByteString.copyFromUtf8(analyticsCookie_);
    }
    /**
     * <code>optional string analyticsCookie = 5;</code>
     * @param value The analyticsCookie to set.
     */
    private void setAnalyticsCookie(
        java.lang.String value) {
      java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000010;
      analyticsCookie_ = value;
    }
    /**
     * <code>optional string analyticsCookie = 5;</code>
     */
    private void clearAnalyticsCookie() {
      bitField0_ = (bitField0_ & ~0x00000010);
      analyticsCookie_ = getDefaultInstance().getAnalyticsCookie();
    }
    /**
     * <code>optional string analyticsCookie = 5;</code>
     * @param value The bytes for analyticsCookie to set.
     */
    private void setAnalyticsCookieBytes(
        com.google.protobuf.ByteString value) {
      analyticsCookie_ = value.toStringUtf8();
      bitField0_ |= 0x00000010;
    }

    public static final int ORDERED_FIELD_NUMBER = 6;
    private boolean ordered_;
    /**
     * <code>optional bool ordered = 6;</code>
     * @return Whether the ordered field is set.
     */
    @java.lang.Override
    public boolean hasOrdered() {
      return ((bitField0_ & 0x00000020) != 0);
    }
    /**
     * <code>optional bool ordered = 6;</code>
     * @return The ordered.
     */
    @java.lang.Override
    public boolean getOrdered() {
      return ordered_;
    }
    /**
     * <code>optional bool ordered = 6;</code>
     * @param value The ordered to set.
     */
    private void setOrdered(boolean value) {
      bitField0_ |= 0x00000020;
      ordered_ = value;
    }
    /**
     * <code>optional bool ordered = 6;</code>
     */
    private void clearOrdered() {
      bitField0_ = (bitField0_ & ~0x00000020);
      ordered_ = false;
    }

    public static finsky.protos.Containers.ContainerMetadata parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data);
    }
    public static finsky.protos.Containers.ContainerMetadata parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data, extensionRegistry);
    }
    public static finsky.protos.Containers.ContainerMetadata parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data);
    }
    public static finsky.protos.Containers.ContainerMetadata parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data, extensionRegistry);
    }
    public static finsky.protos.Containers.ContainerMetadata parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data);
    }
    public static finsky.protos.Containers.ContainerMetadata parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, data, extensionRegistry);
    }
    public static finsky.protos.Containers.ContainerMetadata parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input);
    }
    public static finsky.protos.Containers.ContainerMetadata parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input, extensionRegistry);
    }
    public static finsky.protos.Containers.ContainerMetadata parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return parseDelimitedFrom(DEFAULT_INSTANCE, input);
    }
    public static finsky.protos.Containers.ContainerMetadata parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
    }
    public static finsky.protos.Containers.ContainerMetadata parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input);
    }
    public static finsky.protos.Containers.ContainerMetadata parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageLite.parseFrom(
          DEFAULT_INSTANCE, input, extensionRegistry);
    }

    public static Builder newBuilder() {
      return (Builder) DEFAULT_INSTANCE.createBuilder();
    }
    public static Builder newBuilder(finsky.protos.Containers.ContainerMetadata prototype) {
      return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
    }

    /**
     * Protobuf type {@code finsky.protos.Containers.ContainerMetadata}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageLite.Builder<
          finsky.protos.Containers.ContainerMetadata, Builder> implements
        // @@protoc_insertion_point(builder_implements:finsky.protos.Containers.ContainerMetadata)
        finsky.protos.Containers.ContainerMetadataOrBuilder {
      // Construct using finsky.protos.Containers.ContainerMetadata.newBuilder()
      private Builder() {
        super(DEFAULT_INSTANCE);
      }


      /**
       * <code>optional string browseUrl = 1;</code>
       * @return Whether the browseUrl field is set.
       */
      @java.lang.Override
      public boolean hasBrowseUrl() {
        return instance.hasBrowseUrl();
      }
      /**
       * <code>optional string browseUrl = 1;</code>
       * @return The browseUrl.
       */
      @java.lang.Override
      public java.lang.String getBrowseUrl() {
        return instance.getBrowseUrl();
      }
      /**
       * <code>optional string browseUrl = 1;</code>
       * @return The bytes for browseUrl.
       */
      @java.lang.Override
      public com.google.protobuf.ByteString
          getBrowseUrlBytes() {
        return instance.getBrowseUrlBytes();
      }
      /**
       * <code>optional string browseUrl = 1;</code>
       * @param value The browseUrl to set.
       * @return This builder for chaining.
       */
      public Builder setBrowseUrl(
          java.lang.String value) {
        copyOnWrite();
        instance.setBrowseUrl(value);
        return this;
      }
      /**
       * <code>optional string browseUrl = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearBrowseUrl() {
        copyOnWrite();
        instance.clearBrowseUrl();
        return this;
      }
      /**
       * <code>optional string browseUrl = 1;</code>
       * @param value The bytes for browseUrl to set.
       * @return This builder for chaining.
       */
      public Builder setBrowseUrlBytes(
          com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.setBrowseUrlBytes(value);
        return this;
      }

      /**
       * <code>optional string nextPageUrl = 2;</code>
       * @return Whether the nextPageUrl field is set.
       */
      @java.lang.Override
      public boolean hasNextPageUrl() {
        return instance.hasNextPageUrl();
      }
      /**
       * <code>optional string nextPageUrl = 2;</code>
       * @return The nextPageUrl.
       */
      @java.lang.Override
      public java.lang.String getNextPageUrl() {
        return instance.getNextPageUrl();
      }
      /**
       * <code>optional string nextPageUrl = 2;</code>
       * @return The bytes for nextPageUrl.
       */
      @java.lang.Override
      public com.google.protobuf.ByteString
          getNextPageUrlBytes() {
        return instance.getNextPageUrlBytes();
      }
      /**
       * <code>optional string nextPageUrl = 2;</code>
       * @param value The nextPageUrl to set.
       * @return This builder for chaining.
       */
      public Builder setNextPageUrl(
          java.lang.String value) {
        copyOnWrite();
        instance.setNextPageUrl(value);
        return this;
      }
      /**
       * <code>optional string nextPageUrl = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearNextPageUrl() {
        copyOnWrite();
        instance.clearNextPageUrl();
        return this;
      }
      /**
       * <code>optional string nextPageUrl = 2;</code>
       * @param value The bytes for nextPageUrl to set.
       * @return This builder for chaining.
       */
      public Builder setNextPageUrlBytes(
          com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.setNextPageUrlBytes(value);
        return this;
      }

      /**
       * <code>optional double relevance = 3;</code>
       * @return Whether the relevance field is set.
       */
      @java.lang.Override
      public boolean hasRelevance() {
        return instance.hasRelevance();
      }
      /**
       * <code>optional double relevance = 3;</code>
       * @return The relevance.
       */
      @java.lang.Override
      public double getRelevance() {
        return instance.getRelevance();
      }
      /**
       * <code>optional double relevance = 3;</code>
       * @param value The relevance to set.
       * @return This builder for chaining.
       */
      public Builder setRelevance(double value) {
        copyOnWrite();
        instance.setRelevance(value);
        return this;
      }
      /**
       * <code>optional double relevance = 3;</code>
       * @return This builder for chaining.
       */
      public Builder clearRelevance() {
        copyOnWrite();
        instance.clearRelevance();
        return this;
      }

      /**
       * <code>optional int64 estimatedResults = 4;</code>
       * @return Whether the estimatedResults field is set.
       */
      @java.lang.Override
      public boolean hasEstimatedResults() {
        return instance.hasEstimatedResults();
      }
      /**
       * <code>optional int64 estimatedResults = 4;</code>
       * @return The estimatedResults.
       */
      @java.lang.Override
      public long getEstimatedResults() {
        return instance.getEstimatedResults();
      }
      /**
       * <code>optional int64 estimatedResults = 4;</code>
       * @param value The estimatedResults to set.
       * @return This builder for chaining.
       */
      public Builder setEstimatedResults(long value) {
        copyOnWrite();
        instance.setEstimatedResults(value);
        return this;
      }
      /**
       * <code>optional int64 estimatedResults = 4;</code>
       * @return This builder for chaining.
       */
      public Builder clearEstimatedResults() {
        copyOnWrite();
        instance.clearEstimatedResults();
        return this;
      }

      /**
       * <code>optional string analyticsCookie = 5;</code>
       * @return Whether the analyticsCookie field is set.
       */
      @java.lang.Override
      public boolean hasAnalyticsCookie() {
        return instance.hasAnalyticsCookie();
      }
      /**
       * <code>optional string analyticsCookie = 5;</code>
       * @return The analyticsCookie.
       */
      @java.lang.Override
      public java.lang.String getAnalyticsCookie() {
        return instance.getAnalyticsCookie();
      }
      /**
       * <code>optional string analyticsCookie = 5;</code>
       * @return The bytes for analyticsCookie.
       */
      @java.lang.Override
      public com.google.protobuf.ByteString
          getAnalyticsCookieBytes() {
        return instance.getAnalyticsCookieBytes();
      }
      /**
       * <code>optional string analyticsCookie = 5;</code>
       * @param value The analyticsCookie to set.
       * @return This builder for chaining.
       */
      public Builder setAnalyticsCookie(
          java.lang.String value) {
        copyOnWrite();
        instance.setAnalyticsCookie(value);
        return this;
      }
      /**
       * <code>optional string analyticsCookie = 5;</code>
       * @return This builder for chaining.
       */
      public Builder clearAnalyticsCookie() {
        copyOnWrite();
        instance.clearAnalyticsCookie();
        return this;
      }
      /**
       * <code>optional string analyticsCookie = 5;</code>
       * @param value The bytes for analyticsCookie to set.
       * @return This builder for chaining.
       */
      public Builder setAnalyticsCookieBytes(
          com.google.protobuf.ByteString value) {
        copyOnWrite();
        instance.setAnalyticsCookieBytes(value);
        return this;
      }

      /**
       * <code>optional bool ordered = 6;</code>
       * @return Whether the ordered field is set.
       */
      @java.lang.Override
      public boolean hasOrdered() {
        return instance.hasOrdered();
      }
      /**
       * <code>optional bool ordered = 6;</code>
       * @return The ordered.
       */
      @java.lang.Override
      public boolean getOrdered() {
        return instance.getOrdered();
      }
      /**
       * <code>optional bool ordered = 6;</code>
       * @param value The ordered to set.
       * @return This builder for chaining.
       */
      public Builder setOrdered(boolean value) {
        copyOnWrite();
        instance.setOrdered(value);
        return this;
      }
      /**
       * <code>optional bool ordered = 6;</code>
       * @return This builder for chaining.
       */
      public Builder clearOrdered() {
        copyOnWrite();
        instance.clearOrdered();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:finsky.protos.Containers.ContainerMetadata)
    }
    @java.lang.Override
    @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
    protected final java.lang.Object dynamicMethod(
        com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
        java.lang.Object arg0, java.lang.Object arg1) {
      switch (method) {
        case NEW_MUTABLE_INSTANCE: {
          return new finsky.protos.Containers.ContainerMetadata();
        }
        case NEW_BUILDER: {
          return new Builder();
        }
        case BUILD_MESSAGE_INFO: {
            java.lang.Object[] objects = new java.lang.Object[] {
              "bitField0_",
              "browseUrl_",
              "nextPageUrl_",
              "relevance_",
              "estimatedResults_",
              "analyticsCookie_",
              "ordered_",
            };
            java.lang.String info =
                "\u0001\u0006\u0000\u0001\u0001\u0006\u0006\u0000\u0000\u0000\u0001\u1008\u0000\u0002" +
                "\u1008\u0001\u0003\u1000\u0002\u0004\u1002\u0003\u0005\u1008\u0004\u0006\u1007\u0005" +
                "";
            return newMessageInfo(DEFAULT_INSTANCE, info, objects);
        }
        // fall through
        case GET_DEFAULT_INSTANCE: {
          return DEFAULT_INSTANCE;
        }
        case GET_PARSER: {
          com.google.protobuf.Parser<finsky.protos.Containers.ContainerMetadata> parser = PARSER;
          if (parser == null) {
            synchronized (finsky.protos.Containers.ContainerMetadata.class) {
              parser = PARSER;
              if (parser == null) {
                parser =
                    new DefaultInstanceBasedParser<finsky.protos.Containers.ContainerMetadata>(
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


    // @@protoc_insertion_point(class_scope:finsky.protos.Containers.ContainerMetadata)
    private static final finsky.protos.Containers.ContainerMetadata DEFAULT_INSTANCE;
    static {
      ContainerMetadata defaultInstance = new ContainerMetadata();
      // New instances are implicitly immutable so no need to make
      // immutable.
      DEFAULT_INSTANCE = defaultInstance;
      com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
        ContainerMetadata.class, defaultInstance);
    }

    public static finsky.protos.Containers.ContainerMetadata getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static volatile com.google.protobuf.Parser<ContainerMetadata> PARSER;

    public static com.google.protobuf.Parser<ContainerMetadata> parser() {
      return DEFAULT_INSTANCE.getParserForType();
    }
  }

  public static finsky.protos.Containers parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Containers parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Containers parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Containers parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Containers parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Containers parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Containers parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Containers parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.Containers parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Containers parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.Containers parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Containers parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.Containers prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.Containers}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.Containers, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.Containers)
      finsky.protos.ContainersOrBuilder {
    // Construct using finsky.protos.Containers.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    // @@protoc_insertion_point(builder_scope:finsky.protos.Containers)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.Containers();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = null;java.lang.String info =
              "\u0001\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.Containers> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.Containers.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.Containers>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.Containers)
  private static final finsky.protos.Containers DEFAULT_INSTANCE;
  static {
    Containers defaultInstance = new Containers();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Containers.class, defaultInstance);
  }

  public static finsky.protos.Containers getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Containers> PARSER;

  public static com.google.protobuf.Parser<Containers> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
