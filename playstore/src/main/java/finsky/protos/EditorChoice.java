// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: messages.proto
// Protobuf Java Version: 4.28.2

package finsky.protos;

/**
 * Protobuf type {@code finsky.protos.EditorChoice}
 */
public  final class EditorChoice extends
    com.google.protobuf.GeneratedMessageLite<
        EditorChoice, EditorChoice.Builder> implements
    // @@protoc_insertion_point(message_implements:finsky.protos.EditorChoice)
    EditorChoiceOrBuilder {
  private EditorChoice() {
    bulletins_ = com.google.protobuf.GeneratedMessageLite.emptyProtobufList();
    description_ = "";
    title_ = "";
    subtitle_ = "";
  }
  private int bitField0_;
  public static final int BULLETINS_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.ProtobufList<java.lang.String> bulletins_;
  /**
   * <code>repeated string bulletins = 1;</code>
   * @return A list containing the bulletins.
   */
  @java.lang.Override
  public java.util.List<java.lang.String> getBulletinsList() {
    return bulletins_;
  }
  /**
   * <code>repeated string bulletins = 1;</code>
   * @return The count of bulletins.
   */
  @java.lang.Override
  public int getBulletinsCount() {
    return bulletins_.size();
  }
  /**
   * <code>repeated string bulletins = 1;</code>
   * @param index The index of the element to return.
   * @return The bulletins at the given index.
   */
  @java.lang.Override
  public java.lang.String getBulletins(int index) {
    return bulletins_.get(index);
  }
  /**
   * <code>repeated string bulletins = 1;</code>
   * @param index The index of the value to return.
   * @return The bytes of the bulletins at the given index.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getBulletinsBytes(int index) {
    return com.google.protobuf.ByteString.copyFromUtf8(
        bulletins_.get(index));
  }
  private void ensureBulletinsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<java.lang.String> tmp =
        bulletins_;  if (!tmp.isModifiable()) {
      bulletins_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }
  /**
   * <code>repeated string bulletins = 1;</code>
   * @param index The index to set the value at.
   * @param value The bulletins to set.
   */
  private void setBulletins(
      int index, java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  ensureBulletinsIsMutable();
    bulletins_.set(index, value);
  }
  /**
   * <code>repeated string bulletins = 1;</code>
   * @param value The bulletins to add.
   */
  private void addBulletins(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  ensureBulletinsIsMutable();
    bulletins_.add(value);
  }
  /**
   * <code>repeated string bulletins = 1;</code>
   * @param values The bulletins to add.
   */
  private void addAllBulletins(
      java.lang.Iterable<java.lang.String> values) {
    ensureBulletinsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, bulletins_);
  }
  /**
   * <code>repeated string bulletins = 1;</code>
   */
  private void clearBulletins() {
    bulletins_ = com.google.protobuf.GeneratedMessageLite.emptyProtobufList();
  }
  /**
   * <code>repeated string bulletins = 1;</code>
   * @param value The bytes of the bulletins to add.
   */
  private void addBulletinsBytes(
      com.google.protobuf.ByteString value) {
    ensureBulletinsIsMutable();
    bulletins_.add(value.toStringUtf8());
  }

  public static final int DESCRIPTION_FIELD_NUMBER = 2;
  private java.lang.String description_;
  /**
   * <code>optional string description = 2;</code>
   * @return Whether the description field is set.
   */
  @java.lang.Override
  public boolean hasDescription() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional string description = 2;</code>
   * @return The description.
   */
  @java.lang.Override
  public java.lang.String getDescription() {
    return description_;
  }
  /**
   * <code>optional string description = 2;</code>
   * @return The bytes for description.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getDescriptionBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(description_);
  }
  /**
   * <code>optional string description = 2;</code>
   * @param value The description to set.
   */
  private void setDescription(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000001;
    description_ = value;
  }
  /**
   * <code>optional string description = 2;</code>
   */
  private void clearDescription() {
    bitField0_ = (bitField0_ & ~0x00000001);
    description_ = getDefaultInstance().getDescription();
  }
  /**
   * <code>optional string description = 2;</code>
   * @param value The bytes for description to set.
   */
  private void setDescriptionBytes(
      com.google.protobuf.ByteString value) {
    description_ = value.toStringUtf8();
    bitField0_ |= 0x00000001;
  }

  public static final int STREAM_FIELD_NUMBER = 3;
  private finsky.protos.SubStream stream_;
  /**
   * <code>optional .finsky.protos.SubStream stream = 3;</code>
   */
  @java.lang.Override
  public boolean hasStream() {
    return ((bitField0_ & 0x00000002) != 0);
  }
  /**
   * <code>optional .finsky.protos.SubStream stream = 3;</code>
   */
  @java.lang.Override
  public finsky.protos.SubStream getStream() {
    return stream_ == null ? finsky.protos.SubStream.getDefaultInstance() : stream_;
  }
  /**
   * <code>optional .finsky.protos.SubStream stream = 3;</code>
   */
  private void setStream(finsky.protos.SubStream value) {
    value.getClass();
  stream_ = value;
    bitField0_ |= 0x00000002;
    }
  /**
   * <code>optional .finsky.protos.SubStream stream = 3;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeStream(finsky.protos.SubStream value) {
    value.getClass();
  if (stream_ != null &&
        stream_ != finsky.protos.SubStream.getDefaultInstance()) {
      stream_ =
        finsky.protos.SubStream.newBuilder(stream_).mergeFrom(value).buildPartial();
    } else {
      stream_ = value;
    }
    bitField0_ |= 0x00000002;
  }
  /**
   * <code>optional .finsky.protos.SubStream stream = 3;</code>
   */
  private void clearStream() {  stream_ = null;
    bitField0_ = (bitField0_ & ~0x00000002);
  }

  public static final int TITLE_FIELD_NUMBER = 4;
  private java.lang.String title_;
  /**
   * <code>optional string title = 4;</code>
   * @return Whether the title field is set.
   */
  @java.lang.Override
  public boolean hasTitle() {
    return ((bitField0_ & 0x00000004) != 0);
  }
  /**
   * <code>optional string title = 4;</code>
   * @return The title.
   */
  @java.lang.Override
  public java.lang.String getTitle() {
    return title_;
  }
  /**
   * <code>optional string title = 4;</code>
   * @return The bytes for title.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getTitleBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(title_);
  }
  /**
   * <code>optional string title = 4;</code>
   * @param value The title to set.
   */
  private void setTitle(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000004;
    title_ = value;
  }
  /**
   * <code>optional string title = 4;</code>
   */
  private void clearTitle() {
    bitField0_ = (bitField0_ & ~0x00000004);
    title_ = getDefaultInstance().getTitle();
  }
  /**
   * <code>optional string title = 4;</code>
   * @param value The bytes for title to set.
   */
  private void setTitleBytes(
      com.google.protobuf.ByteString value) {
    title_ = value.toStringUtf8();
    bitField0_ |= 0x00000004;
  }

  public static final int SUBTITLE_FIELD_NUMBER = 5;
  private java.lang.String subtitle_;
  /**
   * <code>optional string subtitle = 5;</code>
   * @return Whether the subtitle field is set.
   */
  @java.lang.Override
  public boolean hasSubtitle() {
    return ((bitField0_ & 0x00000008) != 0);
  }
  /**
   * <code>optional string subtitle = 5;</code>
   * @return The subtitle.
   */
  @java.lang.Override
  public java.lang.String getSubtitle() {
    return subtitle_;
  }
  /**
   * <code>optional string subtitle = 5;</code>
   * @return The bytes for subtitle.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSubtitleBytes() {
    return com.google.protobuf.ByteString.copyFromUtf8(subtitle_);
  }
  /**
   * <code>optional string subtitle = 5;</code>
   * @param value The subtitle to set.
   */
  private void setSubtitle(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  bitField0_ |= 0x00000008;
    subtitle_ = value;
  }
  /**
   * <code>optional string subtitle = 5;</code>
   */
  private void clearSubtitle() {
    bitField0_ = (bitField0_ & ~0x00000008);
    subtitle_ = getDefaultInstance().getSubtitle();
  }
  /**
   * <code>optional string subtitle = 5;</code>
   * @param value The bytes for subtitle to set.
   */
  private void setSubtitleBytes(
      com.google.protobuf.ByteString value) {
    subtitle_ = value.toStringUtf8();
    bitField0_ |= 0x00000008;
  }

  public static finsky.protos.EditorChoice parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.EditorChoice parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.EditorChoice parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.EditorChoice parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.EditorChoice parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static finsky.protos.EditorChoice parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static finsky.protos.EditorChoice parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.EditorChoice parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static finsky.protos.EditorChoice parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }

  public static finsky.protos.EditorChoice parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static finsky.protos.EditorChoice parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static finsky.protos.EditorChoice parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(finsky.protos.EditorChoice prototype) {
    return DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code finsky.protos.EditorChoice}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        finsky.protos.EditorChoice, Builder> implements
      // @@protoc_insertion_point(builder_implements:finsky.protos.EditorChoice)
      finsky.protos.EditorChoiceOrBuilder {
    // Construct using finsky.protos.EditorChoice.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>repeated string bulletins = 1;</code>
     * @return A list containing the bulletins.
     */
    @java.lang.Override
    public java.util.List<java.lang.String>
        getBulletinsList() {
      return java.util.Collections.unmodifiableList(
          instance.getBulletinsList());
    }
    /**
     * <code>repeated string bulletins = 1;</code>
     * @return The count of bulletins.
     */
    @java.lang.Override
    public int getBulletinsCount() {
      return instance.getBulletinsCount();
    }
    /**
     * <code>repeated string bulletins = 1;</code>
     * @param index The index of the element to return.
     * @return The bulletins at the given index.
     */
    @java.lang.Override
    public java.lang.String getBulletins(int index) {
      return instance.getBulletins(index);
    }
    /**
     * <code>repeated string bulletins = 1;</code>
     * @param index The index of the value to return.
     * @return The bytes of the bulletins at the given index.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getBulletinsBytes(int index) {
      return instance.getBulletinsBytes(index);
    }
    /**
     * <code>repeated string bulletins = 1;</code>
     * @param index The index to set the value at.
     * @param value The bulletins to set.
     * @return This builder for chaining.
     */
    public Builder setBulletins(
        int index, java.lang.String value) {
      copyOnWrite();
      instance.setBulletins(index, value);
      return this;
    }
    /**
     * <code>repeated string bulletins = 1;</code>
     * @param value The bulletins to add.
     * @return This builder for chaining.
     */
    public Builder addBulletins(
        java.lang.String value) {
      copyOnWrite();
      instance.addBulletins(value);
      return this;
    }
    /**
     * <code>repeated string bulletins = 1;</code>
     * @param values The bulletins to add.
     * @return This builder for chaining.
     */
    public Builder addAllBulletins(
        java.lang.Iterable<java.lang.String> values) {
      copyOnWrite();
      instance.addAllBulletins(values);
      return this;
    }
    /**
     * <code>repeated string bulletins = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearBulletins() {
      copyOnWrite();
      instance.clearBulletins();
      return this;
    }
    /**
     * <code>repeated string bulletins = 1;</code>
     * @param value The bytes of the bulletins to add.
     * @return This builder for chaining.
     */
    public Builder addBulletinsBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.addBulletinsBytes(value);
      return this;
    }

    /**
     * <code>optional string description = 2;</code>
     * @return Whether the description field is set.
     */
    @java.lang.Override
    public boolean hasDescription() {
      return instance.hasDescription();
    }
    /**
     * <code>optional string description = 2;</code>
     * @return The description.
     */
    @java.lang.Override
    public java.lang.String getDescription() {
      return instance.getDescription();
    }
    /**
     * <code>optional string description = 2;</code>
     * @return The bytes for description.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getDescriptionBytes() {
      return instance.getDescriptionBytes();
    }
    /**
     * <code>optional string description = 2;</code>
     * @param value The description to set.
     * @return This builder for chaining.
     */
    public Builder setDescription(
        java.lang.String value) {
      copyOnWrite();
      instance.setDescription(value);
      return this;
    }
    /**
     * <code>optional string description = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearDescription() {
      copyOnWrite();
      instance.clearDescription();
      return this;
    }
    /**
     * <code>optional string description = 2;</code>
     * @param value The bytes for description to set.
     * @return This builder for chaining.
     */
    public Builder setDescriptionBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setDescriptionBytes(value);
      return this;
    }

    /**
     * <code>optional .finsky.protos.SubStream stream = 3;</code>
     */
    @java.lang.Override
    public boolean hasStream() {
      return instance.hasStream();
    }
    /**
     * <code>optional .finsky.protos.SubStream stream = 3;</code>
     */
    @java.lang.Override
    public finsky.protos.SubStream getStream() {
      return instance.getStream();
    }
    /**
     * <code>optional .finsky.protos.SubStream stream = 3;</code>
     */
    public Builder setStream(finsky.protos.SubStream value) {
      copyOnWrite();
      instance.setStream(value);
      return this;
      }
    /**
     * <code>optional .finsky.protos.SubStream stream = 3;</code>
     */
    public Builder setStream(
        finsky.protos.SubStream.Builder builderForValue) {
      copyOnWrite();
      instance.setStream(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .finsky.protos.SubStream stream = 3;</code>
     */
    public Builder mergeStream(finsky.protos.SubStream value) {
      copyOnWrite();
      instance.mergeStream(value);
      return this;
    }
    /**
     * <code>optional .finsky.protos.SubStream stream = 3;</code>
     */
    public Builder clearStream() {  copyOnWrite();
      instance.clearStream();
      return this;
    }

    /**
     * <code>optional string title = 4;</code>
     * @return Whether the title field is set.
     */
    @java.lang.Override
    public boolean hasTitle() {
      return instance.hasTitle();
    }
    /**
     * <code>optional string title = 4;</code>
     * @return The title.
     */
    @java.lang.Override
    public java.lang.String getTitle() {
      return instance.getTitle();
    }
    /**
     * <code>optional string title = 4;</code>
     * @return The bytes for title.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getTitleBytes() {
      return instance.getTitleBytes();
    }
    /**
     * <code>optional string title = 4;</code>
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
     * <code>optional string title = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearTitle() {
      copyOnWrite();
      instance.clearTitle();
      return this;
    }
    /**
     * <code>optional string title = 4;</code>
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
     * <code>optional string subtitle = 5;</code>
     * @return Whether the subtitle field is set.
     */
    @java.lang.Override
    public boolean hasSubtitle() {
      return instance.hasSubtitle();
    }
    /**
     * <code>optional string subtitle = 5;</code>
     * @return The subtitle.
     */
    @java.lang.Override
    public java.lang.String getSubtitle() {
      return instance.getSubtitle();
    }
    /**
     * <code>optional string subtitle = 5;</code>
     * @return The bytes for subtitle.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getSubtitleBytes() {
      return instance.getSubtitleBytes();
    }
    /**
     * <code>optional string subtitle = 5;</code>
     * @param value The subtitle to set.
     * @return This builder for chaining.
     */
    public Builder setSubtitle(
        java.lang.String value) {
      copyOnWrite();
      instance.setSubtitle(value);
      return this;
    }
    /**
     * <code>optional string subtitle = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearSubtitle() {
      copyOnWrite();
      instance.clearSubtitle();
      return this;
    }
    /**
     * <code>optional string subtitle = 5;</code>
     * @param value The bytes for subtitle to set.
     * @return This builder for chaining.
     */
    public Builder setSubtitleBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setSubtitleBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:finsky.protos.EditorChoice)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new finsky.protos.EditorChoice();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "bitField0_",
            "bulletins_",
            "description_",
            "stream_",
            "title_",
            "subtitle_",
          };
          java.lang.String info =
              "\u0001\u0005\u0000\u0001\u0001\u0005\u0005\u0000\u0001\u0000\u0001\u001a\u0002\u1008" +
              "\u0000\u0003\u1009\u0001\u0004\u1008\u0002\u0005\u1008\u0003";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<finsky.protos.EditorChoice> parser = PARSER;
        if (parser == null) {
          synchronized (finsky.protos.EditorChoice.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<finsky.protos.EditorChoice>(
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


  // @@protoc_insertion_point(class_scope:finsky.protos.EditorChoice)
  private static final finsky.protos.EditorChoice DEFAULT_INSTANCE;
  static {
    EditorChoice defaultInstance = new EditorChoice();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      EditorChoice.class, defaultInstance);
  }

  public static finsky.protos.EditorChoice getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<EditorChoice> PARSER;

  public static com.google.protobuf.Parser<EditorChoice> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

