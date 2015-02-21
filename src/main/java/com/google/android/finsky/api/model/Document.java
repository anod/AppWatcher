package com.google.android.finsky.api.model;

import android.os.*;

import java.util.*;

import com.google.android.finsky.utils.*;
import com.google.android.finsky.protos.*;

import android.text.*;

public class Document implements Parcelable {
    public static Parcelable.Creator<Document> CREATOR;
    private Document[] mChildDocuments;
    private final DocumentV2.DocV2 mDocument;
    private Map<Integer, List<Common.Image>> mImageTypeMap;
    private List<Document> mSubscriptionsList;
    private Document mTrustedSourceProfileDocument;

    static {
        Document.CREATOR = (Parcelable.Creator<Document>) new Parcelable.Creator<Document>() {
            public Document createFromParcel(final Parcel parcel) {
                return null;//new Document(ParcelableProto.getProtoFromParcel(parcel));
            }

            public Document[] newArray(final int n) {
                return new Document[n];
            }
        };
    }

    public Document(final DocumentV2.DocV2 mDocument) {
        super();
        this.mDocument = mDocument;
    }

    private DocAnnotations.SectionMetadata getCrossSellSectionMetadata() {
        if (this.mDocument.annotations != null) {
            return this.mDocument.annotations.sectionCrossSell;
        }
        return null;
    }

    private Map<Integer, List<Common.Image>> getImageTypeMap() {
        if (this.mImageTypeMap == null) {
            this.mImageTypeMap = new HashMap<Integer, List<Common.Image>>();
            for (final Common.Image image2 : this.mDocument.image) {
                final int imageType = image2.imageType;
                if (!this.mImageTypeMap.containsKey(imageType)) {
                    this.mImageTypeMap.put(imageType, new ArrayList<Common.Image>());
                }
                this.mImageTypeMap.get(imageType).add(image2);
            }
        }
        return this.mImageTypeMap;
    }

    private DocAnnotations.SectionMetadata getMoreBySectionMetadata() {
        if (this.mDocument.annotations != null) {
            return this.mDocument.annotations.sectionMoreBy;
        }
        return null;
    }

    private DocAnnotations.SectionMetadata getRelatedSectionMetadata() {
        if (this.mDocument.annotations != null) {
            return this.mDocument.annotations.sectionRelated;
        }
        return null;
    }

//    private DocumentV2.Template getTemplate() {
//        if (this.mDocument.annotations != null) {
//            return this.mDocument.annotations.template;
//        }
//        return null;
//    }

    public static boolean isPreorderOffer(final Common.Offer offer) {
        return offer != null && (offer.offerType == 1 || offer.offerType == 7) && offer.hasOnSaleDate && offer.onSaleDate > System.currentTimeMillis();
    }

    public boolean canUseAsPartialDocument() {
        return this.getDocumentType() != 12 && this.getSongDetails() == null && this.mDocument.detailsReusable;
    }

    public int describeContents() {
        return 0;
    }

//    public DocumentV2.ActionBanner getActionBanner() {
//        if (this.isActionBanner()) {
//            return this.getTemplate().actionBanner;
//        }
//        return null;
//    }

    public DocDetails.AlbumDetails getAlbumDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.albumDetails;
        }
        return null;
    }

    //    public DocumentV2.SeriesAntenna getAntennaInfo() {
