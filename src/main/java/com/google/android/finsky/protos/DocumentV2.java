package com.google.android.finsky.protos;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;

import java.io.IOException;
import java.util.Arrays;

public interface DocumentV2
{
    public static final class Annotations extends MessageNano
    {
        public DocAnnotations.Badge[] badgeForCreator;
        public DocAnnotations.Badge[] badgeForDoc;
        public DocV2 creatorDoc;
        public DocAnnotations.BadgeContainer[] docBadgeContainer;
        public boolean hasOfferNote;
        public boolean hasPrivacyPolicyUrl;
        public DocAnnotations.Link link;
       // public OBSOLETE_Reason oBSOLETEReason;
        public String offerNote;
        public DocAnnotations.Warning optimalDeviceClassWarning;
       // public OverflowLink[] overflowLink;
       // public PlusOneData plusOneData;
        public String privacyPolicyUrl;
        public DocAnnotations.PromotedDoc[] promotedDoc;
        public DocAnnotations.SectionMetadata sectionBodyOfWork;
        public DocAnnotations.SectionMetadata sectionCoreContent;
        public DocAnnotations.SectionMetadata sectionCrossSell;
        public DocAnnotations.SectionMetadata sectionMoreBy;
        public DocAnnotations.SectionMetadata sectionPurchaseCrossSell;
        public DocAnnotations.SectionMetadata sectionRateAndReview;
        public DocAnnotations.SectionMetadata sectionRelated;
        public DocAnnotations.SectionMetadata sectionRelatedDocType;
        public DocAnnotations.SectionMetadata sectionSuggestForRating;
        public DocV2[] subscription;
       // public SuggestionReasons suggestionReasons;
       // public Template template;
        public DocAnnotations.Warning[] warning;
        
        public Annotations() {
            super();
            this.clear();
        }
        
        public Annotations clear() {
            this.sectionRelated = null;
            this.sectionRelatedDocType = null;
            this.sectionMoreBy = null;
            this.sectionBodyOfWork = null;
            this.sectionCoreContent = null;
            this.sectionCrossSell = null;
            this.sectionPurchaseCrossSell = null;
            this.sectionSuggestForRating = null;
            this.sectionRateAndReview = null;
          //  this.plusOneData = null;
            this.warning = DocAnnotations.Warning.emptyArray();
            this.optimalDeviceClassWarning = null;
            this.link = null;
          //  this.template = null;
            this.badgeForCreator = DocAnnotations.Badge.emptyArray();
            this.badgeForDoc = DocAnnotations.Badge.emptyArray();
            this.docBadgeContainer = DocAnnotations.BadgeContainer.emptyArray();
            this.promotedDoc = DocAnnotations.PromotedDoc.emptyArray();
            this.offerNote = "";
            this.hasOfferNote = false;
            this.subscription = DocV2.emptyArray();
          //  this.suggestionReasons = null;
          //  this.oBSOLETEReason = null;
            this.privacyPolicyUrl = "";
            this.hasPrivacyPolicyUrl = false;
          //  this.overflowLink = OverflowLink.emptyArray();
            this.creatorDoc = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            DocAnnotations.Warning warning;
            DocAnnotations.Badge badge;
            DocAnnotations.Badge badge2;
            DocAnnotations.PromotedDoc promotedDoc;
            DocV2 docV2;
            DocAnnotations.BadgeContainer badgeContainer;
//            OverflowLink overflowLink;
            computeSerializedSize = super.computeSerializedSize();
            if (this.sectionRelated != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.sectionRelated);
            }
            if (this.sectionMoreBy != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.sectionMoreBy);
            }
//            if (this.plusOneData != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, this.plusOneData);
//            }
            if (this.warning != null && this.warning.length > 0) {
                for (int i = 0; i < this.warning.length; ++i) {
                    warning = this.warning[i];
                    if (warning != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, warning);
                    }
                }
            }
            if (this.sectionBodyOfWork != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(5, this.sectionBodyOfWork);
            }
            if (this.sectionCoreContent != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(6, this.sectionCoreContent);
            }
//            if (this.template != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(7, this.template);
//            }
            if (this.badgeForCreator != null && this.badgeForCreator.length > 0) {
                for (int j = 0; j < this.badgeForCreator.length; ++j) {
                    badge = this.badgeForCreator[j];
                    if (badge != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(8, badge);
                    }
                }
            }
            if (this.badgeForDoc != null && this.badgeForDoc.length > 0) {
                for (int k = 0; k < this.badgeForDoc.length; ++k) {
                    badge2 = this.badgeForDoc[k];
                    if (badge2 != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(9, badge2);
                    }
                }
            }
            if (this.link != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(10, this.link);
            }
            if (this.sectionCrossSell != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(11, this.sectionCrossSell);
            }
            if (this.sectionRelatedDocType != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(12, this.sectionRelatedDocType);
            }
            if (this.promotedDoc != null && this.promotedDoc.length > 0) {
                for (int l = 0; l < this.promotedDoc.length; ++l) {
                    promotedDoc = this.promotedDoc[l];
                    if (promotedDoc != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(13, promotedDoc);
                    }
                }
            }
            if (this.hasOfferNote || !this.offerNote.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(14, this.offerNote);
            }
            if (this.subscription != null && this.subscription.length > 0) {
                for (int n = 0; n < this.subscription.length; ++n) {
                    docV2 = this.subscription[n];
                    if (docV2 != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(16, docV2);
                    }
                }
            }
//            if (this.oBSOLETEReason != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(17, this.oBSOLETEReason);
//            }
            if (this.hasPrivacyPolicyUrl || !this.privacyPolicyUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(18, this.privacyPolicyUrl);
            }
