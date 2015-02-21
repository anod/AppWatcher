package com.google.android.finsky.protos;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;
import java.io.IOException;

public abstract interface DocumentV1
{
    public static final class DocV1 extends MessageNano
    {
        private static volatile DocV1[] _emptyArray;
        public String creator;
        public String descriptionHtml;
        public DocDetails.DocumentDetails details;
        public String detailsUrl;
        public String docid;
        public DocumentV1.OBSOLETE_FinskyDoc finskyDoc;
        public boolean hasCreator;
        public boolean hasDescriptionHtml;
        public boolean hasDetailsUrl;
        public boolean hasDocid;
        public boolean hasMoreByBrowseUrl;
        public boolean hasMoreByHeader;
        public boolean hasMoreByListUrl;
        public boolean hasRelatedBrowseUrl;
        public boolean hasRelatedHeader;
        public boolean hasRelatedListUrl;
        public boolean hasReviewsUrl;
        public boolean hasShareUrl;
        public boolean hasTitle;
        public boolean hasWarningMessage;
        public String moreByBrowseUrl;
        public String moreByHeader;
        public String moreByListUrl;
//        public DocumentV2.PlusOneData plusOneData;
        public String relatedBrowseUrl;
        public String relatedHeader;
        public String relatedListUrl;
        public String reviewsUrl;
        public String shareUrl;
        public String title;
        public String warningMessage;

        public DocV1()
        {
            clear();
        }

        public static DocV1[] emptyArray()
        {
            if (_emptyArray == null);
            synchronized (InternalNano.LAZY_INIT_LOCK)
            {
                if (_emptyArray == null)
                    _emptyArray = new DocV1[0];
                return _emptyArray;
            }
        }