//        return this.getTemplate().seriesAntenna;
//    }
//
    public DocDetails.AppDetails getAppDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.appDetails;
        }
        return null;
    }

    public DocDetails.ArtistDetails getArtistDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.artistDetails;
        }
        return null;
    }

    public int getAvailabilityRestriction() {
        if (this.mDocument.availability != null) {
            return this.mDocument.availability.restriction;
        }
        return -1;
    }

    public Common.Offer[] getAvailableOffers() {
        return this.mDocument.offer;
    }

    public int getBackend() {
        return this.mDocument.backendId;
    }

    public String getBackendDocId() {
        return this.mDocument.backendDocid;
    }

    public DocumentV2.DocV2 getBackingDocV2() {
        return this.mDocument;
    }

    public DocAnnotations.BadgeContainer getBadgeContainer() {
        return this.mDocument.annotations.docBadgeContainer[0];
    }

    public String getBodyOfWorkBrowseUrl() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        if (this.hasBodyOfWork()) {
            return annotations.sectionBodyOfWork.browseUrl;
        }
        return "";
    }

    public String getBodyOfWorkHeader() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        if (this.hasBodyOfWork()) {
            return annotations.sectionBodyOfWork.header;
        }
        return "";
    }

    public String getBodyOfWorkListUrl() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        if (this.hasBodyOfWork()) {
            return annotations.sectionBodyOfWork.listUrl;
        }
        return "";
    }

    public DocDetails.BookDetails getBookDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.bookDetails;
        }
        return null;
    }

    public int getCensoring() {
        return this.mDocument.details.albumDetails.details.censoring;
    }

    public Document getChildAt(final int n) {
        if (this.mChildDocuments == null) {
            this.mChildDocuments = new Document[this.getChildCount()];
        }
        if (this.mChildDocuments[n] == null) {
            this.mChildDocuments[n] = new Document(this.mDocument.child[n]);
        }
        return this.mChildDocuments[n];
    }

    public int getChildCount() {
        return this.mDocument.child.length;
    }

    public Document[] getChildren() {
        if (this.mChildDocuments == null) {
            this.mChildDocuments = new Document[this.getChildCount()];
        }
        for (int childCount = this.getChildCount(), i = 0; i < childCount; ++i) {
            if (this.mChildDocuments[i] == null) {
                this.mChildDocuments[i] = new Document(this.mDocument.child[i]);
            }
        }
        return this.mChildDocuments;
    }

    public Containers.ContainerMetadata getContainerAnnotation() {
        return this.mDocument.containerMetadata;
    }

    public Containers.ContainerView[] getContainerViews() {
        return this.getContainerAnnotation().containerView;
    }

//    public DocumentV2.ContainerWithBanner getContainerWithBannerTemplate() {
//        return this.mDocument.annotations.template.containerWithBanner;
//    }

    public String getCoreContentHeader() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        if (annotations != null && annotations.sectionCoreContent != null) {
            return annotations.sectionCoreContent.header;
        }
        return "";
    }

    public String getCoreContentListUrl() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        if (annotations != null && annotations.sectionCoreContent != null) {
            return annotations.sectionCoreContent.listUrl;
        }
        return "";
    }

    public String getCreator() {
        return this.mDocument.creator;
    }

    public DocAnnotations.Badge[] getCreatorBadges() {
        return this.mDocument.annotations.badgeForCreator;
    }

    public Document getCreatorDoc() {
        if (this.hasCreatorDoc()) {
            return new Document(this.mDocument.annotations.creatorDoc);
        }
        return null;
    }

    public String getCrossSellBrowseUrl() {
        if (this.hasCrossSell()) {
            return this.getCrossSellSectionMetadata().browseUrl;
        }
        return "";
    }

    public String getCrossSellHeader() {
        if (this.hasCrossSell()) {
            return this.getCrossSellSectionMetadata().header;
        }
        return "";
    }

    public String getCrossSellListUrl() {
        if (this.hasCrossSell()) {
            return this.getCrossSellSectionMetadata().listUrl;
        }
        return "";
    }

    public DocAnnotations.SectionMetadata getCrossSellSection() {
        if (this.mDocument.annotations != null) {
            return this.mDocument.annotations.sectionCrossSell;
        }
        return null;
    }

//    public DocumentV2.DealOfTheDay getDealOfTheDayInfo() {
//        return this.getTemplate().dealOfTheDay;
//    }
//
//    public CharSequence getDescription() {
//        return FastHtmlParser.fromHtml(this.getRawDescription());
//    }