//            if (this.suggestionReasons != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(19, this.suggestionReasons);
//            }
            if (this.optimalDeviceClassWarning != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(20, this.optimalDeviceClassWarning);
            }
            if (this.docBadgeContainer != null && this.docBadgeContainer.length > 0) {
                for (int n2 = 0; n2 < this.docBadgeContainer.length; ++n2) {
                    badgeContainer = this.docBadgeContainer[n2];
                    if (badgeContainer != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(21, badgeContainer);
                    }
                }
            }
            if (this.sectionSuggestForRating != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(22, this.sectionSuggestForRating);
            }
            if (this.sectionRateAndReview != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(23, this.sectionRateAndReview);
            }
            if (this.sectionPurchaseCrossSell != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(24, this.sectionPurchaseCrossSell);
            }
//            if (this.overflowLink != null && this.overflowLink.length > 0) {
//                for (int n3 = 0; n3 < this.overflowLink.length; ++n3) {
//                    overflowLink = this.overflowLink[n3];
//                    if (overflowLink != null) {
//                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(25, overflowLink);
//                    }
//                }
//            }
            if (this.creatorDoc != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(26, this.creatorDoc);
            }
            return computeSerializedSize;
        }
        
        @Override
        public Annotations mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            DocAnnotations.Warning[] warning;
            int repeatedFieldArrayLength2;
            int j;
            DocAnnotations.Badge[] badgeForCreator;
            int repeatedFieldArrayLength3;
            int k;
            DocAnnotations.Badge[] badgeForDoc;
            int repeatedFieldArrayLength4;
            int l;
            DocAnnotations.PromotedDoc[] promotedDoc;
            int repeatedFieldArrayLength5;
            int length;
            DocV2[] subscription;
            int repeatedFieldArrayLength6;
            int length2;
            DocAnnotations.BadgeContainer[] docBadgeContainer;
            int repeatedFieldArrayLength7;
            int length3;
//            OverflowLink[] overflowLink;
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            return this;
                        }
                        break;
                    }
                    case 0: {
                        return this;
                    }
                    case 10: {
                        if (this.sectionRelated == null) {
                            this.sectionRelated = new DocAnnotations.SectionMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.sectionRelated);
                        continue;
                    }
                    case 18: {
                        if (this.sectionMoreBy == null) {
                            this.sectionMoreBy = new DocAnnotations.SectionMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.sectionMoreBy);
                        continue;
                    }
//                    case 26: {
//                        if (this.plusOneData == null) {
//                            this.plusOneData = new PlusOneData();
//                        }
//                        codedInputByteBufferNano.readMessage(this.plusOneData);
//                        continue;
//                    }
                    case 34: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 34);
                        if (this.warning == null) {
                            i = 0;
                        }
                        else {
                            i = this.warning.length;
                        }
                        warning = new DocAnnotations.Warning[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.warning, 0, warning, 0, i);
                        }
                        while (i < -1 + warning.length) {
                            codedInputByteBufferNano.readMessage(warning[i] = new DocAnnotations.Warning());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(warning[i] = new DocAnnotations.Warning());
                        this.warning = warning;
                        continue;
                    }
                    case 42: {
                        if (this.sectionBodyOfWork == null) {
                            this.sectionBodyOfWork = new DocAnnotations.SectionMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.sectionBodyOfWork);
                        continue;
                    }
                    case 50: {
                        if (this.sectionCoreContent == null) {
                            this.sectionCoreContent = new DocAnnotations.SectionMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.sectionCoreContent);
                        continue;
                    }
