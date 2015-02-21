package com.google.android.finsky.protos;

import com.google.protobuf.nano.*;
import java.io.*;
import java.util.*;

public interface ResolveLink
{
    public static final class DirectPurchase extends MessageNano
    {
        public String detailsUrl;
        public boolean hasDetailsUrl;
        public boolean hasOfferType;
        public boolean hasParentDocid;
        public boolean hasPurchaseDocid;
        public int offerType;
        public String parentDocid;
        public String purchaseDocid;
        
        public DirectPurchase() {
            super();
            this.clear();
        }
        
        public DirectPurchase clear() {
            this.detailsUrl = "";
            this.hasDetailsUrl = false;
            this.purchaseDocid = "";
            this.hasPurchaseDocid = false;
            this.parentDocid = "";
            this.hasParentDocid = false;
            this.offerType = 1;
            this.hasOfferType = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasDetailsUrl || !this.detailsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.detailsUrl);
            }
            if (this.hasPurchaseDocid || !this.purchaseDocid.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.purchaseDocid);
            }
            if (this.hasParentDocid || !this.parentDocid.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.parentDocid);
            }
            if (this.offerType != 1 || this.hasOfferType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, this.offerType);
            }
            return computeSerializedSize;
        }
        
        @Override
        public DirectPurchase mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
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
                        this.detailsUrl = codedInputByteBufferNano.readString();
                        this.hasDetailsUrl = true;
                        continue;
                    }
                    case 18: {
                        this.purchaseDocid = codedInputByteBufferNano.readString();
                        this.hasPurchaseDocid = true;
                        continue;
                    }
                    case 26: {
                        this.parentDocid = codedInputByteBufferNano.readString();
                        this.hasParentDocid = true;
                        continue;
                    }
                    case 32: {
                        int32 = codedInputByteBufferNano.readInt32();
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
                            case 12: {
                                this.offerType = int32;
                                this.hasOfferType = true;
                                continue;
                            }
                        }
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasDetailsUrl || !this.detailsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.detailsUrl);
            }
            if (this.hasPurchaseDocid || !this.purchaseDocid.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.purchaseDocid);
            }
            if (this.hasParentDocid || !this.parentDocid.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.parentDocid);
            }
            if (this.offerType != 1 || this.hasOfferType) {
                codedOutputByteBufferNano.writeInt32(4, this.offerType);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class RedeemGiftCard extends MessageNano
    {
        public boolean hasPartnerPayload;
        public boolean hasPrefillCode;
        public String partnerPayload;
        public String prefillCode;
        
        public RedeemGiftCard() {
            super();
            this.clear();
        }
        
        public RedeemGiftCard clear() {
            this.prefillCode = "";
            this.hasPrefillCode = false;
            this.partnerPayload = "";
            this.hasPartnerPayload = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasPrefillCode || !this.prefillCode.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.prefillCode);
            }
            if (this.hasPartnerPayload || !this.partnerPayload.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.partnerPayload);
            }
            return computeSerializedSize;
        }
        
        @Override
        public RedeemGiftCard mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
        Label_0048:
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            break Label_0048;
                        }
                        continue;
                    }
                    case 0: {
                        break Label_0048;
                    }
                    case 10: {
                        this.prefillCode = codedInputByteBufferNano.readString();
                        this.hasPrefillCode = true;
                        continue;
                    }
                    case 18: {
                        this.partnerPayload = codedInputByteBufferNano.readString();
                        this.hasPartnerPayload = true;
                        continue;
                    }
                }
            }
            return this;
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasPrefillCode || !this.prefillCode.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.prefillCode);
            }
            if (this.hasPartnerPayload || !this.partnerPayload.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.partnerPayload);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class ResolvedLink extends MessageNano
    {
        public int backend;
        public String browseUrl;
        public String detailsUrl;
        public DirectPurchase directPurchase;
        public Common.Docid docid;
        public boolean hasBackend;
        public boolean hasBrowseUrl;
        public boolean hasDetailsUrl;
        public boolean hasHomeUrl;
        public boolean hasQuery;
        public boolean hasSearchUrl;
        public boolean hasServerLogsCookie;
        public boolean hasWishlistUrl;
        public String homeUrl;
        public String query;
        public RedeemGiftCard redeemGiftCard;
        public String searchUrl;
        public byte[] serverLogsCookie;
        public String wishlistUrl;
        
        public ResolvedLink() {
            super();
            this.clear();
        }
        
        public ResolvedLink clear() {
            this.detailsUrl = "";
            this.hasDetailsUrl = false;
            this.browseUrl = "";
            this.hasBrowseUrl = false;
            this.searchUrl = "";
            this.hasSearchUrl = false;
            this.wishlistUrl = "";
            this.hasWishlistUrl = false;
            this.directPurchase = null;
            this.homeUrl = "";
            this.hasHomeUrl = false;
            this.redeemGiftCard = null;
            this.docid = null;
            this.serverLogsCookie = WireFormatNano.EMPTY_BYTES;
            this.hasServerLogsCookie = false;
            this.backend = 0;
            this.hasBackend = false;
            this.query = "";
            this.hasQuery = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasDetailsUrl || !this.detailsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.detailsUrl);
            }
            if (this.hasBrowseUrl || !this.browseUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.browseUrl);
            }
            if (this.hasSearchUrl || !this.searchUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.searchUrl);
            }
            if (this.directPurchase != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, this.directPurchase);
            }
            if (this.hasHomeUrl || !this.homeUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.homeUrl);
            }
            if (this.redeemGiftCard != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(6, this.redeemGiftCard);
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBytesSize(7, this.serverLogsCookie);
            }
            if (this.docid != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(8, this.docid);
            }
            if (this.hasWishlistUrl || !this.wishlistUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(9, this.wishlistUrl);
            }
            if (this.backend != 0 || this.hasBackend) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(10, this.backend);
            }
            if (this.hasQuery || !this.query.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(11, this.query);
            }
            return computeSerializedSize;
        }
        
        @Override
        public ResolvedLink mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            return this;
                        }
                        continue;
                    }
                    case 0: {
                        return this;
                    }
                    case 10: {
                        this.detailsUrl = codedInputByteBufferNano.readString();
                        this.hasDetailsUrl = true;
                        continue;
                    }
                    case 18: {
                        this.browseUrl = codedInputByteBufferNano.readString();
                        this.hasBrowseUrl = true;
                        continue;
                    }
                    case 26: {
                        this.searchUrl = codedInputByteBufferNano.readString();
                        this.hasSearchUrl = true;
                        continue;
                    }
                    case 34: {
                        if (this.directPurchase == null) {
                            this.directPurchase = new DirectPurchase();
                        }
                        codedInputByteBufferNano.readMessage(this.directPurchase);
                        continue;
                    }
                    case 42: {
                        this.homeUrl = codedInputByteBufferNano.readString();
                        this.hasHomeUrl = true;
                        continue;
                    }
                    case 50: {
                        if (this.redeemGiftCard == null) {
                            this.redeemGiftCard = new RedeemGiftCard();
                        }
                        codedInputByteBufferNano.readMessage(this.redeemGiftCard);
                        continue;
                    }
                    case 58: {
                        this.serverLogsCookie = codedInputByteBufferNano.readBytes();
                        this.hasServerLogsCookie = true;
                        continue;
                    }
                    case 66: {
                        if (this.docid == null) {
                            this.docid = new Common.Docid();
                        }
                        codedInputByteBufferNano.readMessage(this.docid);
                        continue;
                    }
                    case 74: {
                        this.wishlistUrl = codedInputByteBufferNano.readString();
                        this.hasWishlistUrl = true;
                        continue;
                    }
                    case 80: {
                        int32 = codedInputByteBufferNano.readInt32();
                        switch (int32) {
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
                                this.backend = int32;
                                this.hasBackend = true;
                                continue;
                            }
                        }
                    }
                    case 90: {
                        this.query = codedInputByteBufferNano.readString();
                        this.hasQuery = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasDetailsUrl || !this.detailsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.detailsUrl);
            }
            if (this.hasBrowseUrl || !this.browseUrl.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.browseUrl);
            }
            if (this.hasSearchUrl || !this.searchUrl.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.searchUrl);
            }
            if (this.directPurchase != null) {
                codedOutputByteBufferNano.writeMessage(4, this.directPurchase);
            }
            if (this.hasHomeUrl || !this.homeUrl.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.homeUrl);
            }
            if (this.redeemGiftCard != null) {
                codedOutputByteBufferNano.writeMessage(6, this.redeemGiftCard);
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                codedOutputByteBufferNano.writeBytes(7, this.serverLogsCookie);
            }
            if (this.docid != null) {
                codedOutputByteBufferNano.writeMessage(8, this.docid);
            }
            if (this.hasWishlistUrl || !this.wishlistUrl.equals("")) {
                codedOutputByteBufferNano.writeString(9, this.wishlistUrl);
            }
            if (this.backend != 0 || this.hasBackend) {
                codedOutputByteBufferNano.writeInt32(10, this.backend);
            }
            if (this.hasQuery || !this.query.equals("")) {
                codedOutputByteBufferNano.writeString(11, this.query);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