//    @Deprecated
//    public String getDescriptionReason() {
//        final DocumentV2.SuggestionReasons suggestionReasons = this.getSuggestionReasons();
//        if (suggestionReasons != null && suggestionReasons.reason.length > 0) {
//            return suggestionReasons.reason[0].descriptionHtml;
//        }
//        return "";
//    }

    public String getDetailsUrl() {
        return this.mDocument.detailsUrl;
    }

    public DocDetails.ArtistDetails getDisplayArtist() {
        if (this.getAlbumDetails() != null) {
            return this.getAlbumDetails().displayArtist;
        }
        return null;
    }

    public String getDocId() {
        return this.mDocument.docid;
    }

    public int getDocumentType() {
        return this.mDocument.docType;
    }

    //    public DocumentV2.EditorialSeriesContainer getEditorialSeriesContainer() {
//        return this.mDocument.annotations.template.editorialSeriesContainer;
//    }
//
//    public DocumentV2.EmptyContainer getEmptyContainer() {
//        return this.getTemplate().emptyContainer;
//    }
//
    public DocAnnotations.Badge getFirstCreatorBadge() {
        return this.mDocument.annotations.badgeForCreator[0];
    }

    public DocAnnotations.Badge getFirstItemBadge() {
        return this.mDocument.annotations.badgeForDoc[0];
    }

    public String getFormattedPrice(final int n) {
        final Common.Offer offer = this.getOffer(n);
        if (offer != null && offer.hasFormattedAmount) {
            return offer.formattedAmount;
        }
        return null;
    }

//    public Common.Docid getFullDocid() {
//        return DocUtils.createDocid(this.getBackend(), this.getDocumentType(), this.getBackendDocId());
//    }

    public List<Common.Image> getImages(final int n) {
        return this.getImageTypeMap().get(n);
    }

    public DocAnnotations.Badge[] getItemBadges() {
        return this.mDocument.annotations.badgeForDoc;
    }

    public DocAnnotations.Link getLinkAnnotation() {
        if (this.mDocument.annotations != null) {
            return this.mDocument.annotations.link;
        }
        return null;
    }

    public DocDetails.MagazineDetails getMagazineDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.magazineDetails;
        }
        return null;
    }

    public String getMoreByBrowseUrl() {
        if (this.hasMoreBy()) {
            return this.getMoreBySectionMetadata().browseUrl;
        }
        return "";
    }

    public String getMoreByHeader() {
        if (this.hasMoreBy()) {
            return this.getMoreBySectionMetadata().header;
        }
        return "";
    }

    public String getMoreByListUrl() {
        if (this.hasMoreBy()) {
            return this.getMoreBySectionMetadata().listUrl;
        }
        return "";
    }
//
//    public DocumentV2.Dismissal getNeutralDismissal() {
//        if (this.hasNeutralDismissal()) {
//            return this.getSuggestionReasons().neutralDismissal;
//        }
//        return null;
//    }

    //    public DocumentV2.NextBanner getNextBannerInfo() {
//        return this.getTemplate().nextBanner;
//    }
//
    public int getNormalizedContentRating() {
        final DocDetails.AppDetails appDetails = this.getAppDetails();
        if (appDetails == null) {
            return -1;
        }
        return -1 + appDetails.contentRating;
    }

    public Common.Offer getOffer(final int type) {
        for (final Common.Offer offer : this.getAvailableOffers()) {
            if (offer.offerType == type) {
                return offer;
            }
        }
        return null;
    }

    public String getOfferNote() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        if (annotations != null) {
            return annotations.offerNote;
        }
        return "";
    }

    public DocAnnotations.Warning getOptimalDeviceClassWarning() {
        if (this.hasOptimalDeviceClassWarning()) {
            return this.mDocument.annotations.optimalDeviceClassWarning;
        }
        return null;
    }