//                    case 58: {
////                        if (this.template == null) {
////                            this.template = new Template();
////                        }
////                        codedInputByteBufferNano.readMessage(this.template);
//                        continue;
//                    }
                    case 66: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 66);
                        if (this.badgeForCreator == null) {
                            j = 0;
                        }
                        else {
                            j = this.badgeForCreator.length;
                        }
                        badgeForCreator = new DocAnnotations.Badge[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.badgeForCreator, 0, badgeForCreator, 0, j);
                        }
                        while (j < -1 + badgeForCreator.length) {
                            codedInputByteBufferNano.readMessage(badgeForCreator[j] = new DocAnnotations.Badge());
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        codedInputByteBufferNano.readMessage(badgeForCreator[j] = new DocAnnotations.Badge());
                        this.badgeForCreator = badgeForCreator;
                        continue;
                    }
                    case 74: {
                        repeatedFieldArrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 74);
                        if (this.badgeForDoc == null) {
                            k = 0;
                        }
                        else {
                            k = this.badgeForDoc.length;
                        }
                        badgeForDoc = new DocAnnotations.Badge[k + repeatedFieldArrayLength3];
                        if (k != 0) {
                            System.arraycopy(this.badgeForDoc, 0, badgeForDoc, 0, k);
                        }
                        while (k < -1 + badgeForDoc.length) {
                            codedInputByteBufferNano.readMessage(badgeForDoc[k] = new DocAnnotations.Badge());
                            codedInputByteBufferNano.readTag();
                            ++k;
                        }
                        codedInputByteBufferNano.readMessage(badgeForDoc[k] = new DocAnnotations.Badge());
                        this.badgeForDoc = badgeForDoc;
                        continue;
                    }
                    case 82: {
                        if (this.link == null) {
                            this.link = new DocAnnotations.Link();
                        }
                        codedInputByteBufferNano.readMessage(this.link);
                        continue;
                    }
                    case 90: {
                        if (this.sectionCrossSell == null) {
                            this.sectionCrossSell = new DocAnnotations.SectionMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.sectionCrossSell);
                        continue;
                    }
                    case 98: {
                        if (this.sectionRelatedDocType == null) {
                            this.sectionRelatedDocType = new DocAnnotations.SectionMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.sectionRelatedDocType);
                        continue;
                    }
                    case 106: {
                        repeatedFieldArrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 106);
                        if (this.promotedDoc == null) {
                            l = 0;
                        }
                        else {
                            l = this.promotedDoc.length;
                        }
                        promotedDoc = new DocAnnotations.PromotedDoc[l + repeatedFieldArrayLength4];
                        if (l != 0) {
                            System.arraycopy(this.promotedDoc, 0, promotedDoc, 0, l);
                        }
                        while (l < -1 + promotedDoc.length) {
                            codedInputByteBufferNano.readMessage(promotedDoc[l] = new DocAnnotations.PromotedDoc());
                            codedInputByteBufferNano.readTag();
                            ++l;
                        }
                        codedInputByteBufferNano.readMessage(promotedDoc[l] = new DocAnnotations.PromotedDoc());
                        this.promotedDoc = promotedDoc;
                        continue;
                    }
                    case 114: {
                        this.offerNote = codedInputByteBufferNano.readString();
                        this.hasOfferNote = true;
                        continue;
                    }
                    case 130: {
                        repeatedFieldArrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 130);
                        if (this.subscription == null) {
                            length = 0;
                        }
                        else {
                            length = this.subscription.length;
                        }
                        subscription = new DocV2[length + repeatedFieldArrayLength5];
                        if (length != 0) {
                            System.arraycopy(this.subscription, 0, subscription, 0, length);
                        }
                        while (length < -1 + subscription.length) {
                            codedInputByteBufferNano.readMessage(subscription[length] = new DocV2());
                            codedInputByteBufferNano.readTag();
                            ++length;
                        }
                        codedInputByteBufferNano.readMessage(subscription[length] = new DocV2());
                        this.subscription = subscription;
                        continue;
                    }
//                    case 138: {
////                        if (this.oBSOLETEReason == null) {
////                            this.oBSOLETEReason = new OBSOLETE_Reason();
////                        }
////                        codedInputByteBufferNano.readMessage(this.oBSOLETEReason);
//                        continue;
//                    }
                    case 146: {
                        this.privacyPolicyUrl = codedInputByteBufferNano.readString();
                        this.hasPrivacyPolicyUrl = true;
                        continue;
                    }
//                    case 154: {
////                        if (this.suggestionReasons == null) {
////                            this.suggestionReasons = new SuggestionReasons();
////                        }
////                        codedInputByteBufferNano.readMessage(this.suggestionReasons);
//                        continue;
//                    }
                    case 162: {
                        if (this.optimalDeviceClassWarning == null) {
                            this.optimalDeviceClassWarning = new DocAnnotations.Warning();
                        }
                        codedInputByteBufferNano.readMessage(this.optimalDeviceClassWarning);
                        continue;
                    }
                    case 170: {
                        repeatedFieldArrayLength6 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 170);
                        if (this.docBadgeContainer == null) {
                            length2 = 0;
                        }
                        else {
                            length2 = this.docBadgeContainer.length;
                        }
                        docBadgeContainer = new DocAnnotations.BadgeContainer[length2 + repeatedFieldArrayLength6];
                        if (length2 != 0) {
                            System.arraycopy(this.docBadgeContainer, 0, docBadgeContainer, 0, length2);
                        }
                        while (length2 < -1 + docBadgeContainer.length) {
                            codedInputByteBufferNano.readMessage(docBadgeContainer[length2] = new DocAnnotations.BadgeContainer());
                            codedInputByteBufferNano.readTag();
                            ++length2;
                        }
                        codedInputByteBufferNano.readMessage(docBadgeContainer[length2] = new DocAnnotations.BadgeContainer());
                        this.docBadgeContainer = docBadgeContainer;
                        continue;
                    }
                    case 178: {
                        if (this.sectionSuggestForRating == null) {
                            this.sectionSuggestForRating = new DocAnnotations.SectionMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.sectionSuggestForRating);
                        continue;
                    }
                    case 186: {
                        if (this.sectionRateAndReview == null) {
                            this.sectionRateAndReview = new DocAnnotations.SectionMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.sectionRateAndReview);
                        continue;
                    }
                    case 194: {
                        if (this.sectionPurchaseCrossSell == null) {
                            this.sectionPurchaseCrossSell = new DocAnnotations.SectionMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.sectionPurchaseCrossSell);
                        continue;
                    }
