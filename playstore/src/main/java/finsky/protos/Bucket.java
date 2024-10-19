// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: messages.proto
// Protobuf Java Version: 4.28.2

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.Bucket}
 */
public  final class Bucket extends
    com.google.protobuf.GeneratedMessageLite<
        Bucket, Bucket.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.Bucket)
    BucketOrBuilder {
  private Bucket() {
    title_ = "";
    iconUrl_ = "";
    fullContentsUrl_ = "";
    analyticsCookie_ = "";
    fullContentsListUrl_ = "";
    nextPageUrl_ = "";
  }
  private int bitField0_;
  public static final int MULTICORPUS_FIELD_NUMBER = 2;
  private boolean multiCorpus_;
  /**
   * <code>optional bool multiCorpus = 2;</code>
   * @return Whether the multiCorpus field is set.
   */
  @java.lang.Override
  public boolean hasMultiCorpus() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional bool multiCorpus = 2;</code>
   * @return The multiCorpus.
   */
  @java.lang.Override
  public boolean getMultiCorpus() {
    return multiCorpus_;
  }
  /**
   * <code>optional bool multiCorpus = 2;</code>
   * @param value The multiCorpus to set.
   */
  private void setMultiCorpus(boolean value) {
    bitField0_ |= 0x00000001;
    multiCorpus_ = value;
  }
  /**
   * <code>optional bool multiCorpus = 2;</code>
   */
  private void clearMultiCorpus() {
    bitField0_ = (bitField0_ & ~0x00000001);
    multiCorpus_ = false;
  }

  public static final int TITLE_FIELD_NUMBER = 3;
  private java.lang.String title_;
  /**
   * <code>optional string title = 3;</code>
   * @return Whether the title field is set.
   */
  @java.lang.Override
  public boolean hasTitle() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional string title = 3;</code>
   * @return The title.
   */
  @java.lang.Override
  public java.lang.String getTitle() {
    return title_;
  }
  /**
   * <code>optional string title = 3;</code>
   * @return The bytes for title.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getTitleBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(title_);
  }
  /**
   * <code>optional string title = 3;</code>
   * @param value The title to set.
   */
  private void setTitle(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000002;
    title_ = value;
  }
  /**
   * <code>optional string title = 3;</code>
   */
  private void clearTitle() {
    bitField0_ = (bitField0_ & ~0x00000002);
    title_ = getDefaultInstance().getTitle();
  }
  /**
   * <code>optional string title = 3;</code>
   * @param value The bytes for title to set.
   */
  private void setTitleBytes(
      com.google.protobuf.ByteString value) {
    title_ = value.toStringUtf8();
    bitField0_ |= 0x00000002;
  }

  public static final int ICONURL_FIELD_NUMBER = 4;
  private java.lang.String iconUrl_;
  /**
   * <code>optional string iconUrl = 4;</code>
   * @return Whether the iconUrl field is set.
   */
  @java.lang.Override
  public boolean hasIconUrl() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <code>optional string iconUrl = 4;</code>
   * @return The iconUrl.
   */
  @java.lang.Override
  public java.lang.String getIconUrl() {
    return iconUrl_;
  }
  /**
   * <code>optional string iconUrl = 4;</code>
   * @return The bytes for iconUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getIconUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(iconUrl_);
  }
  /**
   * <code>optional string iconUrl = 4;</code>
   * @param value The iconUrl to set.
   */
  private void setIconUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000004;
    iconUrl_ = value;
  }
  /**
   * <code>optional string iconUrl = 4;</code>
   */
  private void clearIconUrl() {
    bitField0_ = (bitField0_ & ~0x00000004);
    iconUrl_ = getDefaultInstance().getIconUrl();
  }
  /**
   * <code>optional string iconUrl = 4;</code>
   * @param value The bytes for iconUrl to set.
   */
  private void setIconUrlBytes(
      com.google.protobuf.ByteString value) {
    iconUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000004;
  }

  public static final int FULLCONTENTSURL_FIELD_NUMBER = 5;
  private java.lang.String fullContentsUrl_;
  /**
   * <code>optional string fullContentsUrl = 5;</code>
   * @return Whether the fullContentsUrl field is set.
   */
  @java.lang.Override
  public boolean hasFullContentsUrl() {
    return ((bitField0_ & 0x00000008) != 0);
  }
  /**
   * <code>optional string fullContentsUrl = 5;</code>
   * @return The fullContentsUrl.
   */
  @java.lang.Override
  public java.lang.String getFullContentsUrl() {
    return fullContentsUrl_;
  }
  /**
   * <code>optional string fullContentsUrl = 5;</code>
   * @return The bytes for fullContentsUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getFullContentsUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(fullContentsUrl_);
  }
  /**
   * <code>optional string fullContentsUrl = 5;</code>
   * @param value The fullContentsUrl to set.
   */
  private void setFullContentsUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000008;
    fullContentsUrl_ = value;
  }
  /**
   * <code>optional string fullContentsUrl = 5;</code>
   */
  private void clearFullContentsUrl() {
    bitField0_ = (bitField0_ & ~0x00000008);
    fullContentsUrl_ = getDefaultInstance().getFullContentsUrl();
  }
  /**
   * <code>optional string fullContentsUrl = 5;</code>
   * @param value The bytes for fullContentsUrl to set.
   */
  private void setFullContentsUrlBytes(
      com.google.protobuf.ByteString value) {
    fullContentsUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000008;
  }

  public static final int RELEVANCE_FIELD_NUMBER = 6;
  private double relevance_;
  /**
   * <code>optional double relevance = 6;</code>
   * @return Whether the relevance field is set.
   */
  @java.lang.Override
  public boolean hasRelevance() {
    return ((bitField0_ & 0x00000010) != 0);
  }
  /**
   * <code>optional double relevance = 6;</code>
   * @return The relevance.
   */
  @java.lang.Override
  public double getRelevance() {
    return relevance_;
  }
  /**
   * <code>optional double relevance = 6;</code>
   * @param value The relevance to set.
   */
  private void setRelevance(double value) {
    bitField0_ |= 0x00000010;
    relevance_ = value;
  }
  /**
   * <code>optional double relevance = 6;</code>
   */
  private void clearRelevance() {
    bitField0_ = (bitField0_ & ~0x00000010);
    relevance_ = 0D;
  }

  public static final int ESTIMATEDRESULTS_FIELD_NUMBER = 7;
  private long estimatedResults_;
  /**
   * <code>optional int64 estimatedResults = 7;</code>
   * @return Whether the estimatedResults field is set.
   */
  @java.lang.Override
  public boolean hasEstimatedResults() {
    return ((bitField0_ & 0x00000020) != 0);
  }
  /**
   * <code>optional int64 estimatedResults = 7;</code>
   * @return The estimatedResults.
   */
  @java.lang.Override
  public long getEstimatedResults() {
    return estimatedResults_;
  }
  /**
   * <code>optional int64 estimatedResults = 7;</code>
   * @param value The estimatedResults to set.
   */
  private void setEstimatedResults(long value) {
    bitField0_ |= 0x00000020;
    estimatedResults_ = value;
  }
  /**
   * <code>optional int64 estimatedResults = 7;</code>
   */
  private void clearEstimatedResults() {
    bitField0_ = (bitField0_ & ~0x00000020);
    estimatedResults_ = 0L;
  }

  public static final int ANALYTICSCOOKIE_FIELD_NUMBER = 8;
  private java.lang.String analyticsCookie_;
  /**
   * <code>optional string analyticsCookie = 8;</code>
   * @return Whether the analyticsCookie field is set.
   */
  @java.lang.Override
  public boolean hasAnalyticsCookie() {
    return ((bitField0_ & 0x00000040) != 0);
  }
  /**
   * <code>optional string analyticsCookie = 8;</code>
   * @return The analyticsCookie.
   */
  @java.lang.Override
  public java.lang.String getAnalyticsCookie() {
    return analyticsCookie_;
  }
  /**
   * <code>optional string analyticsCookie = 8;</code>
   * @return The bytes for analyticsCookie.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getAnalyticsCookieBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(analyticsCookie_);
  }
  /**
   * <code>optional string analyticsCookie = 8;</code>
   * @param value The analyticsCookie to set.
   */
  private void setAnalyticsCookie(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000040;
    analyticsCookie_ = value;
  }
  /**
   * <code>optional string analyticsCookie = 8;</code>
   */
  private void clearAnalyticsCookie() {
    bitField0_ = (bitField0_ & ~0x00000040);
    analyticsCookie_ = getDefaultInstance().getAnalyticsCookie();
  }
  /**
   * <code>optional string analyticsCookie = 8;</code>
   * @param value The bytes for analyticsCookie to set.
   */
  private void setAnalyticsCookieBytes(
      com.google.protobuf.ByteString value) {
    analyticsCookie_ = value.toStringUtf8();
    bitField0_ |= 0x00000040;
  }

  public static final int FULLCONTENTSLISTURL_FIELD_NUMBER = 9;
  private java.lang.String fullContentsListUrl_;
  /**
   * <code>optional string fullContentsListUrl = 9;</code>
   * @return Whether the fullContentsListUrl field is set.
   */
  @java.lang.Override
  public boolean hasFullContentsListUrl() {
    return ((bitField0_ & 0x00000080) != 0);
  }
  /**
   * <code>optional string fullContentsListUrl = 9;</code>
   * @return The fullContentsListUrl.
   */
  @java.lang.Override
  public java.lang.String getFullContentsListUrl() {
    return fullContentsListUrl_;
  }
  /**
   * <code>optional string fullContentsListUrl = 9;</code>
   * @return The bytes for fullContentsListUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getFullContentsListUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(fullContentsListUrl_);
  }
  /**
   * <code>optional string fullContentsListUrl = 9;</code>
   * @param value The fullContentsListUrl to set.
   */
  private void setFullContentsListUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000080;
    fullContentsListUrl_ = value;
  }
  /**
   * <code>optional string fullContentsListUrl = 9;</code>
   */
  private void clearFullContentsListUrl() {
    bitField0_ = (bitField0_ & ~0x00000080);
    fullContentsListUrl_ = getDefaultInstance().getFullContentsListUrl();
  }
  /**
   * <code>optional string fullContentsListUrl = 9;</code>
   * @param value The bytes for fullContentsListUrl to set.
   */
  private void setFullContentsListUrlBytes(
      com.google.protobuf.ByteString value) {
    fullContentsListUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000080;
  }

  public static final int NEXTPAGEURL_FIELD_NUMBER = 10;
  private java.lang.String nextPageUrl_;
  /**
   * <code>optional string nextPageUrl = 10;</code>
   * @return Whether the nextPageUrl field is set.
   */
  @java.lang.Override
  public boolean hasNextPageUrl() {
    return ((bitField0_ & 0x00000100) != 0);
  }
  /**
   * <code>optional string nextPageUrl = 10;</code>
   * @return The nextPageUrl.
   */
  @java.lang.Override
  public java.lang.String getNextPageUrl() {
    return nextPageUrl_;
  }
  /**
   * <code>optional string nextPageUrl = 10;</code>
   * @return The bytes for nextPageUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNextPageUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(nextPageUrl_);
  }
  /**
   * <code>optional string nextPageUrl = 10;</code>
   * @param value The nextPageUrl to set.
   */
  private void setNextPageUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000100;
    nextPageUrl_ = value;
  }
  /**
   * <code>optional string nextPageUrl = 10;</code>
   */
  private void clearNextPageUrl() {
    bitField0_ = (bitField0_ & ~0x00000100);
    nextPageUrl_ = getDefaultInstance().getNextPageUrl();
  }
  /**
   * <code>optional string nextPageUrl = 10;</code>
   * @param value The bytes for nextPageUrl to set.
   */
  private void setNextPageUrlBytes(
      com.google.protobuf.ByteString value) {
    nextPageUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000100;
  }

  public static final int ORDERED_FIELD_NUMBER = 11;
  private boolean ordered_;
  /**
   * <code>optional bool ordered = 11;</code>
   * @return Whether the ordered field is set.
   */
  @java.lang.Override
  public boolean hasOrdered() {
    return ((bitField0_ & 0x00000200) != 0);
  }
  /**
   * <code>optional bool ordered = 11;</code>
   * @return The ordered.
   */
  @java.lang.Override
  public boolean getOrdered() {
    return ordered_;
  }
  /**
   * <code>optional bool ordered = 11;</code>
   * @param value The ordered to set.
   */
  private void setOrdered(boolean value) {
    bitField0_ |= 0x00000200;
    ordered_ = value;
  }
  /**
   * <code>optional bool ordered = 11;</code>
   */
  private void clearOrdered() {
    bitField0_ = (bitField0_ & ~0x00000200);
    ordered_ = false;
  }

  public static finsky.protos.Bucket parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Bucket parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Bucket parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Bucket parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Bucket parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.Bucket parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.Bucket parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Bucket parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static finsky.protos.Bucket parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }

  public static finsky.protos.Bucket parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.Bucket parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.Bucket parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.Bucket prototype) {
    return DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.Bucket}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.Bucket, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.Bucket)
      finsky.protos.BucketOrBuilder {
    // Construct using finsky.protos.Bucket.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional bool multiCorpus = 2;</code>
     * @return Whether the multiCorpus field is set.
     */
    @java.lang.Override
    public boolean hasMultiCorpus() {
      return instance.hasMultiCorpus();
    }
    /**
     * <code>optional bool multiCorpus = 2;</code>
     * @return The multiCorpus.
     */
    @java.lang.Override
    public boolean getMultiCorpus() {
      return instance.getMultiCorpus();
    }
    /**
     * <code>optional bool multiCorpus = 2;</code>
     * @param value The multiCorpus to set.
     * @return This builder for chaining.
     */
    public Builder setMultiCorpus(boolean value) {
      copyOnWrite();
      instance.setMultiCorpus(value);
      return this;
    }
    /**
     * <code>optional bool multiCorpus = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearMultiCorpus() {
      copyOnWrite();
      instance.clearMultiCorpus();
      return this;
    }

    /**
     * <code>optional string title = 3;</code>
     * @return Whether the title field is set.
     */
    @java.lang.Override
    public boolean hasTitle() {
      return instance.hasTitle();
    }
    /**
     * <code>optional string title = 3;</code>
     * @return The title.
     */
    @java.lang.Override
    public java.lang.String getTitle() {
      return instance.getTitle();
    }
    /**
     * <code>optional string title = 3;</code>
     * @return The bytes for title.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getTitleBytes() {
      return instance.getTitleBytes();
    }
    /**
     * <code>optional string title = 3;</code>
     * @param value The title to set.
     * @return This builder for chaining.
     */
    public Builder setTitle(
        java.lang.String value) {
      copyOnWrite();
      instance.setTitle(value);
      return this;
    }
    /**
     * <code>optional string title = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearTitle() {
      copyOnWrite();
      instance.clearTitle();
      return this;
    }
    /**
     * <code>optional string title = 3;</code>
     * @param value The bytes for title to set.
     * @return This builder for chaining.
     */
    public Builder setTitleBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setTitleBytes(value);
      return this;
    }

    /**
     * <code>optional string iconUrl = 4;</code>
     * @return Whether the iconUrl field is set.
     */
    @java.lang.Override
    public boolean hasIconUrl() {
      return instance.hasIconUrl();
    }
    /**
     * <code>optional string iconUrl = 4;</code>
     * @return The iconUrl.
     */
    @java.lang.Override
    public java.lang.String getIconUrl() {
      return instance.getIconUrl();
    }
    /**
     * <code>optional string iconUrl = 4;</code>
     * @return The bytes for iconUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getIconUrlBytes() {
      return instance.getIconUrlBytes();
    }
    /**
     * <code>optional string iconUrl = 4;</code>
     * @param value The iconUrl to set.
     * @return This builder for chaining.
     */
    public Builder setIconUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setIconUrl(value);
      return this;
    }
    /**
     * <code>optional string iconUrl = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearIconUrl() {
      copyOnWrite();
      instance.clearIconUrl();
      return this;
    }
    /**
     * <code>optional string iconUrl = 4;</code>
     * @param value The bytes for iconUrl to set.
     * @return This builder for chaining.
     */
    public Builder setIconUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setIconUrlBytes(value);
      return this;
    }

    /**
     * <code>optional string fullContentsUrl = 5;</code>
     * @return Whether the fullContentsUrl field is set.
     */
    @java.lang.Override
    public boolean hasFullContentsUrl() {
      return instance.hasFullContentsUrl();
    }
    /**
     * <code>optional string fullContentsUrl = 5;</code>
     * @return The fullContentsUrl.
     */
    @java.lang.Override
    public java.lang.String getFullContentsUrl() {
      return instance.getFullContentsUrl();
    }
    /**
     * <code>optional string fullContentsUrl = 5;</code>
     * @return The bytes for fullContentsUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getFullContentsUrlBytes() {
      return instance.getFullContentsUrlBytes();
    }
    /**
     * <code>optional string fullContentsUrl = 5;</code>
     * @param value The fullContentsUrl to set.
     * @return This builder for chaining.
     */
    public Builder setFullContentsUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setFullContentsUrl(value);
      return this;
    }
    /**
     * <code>optional string fullContentsUrl = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearFullContentsUrl() {
      copyOnWrite();
      instance.clearFullContentsUrl();
      return this;
    }
    /**
     * <code>optional string fullContentsUrl = 5;</code>
     * @param value The bytes for fullContentsUrl to set.
     * @return This builder for chaining.
     */
    public Builder setFullContentsUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setFullContentsUrlBytes(value);
      return this;
    }

    /**
     * <code>optional double relevance = 6;</code>
     * @return Whether the relevance field is set.
     */
    @java.lang.Override
    public boolean hasRelevance() {
      return instance.hasRelevance();
    }
    /**
     * <code>optional double relevance = 6;</code>
     * @return The relevance.
     */
    @java.lang.Override
    public double getRelevance() {
      return instance.getRelevance();
    }
    /**
     * <code>optional double relevance = 6;</code>
     * @param value The relevance to set.
     * @return This builder for chaining.
     */
    public Builder setRelevance(double value) {
      copyOnWrite();
      instance.setRelevance(value);
      return this;
    }
    /**
     * <code>optional double relevance = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearRelevance() {
      copyOnWrite();
      instance.clearRelevance();
      return this;
    }

    /**
     * <code>optional int64 estimatedResults = 7;</code>
     * @return Whether the estimatedResults field is set.
     */
    @java.lang.Override
    public boolean hasEstimatedResults() {
      return instance.hasEstimatedResults();
    }
    /**
     * <code>optional int64 estimatedResults = 7;</code>
     * @return The estimatedResults.
     */
    @java.lang.Override
    public long getEstimatedResults() {
      return instance.getEstimatedResults();
    }
    /**
     * <code>optional int64 estimatedResults = 7;</code>
     * @param value The estimatedResults to set.
     * @return This builder for chaining.
     */
    public Builder setEstimatedResults(long value) {
      copyOnWrite();
      instance.setEstimatedResults(value);
      return this;
    }
    /**
     * <code>optional int64 estimatedResults = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearEstimatedResults() {
      copyOnWrite();
      instance.clearEstimatedResults();
      return this;
    }

    /**
     * <code>optional string analyticsCookie = 8;</code>
     * @return Whether the analyticsCookie field is set.
     */
    @java.lang.Override
    public boolean hasAnalyticsCookie() {
      return instance.hasAnalyticsCookie();
    }
    /**
     * <code>optional string analyticsCookie = 8;</code>
     * @return The analyticsCookie.
     */
    @java.lang.Override
    public java.lang.String getAnalyticsCookie() {
      return instance.getAnalyticsCookie();
    }
    /**
     * <code>optional string analyticsCookie = 8;</code>
     * @return The bytes for analyticsCookie.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getAnalyticsCookieBytes() {
      return instance.getAnalyticsCookieBytes();
    }
    /**
     * <code>optional string analyticsCookie = 8;</code>
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
     * <code>optional string analyticsCookie = 8;</code>
     * @return This builder for chaining.
     */
    public Builder clearAnalyticsCookie() {
      copyOnWrite();
      instance.clearAnalyticsCookie();
      return this;
    }
    /**
     * <code>optional string analyticsCookie = 8;</code>
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
     * <code>optional string fullContentsListUrl = 9;</code>
     * @return Whether the fullContentsListUrl field is set.
     */
    @java.lang.Override
    public boolean hasFullContentsListUrl() {
      return instance.hasFullContentsListUrl();
    }
    /**
     * <code>optional string fullContentsListUrl = 9;</code>
     * @return The fullContentsListUrl.
     */
    @java.lang.Override
    public java.lang.String getFullContentsListUrl() {
      return instance.getFullContentsListUrl();
    }
    /**
     * <code>optional string fullContentsListUrl = 9;</code>
     * @return The bytes for fullContentsListUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getFullContentsListUrlBytes() {
      return instance.getFullContentsListUrlBytes();
    }
    /**
     * <code>optional string fullContentsListUrl = 9;</code>
     * @param value The fullContentsListUrl to set.
     * @return This builder for chaining.
     */
    public Builder setFullContentsListUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setFullContentsListUrl(value);
      return this;
    }
    /**
     * <code>optional string fullContentsListUrl = 9;</code>
     * @return This builder for chaining.
     */
    public Builder clearFullContentsListUrl() {
      copyOnWrite();
      instance.clearFullContentsListUrl();
      return this;
    }
    /**
     * <code>optional string fullContentsListUrl = 9;</code>
     * @param value The bytes for fullContentsListUrl to set.
     * @return This builder for chaining.
     */
    public Builder setFullContentsListUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setFullContentsListUrlBytes(value);
      return this;
    }

    /**
     * <code>optional string nextPageUrl = 10;</code>
     * @return Whether the nextPageUrl field is set.
     */
    @java.lang.Override
    public boolean hasNextPageUrl() {
      return instance.hasNextPageUrl();
    }
    /**
     * <code>optional string nextPageUrl = 10;</code>
     * @return The nextPageUrl.
     */
    @java.lang.Override
    public java.lang.String getNextPageUrl() {
      return instance.getNextPageUrl();
    }
    /**
     * <code>optional string nextPageUrl = 10;</code>
     * @return The bytes for nextPageUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getNextPageUrlBytes() {
      return instance.getNextPageUrlBytes();
    }
    /**
     * <code>optional string nextPageUrl = 10;</code>
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
     * <code>optional string nextPageUrl = 10;</code>
     * @return This builder for chaining.
     */
    public Builder clearNextPageUrl() {
      copyOnWrite();
      instance.clearNextPageUrl();
      return this;
    }
    /**
     * <code>optional string nextPageUrl = 10;</code>
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
     * <code>optional bool ordered = 11;</code>
     * @return Whether the ordered field is set.
     */
    @java.lang.Override
    public boolean hasOrdered() {
      return instance.hasOrdered();
    }
    /**
     * <code>optional bool ordered = 11;</code>
     * @return The ordered.
     */
    @java.lang.Override
    public boolean getOrdered() {
      return instance.getOrdered();
    }
    /**
     * <code>optional bool ordered = 11;</code>
     * @param value The ordered to set.
     * @return This builder for chaining.
     */
    public Builder setOrdered(boolean value) {
      copyOnWrite();
      instance.setOrdered(value);
      return this;
    }
    /**
     * <code>optional bool ordered = 11;</code>
     * @return This builder for chaining.
     */
    public Builder clearOrdered() {
      copyOnWrite();
      instance.clearOrdered();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.Bucket)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.Bucket();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "multiCorpus_",
            "title_",
            "iconUrl_",
            "fullContentsUrl_",
            "relevance_",
            "estimatedResults_",
            "analyticsCookie_",
            "fullContentsListUrl_",
            "nextPageUrl_",
            "ordered_",
          };
          java.lang.String info =
              "\u0001\n\u0000\u0001\u0002\u000b\n\u0000\u0000\u0000\u0002\u1007\u0000\u0003\u1008" +
              "\u0001\u0004\u1008\u0002\u0005\u1008\u0003\u0006\u1000\u0004\u0007\u1002\u0005\b" +
              "\u1008\u0006\t\u1008\u0007\n\u1008\b\u000b\u1007\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.Bucket> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.Bucket.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.Bucket>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.Bucket)
  private static final finsky.protos.Bucket DEFAULT_INSTANCE;
  static {
    Bucket defaultInstance = new Bucket();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Bucket.class, defaultInstance);
  }

  public static finsky.protos.Bucket getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Bucket> PARSER;

  public static com.google.protobuf.Parser<Bucket> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

