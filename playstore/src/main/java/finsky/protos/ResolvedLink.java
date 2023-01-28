// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.ResolvedLink}
 */
public  final class ResolvedLink extends
    com.google.protobuf.GeneratedMessageLite<
        ResolvedLink, ResolvedLink.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.ResolvedLink)
    ResolvedLinkOrBuilder {
  private ResolvedLink() {
    detailsUrl_ = "";
    browseUrl_ = "";
    searchUrl_ = "";
    homeUrl_ = "";
    serverLogsCookie_ = com.google.protobuf.ByteString.EMPTY;
    wishlistUrl_ = "";
    query_ = "";
    myAccountUrl_ = "";
  }
  private int bitField0_;
  public static final int DETAILSURL_FIELD_NUMBER = 1;
  private java.lang.String detailsUrl_;
  /**
   * <code>optional string detailsUrl = 1;</code>
   * @return Whether the detailsUrl field is set.
   */
  @java.lang.Override
  public boolean hasDetailsUrl() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional string detailsUrl = 1;</code>
   * @return The detailsUrl.
   */
  @java.lang.Override
  public java.lang.String getDetailsUrl() {
    return detailsUrl_;
  }
  /**
   * <code>optional string detailsUrl = 1;</code>
   * @return The bytes for detailsUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getDetailsUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(detailsUrl_);
  }
  /**
   * <code>optional string detailsUrl = 1;</code>
   * @param value The detailsUrl to set.
   */
  private void setDetailsUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000001;
    detailsUrl_ = value;
  }
  /**
   * <code>optional string detailsUrl = 1;</code>
   */
  private void clearDetailsUrl() {
    bitField0_ = (bitField0_ & ~0x00000001);
    detailsUrl_ = getDefaultInstance().getDetailsUrl();
  }
  /**
   * <code>optional string detailsUrl = 1;</code>
   * @param value The bytes for detailsUrl to set.
   */
  private void setDetailsUrlBytes(
      com.google.protobuf.ByteString value) {
    detailsUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000001;
  }

  public static final int BROWSEURL_FIELD_NUMBER = 2;
  private java.lang.String browseUrl_;
  /**
   * <code>optional string browseUrl = 2;</code>
   * @return Whether the browseUrl field is set.
   */
  @java.lang.Override
  public boolean hasBrowseUrl() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional string browseUrl = 2;</code>
   * @return The browseUrl.
   */
  @java.lang.Override
  public java.lang.String getBrowseUrl() {
    return browseUrl_;
  }
  /**
   * <code>optional string browseUrl = 2;</code>
   * @return The bytes for browseUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getBrowseUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(browseUrl_);
  }
  /**
   * <code>optional string browseUrl = 2;</code>
   * @param value The browseUrl to set.
   */
  private void setBrowseUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000002;
    browseUrl_ = value;
  }
  /**
   * <code>optional string browseUrl = 2;</code>
   */
  private void clearBrowseUrl() {
    bitField0_ = (bitField0_ & ~0x00000002);
    browseUrl_ = getDefaultInstance().getBrowseUrl();
  }
  /**
   * <code>optional string browseUrl = 2;</code>
   * @param value The bytes for browseUrl to set.
   */
  private void setBrowseUrlBytes(
      com.google.protobuf.ByteString value) {
    browseUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000002;
  }

  public static final int SEARCHURL_FIELD_NUMBER = 3;
  private java.lang.String searchUrl_;
  /**
   * <code>optional string searchUrl = 3;</code>
   * @return Whether the searchUrl field is set.
   */
  @java.lang.Override
  public boolean hasSearchUrl() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <code>optional string searchUrl = 3;</code>
   * @return The searchUrl.
   */
  @java.lang.Override
  public java.lang.String getSearchUrl() {
    return searchUrl_;
  }
  /**
   * <code>optional string searchUrl = 3;</code>
   * @return The bytes for searchUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSearchUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(searchUrl_);
  }
  /**
   * <code>optional string searchUrl = 3;</code>
   * @param value The searchUrl to set.
   */
  private void setSearchUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000004;
    searchUrl_ = value;
  }
  /**
   * <code>optional string searchUrl = 3;</code>
   */
  private void clearSearchUrl() {
    bitField0_ = (bitField0_ & ~0x00000004);
    searchUrl_ = getDefaultInstance().getSearchUrl();
  }
  /**
   * <code>optional string searchUrl = 3;</code>
   * @param value The bytes for searchUrl to set.
   */
  private void setSearchUrlBytes(
      com.google.protobuf.ByteString value) {
    searchUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000004;
  }

  public static final int HOMEURL_FIELD_NUMBER = 5;
  private java.lang.String homeUrl_;
  /**
   * <pre>
   *    optional DirectPurchase directPurchase = 4;
   * </pre>
   *
   * <code>optional string homeUrl = 5;</code>
   * @return Whether the homeUrl field is set.
   */
  @java.lang.Override
  public boolean hasHomeUrl() {
    return ((bitField0_ & 0x00000008) != 0);
  }
  /**
   * <pre>
   *    optional DirectPurchase directPurchase = 4;
   * </pre>
   *
   * <code>optional string homeUrl = 5;</code>
   * @return The homeUrl.
   */
  @java.lang.Override
  public java.lang.String getHomeUrl() {
    return homeUrl_;
  }
  /**
   * <pre>
   *    optional DirectPurchase directPurchase = 4;
   * </pre>
   *
   * <code>optional string homeUrl = 5;</code>
   * @return The bytes for homeUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getHomeUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(homeUrl_);
  }
  /**
   * <pre>
   *    optional DirectPurchase directPurchase = 4;
   * </pre>
   *
   * <code>optional string homeUrl = 5;</code>
   * @param value The homeUrl to set.
   */
  private void setHomeUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000008;
    homeUrl_ = value;
  }
  /**
   * <pre>
   *    optional DirectPurchase directPurchase = 4;
   * </pre>
   *
   * <code>optional string homeUrl = 5;</code>
   */
  private void clearHomeUrl() {
    bitField0_ = (bitField0_ & ~0x00000008);
    homeUrl_ = getDefaultInstance().getHomeUrl();
  }
  /**
   * <pre>
   *    optional DirectPurchase directPurchase = 4;
   * </pre>
   *
   * <code>optional string homeUrl = 5;</code>
   * @param value The bytes for homeUrl to set.
   */
  private void setHomeUrlBytes(
      com.google.protobuf.ByteString value) {
    homeUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000008;
  }

  public static final int SERVERLOGSCOOKIE_FIELD_NUMBER = 7;
  private com.google.protobuf.ByteString serverLogsCookie_;
  /**
   * <pre>
   *    optional RedeemGiftCard redeemGiftCard = 6;
   * </pre>
   *
   * <code>optional bytes serverLogsCookie = 7;</code>
   * @return Whether the serverLogsCookie field is set.
   */
  @java.lang.Override
  public boolean hasServerLogsCookie() {
    return ((bitField0_ & 0x00000010) != 0);
  }
  /**
   * <pre>
   *    optional RedeemGiftCard redeemGiftCard = 6;
   * </pre>
   *
   * <code>optional bytes serverLogsCookie = 7;</code>
   * @return The serverLogsCookie.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getServerLogsCookie() {
    return serverLogsCookie_;
  }
  /**
   * <pre>
   *    optional RedeemGiftCard redeemGiftCard = 6;
   * </pre>
   *
   * <code>optional bytes serverLogsCookie = 7;</code>
   * @param value The serverLogsCookie to set.
   */
  private void setServerLogsCookie(com.google.protobuf.ByteString value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000010;
    serverLogsCookie_ = value;
  }
  /**
   * <pre>
   *    optional RedeemGiftCard redeemGiftCard = 6;
   * </pre>
   *
   * <code>optional bytes serverLogsCookie = 7;</code>
   */
  private void clearServerLogsCookie() {
    bitField0_ = (bitField0_ & ~0x00000010);
    serverLogsCookie_ = getDefaultInstance().getServerLogsCookie();
  }

  public static final int WISHLISTURL_FIELD_NUMBER = 9;
  private java.lang.String wishlistUrl_;
  /**
   * <pre>
   *    optional DocId DocId = 8;
   * </pre>
   *
   * <code>optional string wishlistUrl = 9;</code>
   * @return Whether the wishlistUrl field is set.
   */
  @java.lang.Override
  public boolean hasWishlistUrl() {
    return ((bitField0_ & 0x00000020) != 0);
  }
  /**
   * <pre>
   *    optional DocId DocId = 8;
   * </pre>
   *
   * <code>optional string wishlistUrl = 9;</code>
   * @return The wishlistUrl.
   */
  @java.lang.Override
  public java.lang.String getWishlistUrl() {
    return wishlistUrl_;
  }
  /**
   * <pre>
   *    optional DocId DocId = 8;
   * </pre>
   *
   * <code>optional string wishlistUrl = 9;</code>
   * @return The bytes for wishlistUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getWishlistUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(wishlistUrl_);
  }
  /**
   * <pre>
   *    optional DocId DocId = 8;
   * </pre>
   *
   * <code>optional string wishlistUrl = 9;</code>
   * @param value The wishlistUrl to set.
   */
  private void setWishlistUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000020;
    wishlistUrl_ = value;
  }
  /**
   * <pre>
   *    optional DocId DocId = 8;
   * </pre>
   *
   * <code>optional string wishlistUrl = 9;</code>
   */
  private void clearWishlistUrl() {
    bitField0_ = (bitField0_ & ~0x00000020);
    wishlistUrl_ = getDefaultInstance().getWishlistUrl();
  }
  /**
   * <pre>
   *    optional DocId DocId = 8;
   * </pre>
   *
   * <code>optional string wishlistUrl = 9;</code>
   * @param value The bytes for wishlistUrl to set.
   */
  private void setWishlistUrlBytes(
      com.google.protobuf.ByteString value) {
    wishlistUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000020;
  }

  public static final int BACKEND_FIELD_NUMBER = 10;
  private int backend_;
  /**
   * <code>optional int32 backend = 10;</code>
   * @return Whether the backend field is set.
   */
  @java.lang.Override
  public boolean hasBackend() {
    return ((bitField0_ & 0x00000040) != 0);
  }
  /**
   * <code>optional int32 backend = 10;</code>
   * @return The backend.
   */
  @java.lang.Override
  public int getBackend() {
    return backend_;
  }
  /**
   * <code>optional int32 backend = 10;</code>
   * @param value The backend to set.
   */
  private void setBackend(int value) {
    bitField0_ |= 0x00000040;
    backend_ = value;
  }
  /**
   * <code>optional int32 backend = 10;</code>
   */
  private void clearBackend() {
    bitField0_ = (bitField0_ & ~0x00000040);
    backend_ = 0;
  }

  public static final int QUERY_FIELD_NUMBER = 11;
  private java.lang.String query_;
  /**
   * <code>optional string query = 11;</code>
   * @return Whether the query field is set.
   */
  @java.lang.Override
  public boolean hasQuery() {
    return ((bitField0_ & 0x00000080) != 0);
  }
  /**
   * <code>optional string query = 11;</code>
   * @return The query.
   */
  @java.lang.Override
  public java.lang.String getQuery() {
    return query_;
  }
  /**
   * <code>optional string query = 11;</code>
   * @return The bytes for query.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getQueryBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(query_);
  }
  /**
   * <code>optional string query = 11;</code>
   * @param value The query to set.
   */
  private void setQuery(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000080;
    query_ = value;
  }
  /**
   * <code>optional string query = 11;</code>
   */
  private void clearQuery() {
    bitField0_ = (bitField0_ & ~0x00000080);
    query_ = getDefaultInstance().getQuery();
  }
  /**
   * <code>optional string query = 11;</code>
   * @param value The bytes for query to set.
   */
  private void setQueryBytes(
      com.google.protobuf.ByteString value) {
    query_ = value.toStringUtf8();
    bitField0_ |= 0x00000080;
  }

  public static final int MYACCOUNTURL_FIELD_NUMBER = 12;
  private java.lang.String myAccountUrl_;
  /**
   * <pre>
   *    optional HelpCenter helpCenter = 13;
   * </pre>
   *
   * <code>optional string myAccountUrl = 12;</code>
   * @return Whether the myAccountUrl field is set.
   */
  @java.lang.Override
  public boolean hasMyAccountUrl() {
    return ((bitField0_ & 0x00000100) != 0);
  }
  /**
   * <pre>
   *    optional HelpCenter helpCenter = 13;
   * </pre>
   *
   * <code>optional string myAccountUrl = 12;</code>
   * @return The myAccountUrl.
   */
  @java.lang.Override
  public java.lang.String getMyAccountUrl() {
    return myAccountUrl_;
  }
  /**
   * <pre>
   *    optional HelpCenter helpCenter = 13;
   * </pre>
   *
   * <code>optional string myAccountUrl = 12;</code>
   * @return The bytes for myAccountUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getMyAccountUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(myAccountUrl_);
  }
  /**
   * <pre>
   *    optional HelpCenter helpCenter = 13;
   * </pre>
   *
   * <code>optional string myAccountUrl = 12;</code>
   * @param value The myAccountUrl to set.
   */
  private void setMyAccountUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000100;
    myAccountUrl_ = value;
  }
  /**
   * <pre>
   *    optional HelpCenter helpCenter = 13;
   * </pre>
   *
   * <code>optional string myAccountUrl = 12;</code>
   */
  private void clearMyAccountUrl() {
    bitField0_ = (bitField0_ & ~0x00000100);
    myAccountUrl_ = getDefaultInstance().getMyAccountUrl();
  }
  /**
   * <pre>
   *    optional HelpCenter helpCenter = 13;
   * </pre>
   *
   * <code>optional string myAccountUrl = 12;</code>
   * @param value The bytes for myAccountUrl to set.
   */
  private void setMyAccountUrlBytes(
      com.google.protobuf.ByteString value) {
    myAccountUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000100;
  }

  public static finsky.protos.ResolvedLink parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.ResolvedLink parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.ResolvedLink parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.ResolvedLink parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.ResolvedLink parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.ResolvedLink parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.ResolvedLink parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.ResolvedLink parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.ResolvedLink parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.ResolvedLink parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.ResolvedLink parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.ResolvedLink parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.ResolvedLink prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.ResolvedLink}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.ResolvedLink, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.ResolvedLink)
      finsky.protos.ResolvedLinkOrBuilder {
    // Construct using finsky.protos.ResolvedLink.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional string detailsUrl = 1;</code>
     * @return Whether the detailsUrl field is set.
     */
    @java.lang.Override
    public boolean hasDetailsUrl() {
      return instance.hasDetailsUrl();
    }
    /**
     * <code>optional string detailsUrl = 1;</code>
     * @return The detailsUrl.
     */
    @java.lang.Override
    public java.lang.String getDetailsUrl() {
      return instance.getDetailsUrl();
    }
    /**
     * <code>optional string detailsUrl = 1;</code>
     * @return The bytes for detailsUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getDetailsUrlBytes() {
      return instance.getDetailsUrlBytes();
    }
    /**
     * <code>optional string detailsUrl = 1;</code>
     * @param value The detailsUrl to set.
     * @return This builder for chaining.
     */
    public Builder setDetailsUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setDetailsUrl(value);
      return this;
    }
    /**
     * <code>optional string detailsUrl = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearDetailsUrl() {
      copyOnWrite();
      instance.clearDetailsUrl();
      return this;
    }
    /**
     * <code>optional string detailsUrl = 1;</code>
     * @param value The bytes for detailsUrl to set.
     * @return This builder for chaining.
     */
    public Builder setDetailsUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setDetailsUrlBytes(value);
      return this;
    }

    /**
     * <code>optional string browseUrl = 2;</code>
     * @return Whether the browseUrl field is set.
     */
    @java.lang.Override
    public boolean hasBrowseUrl() {
      return instance.hasBrowseUrl();
    }
    /**
     * <code>optional string browseUrl = 2;</code>
     * @return The browseUrl.
     */
    @java.lang.Override
    public java.lang.String getBrowseUrl() {
      return instance.getBrowseUrl();
    }
    /**
     * <code>optional string browseUrl = 2;</code>
     * @return The bytes for browseUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getBrowseUrlBytes() {
      return instance.getBrowseUrlBytes();
    }
    /**
     * <code>optional string browseUrl = 2;</code>
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
     * <code>optional string browseUrl = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearBrowseUrl() {
      copyOnWrite();
      instance.clearBrowseUrl();
      return this;
    }
    /**
     * <code>optional string browseUrl = 2;</code>
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
     * <code>optional string searchUrl = 3;</code>
     * @return Whether the searchUrl field is set.
     */
    @java.lang.Override
    public boolean hasSearchUrl() {
      return instance.hasSearchUrl();
    }
    /**
     * <code>optional string searchUrl = 3;</code>
     * @return The searchUrl.
     */
    @java.lang.Override
    public java.lang.String getSearchUrl() {
      return instance.getSearchUrl();
    }
    /**
     * <code>optional string searchUrl = 3;</code>
     * @return The bytes for searchUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSearchUrlBytes() {
      return instance.getSearchUrlBytes();
    }
    /**
     * <code>optional string searchUrl = 3;</code>
     * @param value The searchUrl to set.
     * @return This builder for chaining.
     */
    public Builder setSearchUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setSearchUrl(value);
      return this;
    }
    /**
     * <code>optional string searchUrl = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearSearchUrl() {
      copyOnWrite();
      instance.clearSearchUrl();
      return this;
    }
    /**
     * <code>optional string searchUrl = 3;</code>
     * @param value The bytes for searchUrl to set.
     * @return This builder for chaining.
     */
    public Builder setSearchUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSearchUrlBytes(value);
      return this;
    }

    /**
     * <pre>
     *    optional DirectPurchase directPurchase = 4;
     * </pre>
     *
     * <code>optional string homeUrl = 5;</code>
     * @return Whether the homeUrl field is set.
     */
    @java.lang.Override
    public boolean hasHomeUrl() {
      return instance.hasHomeUrl();
    }
    /**
     * <pre>
     *    optional DirectPurchase directPurchase = 4;
     * </pre>
     *
     * <code>optional string homeUrl = 5;</code>
     * @return The homeUrl.
     */
    @java.lang.Override
    public java.lang.String getHomeUrl() {
      return instance.getHomeUrl();
    }
    /**
     * <pre>
     *    optional DirectPurchase directPurchase = 4;
     * </pre>
     *
     * <code>optional string homeUrl = 5;</code>
     * @return The bytes for homeUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getHomeUrlBytes() {
      return instance.getHomeUrlBytes();
    }
    /**
     * <pre>
     *    optional DirectPurchase directPurchase = 4;
     * </pre>
     *
     * <code>optional string homeUrl = 5;</code>
     * @param value The homeUrl to set.
     * @return This builder for chaining.
     */
    public Builder setHomeUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setHomeUrl(value);
      return this;
    }
    /**
     * <pre>
     *    optional DirectPurchase directPurchase = 4;
     * </pre>
     *
     * <code>optional string homeUrl = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearHomeUrl() {
      copyOnWrite();
      instance.clearHomeUrl();
      return this;
    }
    /**
     * <pre>
     *    optional DirectPurchase directPurchase = 4;
     * </pre>
     *
     * <code>optional string homeUrl = 5;</code>
     * @param value The bytes for homeUrl to set.
     * @return This builder for chaining.
     */
    public Builder setHomeUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setHomeUrlBytes(value);
      return this;
    }

    /**
     * <pre>
     *    optional RedeemGiftCard redeemGiftCard = 6;
     * </pre>
     *
     * <code>optional bytes serverLogsCookie = 7;</code>
     * @return Whether the serverLogsCookie field is set.
     */
    @java.lang.Override
    public boolean hasServerLogsCookie() {
      return instance.hasServerLogsCookie();
    }
    /**
     * <pre>
     *    optional RedeemGiftCard redeemGiftCard = 6;
     * </pre>
     *
     * <code>optional bytes serverLogsCookie = 7;</code>
     * @return The serverLogsCookie.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getServerLogsCookie() {
      return instance.getServerLogsCookie();
    }
    /**
     * <pre>
     *    optional RedeemGiftCard redeemGiftCard = 6;
     * </pre>
     *
     * <code>optional bytes serverLogsCookie = 7;</code>
     * @param value The serverLogsCookie to set.
     * @return This builder for chaining.
     */
    public Builder setServerLogsCookie(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setServerLogsCookie(value);
      return this;
    }
    /**
     * <pre>
     *    optional RedeemGiftCard redeemGiftCard = 6;
     * </pre>
     *
     * <code>optional bytes serverLogsCookie = 7;</code>
     * @return This builder for chaining.
     */
    public Builder clearServerLogsCookie() {
      copyOnWrite();
      instance.clearServerLogsCookie();
      return this;
    }

    /**
     * <pre>
     *    optional DocId DocId = 8;
     * </pre>
     *
     * <code>optional string wishlistUrl = 9;</code>
     * @return Whether the wishlistUrl field is set.
     */
    @java.lang.Override
    public boolean hasWishlistUrl() {
      return instance.hasWishlistUrl();
    }
    /**
     * <pre>
     *    optional DocId DocId = 8;
     * </pre>
     *
     * <code>optional string wishlistUrl = 9;</code>
     * @return The wishlistUrl.
     */
    @java.lang.Override
    public java.lang.String getWishlistUrl() {
      return instance.getWishlistUrl();
    }
    /**
     * <pre>
     *    optional DocId DocId = 8;
     * </pre>
     *
     * <code>optional string wishlistUrl = 9;</code>
     * @return The bytes for wishlistUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getWishlistUrlBytes() {
      return instance.getWishlistUrlBytes();
    }
    /**
     * <pre>
     *    optional DocId DocId = 8;
     * </pre>
     *
     * <code>optional string wishlistUrl = 9;</code>
     * @param value The wishlistUrl to set.
     * @return This builder for chaining.
     */
    public Builder setWishlistUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setWishlistUrl(value);
      return this;
    }
    /**
     * <pre>
     *    optional DocId DocId = 8;
     * </pre>
     *
     * <code>optional string wishlistUrl = 9;</code>
     * @return This builder for chaining.
     */
    public Builder clearWishlistUrl() {
      copyOnWrite();
      instance.clearWishlistUrl();
      return this;
    }
    /**
     * <pre>
     *    optional DocId DocId = 8;
     * </pre>
     *
     * <code>optional string wishlistUrl = 9;</code>
     * @param value The bytes for wishlistUrl to set.
     * @return This builder for chaining.
     */
    public Builder setWishlistUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setWishlistUrlBytes(value);
      return this;
    }

    /**
     * <code>optional int32 backend = 10;</code>
     * @return Whether the backend field is set.
     */
    @java.lang.Override
    public boolean hasBackend() {
      return instance.hasBackend();
    }
    /**
     * <code>optional int32 backend = 10;</code>
     * @return The backend.
     */
    @java.lang.Override
    public int getBackend() {
      return instance.getBackend();
    }
    /**
     * <code>optional int32 backend = 10;</code>
     * @param value The backend to set.
     * @return This builder for chaining.
     */
    public Builder setBackend(int value) {
      copyOnWrite();
      instance.setBackend(value);
      return this;
    }
    /**
     * <code>optional int32 backend = 10;</code>
     * @return This builder for chaining.
     */
    public Builder clearBackend() {
      copyOnWrite();
      instance.clearBackend();
      return this;
    }

    /**
     * <code>optional string query = 11;</code>
     * @return Whether the query field is set.
     */
    @java.lang.Override
    public boolean hasQuery() {
      return instance.hasQuery();
    }
    /**
     * <code>optional string query = 11;</code>
     * @return The query.
     */
    @java.lang.Override
    public java.lang.String getQuery() {
      return instance.getQuery();
    }
    /**
     * <code>optional string query = 11;</code>
     * @return The bytes for query.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getQueryBytes() {
      return instance.getQueryBytes();
    }
    /**
     * <code>optional string query = 11;</code>
     * @param value The query to set.
     * @return This builder for chaining.
     */
    public Builder setQuery(
        java.lang.String value) {
      copyOnWrite();
      instance.setQuery(value);
      return this;
    }
    /**
     * <code>optional string query = 11;</code>
     * @return This builder for chaining.
     */
    public Builder clearQuery() {
      copyOnWrite();
      instance.clearQuery();
      return this;
    }
    /**
     * <code>optional string query = 11;</code>
     * @param value The bytes for query to set.
     * @return This builder for chaining.
     */
    public Builder setQueryBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setQueryBytes(value);
      return this;
    }

    /**
     * <pre>
     *    optional HelpCenter helpCenter = 13;
     * </pre>
     *
     * <code>optional string myAccountUrl = 12;</code>
     * @return Whether the myAccountUrl field is set.
     */
    @java.lang.Override
    public boolean hasMyAccountUrl() {
      return instance.hasMyAccountUrl();
    }
    /**
     * <pre>
     *    optional HelpCenter helpCenter = 13;
     * </pre>
     *
     * <code>optional string myAccountUrl = 12;</code>
     * @return The myAccountUrl.
     */
    @java.lang.Override
    public java.lang.String getMyAccountUrl() {
      return instance.getMyAccountUrl();
    }
    /**
     * <pre>
     *    optional HelpCenter helpCenter = 13;
     * </pre>
     *
     * <code>optional string myAccountUrl = 12;</code>
     * @return The bytes for myAccountUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getMyAccountUrlBytes() {
      return instance.getMyAccountUrlBytes();
    }
    /**
     * <pre>
     *    optional HelpCenter helpCenter = 13;
     * </pre>
     *
     * <code>optional string myAccountUrl = 12;</code>
     * @param value The myAccountUrl to set.
     * @return This builder for chaining.
     */
    public Builder setMyAccountUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setMyAccountUrl(value);
      return this;
    }
    /**
     * <pre>
     *    optional HelpCenter helpCenter = 13;
     * </pre>
     *
     * <code>optional string myAccountUrl = 12;</code>
     * @return This builder for chaining.
     */
    public Builder clearMyAccountUrl() {
      copyOnWrite();
      instance.clearMyAccountUrl();
      return this;
    }
    /**
     * <pre>
     *    optional HelpCenter helpCenter = 13;
     * </pre>
     *
     * <code>optional string myAccountUrl = 12;</code>
     * @param value The bytes for myAccountUrl to set.
     * @return This builder for chaining.
     */
    public Builder setMyAccountUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setMyAccountUrlBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.ResolvedLink)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.ResolvedLink();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "detailsUrl_",
            "browseUrl_",
            "searchUrl_",
            "homeUrl_",
            "serverLogsCookie_",
            "wishlistUrl_",
            "backend_",
            "query_",
            "myAccountUrl_",
          };
          java.lang.String info =
              "\u0001\t\u0000\u0001\u0001\f\t\u0000\u0000\u0000\u0001\u1008\u0000\u0002\u1008\u0001" +
              "\u0003\u1008\u0002\u0005\u1008\u0003\u0007\u100a\u0004\t\u1008\u0005\n\u1004\u0006" +
              "\u000b\u1008\u0007\f\u1008\b";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.ResolvedLink> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.ResolvedLink.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.ResolvedLink>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.ResolvedLink)
  private static final finsky.protos.ResolvedLink DEFAULT_INSTANCE;
  static {
    ResolvedLink defaultInstance = new ResolvedLink();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ResolvedLink.class, defaultInstance);
  }

  public static finsky.protos.ResolvedLink getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ResolvedLink> PARSER;

  public static com.google.protobuf.Parser<ResolvedLink> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