//                    case 202: {
////                        repeatedFieldArrayLength7 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 202);
////                        if (this.overflowLink == null) {
////                            length3 = 0;
////                        }
////                        else {
////                            length3 = this.overflowLink.length;
////                        }
////                        overflowLink = new OverflowLink[length3 + repeatedFieldArrayLength7];
////                        if (length3 != 0) {
////                            System.arraycopy(this.overflowLink, 0, overflowLink, 0, length3);
////                        }
////                        while (length3 < -1 + overflowLink.length) {
////                            codedInputByteBufferNano.readMessage(overflowLink[length3] = new OverflowLink());
////                            codedInputByteBufferNano.readTag();
////                            ++length3;
////                        }
////                        codedInputByteBufferNano.readMessage(overflowLink[length3] = new OverflowLink());
////                        this.overflowLink = overflowLink;
//                        continue;
//                    }
                    case 210: {
                        if (this.creatorDoc == null) {
                            this.creatorDoc = new DocV2();
                        }
                        codedInputByteBufferNano.readMessage(this.creatorDoc);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            DocAnnotations.Warning warning;
            DocAnnotations.Badge badge;
            DocAnnotations.Badge badge2;
            DocAnnotations.PromotedDoc promotedDoc;
            DocV2 docV2;
            DocAnnotations.BadgeContainer badgeContainer;
//            OverflowLink overflowLink;
            if (this.sectionRelated != null) {
                codedOutputByteBufferNano.writeMessage(1, this.sectionRelated);
            }
            if (this.sectionMoreBy != null) {
                codedOutputByteBufferNano.writeMessage(2, this.sectionMoreBy);
            }
//            if (this.plusOneData != null) {
//                codedOutputByteBufferNano.writeMessage(3, this.plusOneData);
//            }
            if (this.warning != null && this.warning.length > 0) {
                for (int i = 0; i < this.warning.length; ++i) {
                    warning = this.warning[i];
                    if (warning != null) {
                        codedOutputByteBufferNano.writeMessage(4, warning);
                    }
                }
            }
            if (this.sectionBodyOfWork != null) {
                codedOutputByteBufferNano.writeMessage(5, this.sectionBodyOfWork);
            }
            if (this.sectionCoreContent != null) {
                codedOutputByteBufferNano.writeMessage(6, this.sectionCoreContent);
            }
//            if (this.template != null) {
//                codedOutputByteBufferNano.writeMessage(7, this.template);
//            }
            if (this.badgeForCreator != null && this.badgeForCreator.length > 0) {
                for (int j = 0; j < this.badgeForCreator.length; ++j) {
                    badge = this.badgeForCreator[j];
                    if (badge != null) {
                        codedOutputByteBufferNano.writeMessage(8, badge);
                    }
                }
            }
            if (this.badgeForDoc != null && this.badgeForDoc.length > 0) {
                for (int k = 0; k < this.badgeForDoc.length; ++k) {
                    badge2 = this.badgeForDoc[k];
                    if (badge2 != null) {
                        codedOutputByteBufferNano.writeMessage(9, badge2);
                    }
                }
            }
            if (this.link != null) {
                codedOutputByteBufferNano.writeMessage(10, this.link);
            }
            if (this.sectionCrossSell != null) {
                codedOutputByteBufferNano.writeMessage(11, this.sectionCrossSell);
            }
            if (this.sectionRelatedDocType != null) {
                codedOutputByteBufferNano.writeMessage(12, this.sectionRelatedDocType);
            }
            if (this.promotedDoc != null && this.promotedDoc.length > 0) {
                for (int l = 0; l < this.promotedDoc.length; ++l) {
                    promotedDoc = this.promotedDoc[l];
                    if (promotedDoc != null) {
                        codedOutputByteBufferNano.writeMessage(13, promotedDoc);
                    }
                }
            }
            if (this.hasOfferNote || !this.offerNote.equals("")) {
                codedOutputByteBufferNano.writeString(14, this.offerNote);
            }
            if (this.subscription != null && this.subscription.length > 0) {
                for (int n = 0; n < this.subscription.length; ++n) {
                    docV2 = this.subscription[n];
                    if (docV2 != null) {
                        codedOutputByteBufferNano.writeMessage(16, docV2);
                    }
                }
            }
//            if (this.oBSOLETEReason != null) {
//                codedOutputByteBufferNano.writeMessage(17, this.oBSOLETEReason);
//            }
            if (this.hasPrivacyPolicyUrl || !this.privacyPolicyUrl.equals("")) {
                codedOutputByteBufferNano.writeString(18, this.privacyPolicyUrl);
            }
//            if (this.suggestionReasons != null) {
//                codedOutputByteBufferNano.writeMessage(19, this.suggestionReasons);
//            }
            if (this.optimalDeviceClassWarning != null) {
                codedOutputByteBufferNano.writeMessage(20, this.optimalDeviceClassWarning);
            }
            if (this.docBadgeContainer != null && this.docBadgeContainer.length > 0) {
                for (int n2 = 0; n2 < this.docBadgeContainer.length; ++n2) {
                    badgeContainer = this.docBadgeContainer[n2];
                    if (badgeContainer != null) {
                        codedOutputByteBufferNano.writeMessage(21, badgeContainer);
                    }
                }
            }
            if (this.sectionSuggestForRating != null) {
                codedOutputByteBufferNano.writeMessage(22, this.sectionSuggestForRating);
            }
            if (this.sectionRateAndReview != null) {
                codedOutputByteBufferNano.writeMessage(23, this.sectionRateAndReview);
            }
            if (this.sectionPurchaseCrossSell != null) {
                codedOutputByteBufferNano.writeMessage(24, this.sectionPurchaseCrossSell);
            }
//            if (this.overflowLink != null && this.overflowLink.length > 0) {
//                for (int n3 = 0; n3 < this.overflowLink.length; ++n3) {
//                    overflowLink = this.overflowLink[n3];
//                    if (overflowLink != null) {
//                        codedOutputByteBufferNano.writeMessage(25, overflowLink);
//                    }
//                }
//            }
            if (this.creatorDoc != null) {
                codedOutputByteBufferNano.writeMessage(26, this.creatorDoc);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class DocV2 extends MessageNano
    {
        private static volatile DocV2[] _emptyArray;
        public Rating.AggregateRating aggregateRating;
        public Annotations annotations;
        public FilterRules.Availability availability;
        public String backendDocid;
        public int backendId;
        public String backendUrl;
        public DocV2[] child;
        public Containers.ContainerMetadata containerMetadata;
        public String creator;
        public String descriptionHtml;
        public DocDetails.DocumentDetails details;
        public boolean detailsReusable;
        public String detailsUrl;
        public int docType;
        public String docid;
        public boolean hasBackendDocid;
        public boolean hasBackendId;
        public boolean hasBackendUrl;
        public boolean hasCreator;
        public boolean hasDescriptionHtml;
        public boolean hasDetailsReusable;
        public boolean hasDetailsUrl;
        public boolean hasDocType;
        public boolean hasDocid;
        public boolean hasMature;
        public boolean hasPromotionalDescription;
        public boolean hasPurchaseDetailsUrl;
        public boolean hasReviewsUrl;
        public boolean hasServerLogsCookie;
        public boolean hasShareUrl;
        public boolean hasSubtitle;
        public boolean hasTitle;
        public boolean hasTranslatedDescriptionHtml;
        public Common.Image[] image;
        public boolean mature;
        public Common.Offer[] offer;
        public DocDetails.ProductDetails productDetails;
        public String promotionalDescription;
        public String purchaseDetailsUrl;
        public String reviewsUrl;
        public byte[] serverLogsCookie;
        public String shareUrl;
        public String subtitle;
        public String title;
        public String translatedDescriptionHtml;
        
        public DocV2() {
            super();
            this.clear();
        }
        
        public static DocV2[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DocV2[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public DocV2 clear() {
            this.docid = "";
            this.hasDocid = false;
            this.backendDocid = "";
            this.hasBackendDocid = false;
            this.docType = 1;
            this.hasDocType = false;
            this.backendId = 0;
            this.hasBackendId = false;
            this.title = "";
            this.hasTitle = false;
            this.subtitle = "";
            this.hasSubtitle = false;
            this.creator = "";
            this.hasCreator = false;
            this.descriptionHtml = "";
            this.hasDescriptionHtml = false;
            this.translatedDescriptionHtml = "";
            this.hasTranslatedDescriptionHtml = false;
            this.promotionalDescription = "";
            this.hasPromotionalDescription = false;
            this.offer = Common.Offer.emptyArray();
            this.availability = null;
            this.image = Common.Image.emptyArray();
            this.child = emptyArray();
            this.containerMetadata = null;
            this.details = null;
            this.productDetails = null;
            this.aggregateRating = null;
            this.annotations = null;
            this.detailsUrl = "";
            this.hasDetailsUrl = false;
            this.shareUrl = "";
            this.hasShareUrl = false;
            this.reviewsUrl = "";
            this.hasReviewsUrl = false;
            this.backendUrl = "";
            this.hasBackendUrl = false;
            this.purchaseDetailsUrl = "";
            this.hasPurchaseDetailsUrl = false;
            this.detailsReusable = false;
            this.hasDetailsReusable = false;
            this.serverLogsCookie = WireFormatNano.EMPTY_BYTES;
            this.hasServerLogsCookie = false;
            this.mature = false;
            this.hasMature = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            Common.Offer offer;
            Common.Image image;
            DocV2 docV2;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasDocid || !this.docid.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.docid);
            }
            if (this.hasBackendDocid || !this.backendDocid.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.backendDocid);
            }
            if (this.docType != 1 || this.hasDocType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.docType);
            }
            if (this.backendId != 0 || this.hasBackendId) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, this.backendId);
            }
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.title);
            }
            if (this.hasCreator || !this.creator.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(6, this.creator);
            }
            if (this.hasDescriptionHtml || !this.descriptionHtml.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(7, this.descriptionHtml);
            }
            if (this.offer != null && this.offer.length > 0) {
                for (int i = 0; i < this.offer.length; ++i) {
                    offer = this.offer[i];
                    if (offer != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(8, offer);
                    }
                }
            }
            if (this.availability != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(9, this.availability);
            }
            if (this.image != null && this.image.length > 0) {
                for (int j = 0; j < this.image.length; ++j) {
                    image = this.image[j];
                    if (image != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(10, image);
                    }
                }
            }
            if (this.child != null && this.child.length > 0) {
                for (int k = 0; k < this.child.length; ++k) {
                    docV2 = this.child[k];
                    if (docV2 != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(11, docV2);
                    }
                }
            }
            if (this.containerMetadata != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(12, this.containerMetadata);
            }
            if (this.details != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(13, this.details);
            }
            if (this.aggregateRating != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(14, this.aggregateRating);
            }
            if (this.annotations != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(15, this.annotations);
            }
            if (this.hasDetailsUrl || !this.detailsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(16, this.detailsUrl);
            }
            if (this.hasShareUrl || !this.shareUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(17, this.shareUrl);
            }
            if (this.hasReviewsUrl || !this.reviewsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(18, this.reviewsUrl);
            }
            if (this.hasBackendUrl || !this.backendUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(19, this.backendUrl);
            }
            if (this.hasPurchaseDetailsUrl || !this.purchaseDetailsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(20, this.purchaseDetailsUrl);
            }
            if (this.hasDetailsReusable || this.detailsReusable) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(21, this.detailsReusable);
            }
            if (this.hasSubtitle || !this.subtitle.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(22, this.subtitle);
            }
            if (this.hasTranslatedDescriptionHtml || !this.translatedDescriptionHtml.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(23, this.translatedDescriptionHtml);
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBytesSize(24, this.serverLogsCookie);
            }
            if (this.productDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(25, this.productDetails);
            }
            if (this.hasMature || this.mature) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(26, this.mature);
            }
            if (this.hasPromotionalDescription || !this.promotionalDescription.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(27, this.promotionalDescription);
            }
            return computeSerializedSize;
        }
        
        @Override
        public DocV2 mergeFrom(CodedInputByteBufferNano input) throws IOException {
            int tag;
            while (true) {
                tag = input.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(input, tag)) {
                            return this;
                        }
                        break;
                    }
                    case 0: {
                        return this;
                    }
                    case 10: {
                        this.docid = input.readString();
                        this.hasDocid = true;
                        continue;
                    }
                    case 18: {
                        this.backendDocid = input.readString();
                        this.hasBackendDocid = true;
                        continue;
                    }
                    case 24: {
                        int int32 = input.readInt32();
                        switch (int32) {
                            default: {
                                continue;
                            }
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14:
                            case 15:
                            case 16:
                            case 17:
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                            case 29:
                            case 30:
                            case 31:
                            case 32: {
                                this.docType = int32;
                                this.hasDocType = true;
                                continue;
                            }
                        }
                    }
                    case 32: {
                        int int2 = input.readInt32();
                        switch (int2) {
                            default: {
                                continue;
                            }
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 9:
                            case 10:
                            case 11: {
                                this.backendId = int2;
                                this.hasBackendId = true;
                                continue;
                            }
                        }
                    }
                    case 42: {
                        this.title = input.readString();
                        this.hasTitle = true;
                        continue;
                    }
                    case 50: {
                        this.creator = input.readString();
                        this.hasCreator = true;
                        continue;
                    }
                    case 58: {
                        this.descriptionHtml = input.readString();
                        this.hasDescriptionHtml = true;
                        continue;
                    }
                    case 66: {
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 66);
                        int i = this.offer == null ? 0 : this.offer.length;
                        Common.Offer[] newArray = new Common.Offer[i + arrayLength];
                        if (i != 0) {
                            System.arraycopy(this.offer, 0, newArray, 0, i);
                        }
                        for (; i < newArray.length - 1; i++) {
                            newArray[i] = new Common.Offer();
                            input.readMessage(newArray[i]);
                            input.readTag();
                        }
                        // Last one without readTag.
                        newArray[i] = new Common.Offer();
                        input.readMessage(newArray[i]);
                        this.offer = newArray;
                        continue;
                    }
                    case 74: {
                        if (this.availability == null) {
                            this.availability = new FilterRules.Availability();
                        }
                        input.readMessage(this.availability);
                        continue;
                    }
                    case 82: {
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 82);
                        int i = this.image == null ? 0 : this.image.length;
                        Common.Image[] newArray = new Common.Image[i + arrayLength];
                        if (i != 0) {
                            System.arraycopy(this.image, 0, newArray, 0, i);
                        }
                        for (; i < newArray.length - 1; i++) {
                            newArray[i] = new Common.Image();
                            input.readMessage(newArray[i]);
                            input.readTag();
                        }
                        // Last one without readTag.
                        newArray[i] = new Common.Image();
                        input.readMessage(newArray[i]);
                        this.image = newArray;
                        continue;
                    }
                    case 90: {
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 90);
                        int i = this.child == null ? 0 : this.child.length;
                        DocV2[] newArray = new DocV2[i + arrayLength];
                        if (i != 0) {
                            System.arraycopy(this.child, 0, newArray, 0, i);
                        }
                        for (; i < newArray.length - 1; i++) {
                            newArray[i] = new DocV2();
                            input.readMessage(newArray[i]);
                            input.readTag();
                        }
                        // Last one without readTag.
                        newArray[i] = new DocV2();
                        input.readMessage(newArray[i]);
                        this.child = newArray;
                        continue;
                    }
                    case 98: {
                        if (this.containerMetadata == null) {
                            this.containerMetadata = new Containers.ContainerMetadata();
                        }
                        input.readMessage(this.containerMetadata);
                        continue;
                    }
                    case 106: {
                        if (this.details == null) {
                            this.details = new DocDetails.DocumentDetails();
                        }
                        input.readMessage(this.details);
                        continue;
                    }
                    case 114: {
                        if (this.aggregateRating == null) {
                            this.aggregateRating = new Rating.AggregateRating();
                        }
                        input.readMessage(this.aggregateRating);
                        continue;
                    }
                    case 122: {
                        if (this.annotations == null) {
                            this.annotations = new Annotations();
                        }
                        input.readMessage(this.annotations);
                        continue;
                    }
                    case 130: {
                        this.detailsUrl = input.readString();
                        this.hasDetailsUrl = true;
                        continue;
                    }
                    case 138: {
                        this.shareUrl = input.readString();
                        this.hasShareUrl = true;
                        continue;
                    }
                    case 146: {
                        this.reviewsUrl = input.readString();
                        this.hasReviewsUrl = true;
                        continue;
                    }
                    case 154: {
                        this.backendUrl = input.readString();
                        this.hasBackendUrl = true;
                        continue;
                    }
                    case 162: {
                        this.purchaseDetailsUrl = input.readString();
                        this.hasPurchaseDetailsUrl = true;
                        continue;
                    }
                    case 168: {
                        this.detailsReusable = input.readBool();
                        this.hasDetailsReusable = true;
                        continue;
                    }
                    case 178: {
                        this.subtitle = input.readString();
                        this.hasSubtitle = true;
                        continue;
                    }
                    case 186: {
                        this.translatedDescriptionHtml = input.readString();
                        this.hasTranslatedDescriptionHtml = true;
                        continue;
                    }
                    case 194: {
                        this.serverLogsCookie = input.readBytes();
                        this.hasServerLogsCookie = true;
                        continue;
                    }
                    case 202: {
                        if (this.productDetails == null) {
                            this.productDetails = new DocDetails.ProductDetails();
                        }
                        input.readMessage(this.productDetails);
                        continue;
                    }
                    case 208: {
                        this.mature = input.readBool();
                        this.hasMature = true;
                        continue;
                    }
                    case 218: {
                        this.promotionalDescription = input.readString();
                        this.hasPromotionalDescription = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            Common.Offer offer;
            Common.Image image;
            DocV2 docV2;
            if (this.hasDocid || !this.docid.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.docid);
            }
            if (this.hasBackendDocid || !this.backendDocid.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.backendDocid);
            }
            if (this.docType != 1 || this.hasDocType) {
                codedOutputByteBufferNano.writeInt32(3, this.docType);
            }
            if (this.backendId != 0 || this.hasBackendId) {
                codedOutputByteBufferNano.writeInt32(4, this.backendId);
            }
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.title);
            }
            if (this.hasCreator || !this.creator.equals("")) {
                codedOutputByteBufferNano.writeString(6, this.creator);
            }
            if (this.hasDescriptionHtml || !this.descriptionHtml.equals("")) {
                codedOutputByteBufferNano.writeString(7, this.descriptionHtml);
            }
            if (this.offer != null && this.offer.length > 0) {
                for (int i = 0; i < this.offer.length; ++i) {
                    offer = this.offer[i];
                    if (offer != null) {
                        codedOutputByteBufferNano.writeMessage(8, offer);
                    }
                }
            }
            if (this.availability != null) {
                codedOutputByteBufferNano.writeMessage(9, this.availability);
            }
            if (this.image != null && this.image.length > 0) {
                for (int j = 0; j < this.image.length; ++j) {
                    image = this.image[j];
                    if (image != null) {
                        codedOutputByteBufferNano.writeMessage(10, image);
                    }
                }
            }
            if (this.child != null && this.child.length > 0) {
                for (int k = 0; k < this.child.length; ++k) {
                    docV2 = this.child[k];
                    if (docV2 != null) {
                        codedOutputByteBufferNano.writeMessage(11, docV2);
                    }
                }
            }
            if (this.containerMetadata != null) {
                codedOutputByteBufferNano.writeMessage(12, this.containerMetadata);
            }
            if (this.details != null) {
                codedOutputByteBufferNano.writeMessage(13, this.details);
            }
            if (this.aggregateRating != null) {
                codedOutputByteBufferNano.writeMessage(14, this.aggregateRating);
            }
            if (this.annotations != null) {
                codedOutputByteBufferNano.writeMessage(15, this.annotations);
            }
            if (this.hasDetailsUrl || !this.detailsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(16, this.detailsUrl);
            }
            if (this.hasShareUrl || !this.shareUrl.equals("")) {
                codedOutputByteBufferNano.writeString(17, this.shareUrl);
            }
            if (this.hasReviewsUrl || !this.reviewsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(18, this.reviewsUrl);
            }
            if (this.hasBackendUrl || !this.backendUrl.equals("")) {
                codedOutputByteBufferNano.writeString(19, this.backendUrl);
            }
            if (this.hasPurchaseDetailsUrl || !this.purchaseDetailsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(20, this.purchaseDetailsUrl);
            }
            if (this.hasDetailsReusable || this.detailsReusable) {
                codedOutputByteBufferNano.writeBool(21, this.detailsReusable);
            }
            if (this.hasSubtitle || !this.subtitle.equals("")) {
                codedOutputByteBufferNano.writeString(22, this.subtitle);
            }
            if (this.hasTranslatedDescriptionHtml || !this.translatedDescriptionHtml.equals("")) {
                codedOutputByteBufferNano.writeString(23, this.translatedDescriptionHtml);
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                codedOutputByteBufferNano.writeBytes(24, this.serverLogsCookie);
            }
            if (this.productDetails != null) {
                codedOutputByteBufferNano.writeMessage(25, this.productDetails);
            }
            if (this.hasMature || this.mature) {
                codedOutputByteBufferNano.writeBool(26, this.mature);
            }
            if (this.hasPromotionalDescription || !this.promotionalDescription.equals("")) {
                codedOutputByteBufferNano.writeString(27, this.promotionalDescription);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class Review extends MessageNano
    {
        private static volatile Review[] _emptyArray;
        public DocV2 author;
        public String authorName;
        public String comment;
        public String commentId;
        public String deviceName;
        public String documentVersion;
        public boolean hasAuthorName;
        public boolean hasComment;
        public boolean hasCommentId;
        public boolean hasDeviceName;
        public boolean hasDocumentVersion;
        public boolean hasReplyText;
        public boolean hasReplyTimestampMsec;
        public boolean hasSource;
        public boolean hasStarRating;
        public boolean hasTimestampMsec;
        public boolean hasTitle;
        public boolean hasUrl;
        //public PlusData.OBSOLETE_PlusProfile oBSOLETEPlusProfile;
        public String replyText;
        public long replyTimestampMsec;
        public Common.Image sentiment;
        public String source;
        public int starRating;
        public long timestampMsec;
        public String title;
        public String url;

        public Review() {
            super();
            this.clear();
        }

        public static Review[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Review[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Review clear() {
            this.commentId = "";
            this.hasCommentId = false;
            this.author = null;
            this.starRating = 0;
            this.hasStarRating = false;
            this.sentiment = null;
            this.title = "";
            this.hasTitle = false;
            this.comment = "";
            this.hasComment = false;
            this.url = "";
            this.hasUrl = false;
            this.source = "";
            this.hasSource = false;
            this.documentVersion = "";
            this.hasDocumentVersion = false;
            this.timestampMsec = 0L;
            this.hasTimestampMsec = false;
            this.deviceName = "";
            this.hasDeviceName = false;
            this.replyText = "";
            this.hasReplyText = false;
            this.replyTimestampMsec = 0L;
            this.hasReplyTimestampMsec = false;
            //this.oBSOLETEPlusProfile = null;
            this.authorName = "";
            this.hasAuthorName = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasAuthorName || !this.authorName.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.authorName);
            }
            if (this.hasUrl || !this.url.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.url);
            }
            if (this.hasSource || !this.source.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.source);
            }
            if (this.hasDocumentVersion || !this.documentVersion.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.documentVersion);
            }
            if (this.hasTimestampMsec || this.timestampMsec != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(5, this.timestampMsec);
            }
            if (this.hasStarRating || this.starRating != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(6, this.starRating);
            }
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(7, this.title);
            }
            if (this.hasComment || !this.comment.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(8, this.comment);
            }
            if (this.hasCommentId || !this.commentId.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(9, this.commentId);
            }
            if (this.hasDeviceName || !this.deviceName.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(19, this.deviceName);
            }
            if (this.hasReplyText || !this.replyText.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(29, this.replyText);
            }
            if (this.hasReplyTimestampMsec || this.replyTimestampMsec != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(30, this.replyTimestampMsec);
            }