        public DocV1 clear()
        {
            this.finskyDoc = null;
            this.docid = "";
            this.hasDocid = false;
            this.detailsUrl = "";
            this.hasDetailsUrl = false;
            this.reviewsUrl = "";
            this.hasReviewsUrl = false;
            this.relatedListUrl = "";
            this.hasRelatedListUrl = false;
            this.relatedBrowseUrl = "";
            this.hasRelatedBrowseUrl = false;
            this.relatedHeader = "";
            this.hasRelatedHeader = false;
            this.moreByListUrl = "";
            this.hasMoreByListUrl = false;
            this.moreByBrowseUrl = "";
            this.hasMoreByBrowseUrl = false;
            this.moreByHeader = "";
            this.hasMoreByHeader = false;
            this.shareUrl = "";
            this.hasShareUrl = false;
            this.title = "";
            this.hasTitle = false;
            this.creator = "";
            this.hasCreator = false;
            this.details = null;
            this.descriptionHtml = "";
            this.hasDescriptionHtml = false;
//            this.plusOneData = null;
            this.warningMessage = "";
            this.hasWarningMessage = false;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize()
        {
            int i = super.computeSerializedSize();
            if (this.finskyDoc != null)
                i += CodedOutputByteBufferNano.computeMessageSize(1, this.finskyDoc);
            if ((this.hasDocid) || (!this.docid.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(2, this.docid);
            if ((this.hasDetailsUrl) || (!this.detailsUrl.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(3, this.detailsUrl);
            if ((this.hasReviewsUrl) || (!this.reviewsUrl.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(4, this.reviewsUrl);
            if ((this.hasRelatedListUrl) || (!this.relatedListUrl.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(5, this.relatedListUrl);
            if ((this.hasMoreByListUrl) || (!this.moreByListUrl.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(6, this.moreByListUrl);
            if ((this.hasShareUrl) || (!this.shareUrl.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(7, this.shareUrl);
            if ((this.hasCreator) || (!this.creator.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(8, this.creator);
            if (this.details != null)
                i += CodedOutputByteBufferNano.computeMessageSize(9, this.details);
            if ((this.hasDescriptionHtml) || (!this.descriptionHtml.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(10, this.descriptionHtml);
            if ((this.hasRelatedBrowseUrl) || (!this.relatedBrowseUrl.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(11, this.relatedBrowseUrl);
            if ((this.hasMoreByBrowseUrl) || (!this.moreByBrowseUrl.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(12, this.moreByBrowseUrl);
            if ((this.hasRelatedHeader) || (!this.relatedHeader.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(13, this.relatedHeader);
            if ((this.hasMoreByHeader) || (!this.moreByHeader.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(14, this.moreByHeader);
            if ((this.hasTitle) || (!this.title.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(15, this.title);
//            if (this.plusOneData != null)
//                i += CodedOutputByteBufferNano.computeMessageSize(16, this.plusOneData);
            if ((this.hasWarningMessage) || (!this.warningMessage.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(17, this.warningMessage);
            return i;
        }

        public DocV1 mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException
        {
            while (true)
            {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i)
                {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 10:
                        if (this.finskyDoc == null)
                            this.finskyDoc = new DocumentV1.OBSOLETE_FinskyDoc();
                        paramCodedInputByteBufferNano.readMessage(this.finskyDoc);
                        break;
                    case 18:
                        this.docid = paramCodedInputByteBufferNano.readString();
                        this.hasDocid = true;
                        break;
                    case 26:
                        this.detailsUrl = paramCodedInputByteBufferNano.readString();
                        this.hasDetailsUrl = true;
                        break;
                    case 34:
                        this.reviewsUrl = paramCodedInputByteBufferNano.readString();
                        this.hasReviewsUrl = true;
                        break;
                    case 42:
                        this.relatedListUrl = paramCodedInputByteBufferNano.readString();
                        this.hasRelatedListUrl = true;
                        break;
                    case 50:
                        this.moreByListUrl = paramCodedInputByteBufferNano.readString();
                        this.hasMoreByListUrl = true;
                        break;
                    case 58:
                        this.shareUrl = paramCodedInputByteBufferNano.readString();
                        this.hasShareUrl = true;
                        break;
                    case 66:
                        this.creator = paramCodedInputByteBufferNano.readString();
                        this.hasCreator = true;
                        break;
                    case 74:
                        if (this.details == null)
                            this.details = new DocDetails.DocumentDetails();
                        paramCodedInputByteBufferNano.readMessage(this.details);
                        break;
                    case 82:
                        this.descriptionHtml = paramCodedInputByteBufferNano.readString();
                        this.hasDescriptionHtml = true;
                        break;
                    case 90:
                        this.relatedBrowseUrl = paramCodedInputByteBufferNano.readString();
                        this.hasRelatedBrowseUrl = true;
                        break;
                    case 98:
                        this.moreByBrowseUrl = paramCodedInputByteBufferNano.readString();
                        this.hasMoreByBrowseUrl = true;
                        break;
                    case 106:
                        this.relatedHeader = paramCodedInputByteBufferNano.readString();
                        this.hasRelatedHeader = true;
                        break;
                    case 114:
                        this.moreByHeader = paramCodedInputByteBufferNano.readString();
                        this.hasMoreByHeader = true;
                        break;
                    case 122:
                        this.title = paramCodedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        break;
                    case 130:
//                        if (this.plusOneData == null)
//                            this.plusOneData = new DocumentV2.PlusOneData();
//                        paramCodedInputByteBufferNano.readMessage(this.plusOneData);
                        break;
                    case 138:
                }
                this.warningMessage = paramCodedInputByteBufferNano.readString();
                this.hasWarningMessage = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException
        {
            if (this.finskyDoc != null)
                paramCodedOutputByteBufferNano.writeMessage(1, this.finskyDoc);
            if ((this.hasDocid) || (!this.docid.equals("")))
                paramCodedOutputByteBufferNano.writeString(2, this.docid);
            if ((this.hasDetailsUrl) || (!this.detailsUrl.equals("")))
                paramCodedOutputByteBufferNano.writeString(3, this.detailsUrl);
            if ((this.hasReviewsUrl) || (!this.reviewsUrl.equals("")))
                paramCodedOutputByteBufferNano.writeString(4, this.reviewsUrl);
            if ((this.hasRelatedListUrl) || (!this.relatedListUrl.equals("")))
                paramCodedOutputByteBufferNano.writeString(5, this.relatedListUrl);
            if ((this.hasMoreByListUrl) || (!this.moreByListUrl.equals("")))
                paramCodedOutputByteBufferNano.writeString(6, this.moreByListUrl);
            if ((this.hasShareUrl) || (!this.shareUrl.equals("")))
                paramCodedOutputByteBufferNano.writeString(7, this.shareUrl);
            if ((this.hasCreator) || (!this.creator.equals("")))
                paramCodedOutputByteBufferNano.writeString(8, this.creator);
            if (this.details != null)
                paramCodedOutputByteBufferNano.writeMessage(9, this.details);
            if ((this.hasDescriptionHtml) || (!this.descriptionHtml.equals("")))
                paramCodedOutputByteBufferNano.writeString(10, this.descriptionHtml);
            if ((this.hasRelatedBrowseUrl) || (!this.relatedBrowseUrl.equals("")))
                paramCodedOutputByteBufferNano.writeString(11, this.relatedBrowseUrl);
            if ((this.hasMoreByBrowseUrl) || (!this.moreByBrowseUrl.equals("")))
                paramCodedOutputByteBufferNano.writeString(12, this.moreByBrowseUrl);
            if ((this.hasRelatedHeader) || (!this.relatedHeader.equals("")))
                paramCodedOutputByteBufferNano.writeString(13, this.relatedHeader);
            if ((this.hasMoreByHeader) || (!this.moreByHeader.equals("")))
                paramCodedOutputByteBufferNano.writeString(14, this.moreByHeader);
            if ((this.hasTitle) || (!this.title.equals("")))
                paramCodedOutputByteBufferNano.writeString(15, this.title);
//            if (this.plusOneData != null)
//                paramCodedOutputByteBufferNano.writeMessage(16, this.plusOneData);
            if ((this.hasWarningMessage) || (!this.warningMessage.equals("")))
                paramCodedOutputByteBufferNano.writeString(17, this.warningMessage);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class OBSOLETE_FinskyDoc extends MessageNano
    {
        private static volatile OBSOLETE_FinskyDoc[] _emptyArray;
//        public Rating.AggregateRating aggregateRating;
//        public FilterRules.Availability availability;
        public OBSOLETE_FinskyDoc[] child;
        public Common.Docid docid;
        public Common.Docid fetchDocid;
        public boolean hasTitle;
        public boolean hasUrl;
        public Common.Image[] image;
        public Common.Offer[] offer;
        public Common.Offer priceDeprecated;
        public Common.Docid sampleDocid;
        public String title;
        public DocumentV1.OBSOLETE_FinskyTranslatedText[] translatedSnippet;
        public String url;

        public OBSOLETE_FinskyDoc()
        {
            clear();
        }

        public static OBSOLETE_FinskyDoc[] emptyArray()
        {
            if (_emptyArray == null);
            synchronized (InternalNano.LAZY_INIT_LOCK)
            {
                if (_emptyArray == null)
                    _emptyArray = new OBSOLETE_FinskyDoc[0];
                return _emptyArray;
            }
        }

        public OBSOLETE_FinskyDoc clear()
        {
            this.docid = null;
            this.fetchDocid = null;
            this.sampleDocid = null;
            this.title = "";
            this.hasTitle = false;
            this.url = "";
            this.hasUrl = false;
            this.translatedSnippet = DocumentV1.OBSOLETE_FinskyTranslatedText.emptyArray();
            this.priceDeprecated = null;
            this.offer = Common.Offer.emptyArray();
//            this.availability = null;
            this.image = Common.Image.emptyArray();
            this.child = emptyArray();
//            this.aggregateRating = null;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize()
        {
            int i = super.computeSerializedSize();
            if (this.docid != null)
                i += CodedOutputByteBufferNano.computeMessageSize(1, this.docid);
            if (this.fetchDocid != null)
                i += CodedOutputByteBufferNano.computeMessageSize(2, this.fetchDocid);
            if (this.sampleDocid != null)
                i += CodedOutputByteBufferNano.computeMessageSize(3, this.sampleDocid);
            if ((this.hasTitle) || (!this.title.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(4, this.title);
            if ((this.hasUrl) || (!this.url.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(5, this.url);
            if (this.priceDeprecated != null)
                i += CodedOutputByteBufferNano.computeMessageSize(7, this.priceDeprecated);
//            if (this.availability != null)
//                i += CodedOutputByteBufferNano.computeMessageSize(9, this.availability);
            if ((this.image != null) && (this.image.length > 0))
                for (int n = 0; n < this.image.length; n++)
                {
                    Common.Image localImage = this.image[n];
                    if (localImage != null)
                        i += CodedOutputByteBufferNano.computeMessageSize(10, localImage);
                }
            if ((this.child != null) && (this.child.length > 0))
                for (int m = 0; m < this.child.length; m++)
                {
                    OBSOLETE_FinskyDoc localOBSOLETE_FinskyDoc = this.child[m];
                    if (localOBSOLETE_FinskyDoc != null)
                        i += CodedOutputByteBufferNano.computeMessageSize(11, localOBSOLETE_FinskyDoc);
                }
//            if (this.aggregateRating != null)
//                i += CodedOutputByteBufferNano.computeMessageSize(13, this.aggregateRating);
            if ((this.offer != null) && (this.offer.length > 0))
                for (int k = 0; k < this.offer.length; k++)
                {
                    Common.Offer localOffer = this.offer[k];
                    if (localOffer != null)
                        i += CodedOutputByteBufferNano.computeMessageSize(14, localOffer);
                }
            if ((this.translatedSnippet != null) && (this.translatedSnippet.length > 0))
                for (int j = 0; j < this.translatedSnippet.length; j++)
                {
                    DocumentV1.OBSOLETE_FinskyTranslatedText localOBSOLETE_FinskyTranslatedText = this.translatedSnippet[j];
                    if (localOBSOLETE_FinskyTranslatedText != null)
                        i += CodedOutputByteBufferNano.computeMessageSize(15, localOBSOLETE_FinskyTranslatedText);
                }
            return i;
        }

        public OBSOLETE_FinskyDoc mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException
        {
            while (true)
            {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i)
                {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 10:
                        if (this.docid == null)
                            this.docid = new Common.Docid();
                        paramCodedInputByteBufferNano.readMessage(this.docid);
                        break;
                    case 18:
                        if (this.fetchDocid == null)
                            this.fetchDocid = new Common.Docid();
                        paramCodedInputByteBufferNano.readMessage(this.fetchDocid);
                        break;
                    case 26:
                        if (this.sampleDocid == null)
                            this.sampleDocid = new Common.Docid();
                        paramCodedInputByteBufferNano.readMessage(this.sampleDocid);
                        break;
                    case 34:
                        this.title = paramCodedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        break;
                    case 42:
                        this.url = paramCodedInputByteBufferNano.readString();
                        this.hasUrl = true;
                        break;
                    case 58:
                        if (this.priceDeprecated == null)
                            this.priceDeprecated = new Common.Offer();
                        paramCodedInputByteBufferNano.readMessage(this.priceDeprecated);
                        break;
                    case 74:
//                        if (this.availability == null)
//                            this.availability = new FilterRules.Availability();
//                        paramCodedInputByteBufferNano.readMessage(this.availability);
                        break;
                    case 82:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 82);
                        int i3 = this.image == null ? 0 : this.image.length;
                        Common.Image[] newArray = new Common.Image[i3 + arrayLength];
                        if (i3 != 0) {
                            System.arraycopy(this.image, 0, newArray, 0, i3);
                        }
                        for (; i3 < newArray.length - 1; i3++) {
                            newArray[i3] = new Common.Image();
                            paramCodedInputByteBufferNano.readMessage(newArray[i3]);
                            paramCodedInputByteBufferNano.readTag();
                        }
                        // Last one without readTag.
                        newArray[i3] = new Common.Image();
                        paramCodedInputByteBufferNano.readMessage(newArray[i]);
                        this.image = newArray;

                        break;
                    case 90:
//                        int i1 = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 90);
//                        if (this.child == null);
//                        OBSOLETE_FinskyDoc[] arrayOfOBSOLETE_FinskyDoc;
//                        for (int i2 = 0; ; i2 = this.child.length)
//                        {
//                            arrayOfOBSOLETE_FinskyDoc = new OBSOLETE_FinskyDoc[i2 + i1];
//                            if (i2 != 0)
//                                System.arraycopy(this.child, 0, arrayOfOBSOLETE_FinskyDoc, 0, i2);
//                            while (i2 < -1 + arrayOfOBSOLETE_FinskyDoc.length)
//                            {
//                                arrayOfOBSOLETE_FinskyDoc[i2] = new OBSOLETE_FinskyDoc();
//                                paramCodedInputByteBufferNano.readMessage(arrayOfOBSOLETE_FinskyDoc[i2]);
//                                paramCodedInputByteBufferNano.readTag();
//                                i2++;
//                            }
//                        }
//                        arrayOfOBSOLETE_FinskyDoc[i2] = new OBSOLETE_FinskyDoc();
//                        paramCodedInputByteBufferNano.readMessage(arrayOfOBSOLETE_FinskyDoc[i2]);
//                        this.child = arrayOfOBSOLETE_FinskyDoc;
                        break;
                    case 106:
//                        if (this.aggregateRating == null)
//                            this.aggregateRating = new Rating.AggregateRating();
//                        paramCodedInputByteBufferNano.readMessage(this.aggregateRating);
                        break;
                    case 114:
//                        int m = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 114);
//                        if (this.offer == null);
//                        Common.Offer[] arrayOfOffer;
//                        for (int n = 0; ; n = this.offer.length)
//                        {
//                            arrayOfOffer = new Common.Offer[n + m];
//                            if (n != 0)
//                                System.arraycopy(this.offer, 0, arrayOfOffer, 0, n);
//                            while (n < -1 + arrayOfOffer.length)
//                            {
//                                arrayOfOffer[n] = new Common.Offer();
//                                paramCodedInputByteBufferNano.readMessage(arrayOfOffer[n]);
//                                paramCodedInputByteBufferNano.readTag();
//                                n++;
//                            }
//                        }
//                        arrayOfOffer[n] = new Common.Offer();
//                        paramCodedInputByteBufferNano.readMessage(arrayOfOffer[n]);
//                        this.offer = arrayOfOffer;
                        break;
                    case 122:
                }
//                int j = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 122);
//                if (this.translatedSnippet == null);
//                DocumentV1.OBSOLETE_FinskyTranslatedText[] arrayOfOBSOLETE_FinskyTranslatedText;
//                for (int k = 0; ; k = this.translatedSnippet.length)
//                {
//                    arrayOfOBSOLETE_FinskyTranslatedText = new DocumentV1.OBSOLETE_FinskyTranslatedText[k + j];
//                    if (k != 0)
//                        System.arraycopy(this.translatedSnippet, 0, arrayOfOBSOLETE_FinskyTranslatedText, 0, k);
//                    while (k < -1 + arrayOfOBSOLETE_FinskyTranslatedText.length)
//                    {
//                        arrayOfOBSOLETE_FinskyTranslatedText[k] = new DocumentV1.OBSOLETE_FinskyTranslatedText();
//                        paramCodedInputByteBufferNano.readMessage(arrayOfOBSOLETE_FinskyTranslatedText[k]);
//                        paramCodedInputByteBufferNano.readTag();
//                        k++;
//                    }
//                }
//                arrayOfOBSOLETE_FinskyTranslatedText[k] = new DocumentV1.OBSOLETE_FinskyTranslatedText();
//                paramCodedInputByteBufferNano.readMessage(arrayOfOBSOLETE_FinskyTranslatedText[k]);
//                this.translatedSnippet = arrayOfOBSOLETE_FinskyTranslatedText;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException
        {
            if (this.docid != null)
                paramCodedOutputByteBufferNano.writeMessage(1, this.docid);
            if (this.fetchDocid != null)
                paramCodedOutputByteBufferNano.writeMessage(2, this.fetchDocid);
            if (this.sampleDocid != null)
                paramCodedOutputByteBufferNano.writeMessage(3, this.sampleDocid);
            if ((this.hasTitle) || (!this.title.equals("")))
                paramCodedOutputByteBufferNano.writeString(4, this.title);
            if ((this.hasUrl) || (!this.url.equals("")))
                paramCodedOutputByteBufferNano.writeString(5, this.url);
            if (this.priceDeprecated != null)
                paramCodedOutputByteBufferNano.writeMessage(7, this.priceDeprecated);
//            if (this.availability != null)
//                paramCodedOutputByteBufferNano.writeMessage(9, this.availability);
            if ((this.image != null) && (this.image.length > 0))
                for (int m = 0; m < this.image.length; m++)
                {
                    Common.Image localImage = this.image[m];
                    if (localImage != null)
                        paramCodedOutputByteBufferNano.writeMessage(10, localImage);
                }
            if ((this.child != null) && (this.child.length > 0))
                for (int k = 0; k < this.child.length; k++)
                {
                    OBSOLETE_FinskyDoc localOBSOLETE_FinskyDoc = this.child[k];
                    if (localOBSOLETE_FinskyDoc != null)
                        paramCodedOutputByteBufferNano.writeMessage(11, localOBSOLETE_FinskyDoc);
                }
//            if (this.aggregateRating != null)
//                paramCodedOutputByteBufferNano.writeMessage(13, this.aggregateRating);
            if ((this.offer != null) && (this.offer.length > 0))
                for (int j = 0; j < this.offer.length; j++)
                {
                    Common.Offer localOffer = this.offer[j];
                    if (localOffer != null)
                        paramCodedOutputByteBufferNano.writeMessage(14, localOffer);
                }
            if ((this.translatedSnippet != null) && (this.translatedSnippet.length > 0))
                for (int i = 0; i < this.translatedSnippet.length; i++)
                {
                    DocumentV1.OBSOLETE_FinskyTranslatedText localOBSOLETE_FinskyTranslatedText = this.translatedSnippet[i];
                    if (localOBSOLETE_FinskyTranslatedText != null)
                        paramCodedOutputByteBufferNano.writeMessage(15, localOBSOLETE_FinskyTranslatedText);
                }
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class OBSOLETE_FinskyTranslatedText extends MessageNano
    {
        private static volatile OBSOLETE_FinskyTranslatedText[] _emptyArray;
        public boolean hasSourceLocale;
        public boolean hasTargetLocale;
        public boolean hasText;
        public String sourceLocale;
        public String targetLocale;
        public String text;

        public OBSOLETE_FinskyTranslatedText()
        {
            clear();
        }

        public static OBSOLETE_FinskyTranslatedText[] emptyArray()
        {
            if (_emptyArray == null);
            synchronized (InternalNano.LAZY_INIT_LOCK)
            {
                if (_emptyArray == null)
                    _emptyArray = new OBSOLETE_FinskyTranslatedText[0];
                return _emptyArray;
            }
        }

        public OBSOLETE_FinskyTranslatedText clear()
        {
            this.text = "";
            this.hasText = false;
            this.sourceLocale = "";
            this.hasSourceLocale = false;
            this.targetLocale = "";
            this.hasTargetLocale = false;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize()
        {
            int i = super.computeSerializedSize();
            if ((this.hasText) || (!this.text.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(1, this.text);
            if ((this.hasSourceLocale) || (!this.sourceLocale.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(2, this.sourceLocale);
            if ((this.hasTargetLocale) || (!this.targetLocale.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(3, this.targetLocale);
            return i;
        }

        public OBSOLETE_FinskyTranslatedText mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException
        {
            while (true)
            {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i)
                {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 10:
                        this.text = paramCodedInputByteBufferNano.readString();
                        this.hasText = true;
                        break;
                    case 18:
                        this.sourceLocale = paramCodedInputByteBufferNano.readString();
                        this.hasSourceLocale = true;
                        break;
                    case 26:
                }
                this.targetLocale = paramCodedInputByteBufferNano.readString();
                this.hasTargetLocale = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException
        {
            if ((this.hasText) || (!this.text.equals("")))
                paramCodedOutputByteBufferNano.writeString(1, this.text);
            if ((this.hasSourceLocale) || (!this.sourceLocale.equals("")))
                paramCodedOutputByteBufferNano.writeString(2, this.sourceLocale);
            if ((this.hasTargetLocale) || (!this.targetLocale.equals("")))
                paramCodedOutputByteBufferNano.writeString(3, this.targetLocale);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }
}