package com.google.android.finsky.protos;

import com.google.protobuf.nano.*;
import java.io.*;
import java.util.*;

public interface Search
{
    public static final class RelatedSearch extends MessageNano
    {
        private static volatile RelatedSearch[] _emptyArray;
        public int backendId;
        public boolean current;
        public int docType;
        public boolean hasBackendId;
        public boolean hasCurrent;
        public boolean hasDocType;
        public boolean hasHeader;
        public boolean hasSearchUrl;
        public String header;
        public String searchUrl;
        
        public RelatedSearch() {
            super();
            this.clear();
        }
        
        public static RelatedSearch[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new RelatedSearch[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public RelatedSearch clear() {
            this.searchUrl = "";
            this.hasSearchUrl = false;
            this.header = "";
            this.hasHeader = false;
            this.backendId = 0;
            this.hasBackendId = false;
            this.docType = 1;
            this.hasDocType = false;
            this.current = false;
            this.hasCurrent = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasSearchUrl || !this.searchUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.searchUrl);
            }
            if (this.hasHeader || !this.header.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.header);
            }
            if (this.backendId != 0 || this.hasBackendId) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.backendId);
            }
            if (this.docType != 1 || this.hasDocType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, this.docType);
            }
            if (this.hasCurrent || this.current) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(5, this.current);
            }
            return computeSerializedSize;
        }
        
        @Override
        public RelatedSearch mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
            int int2;
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
                        this.searchUrl = codedInputByteBufferNano.readString();
                        this.hasSearchUrl = true;
                        continue;
                    }
                    case 18: {
                        this.header = codedInputByteBufferNano.readString();
                        this.hasHeader = true;
                        continue;
                    }
                    case 24: {
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
                                this.backendId = int32;
                                this.hasBackendId = true;
                                continue;
                            }
                        }
                    }
                    case 32: {
                        int2 = codedInputByteBufferNano.readInt32();
                        switch (int2) {
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
                                this.docType = int2;
                                this.hasDocType = true;
                                continue;
                            }
                        }
                    }
                    case 40: {
                        this.current = codedInputByteBufferNano.readBool();
                        this.hasCurrent = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasSearchUrl || !this.searchUrl.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.searchUrl);
            }
            if (this.hasHeader || !this.header.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.header);
            }
            if (this.backendId != 0 || this.hasBackendId) {
                codedOutputByteBufferNano.writeInt32(3, this.backendId);
            }
            if (this.docType != 1 || this.hasDocType) {
                codedOutputByteBufferNano.writeInt32(4, this.docType);
            }
            if (this.hasCurrent || this.current) {
                codedOutputByteBufferNano.writeBool(5, this.current);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class SearchResponse extends MessageNano
    {
        public boolean aggregateQuery;
        public DocList.Bucket[] bucket;
        public DocumentV2.DocV2[] doc;
        public boolean fullPageReplaced;
        public boolean hasAggregateQuery;
        public boolean hasFullPageReplaced;
        public boolean hasOriginalQuery;
        public boolean hasServerLogsCookie;
        public boolean hasSuggestedQuery;
        public String originalQuery;
        public RelatedSearch[] relatedSearch;
        public byte[] serverLogsCookie;
        public String suggestedQuery;
        
        public SearchResponse() {
            super();
            this.clear();
        }
        
        public SearchResponse clear() {
            this.originalQuery = "";
            this.hasOriginalQuery = false;
            this.suggestedQuery = "";
            this.hasSuggestedQuery = false;
            this.fullPageReplaced = false;
            this.hasFullPageReplaced = false;
            this.aggregateQuery = false;
            this.hasAggregateQuery = false;
            this.bucket = DocList.Bucket.emptyArray();
            this.doc = DocumentV2.DocV2.emptyArray();
            this.relatedSearch = RelatedSearch.emptyArray();
            this.serverLogsCookie = WireFormatNano.EMPTY_BYTES;
            this.hasServerLogsCookie = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            DocList.Bucket bucket;
            DocumentV2.DocV2 docV2;
            RelatedSearch relatedSearch;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasOriginalQuery || !this.originalQuery.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.originalQuery);
            }
            if (this.hasSuggestedQuery || !this.suggestedQuery.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.suggestedQuery);
            }
            if (this.hasAggregateQuery || this.aggregateQuery) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(3, this.aggregateQuery);
            }
            if (this.bucket != null && this.bucket.length > 0) {
                for (int i = 0; i < this.bucket.length; ++i) {
                    bucket = this.bucket[i];
                    if (bucket != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, bucket);
                    }
                }
            }
            if (this.doc != null && this.doc.length > 0) {
                for (int j = 0; j < this.doc.length; ++j) {
                    docV2 = this.doc[j];
                    if (docV2 != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(5, docV2);
                    }
                }
            }
            if (this.relatedSearch != null && this.relatedSearch.length > 0) {
                for (int k = 0; k < this.relatedSearch.length; ++k) {
                    relatedSearch = this.relatedSearch[k];
                    if (relatedSearch != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(6, relatedSearch);
                    }
                }
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBytesSize(7, this.serverLogsCookie);
            }
            if (this.hasFullPageReplaced || this.fullPageReplaced) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(8, this.fullPageReplaced);
            }
            return computeSerializedSize;
        }
        
        @Override
        public SearchResponse mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            DocList.Bucket[] bucket;
            int repeatedFieldArrayLength2;
            int j;
            DocumentV2.DocV2[] doc;
            int repeatedFieldArrayLength3;
            int k;
            RelatedSearch[] relatedSearch;
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
                        this.originalQuery = codedInputByteBufferNano.readString();
                        this.hasOriginalQuery = true;
                        continue;
                    }
                    case 18: {
                        this.suggestedQuery = codedInputByteBufferNano.readString();
                        this.hasSuggestedQuery = true;
                        continue;
                    }
                    case 24: {
                        this.aggregateQuery = codedInputByteBufferNano.readBool();
                        this.hasAggregateQuery = true;
                        continue;
                    }
                    case 34: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 34);
                        if (this.bucket == null) {
                            i = 0;
                        }
                        else {
                            i = this.bucket.length;
                        }
                        bucket = new DocList.Bucket[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.bucket, 0, bucket, 0, i);
                        }
                        while (i < -1 + bucket.length) {
                            codedInputByteBufferNano.readMessage(bucket[i] = new DocList.Bucket());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(bucket[i] = new DocList.Bucket());
                        this.bucket = bucket;
                        continue;
                    }
                    case 42: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 42);
                        if (this.doc == null) {
                            j = 0;
                        }
                        else {
                            j = this.doc.length;
                        }
                        doc = new DocumentV2.DocV2[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.doc, 0, doc, 0, j);
                        }
                        while (j < -1 + doc.length) {
                            codedInputByteBufferNano.readMessage(doc[j] = new DocumentV2.DocV2());
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        codedInputByteBufferNano.readMessage(doc[j] = new DocumentV2.DocV2());
                        this.doc = doc;
                        continue;
                    }
                    case 50: {
                        repeatedFieldArrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 50);
                        if (this.relatedSearch == null) {
                            k = 0;
                        }
                        else {
                            k = this.relatedSearch.length;
                        }
                        relatedSearch = new RelatedSearch[k + repeatedFieldArrayLength3];
                        if (k != 0) {
                            System.arraycopy(this.relatedSearch, 0, relatedSearch, 0, k);
                        }
                        while (k < -1 + relatedSearch.length) {
                            codedInputByteBufferNano.readMessage(relatedSearch[k] = new RelatedSearch());
                            codedInputByteBufferNano.readTag();
                            ++k;
                        }
                        codedInputByteBufferNano.readMessage(relatedSearch[k] = new RelatedSearch());
                        this.relatedSearch = relatedSearch;
                        continue;
                    }
                    case 58: {
                        this.serverLogsCookie = codedInputByteBufferNano.readBytes();
                        this.hasServerLogsCookie = true;
                        continue;
                    }
                    case 64: {
                        this.fullPageReplaced = codedInputByteBufferNano.readBool();
                        this.hasFullPageReplaced = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            DocList.Bucket bucket;
            DocumentV2.DocV2 docV2;
            RelatedSearch relatedSearch;
            if (this.hasOriginalQuery || !this.originalQuery.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.originalQuery);
            }
            if (this.hasSuggestedQuery || !this.suggestedQuery.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.suggestedQuery);
            }
            if (this.hasAggregateQuery || this.aggregateQuery) {
                codedOutputByteBufferNano.writeBool(3, this.aggregateQuery);
            }
            if (this.bucket != null && this.bucket.length > 0) {
                for (int i = 0; i < this.bucket.length; ++i) {
                    bucket = this.bucket[i];
                    if (bucket != null) {
                        codedOutputByteBufferNano.writeMessage(4, bucket);
                    }
                }
            }
            if (this.doc != null && this.doc.length > 0) {
                for (int j = 0; j < this.doc.length; ++j) {
                    docV2 = this.doc[j];
                    if (docV2 != null) {
                        codedOutputByteBufferNano.writeMessage(5, docV2);
                    }
                }
            }
            if (this.relatedSearch != null && this.relatedSearch.length > 0) {
                for (int k = 0; k < this.relatedSearch.length; ++k) {
                    relatedSearch = this.relatedSearch[k];
                    if (relatedSearch != null) {
                        codedOutputByteBufferNano.writeMessage(6, relatedSearch);
                    }
                }
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                codedOutputByteBufferNano.writeBytes(7, this.serverLogsCookie);
            }
            if (this.hasFullPageReplaced || this.fullPageReplaced) {
                codedOutputByteBufferNano.writeBool(8, this.fullPageReplaced);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