//            if (this.oBSOLETEPlusProfile != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(31, this.oBSOLETEPlusProfile);
//            }
            if (this.author != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(33, this.author);
            }
            if (this.sentiment != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(34, this.sentiment);
            }
            return computeSerializedSize;
        }

        @Override
        public Review mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            return this;
                        }
                        break;
                    }
                    case 0: {
                        return this;
                    }
                    case 10: {
                        this.authorName = codedInputByteBufferNano.readString();
                        this.hasAuthorName = true;
                        continue;
                    }
                    case 18: {
                        this.url = codedInputByteBufferNano.readString();
                        this.hasUrl = true;
                        continue;
                    }
                    case 26: {
                        this.source = codedInputByteBufferNano.readString();
                        this.hasSource = true;
                        continue;
                    }
                    case 34: {
                        this.documentVersion = codedInputByteBufferNano.readString();
                        this.hasDocumentVersion = true;
                        continue;
                    }
                    case 40: {
                        this.timestampMsec = codedInputByteBufferNano.readInt64();
                        this.hasTimestampMsec = true;
                        continue;
                    }
                    case 48: {
                        this.starRating = codedInputByteBufferNano.readInt32();
                        this.hasStarRating = true;
                        continue;
                    }
                    case 58: {
                        this.title = codedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        continue;
                    }
                    case 66: {
                        this.comment = codedInputByteBufferNano.readString();
                        this.hasComment = true;
                        continue;
                    }
                    case 74: {
                        this.commentId = codedInputByteBufferNano.readString();
                        this.hasCommentId = true;
                        continue;
                    }
                    case 154: {
                        this.deviceName = codedInputByteBufferNano.readString();
                        this.hasDeviceName = true;
                        continue;
                    }
                    case 234: {
                        this.replyText = codedInputByteBufferNano.readString();
                        this.hasReplyText = true;
                        continue;
                    }
                    case 240: {
                        this.replyTimestampMsec = codedInputByteBufferNano.readInt64();
                        this.hasReplyTimestampMsec = true;
                        continue;
                    }
                    case 250: {
//                        if (this.oBSOLETEPlusProfile == null) {
//                            this.oBSOLETEPlusProfile = new PlusData.OBSOLETE_PlusProfile();
//                        }
//                        codedInputByteBufferNano.readMessage(this.oBSOLETEPlusProfile);
                        continue;
                    }
                    case 266: {
                        if (this.author == null) {
                            this.author = new DocV2();
                        }
                        codedInputByteBufferNano.readMessage(this.author);
                        continue;
                    }
                    case 274: {
                        if (this.sentiment == null) {
                            this.sentiment = new Common.Image();
                        }
                        codedInputByteBufferNano.readMessage(this.sentiment);
                        continue;
                    }
                }
            }
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasAuthorName || !this.authorName.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.authorName);
            }
            if (this.hasUrl || !this.url.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.url);
            }
            if (this.hasSource || !this.source.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.source);
            }
            if (this.hasDocumentVersion || !this.documentVersion.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.documentVersion);
            }
            if (this.hasTimestampMsec || this.timestampMsec != 0L) {
                codedOutputByteBufferNano.writeInt64(5, this.timestampMsec);
            }
            if (this.hasStarRating || this.starRating != 0) {
                codedOutputByteBufferNano.writeInt32(6, this.starRating);
            }
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(7, this.title);
            }
            if (this.hasComment || !this.comment.equals("")) {
                codedOutputByteBufferNano.writeString(8, this.comment);
            }
            if (this.hasCommentId || !this.commentId.equals("")) {
                codedOutputByteBufferNano.writeString(9, this.commentId);
            }
            if (this.hasDeviceName || !this.deviceName.equals("")) {
                codedOutputByteBufferNano.writeString(19, this.deviceName);
            }
            if (this.hasReplyText || !this.replyText.equals("")) {
                codedOutputByteBufferNano.writeString(29, this.replyText);
            }
            if (this.hasReplyTimestampMsec || this.replyTimestampMsec != 0L) {
                codedOutputByteBufferNano.writeInt64(30, this.replyTimestampMsec);
            }
//            if (this.oBSOLETEPlusProfile != null) {
//                codedOutputByteBufferNano.writeMessage(31, this.oBSOLETEPlusProfile);
//            }
            if (this.author != null) {
                codedOutputByteBufferNano.writeMessage(33, this.author);
            }
            if (this.sentiment != null) {
                codedOutputByteBufferNano.writeMessage(34, this.sentiment);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

}
