package com.google.android.finsky.protos;

import com.google.protobuf.nano.*;
import java.io.*;
import java.util.*;

public interface Details
{
    public static final class BulkDetailsEntry extends MessageNano
    {
        private static volatile BulkDetailsEntry[] _emptyArray;
        public DocumentV2.DocV2 doc;
        
        public BulkDetailsEntry() {
            super();
            this.clear();
        }
        
        public static BulkDetailsEntry[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new BulkDetailsEntry[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public BulkDetailsEntry clear() {
            this.doc = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.doc != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.doc);
            }
            return computeSerializedSize;
        }
        
        @Override
        public BulkDetailsEntry mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        if (this.doc == null) {
                            this.doc = new DocumentV2.DocV2();
                        }
                        codedInputByteBufferNano.readMessage(this.doc);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.doc != null) {
                codedOutputByteBufferNano.writeMessage(1, this.doc);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class BulkDetailsRequest extends MessageNano
    {
        public String[] docid;
        public boolean hasIncludeChildDocs;
        public boolean hasIncludeDetails;
        public boolean hasIncludeSplitDetailsForAllApps;
        public boolean hasIncludeSplitDetailsForNewerVersions;
        public boolean hasSourcePackageName;
        public boolean includeChildDocs;
        public boolean includeDetails;
        public boolean includeSplitDetailsForAllApps;
        public boolean includeSplitDetailsForNewerVersions;
        public int[] installedVersionCode;
        public String sourcePackageName;
        
        public BulkDetailsRequest() {
            super();
            this.clear();
        }
        
        public BulkDetailsRequest clear() {
            this.docid = WireFormatNano.EMPTY_STRING_ARRAY;
            this.installedVersionCode = WireFormatNano.EMPTY_INT_ARRAY;
            this.includeChildDocs = true;
            this.hasIncludeChildDocs = false;
            this.includeDetails = false;
            this.hasIncludeDetails = false;
            this.sourcePackageName = "";
            this.hasSourcePackageName = false;
            this.includeSplitDetailsForAllApps = false;
            this.hasIncludeSplitDetailsForAllApps = false;
            this.includeSplitDetailsForNewerVersions = false;
            this.hasIncludeSplitDetailsForNewerVersions = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            int n;
            int n2;
            String s;
            int n3;
            computeSerializedSize = super.computeSerializedSize();
            if (this.docid != null && this.docid.length > 0) {
                n = 0;
                n2 = 0;
                for (int i = 0; i < this.docid.length; ++i) {
                    s = this.docid[i];
                    if (s != null) {
                        ++n;
                        n2 += CodedOutputByteBufferNano.computeStringSizeNoTag(s);
                    }
                }
                computeSerializedSize = computeSerializedSize + n2 + n * 1;
            }
            if (this.hasIncludeChildDocs || !this.includeChildDocs) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(2, this.includeChildDocs);
            }
            if (this.hasIncludeDetails || this.includeDetails) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(3, this.includeDetails);
            }
            if (this.hasSourcePackageName || !this.sourcePackageName.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.sourcePackageName);
            }
            if (this.hasIncludeSplitDetailsForAllApps || this.includeSplitDetailsForAllApps) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(5, this.includeSplitDetailsForAllApps);
            }
            if (this.hasIncludeSplitDetailsForNewerVersions || this.includeSplitDetailsForNewerVersions) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(6, this.includeSplitDetailsForNewerVersions);
            }
            if (this.installedVersionCode != null && this.installedVersionCode.length > 0) {
                n3 = 0;
                for (int j = 0; j < this.installedVersionCode.length; ++j) {
                    n3 += CodedOutputByteBufferNano.computeInt32SizeNoTag(this.installedVersionCode[j]);
                }
                computeSerializedSize = computeSerializedSize + n3 + 1 * this.installedVersionCode.length;
            }
            return computeSerializedSize;
        }
        
        @Override
        public BulkDetailsRequest mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            String[] docid;
            int repeatedFieldArrayLength2;
            int j;
            int[] installedVersionCode;
            int pushLimit;
            int n;
            int position;
            int k;
            int[] installedVersionCode2;

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
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 10);
                        if (this.docid == null) {
                            i = 0;
                        }
                        else {
                            i = this.docid.length;
                        }
                        docid = new String[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.docid, 0, docid, 0, i);
                        }
                        while (i < -1 + docid.length) {
                            docid[i] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        docid[i] = codedInputByteBufferNano.readString();
                        this.docid = docid;
                        continue;
                    }
                    case 16: {
                        this.includeChildDocs = codedInputByteBufferNano.readBool();
                        this.hasIncludeChildDocs = true;
                        continue;
                    }
                    case 24: {
                        this.includeDetails = codedInputByteBufferNano.readBool();
                        this.hasIncludeDetails = true;
                        continue;
                    }
                    case 34: {
                        this.sourcePackageName = codedInputByteBufferNano.readString();
                        this.hasSourcePackageName = true;
                        continue;
                    }
                    case 40: {
                        this.includeSplitDetailsForAllApps = codedInputByteBufferNano.readBool();
                        this.hasIncludeSplitDetailsForAllApps = true;
                        continue;
                    }
                    case 48: {
                        this.includeSplitDetailsForNewerVersions = codedInputByteBufferNano.readBool();
                        this.hasIncludeSplitDetailsForNewerVersions = true;
                        continue;
                    }
                    case 56: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 56);
                        if (this.installedVersionCode == null) {
                            j = 0;
                        }
                        else {
                            j = this.installedVersionCode.length;
                        }
                        installedVersionCode = new int[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.installedVersionCode, 0, installedVersionCode, 0, j);
                        }
                        while (j < -1 + installedVersionCode.length) {
                            installedVersionCode[j] = codedInputByteBufferNano.readInt32();
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        installedVersionCode[j] = codedInputByteBufferNano.readInt32();
                        this.installedVersionCode = installedVersionCode;
                        continue;
                    }
                    case 58: {
                        pushLimit = codedInputByteBufferNano.pushLimit(codedInputByteBufferNano.readRawVarint32());
                        n = 0;
                        position = codedInputByteBufferNano.getPosition();
                        while (codedInputByteBufferNano.getBytesUntilLimit() > 0) {
                            codedInputByteBufferNano.readInt32();
                            ++n;
                        }
                        codedInputByteBufferNano.rewindToPosition(position);
                        if (this.installedVersionCode == null) {
                            k = 0;
                        }
                        else {
                            k = this.installedVersionCode.length;
                        }
                        installedVersionCode2 = new int[k + n];
                        if (k != 0) {
                            System.arraycopy(this.installedVersionCode, 0, installedVersionCode2, 0, k);
                        }
                        while (k < installedVersionCode2.length) {
                            installedVersionCode2[k] = codedInputByteBufferNano.readInt32();
                            ++k;
                        }
                        this.installedVersionCode = installedVersionCode2;
                        codedInputByteBufferNano.popLimit(pushLimit);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            String s;
            if (this.docid != null && this.docid.length > 0) {
                for (int i = 0; i < this.docid.length; ++i) {
                    s = this.docid[i];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(1, s);
                    }
                }
            }
            if (this.hasIncludeChildDocs || !this.includeChildDocs) {
                codedOutputByteBufferNano.writeBool(2, this.includeChildDocs);
            }
            if (this.hasIncludeDetails || this.includeDetails) {
                codedOutputByteBufferNano.writeBool(3, this.includeDetails);
            }
            if (this.hasSourcePackageName || !this.sourcePackageName.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.sourcePackageName);
            }
            if (this.hasIncludeSplitDetailsForAllApps || this.includeSplitDetailsForAllApps) {
                codedOutputByteBufferNano.writeBool(5, this.includeSplitDetailsForAllApps);
            }
            if (this.hasIncludeSplitDetailsForNewerVersions || this.includeSplitDetailsForNewerVersions) {
                codedOutputByteBufferNano.writeBool(6, this.includeSplitDetailsForNewerVersions);
            }
            if (this.installedVersionCode != null && this.installedVersionCode.length > 0) {
                for (int j = 0; j < this.installedVersionCode.length; ++j) {
                    codedOutputByteBufferNano.writeInt32(7, this.installedVersionCode[j]);
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class BulkDetailsResponse extends MessageNano
    {
        public BulkDetailsEntry[] entry;
        
        public BulkDetailsResponse() {
            super();
            this.clear();
        }
        
        public BulkDetailsResponse clear() {
            this.entry = BulkDetailsEntry.emptyArray();
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            BulkDetailsEntry bulkDetailsEntry;
            computeSerializedSize = super.computeSerializedSize();
            if (this.entry != null && this.entry.length > 0) {
                for (int i = 0; i < this.entry.length; ++i) {
                    bulkDetailsEntry = this.entry[i];
                    if (bulkDetailsEntry != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, bulkDetailsEntry);
                    }
                }
            }
            return computeSerializedSize;
        }
        
        @Override
        public BulkDetailsResponse mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            BulkDetailsEntry[] entry;
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
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 10);
                        if (this.entry == null) {
                            i = 0;
                        }
                        else {
                            i = this.entry.length;
                        }
                        entry = new BulkDetailsEntry[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.entry, 0, entry, 0, i);
                        }
                        while (i < -1 + entry.length) {
                            codedInputByteBufferNano.readMessage(entry[i] = new BulkDetailsEntry());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(entry[i] = new BulkDetailsEntry());
                        this.entry = entry;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            BulkDetailsEntry bulkDetailsEntry;
            if (this.entry != null && this.entry.length > 0) {
                for (int i = 0; i < this.entry.length; ++i) {
                    bulkDetailsEntry = this.entry[i];
                    if (bulkDetailsEntry != null) {
                        codedOutputByteBufferNano.writeMessage(1, bulkDetailsEntry);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class DetailsResponse extends MessageNano
    {
        public String analyticsCookie;
        public DiscoveryBadge[] discoveryBadge;
        public DocumentV1.DocV1 docV1;
        public DocumentV2.DocV2 docV2;
        public String footerHtml;
        public boolean hasAnalyticsCookie;
        public boolean hasFooterHtml;
        public boolean hasServerLogsCookie;
        public byte[] serverLogsCookie;
        public DocumentV2.Review userReview;
        
        public DetailsResponse() {
            super();
            this.clear();
        }
        
        public DetailsResponse clear() {
            this.docV1 = null;
            this.docV2 = null;
            this.analyticsCookie = "";
            this.hasAnalyticsCookie = false;
            this.userReview = null;
            this.footerHtml = "";
            this.hasFooterHtml = false;
            this.serverLogsCookie = WireFormatNano.EMPTY_BYTES;
            this.hasServerLogsCookie = false;
            this.discoveryBadge = DiscoveryBadge.emptyArray();
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            DiscoveryBadge discoveryBadge;
            computeSerializedSize = super.computeSerializedSize();
            if (this.docV1 != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.docV1);
            }
            if (this.hasAnalyticsCookie || !this.analyticsCookie.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.analyticsCookie);
            }
            if (this.userReview != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, this.userReview);
            }
            if (this.docV2 != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, this.docV2);
            }
            if (this.hasFooterHtml || !this.footerHtml.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.footerHtml);
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBytesSize(6, this.serverLogsCookie);
            }
            if (this.discoveryBadge != null && this.discoveryBadge.length > 0) {
                for (int i = 0; i < this.discoveryBadge.length; ++i) {
                    discoveryBadge = this.discoveryBadge[i];
                    if (discoveryBadge != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(7, discoveryBadge);
                    }
                }
            }
            return computeSerializedSize;
        }
        
        @Override
        public DetailsResponse mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            DiscoveryBadge[] discoveryBadge;
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
                        if (this.docV1 == null) {
                            this.docV1 = new DocumentV1.DocV1();
                        }
                        codedInputByteBufferNano.readMessage(this.docV1);
                        continue;
                    }
                    case 18: {
                        this.analyticsCookie = codedInputByteBufferNano.readString();
                        this.hasAnalyticsCookie = true;
                        continue;
                    }
                    case 26: {
                        if (this.userReview == null) {
                            this.userReview = new DocumentV2.Review();
                        }
                        codedInputByteBufferNano.readMessage(this.userReview);
                        continue;
                    }
                    case 34: {
                        if (this.docV2 == null) {
                            this.docV2 = new DocumentV2.DocV2();
                        }
                        codedInputByteBufferNano.readMessage(this.docV2);
                        continue;
                    }
                    case 42: {
                        this.footerHtml = codedInputByteBufferNano.readString();
                        this.hasFooterHtml = true;
                        continue;
                    }
                    case 50: {
                        this.serverLogsCookie = codedInputByteBufferNano.readBytes();
                        this.hasServerLogsCookie = true;
                        continue;
                    }
                    case 58: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 58);
                        if (this.discoveryBadge == null) {
                            i = 0;
                        }
                        else {
                            i = this.discoveryBadge.length;
                        }
                        discoveryBadge = new DiscoveryBadge[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.discoveryBadge, 0, discoveryBadge, 0, i);
                        }
                        while (i < -1 + discoveryBadge.length) {
                            codedInputByteBufferNano.readMessage(discoveryBadge[i] = new DiscoveryBadge());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(discoveryBadge[i] = new DiscoveryBadge());
                        this.discoveryBadge = discoveryBadge;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            DiscoveryBadge discoveryBadge;
            if (this.docV1 != null) {
                codedOutputByteBufferNano.writeMessage(1, this.docV1);
            }
            if (this.hasAnalyticsCookie || !this.analyticsCookie.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.analyticsCookie);
            }
            if (this.userReview != null) {
                codedOutputByteBufferNano.writeMessage(3, this.userReview);
            }
            if (this.docV2 != null) {
                codedOutputByteBufferNano.writeMessage(4, this.docV2);
            }
            if (this.hasFooterHtml || !this.footerHtml.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.footerHtml);
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                codedOutputByteBufferNano.writeBytes(6, this.serverLogsCookie);
            }
            if (this.discoveryBadge != null && this.discoveryBadge.length > 0) {
                for (int i = 0; i < this.discoveryBadge.length; ++i) {
                    discoveryBadge = this.discoveryBadge[i];
                    if (discoveryBadge != null) {
                        codedOutputByteBufferNano.writeMessage(7, discoveryBadge);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class DiscoveryBadge extends MessageNano
    {
        private static volatile DiscoveryBadge[] _emptyArray;
        public float aggregateRating;
        public int backgroundColor;
        public String contentDescription;
        public DiscoveryBadgeLink discoveryBadgeLink;
        public String downloadCount;
        public String downloadUnits;
        public boolean hasAggregateRating;
        public boolean hasBackgroundColor;
        public boolean hasContentDescription;
        public boolean hasDownloadCount;
        public boolean hasDownloadUnits;
        public boolean hasIsPlusOne;
        public boolean hasServerLogsCookie;
        public boolean hasTitle;
        public boolean hasUserStarRating;
        public Common.Image image;
        public boolean isPlusOne;
        public byte[] serverLogsCookie;
        public String title;
        public int userStarRating;
        
        public DiscoveryBadge() {
            super();
            this.clear();
        }
        
        public static DiscoveryBadge[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new DiscoveryBadge[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public DiscoveryBadge clear() {
            this.title = "";
            this.hasTitle = false;
            this.contentDescription = "";
            this.hasContentDescription = false;
            this.image = null;
            this.backgroundColor = 0;
            this.hasBackgroundColor = false;
            this.discoveryBadgeLink = null;
            this.serverLogsCookie = WireFormatNano.EMPTY_BYTES;
            this.hasServerLogsCookie = false;
            this.isPlusOne = false;
            this.hasIsPlusOne = false;
            this.aggregateRating = 0.0f;
            this.hasAggregateRating = false;
            this.userStarRating = 0;
            this.hasUserStarRating = false;
            this.downloadCount = "";
            this.hasDownloadCount = false;
            this.downloadUnits = "";
            this.hasDownloadUnits = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.title);
            }
            if (this.image != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.image);
            }
            if (this.hasBackgroundColor || this.backgroundColor != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.backgroundColor);
            }
            if (this.discoveryBadgeLink != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, this.discoveryBadgeLink);
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBytesSize(5, this.serverLogsCookie);
            }
            if (this.hasIsPlusOne || this.isPlusOne) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(6, this.isPlusOne);
            }
            if (this.hasAggregateRating || Float.floatToIntBits(this.aggregateRating) != Float.floatToIntBits(0.0f)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(7, this.aggregateRating);
            }
            if (this.hasUserStarRating || this.userStarRating != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(8, this.userStarRating);
            }
            if (this.hasDownloadCount || !this.downloadCount.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(9, this.downloadCount);
            }
            if (this.hasDownloadUnits || !this.downloadUnits.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(10, this.downloadUnits);
            }
            if (this.hasContentDescription || !this.contentDescription.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(11, this.contentDescription);
            }
            return computeSerializedSize;
        }
        
        @Override
        public DiscoveryBadge mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.title = codedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        continue;
                    }
                    case 18: {
                        if (this.image == null) {
                            this.image = new Common.Image();
                        }
                        codedInputByteBufferNano.readMessage(this.image);
                        continue;
                    }
                    case 24: {
                        this.backgroundColor = codedInputByteBufferNano.readInt32();
                        this.hasBackgroundColor = true;
                        continue;
                    }
                    case 34: {
                        if (this.discoveryBadgeLink == null) {
                            this.discoveryBadgeLink = new DiscoveryBadgeLink();
                        }
                        codedInputByteBufferNano.readMessage(this.discoveryBadgeLink);
                        continue;
                    }
                    case 42: {
                        this.serverLogsCookie = codedInputByteBufferNano.readBytes();
                        this.hasServerLogsCookie = true;
                        continue;
                    }
                    case 48: {
                        this.isPlusOne = codedInputByteBufferNano.readBool();
                        this.hasIsPlusOne = true;
                        continue;
                    }
                    case 61: {
                        this.aggregateRating = codedInputByteBufferNano.readFloat();
                        this.hasAggregateRating = true;
                        continue;
                    }
                    case 64: {
                        this.userStarRating = codedInputByteBufferNano.readInt32();
                        this.hasUserStarRating = true;
                        continue;
                    }
                    case 74: {
                        this.downloadCount = codedInputByteBufferNano.readString();
                        this.hasDownloadCount = true;
                        continue;
                    }
                    case 82: {
                        this.downloadUnits = codedInputByteBufferNano.readString();
                        this.hasDownloadUnits = true;
                        continue;
                    }
                    case 90: {
                        this.contentDescription = codedInputByteBufferNano.readString();
                        this.hasContentDescription = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.title);
            }
            if (this.image != null) {
                codedOutputByteBufferNano.writeMessage(2, this.image);
            }
            if (this.hasBackgroundColor || this.backgroundColor != 0) {
                codedOutputByteBufferNano.writeInt32(3, this.backgroundColor);
            }
            if (this.discoveryBadgeLink != null) {
                codedOutputByteBufferNano.writeMessage(4, this.discoveryBadgeLink);
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                codedOutputByteBufferNano.writeBytes(5, this.serverLogsCookie);
            }
            if (this.hasIsPlusOne || this.isPlusOne) {
                codedOutputByteBufferNano.writeBool(6, this.isPlusOne);
            }
            if (this.hasAggregateRating || Float.floatToIntBits(this.aggregateRating) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(7, this.aggregateRating);
            }
            if (this.hasUserStarRating || this.userStarRating != 0) {
                codedOutputByteBufferNano.writeInt32(8, this.userStarRating);
            }
            if (this.hasDownloadCount || !this.downloadCount.equals("")) {
                codedOutputByteBufferNano.writeString(9, this.downloadCount);
            }
            if (this.hasDownloadUnits || !this.downloadUnits.equals("")) {
                codedOutputByteBufferNano.writeString(10, this.downloadUnits);
            }
            if (this.hasContentDescription || !this.contentDescription.equals("")) {
                codedOutputByteBufferNano.writeString(11, this.contentDescription);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class DiscoveryBadgeLink extends MessageNano
    {
        public String criticReviewsUrl;
        public boolean hasCriticReviewsUrl;
        public boolean hasUserReviewsUrl;
        public DocAnnotations.Link link;
        public String userReviewsUrl;
        
        public DiscoveryBadgeLink() {
            super();
            this.clear();
        }
        
        public DiscoveryBadgeLink clear() {
            this.link = null;
            this.userReviewsUrl = "";
            this.hasUserReviewsUrl = false;
            this.criticReviewsUrl = "";
            this.hasCriticReviewsUrl = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.link != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.link);
            }
            if (this.hasUserReviewsUrl || !this.userReviewsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.userReviewsUrl);
            }
            if (this.hasCriticReviewsUrl || !this.criticReviewsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.criticReviewsUrl);
            }
            return computeSerializedSize;
        }
        
        @Override
        public DiscoveryBadgeLink mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        if (this.link == null) {
                            this.link = new DocAnnotations.Link();
                        }
                        codedInputByteBufferNano.readMessage(this.link);
                        continue;
                    }
                    case 18: {
                        this.userReviewsUrl = codedInputByteBufferNano.readString();
                        this.hasUserReviewsUrl = true;
                        continue;
                    }
                    case 26: {
                        this.criticReviewsUrl = codedInputByteBufferNano.readString();
                        this.hasCriticReviewsUrl = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.link != null) {
                codedOutputByteBufferNano.writeMessage(1, this.link);
            }
            if (this.hasUserReviewsUrl || !this.userReviewsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.userReviewsUrl);
            }
            if (this.hasCriticReviewsUrl || !this.criticReviewsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.criticReviewsUrl);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
