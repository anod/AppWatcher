// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: messages.proto
// Protobuf Java Version: 4.28.2

package finsky.protos;

public interface DocDetailsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:finsky.protos.DocDetails)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <pre>
   * optional AlbumDetails albumDetails = 2;
   * optional ArtistDetails artistDetails = 3;
   * optional SongDetails songDetails = 4;
   * optional BookDetails bookDetails = 5;
   * optional VideoDetails videoDetails = 6;
   * optional SubscriptionDetails subscriptionDetails = 7;
   * optional MagazineDetails magazineDetails = 8;
   * optional TvShowDetails tvShowDetails = 9;
   * optional TvSeasonDetails tvSeasonDetails = 10;
   * optional TvEpisodeDetails tvEpisodeDetails = 11;
   * </pre>
   *
   * <code>optional .finsky.protos.AppDetails appDetails = 1;</code>
   * @return Whether the appDetails field is set.
   */
  boolean hasAppDetails();
  /**
   * <pre>
   * optional AlbumDetails albumDetails = 2;
   * optional ArtistDetails artistDetails = 3;
   * optional SongDetails songDetails = 4;
   * optional BookDetails bookDetails = 5;
   * optional VideoDetails videoDetails = 6;
   * optional SubscriptionDetails subscriptionDetails = 7;
   * optional MagazineDetails magazineDetails = 8;
   * optional TvShowDetails tvShowDetails = 9;
   * optional TvSeasonDetails tvSeasonDetails = 10;
   * optional TvEpisodeDetails tvEpisodeDetails = 11;
   * </pre>
   *
   * <code>optional .finsky.protos.AppDetails appDetails = 1;</code>
   * @return The appDetails.
   */
  finsky.protos.AppDetails getAppDetails();
}