//    public DocumentV2.OverflowLink[] getOverflowLinks() {
//        final DocumentV2.Annotations annotations = this.mDocument.annotations;
//        if (annotations != null) {
//            return annotations.overflowLink;
//        }
//        return null;
//    }

    public DocDetails.PersonDetails getPersonDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.personDetails;
        }
        return null;
    }

//    public DocumentV2.PlusOneData getPlusOneData() {
//        if (this.mDocument.annotations != null) {
//            return this.mDocument.annotations.plusOneData;
//        }
//        return null;
//    }

    public DocAnnotations.SectionMetadata getPostPurchaseCrossSellSection() {
        if (this.mDocument.annotations != null) {
            return this.mDocument.annotations.sectionPurchaseCrossSell;
        }
        return null;
    }

    public String getPrivacyPolicyUrl() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        if (annotations != null) {
            return annotations.privacyPolicyUrl;
        }
        return null;
    }

    public DocDetails.ProductDetails getProductDetails() {
        return this.mDocument.productDetails;
    }

    public String getPromotionalDescription() {
        return this.mDocument.promotionalDescription;
    }

    public long getRatingCount() {
        return this.mDocument.aggregateRating.ratingsCount;
    }

    public int[] getRatingHistogram() {
        if (!this.hasRating()) {
            return new int[]{0, 0, 0, 0, 0};
        }
        final Rating.AggregateRating aggregateRating = this.mDocument.aggregateRating;
        return new int[]{(int) aggregateRating.fiveStarRatings, (int) aggregateRating.fourStarRatings, (int) aggregateRating.threeStarRatings, (int) aggregateRating.twoStarRatings, (int) aggregateRating.oneStarRatings};
    }

    public String getRawDescription() {
        return this.mDocument.descriptionHtml;
    }

    public String getRawTranslatedDescription() {
        return this.mDocument.translatedDescriptionHtml;
    }

//    public DocumentV2.RecommendationsContainerWithHeader getRecommendationsContainerWithHeaderTemplate() {
//        return this.mDocument.annotations.template.recommendationsContainerWithHeader;
//    }

//    public String getRelatedBrowseUrl() {
//        if (this.hasRelated()) {
//            return this.getRelatedSectionMetadata().browseUrl;
//        }
//        return "";
//    }

    public String getRelatedDocTypeHeader() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        if (annotations != null && annotations.sectionRelatedDocType != null) {
            return annotations.sectionRelatedDocType.header;
        }
        return "";
    }

    public String getRelatedDocTypeListUrl() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        if (annotations != null && annotations.sectionRelatedDocType != null) {
            return annotations.sectionRelatedDocType.listUrl;
        }
        return "";
    }

//    public String getRelatedHeader() {
//        if (this.hasRelated()) {
//            return this.getRelatedSectionMetadata().header;
//        }
//        return "";
//    }
//
//    public String getRelatedListUrl() {
//        if (this.hasRelated()) {
//            return this.getRelatedSectionMetadata().listUrl;
//        }
//        return "";
//    }

    public int getReleaseType() {
        return this.mDocument.details.albumDetails.details.releaseType[0];
    }

    public String getReviewsUrl() {
        return this.mDocument.reviewsUrl;
    }

    public byte[] getServerLogsCookie() {
        return this.mDocument.serverLogsCookie;
    }

    public String getShareUrl() {
        return this.mDocument.shareUrl;
    }

    public DocDetails.SongDetails getSongDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.songDetails;
        }
        return null;
    }

    public float getStarRating() {
        return this.mDocument.aggregateRating.starRating;
    }

    public List<Document> getSubscriptionsList() {
        if (!this.hasSubscriptions()) {
            return null;
        }
        if (this.mSubscriptionsList == null) {
            this.mSubscriptionsList = new ArrayList<Document>(this.mDocument.annotations.subscription.length);
            final DocumentV2.DocV2[] subscription = this.mDocument.annotations.subscription;
            for (int i = 0; i < subscription.length; ++i) {
                this.mSubscriptionsList.add(new Document(subscription[i]));
            }
        }
        return this.mSubscriptionsList;
    }

    public String getSubtitle() {
        return this.mDocument.subtitle;
    }

    public DocAnnotations.SectionMetadata getSuggestForRatingSection() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        if (annotations != null) {
            return annotations.sectionSuggestForRating;
        }
        return null;
    }

