// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.RelatedLinks}
 */
public  final class RelatedLinks extends
    com.google.protobuf.GeneratedMessageLite<
        RelatedLinks, RelatedLinks.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.RelatedLinks)
    RelatedLinksOrBuilder {
  private RelatedLinks() {
    privacyPolicyUrl_ = "";
    relatedLinks_ = emptyProtobufList();
  }
  private int bitField0_;
  public static final int UNKNOWN1_FIELD_NUMBER = 10;
  private finsky.protos.RelatedLinksUnknown1 unknown1_;
  /**
   * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
   */
  @java.lang.Override
  public boolean hasUnknown1() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
   */
  @java.lang.Override
  public finsky.protos.RelatedLinksUnknown1 getUnknown1() {
    return unknown1_ == null ? finsky.protos.RelatedLinksUnknown1.getDefaultInstance() : unknown1_;
  }
  /**
   * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
   */
  private void setUnknown1(finsky.protos.RelatedLinksUnknown1 value) {
    value.getClass();
  unknown1_ = value;
    bitField0_ |= 0x00000001;
    }
  /**
   * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeUnknown1(finsky.protos.RelatedLinksUnknown1 value) {
    value.getClass();
  if (unknown1_ != null &&
        unknown1_ != finsky.protos.RelatedLinksUnknown1.getDefaultInstance()) {
      unknown1_ =
        finsky.protos.RelatedLinksUnknown1.newBuilder(unknown1_).mergeFrom(value).buildPartial();
    } else {
      unknown1_ = value;
    }
    bitField0_ |= 0x00000001;
  }
  /**
   * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
   */
  private void clearUnknown1() {  unknown1_ = null;
    bitField0_ = (bitField0_ & ~0x00000001);
  }

  public static final int PRIVACYPOLICYURL_FIELD_NUMBER = 18;
  private java.lang.String privacyPolicyUrl_;
  /**
   * <code>optional string privacyPolicyUrl = 18;</code>
   * @return Whether the privacyPolicyUrl field is set.
   */
  @java.lang.Override
  public boolean hasPrivacyPolicyUrl() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional string privacyPolicyUrl = 18;</code>
   * @return The privacyPolicyUrl.
   */
  @java.lang.Override
  public java.lang.String getPrivacyPolicyUrl() {
    return privacyPolicyUrl_;
  }
  /**
   * <code>optional string privacyPolicyUrl = 18;</code>
   * @return The bytes for privacyPolicyUrl.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getPrivacyPolicyUrlBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(privacyPolicyUrl_);
  }
  /**
   * <code>optional string privacyPolicyUrl = 18;</code>
   * @param value The privacyPolicyUrl to set.
   */
  private void setPrivacyPolicyUrl(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000002;
    privacyPolicyUrl_ = value;
  }
  /**
   * <code>optional string privacyPolicyUrl = 18;</code>
   */
  private void clearPrivacyPolicyUrl() {
    bitField0_ = (bitField0_ & ~0x00000002);
    privacyPolicyUrl_ = getDefaultInstance().getPrivacyPolicyUrl();
  }
  /**
   * <code>optional string privacyPolicyUrl = 18;</code>
   * @param value The bytes for privacyPolicyUrl to set.
   */
  private void setPrivacyPolicyUrlBytes(
      com.google.protobuf.ByteString value) {
    privacyPolicyUrl_ = value.toStringUtf8();
    bitField0_ |= 0x00000002;
  }

  public static final int YOUMIGHTALSOLIKE_FIELD_NUMBER = 24;
  private finsky.protos.RelatedLink youMightAlsoLike_;
  /**
   * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
   */
  @java.lang.Override
  public boolean hasYouMightAlsoLike() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
   */
  @java.lang.Override
  public finsky.protos.RelatedLink getYouMightAlsoLike() {
    return youMightAlsoLike_ == null ? finsky.protos.RelatedLink.getDefaultInstance() : youMightAlsoLike_;
  }
  /**
   * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
   */
  private void setYouMightAlsoLike(finsky.protos.RelatedLink value) {
    value.getClass();
  youMightAlsoLike_ = value;
    bitField0_ |= 0x00000004;
    }
  /**
   * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeYouMightAlsoLike(finsky.protos.RelatedLink value) {
    value.getClass();
  if (youMightAlsoLike_ != null &&
        youMightAlsoLike_ != finsky.protos.RelatedLink.getDefaultInstance()) {
      youMightAlsoLike_ =
        finsky.protos.RelatedLink.newBuilder(youMightAlsoLike_).mergeFrom(value).buildPartial();
    } else {
      youMightAlsoLike_ = value;
    }
    bitField0_ |= 0x00000004;
  }
  /**
   * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
   */
  private void clearYouMightAlsoLike() {  youMightAlsoLike_ = null;
    bitField0_ = (bitField0_ & ~0x00000004);
  }

  public static final int RATED_FIELD_NUMBER = 29;
  private finsky.protos.Rated rated_;
  /**
   * <code>optional .finsky.protos.Rated rated = 29;</code>
   */
  @java.lang.Override
  public boolean hasRated() {
    return ((bitField0_ & 0x00000008) != 0);
  }
  /**
   * <code>optional .finsky.protos.Rated rated = 29;</code>
   */
  @java.lang.Override
  public finsky.protos.Rated getRated() {
    return rated_ == null ? finsky.protos.Rated.getDefaultInstance() : rated_;
  }
  /**
   * <code>optional .finsky.protos.Rated rated = 29;</code>
   */
  private void setRated(finsky.protos.Rated value) {
    value.getClass();
  rated_ = value;
    bitField0_ |= 0x00000008;
    }
  /**
   * <code>optional .finsky.protos.Rated rated = 29;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeRated(finsky.protos.Rated value) {
    value.getClass();
  if (rated_ != null &&
        rated_ != finsky.protos.Rated.getDefaultInstance()) {
      rated_ =
        finsky.protos.Rated.newBuilder(rated_).mergeFrom(value).buildPartial();
    } else {
      rated_ = value;
    }
    bitField0_ |= 0x00000008;
  }
  /**
   * <code>optional .finsky.protos.Rated rated = 29;</code>
   */
  private void clearRated() {  rated_ = null;
    bitField0_ = (bitField0_ & ~0x00000008);
  }

  public static final int RELATEDLINKS_FIELD_NUMBER = 34;
  private com.google.protobuf.Internal.ProtobufList<finsky.protos.RelatedLink> relatedLinks_;
  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  @java.lang.Override
  public java.util.List<finsky.protos.RelatedLink> getRelatedLinksList() {
    return relatedLinks_;
  }
  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  public java.util.List<? extends finsky.protos.RelatedLinkOrBuilder> 
      getRelatedLinksOrBuilderList() {
    return relatedLinks_;
  }
  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  @java.lang.Override
  public int getRelatedLinksCount() {
    return relatedLinks_.size();
  }
  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  @java.lang.Override
  public finsky.protos.RelatedLink getRelatedLinks(int index) {
    return relatedLinks_.get(index);
  }
  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  public finsky.protos.RelatedLinkOrBuilder getRelatedLinksOrBuilder(
      int index) {
    return relatedLinks_.get(index);
  }
  private void ensureRelatedLinksIsMutable() {
    com.google.protobuf.Internal.ProtobufList<finsky.protos.RelatedLink> tmp = relatedLinks_;
    if (!tmp.isModifiable()) {
      relatedLinks_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }

  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  private void setRelatedLinks(
      int index, finsky.protos.RelatedLink value) {
    value.getClass();
  ensureRelatedLinksIsMutable();
    relatedLinks_.set(index, value);
  }
  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  private void addRelatedLinks(finsky.protos.RelatedLink value) {
    value.getClass();
  ensureRelatedLinksIsMutable();
    relatedLinks_.add(value);
  }
  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  private void addRelatedLinks(
      int index, finsky.protos.RelatedLink value) {
    value.getClass();
  ensureRelatedLinksIsMutable();
    relatedLinks_.add(index, value);
  }
  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  private void addAllRelatedLinks(
      java.lang.Iterable<? extends finsky.protos.RelatedLink> values) {
    ensureRelatedLinksIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, relatedLinks_);
  }
  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  private void clearRelatedLinks() {
    relatedLinks_ = emptyProtobufList();
  }
  /**
   * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
   */
  private void removeRelatedLinks(int index) {
    ensureRelatedLinksIsMutable();
    relatedLinks_.remove(index);
  }

  public static final int CATEGORYINFO_FIELD_NUMBER = 53;
  private finsky.protos.CategoryInfo categoryInfo_;
  /**
   * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
   */
  @java.lang.Override
  public boolean hasCategoryInfo() {
    return ((bitField0_ & 0x00000010) != 0);
  }
  /**
   * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
   */
  @java.lang.Override
  public finsky.protos.CategoryInfo getCategoryInfo() {
    return categoryInfo_ == null ? finsky.protos.CategoryInfo.getDefaultInstance() : categoryInfo_;
  }
  /**
   * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
   */
  private void setCategoryInfo(finsky.protos.CategoryInfo value) {
    value.getClass();
  categoryInfo_ = value;
    bitField0_ |= 0x00000010;
    }
  /**
   * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeCategoryInfo(finsky.protos.CategoryInfo value) {
    value.getClass();
  if (categoryInfo_ != null &&
        categoryInfo_ != finsky.protos.CategoryInfo.getDefaultInstance()) {
      categoryInfo_ =
        finsky.protos.CategoryInfo.newBuilder(categoryInfo_).mergeFrom(value).buildPartial();
    } else {
      categoryInfo_ = value;
    }
    bitField0_ |= 0x00000010;
  }
  /**
   * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
   */
  private void clearCategoryInfo() {  categoryInfo_ = null;
    bitField0_ = (bitField0_ & ~0x00000010);
  }

  public static finsky.protos.RelatedLinks parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.RelatedLinks parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.RelatedLinks parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.RelatedLinks parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.RelatedLinks parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.RelatedLinks parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.RelatedLinks parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.RelatedLinks parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.RelatedLinks parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.RelatedLinks parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.RelatedLinks parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.RelatedLinks parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.RelatedLinks prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.RelatedLinks}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.RelatedLinks, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.RelatedLinks)
      finsky.protos.RelatedLinksOrBuilder {
    // Construct using finsky.protos.RelatedLinks.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
     */
    @java.lang.Override
    public boolean hasUnknown1() {
      return instance.hasUnknown1();
    }
    /**
     * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
     */
    @java.lang.Override
    public finsky.protos.RelatedLinksUnknown1 getUnknown1() {
      return instance.getUnknown1();
    }
    /**
     * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
     */
    public Builder setUnknown1(finsky.protos.RelatedLinksUnknown1 value) {
      copyOnWrite();
      instance.setUnknown1(value);
      return this;
      }
    /**
     * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
     */
    public Builder setUnknown1(
        finsky.protos.RelatedLinksUnknown1.Builder builderForValue) {
      copyOnWrite();
      instance.setUnknown1(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
     */
    public Builder mergeUnknown1(finsky.protos.RelatedLinksUnknown1 value) {
      copyOnWrite();
      instance.mergeUnknown1(value);
      return this;
    }
    /**
     * <code>optional .finsky.protos.RelatedLinksUnknown1 unknown1 = 10;</code>
     */
    public Builder clearUnknown1() {  copyOnWrite();
      instance.clearUnknown1();
      return this;
    }

    /**
     * <code>optional string privacyPolicyUrl = 18;</code>
     * @return Whether the privacyPolicyUrl field is set.
     */
    @java.lang.Override
    public boolean hasPrivacyPolicyUrl() {
      return instance.hasPrivacyPolicyUrl();
    }
    /**
     * <code>optional string privacyPolicyUrl = 18;</code>
     * @return The privacyPolicyUrl.
     */
    @java.lang.Override
    public java.lang.String getPrivacyPolicyUrl() {
      return instance.getPrivacyPolicyUrl();
    }
    /**
     * <code>optional string privacyPolicyUrl = 18;</code>
     * @return The bytes for privacyPolicyUrl.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getPrivacyPolicyUrlBytes() {
      return instance.getPrivacyPolicyUrlBytes();
    }
    /**
     * <code>optional string privacyPolicyUrl = 18;</code>
     * @param value The privacyPolicyUrl to set.
     * @return This builder for chaining.
     */
    public Builder setPrivacyPolicyUrl(
        java.lang.String value) {
      copyOnWrite();
      instance.setPrivacyPolicyUrl(value);
      return this;
    }
    /**
     * <code>optional string privacyPolicyUrl = 18;</code>
     * @return This builder for chaining.
     */
    public Builder clearPrivacyPolicyUrl() {
      copyOnWrite();
      instance.clearPrivacyPolicyUrl();
      return this;
    }
    /**
     * <code>optional string privacyPolicyUrl = 18;</code>
     * @param value The bytes for privacyPolicyUrl to set.
     * @return This builder for chaining.
     */
    public Builder setPrivacyPolicyUrlBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setPrivacyPolicyUrlBytes(value);
      return this;
    }

    /**
     * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
     */
    @java.lang.Override
    public boolean hasYouMightAlsoLike() {
      return instance.hasYouMightAlsoLike();
    }
    /**
     * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
     */
    @java.lang.Override
    public finsky.protos.RelatedLink getYouMightAlsoLike() {
      return instance.getYouMightAlsoLike();
    }
    /**
     * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
     */
    public Builder setYouMightAlsoLike(finsky.protos.RelatedLink value) {
      copyOnWrite();
      instance.setYouMightAlsoLike(value);
      return this;
      }
    /**
     * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
     */
    public Builder setYouMightAlsoLike(
        finsky.protos.RelatedLink.Builder builderForValue) {
      copyOnWrite();
      instance.setYouMightAlsoLike(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
     */
    public Builder mergeYouMightAlsoLike(finsky.protos.RelatedLink value) {
      copyOnWrite();
      instance.mergeYouMightAlsoLike(value);
      return this;
    }
    /**
     * <code>optional .finsky.protos.RelatedLink youMightAlsoLike = 24;</code>
     */
    public Builder clearYouMightAlsoLike() {  copyOnWrite();
      instance.clearYouMightAlsoLike();
      return this;
    }

    /**
     * <code>optional .finsky.protos.Rated rated = 29;</code>
     */
    @java.lang.Override
    public boolean hasRated() {
      return instance.hasRated();
    }
    /**
     * <code>optional .finsky.protos.Rated rated = 29;</code>
     */
    @java.lang.Override
    public finsky.protos.Rated getRated() {
      return instance.getRated();
    }
    /**
     * <code>optional .finsky.protos.Rated rated = 29;</code>
     */
    public Builder setRated(finsky.protos.Rated value) {
      copyOnWrite();
      instance.setRated(value);
      return this;
      }
    /**
     * <code>optional .finsky.protos.Rated rated = 29;</code>
     */
    public Builder setRated(
        finsky.protos.Rated.Builder builderForValue) {
      copyOnWrite();
      instance.setRated(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .finsky.protos.Rated rated = 29;</code>
     */
    public Builder mergeRated(finsky.protos.Rated value) {
      copyOnWrite();
      instance.mergeRated(value);
      return this;
    }
    /**
     * <code>optional .finsky.protos.Rated rated = 29;</code>
     */
    public Builder clearRated() {  copyOnWrite();
      instance.clearRated();
      return this;
    }

    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    @java.lang.Override
    public java.util.List<finsky.protos.RelatedLink> getRelatedLinksList() {
      return java.util.Collections.unmodifiableList(
          instance.getRelatedLinksList());
    }
    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    @java.lang.Override
    public int getRelatedLinksCount() {
      return instance.getRelatedLinksCount();
    }/**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    @java.lang.Override
    public finsky.protos.RelatedLink getRelatedLinks(int index) {
      return instance.getRelatedLinks(index);
    }
    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    public Builder setRelatedLinks(
        int index, finsky.protos.RelatedLink value) {
      copyOnWrite();
      instance.setRelatedLinks(index, value);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    public Builder setRelatedLinks(
        int index, finsky.protos.RelatedLink.Builder builderForValue) {
      copyOnWrite();
      instance.setRelatedLinks(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    public Builder addRelatedLinks(finsky.protos.RelatedLink value) {
      copyOnWrite();
      instance.addRelatedLinks(value);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    public Builder addRelatedLinks(
        int index, finsky.protos.RelatedLink value) {
      copyOnWrite();
      instance.addRelatedLinks(index, value);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    public Builder addRelatedLinks(
        finsky.protos.RelatedLink.Builder builderForValue) {
      copyOnWrite();
      instance.addRelatedLinks(builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    public Builder addRelatedLinks(
        int index, finsky.protos.RelatedLink.Builder builderForValue) {
      copyOnWrite();
      instance.addRelatedLinks(index,
          builderForValue.build());
      return this;
    }
    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    public Builder addAllRelatedLinks(
        java.lang.Iterable<? extends finsky.protos.RelatedLink> values) {
      copyOnWrite();
      instance.addAllRelatedLinks(values);
      return this;
    }
    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    public Builder clearRelatedLinks() {
      copyOnWrite();
      instance.clearRelatedLinks();
      return this;
    }
    /**
     * <code>repeated .finsky.protos.RelatedLink relatedLinks = 34;</code>
     */
    public Builder removeRelatedLinks(int index) {
      copyOnWrite();
      instance.removeRelatedLinks(index);
      return this;
    }

    /**
     * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
     */
    @java.lang.Override
    public boolean hasCategoryInfo() {
      return instance.hasCategoryInfo();
    }
    /**
     * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
     */
    @java.lang.Override
    public finsky.protos.CategoryInfo getCategoryInfo() {
      return instance.getCategoryInfo();
    }
    /**
     * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
     */
    public Builder setCategoryInfo(finsky.protos.CategoryInfo value) {
      copyOnWrite();
      instance.setCategoryInfo(value);
      return this;
      }
    /**
     * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
     */
    public Builder setCategoryInfo(
        finsky.protos.CategoryInfo.Builder builderForValue) {
      copyOnWrite();
      instance.setCategoryInfo(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
     */
    public Builder mergeCategoryInfo(finsky.protos.CategoryInfo value) {
      copyOnWrite();
      instance.mergeCategoryInfo(value);
      return this;
    }
    /**
     * <code>optional .finsky.protos.CategoryInfo categoryInfo = 53;</code>
     */
    public Builder clearCategoryInfo() {  copyOnWrite();
      instance.clearCategoryInfo();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.RelatedLinks)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.RelatedLinks();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "unknown1_",
            "privacyPolicyUrl_",
            "youMightAlsoLike_",
            "rated_",
            "relatedLinks_",
            finsky.protos.RelatedLink.class,
            "categoryInfo_",
          };
          java.lang.String info =
              "\u0001\u0006\u0000\u0001\n5\u0006\u0000\u0001\u0000\n\u1009\u0000\u0012\u1008\u0001" +
              "\u0018\u1009\u0002\u001d\u1009\u0003\"\u001b5\u1009\u0004";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.RelatedLinks> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.RelatedLinks.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.RelatedLinks>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.RelatedLinks)
  private static final finsky.protos.RelatedLinks DEFAULT_INSTANCE;
  static {
    RelatedLinks defaultInstance = new RelatedLinks();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      RelatedLinks.class, defaultInstance);
  }

  public static finsky.protos.RelatedLinks getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<RelatedLinks> PARSER;

  public static com.google.protobuf.Parser<RelatedLinks> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