//    public DocumentV2.SuggestionReasons getSuggestionReasons() {
//        if (this.mDocument.annotations != null && this.mDocument.annotations.suggestionReasons != null) {
//            return this.mDocument.annotations.suggestionReasons;
//        }
//        return null;
//    }

    public DocDetails.TalentDetails getTalentDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.talentDetails;
        }
        return null;
    }

    public String getTitle() {
        return this.mDocument.title;
    }

//    public Document getTrustedSourcePersonDoc() {
//        if (this.mTrustedSourceProfileDocument == null) {
//            this.mTrustedSourceProfileDocument = new Document(this.getTemplate().trustedSourceContainer.source);
//        }
//        return this.mTrustedSourceProfileDocument;
//    }

    public DocDetails.TvEpisodeDetails getTvEpisodeDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.tvEpisodeDetails;
        }
        return null;
    }

    public DocDetails.TvSeasonDetails getTvSeasonDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.tvSeasonDetails;
        }
        return null;
    }

    public int getVersionCode() {
        if (this.getDocumentType() == 1) {
            return this.getAppDetails().versionCode;
        }
        return -1;
    }

    public DocDetails.VideoCredit[] getVideoCredits() {
        final DocDetails.VideoDetails videoDetails = this.getVideoDetails();
        if (videoDetails != null) {
            return videoDetails.credit;
        }
        return null;
    }

    public DocDetails.VideoDetails getVideoDetails() {
        if (this.hasDetails()) {
            return this.mDocument.details.videoDetails;
        }
        return null;
    }

//    public DocumentV2.WarmWelcome getWarmWelcome() {
//        if (this.isWarmWelcome()) {
//            return this.getTemplate().warmWelcome;
//        }
//        return null;
//    }
//
//    public CharSequence getWarningMessage() {
//        final StringBuilder sb = new StringBuilder();
//        final DocumentV2.Annotations annotations = this.mDocument.annotations;
//        for (int length = annotations.warning.length, i = 0; i < length; ++i) {
//            if (i != 0) {
//                sb.append("<br />");
//            }
//            sb.append(annotations.warning[i].localizedMessage);
//        }
//        return FastHtmlParser.fromHtml(sb.toString());
//    }
//
//    public CharSequence getWhatsNew() {
//        if (!this.hasDetails() || this.getAppDetails() == null) {
//            return "";
//        }
//        return FastHtmlParser.fromHtml(this.getAppDetails().recentChangesHtml);
//    }

    public String getYouTubeWatchUrl() {
        if (this.getBackend() == 4) {
            return this.mDocument.backendUrl;
        }
        return null;
    }

//    public boolean hasAntennaInfo() {
//        return this.getTemplate() != null && this.getTemplate().seriesAntenna != null;
//    }

    public boolean hasBadgeContainer() {
        return this.mDocument.annotations != null && this.mDocument.annotations.docBadgeContainer != null && this.mDocument.annotations.docBadgeContainer.length > 0;
    }

    public boolean hasBodyOfWork() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        return annotations != null && annotations.sectionBodyOfWork != null;
    }

    public boolean hasCensoring() {
        final DocDetails.AlbumDetails albumDetails = this.getAlbumDetails();
        return albumDetails != null && albumDetails.details != null && albumDetails.details.hasCensoring;
    }

    public boolean hasContainerAnnotation() {
        return this.mDocument.containerMetadata != null;
    }

    public boolean hasContainerViews() {
        return this.hasContainerAnnotation() && this.getContainerAnnotation().containerView.length > 0;
    }

//    public boolean hasContainerWithBannerTemplate() {
//        return this.mDocument.annotations != null && this.mDocument.annotations.template != null && this.mDocument.annotations.template.containerWithBanner != null;
//    }

    public boolean hasCreatorBadges() {
        return this.mDocument.annotations.badgeForCreator.length > 0;
    }

    public boolean hasCreatorDoc() {
        return this.mDocument.annotations != null && this.mDocument.annotations.creatorDoc != null;
    }

    public boolean hasCreatorRelatedContent() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        return annotations != null && annotations.sectionMoreBy != null && annotations.sectionMoreBy.listUrl.length() > 0;
    }

    public boolean hasCrossSell() {
        return this.getCrossSellSectionMetadata() != null;
    }

//    public boolean hasDealOfTheDayInfo() {
//        return this.getTemplate() != null && this.getTemplate().dealOfTheDay != null;
//    }

    public boolean hasDetails() {
        return this.mDocument.details != null;
    }

    public boolean hasDocumentType() {
        return this.mDocument.hasDocType;
    }

//    public boolean hasEditorialSeriesContainer() {
//        return this.mDocument.annotations != null && this.mDocument.annotations.template != null && this.mDocument.annotations.template.editorialSeriesContainer != null;
//    }

    public boolean hasImages(final int n) {
        return this.getImageTypeMap().containsKey(n);
    }

    public boolean hasItemBadges() {
        return this.mDocument.annotations.badgeForDoc.length > 0;
    }

    public boolean hasLinkAnnotation() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        return annotations != null && annotations.link != null;
    }

    public boolean hasMoreBy() {
        return this.getMoreBySectionMetadata() != null;
    }

//    public boolean hasNeutralDismissal() {
//        final DocumentV2.SuggestionReasons suggestionReasons = this.getSuggestionReasons();
//        return suggestionReasons != null && suggestionReasons.neutralDismissal != null;
//    }
//
//    public boolean hasNextBanner() {
//        return this.getTemplate() != null && this.getTemplate().nextBanner != null;
//    }

    public boolean hasOptimalDeviceClassWarning() {
        final DocumentV2.Annotations annotations = this.mDocument.annotations;
        return annotations != null && annotations.optimalDeviceClassWarning != null;
    }

//    public boolean hasPlusOneData() {
//        final DocumentV2.Annotations annotations = this.mDocument.annotations;
//        boolean b = false;
//        if (annotations != null) {
//            final DocumentV2.PlusOneData plusOneData = this.mDocument.annotations.plusOneData;
//            b = false;
//            if (plusOneData != null) {
//                b = true;
//            }
//        }
//        return b;
//    }

    public boolean hasProductDetails() {
        return this.mDocument.productDetails != null;
    }

    public boolean hasRating() {
        return this.mDocument.aggregateRating != null;
    }
//
//    public boolean hasReasons() {
//        return this.mDocument != null && this.getSuggestionReasons() != null && this.getSuggestionReasons().reason.length > 0;
//    }
//
//    public boolean hasRecommendationsContainerTemplate() {
//        return this.mDocument.annotations != null && this.mDocument.annotations.template != null && this.mDocument.annotations.template.recommendationsContainer != null;
//    }
//
//    public boolean hasRecommendationsContainerWithHeaderTemplate() {
//        return this.mDocument.annotations != null && this.mDocument.annotations.template != null && this.mDocument.annotations.template.recommendationsContainerWithHeader != null;
//    }
//
//    public boolean hasRelated() {
//        return this.getRelatedSectionMetadata() != null;
//    }

    public boolean hasReleaseType() {
        final DocDetails.AlbumDetails albumDetails = this.getAlbumDetails();
        return albumDetails != null && albumDetails.details != null && albumDetails.details.releaseType.length > 0;
    }

    public boolean hasReviewHistogramData() {
        final int[] ratingHistogram = this.getRatingHistogram();
        for (int length = ratingHistogram.length, i = 0; i < length; ++i) {
            if (ratingHistogram[i] > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSample() {
        final Common.Offer[] offer = this.mDocument.offer;
        for (int length = offer.length, i = 0; i < length; ++i) {
            if (offer[i].offerType == 2) {
                return true;
            }
        }
        return false;
    }

    public boolean hasScreenshots() {
        final List<Common.Image> images = this.getImages(1);
        return images != null && !images.isEmpty() && 1 != this.getBackend();
    }

    public boolean hasSubscriptions() {
        return this.mDocument.annotations != null && this.mDocument.annotations.subscription.length > 0;
    }

    public boolean hasWarningMessage() {
        return this.mDocument.annotations != null && this.mDocument.annotations.warning.length > 0;
    }

    public boolean hasWhatsNew() {
        return this.hasDetails() && this.getAppDetails() != null && !TextUtils.isEmpty((CharSequence) this.getAppDetails().recentChangesHtml);
    }
//
//    public boolean isActionBanner() {
//        return this.getTemplate() != null && this.getTemplate().actionBanner != null;
//    }
//
//    public boolean isAddToCirclesContainer() {
//        return this.getTemplate() != null && this.getTemplate().addToCirclesContainer != null;
//    }
//
//    public boolean isAvailableIfOwned() {
//        return this.mDocument.availability != null && this.mDocument.availability.availableIfOwned;
//    }
//
//    public boolean isDismissable() {
//        return this.hasReasons() && this.hasNeutralDismissal();
//    }
//
//    public boolean isEmptyContainer() {
//        return this.getTemplate() != null && this.getTemplate().emptyContainer != null;
//    }
//
//    public boolean isInProgressSeason() {
//        final DocDetails.TvSeasonDetails tvSeasonDetails = this.getTvSeasonDetails();
//        return this.mDocument.docType == 19 && tvSeasonDetails != null && tvSeasonDetails.hasExpectedEpisodeCount && tvSeasonDetails.episodeCount != tvSeasonDetails.expectedEpisodeCount;
//    }
//
//    public boolean isMature() {
//        return this.mDocument.mature;
//    }
//
//    public boolean isMyCirclesContainer() {
//        return this.getTemplate() != null && this.getTemplate().myCirclesContainer != null;
//    }
//
//    public boolean isOrdered() {
//        return this.mDocument.containerMetadata != null && this.mDocument.containerMetadata.ordered;
//    }
//
//    public boolean isRateAndSuggestCluster() {
//        return this.getTemplate() != null && this.getTemplate().rateAndSuggestContainer != null;
//    }
//
//    public boolean isRateCluster() {
//        return this.getTemplate() != null && this.getTemplate().rateContainer != null;
//    }
//
//    public boolean isSingleCardWithButton() {
//        return false;
//    }
//
//    public boolean isTrustedSourceContainer() {
//        return this.getTemplate() != null && this.getTemplate().trustedSourceContainer != null && this.getTemplate().trustedSourceContainer.source != null;
//    }
//
//    public boolean isWarmWelcome() {
//        return this.getTemplate() != null && this.getTemplate().warmWelcome != null;
//    }

    public boolean needsCheckoutFlow(final int n) {
        final Common.Offer offer = this.getOffer(n);
        return offer != null && offer.checkoutFlowRequired;
    }

    public void setDescription(final String descriptionHtml) {
        this.mDocument.descriptionHtml = descriptionHtml;
        this.mDocument.hasDescriptionHtml = true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append(this.getDocId());
        if (this.getDocumentType() == 1) {
            sb.append(" v=").append(this.getAppDetails().versionCode);
        }
        sb.append('}');
        return sb.toString();
    }

    public void writeToParcel(final Parcel parcel, final int n) {
        parcel.writeParcelable((Parcelable) ParcelableProto.forProto(this.mDocument), 0);
    }
}
