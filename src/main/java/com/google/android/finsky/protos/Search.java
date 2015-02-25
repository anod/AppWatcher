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
        public RelatedSearch mergeFrom(CodedInputByteBufferNano input) throws IOException {
            int tag;
            int int32;
            int int2;
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
                        this.searchUrl = input.readString();
                        this.hasSearchUrl = true;
                        continue;
                    }
                    case 18: {
                        this.header = input.readString();
                        this.hasHeader = true;
                        continue;
                    }
                    case 24: {
                        int32 = input.readInt32();
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
                        int2 = input.readInt32();
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
                        this.current = input.readBool();
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
        public SearchResponse mergeFrom(CodedInputByteBufferNano input) throws IOException {
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
                        this.originalQuery = input.readString();
                        this.hasOriginalQuery = true;
                        continue;
                    }
                    case 18: {
                        this.suggestedQuery = input.readString();
                        this.hasSuggestedQuery = true;
                        continue;
                    }
                    case 24: {
                        this.aggregateQuery = input.readBool();
                        this.hasAggregateQuery = true;
                        continue;
                    }
                    case 34: {
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 34);
                        int i = this.bucket == null ? 0 : this.bucket.length;
                        DocList.Bucket[] newArray = new DocList.Bucket[i + arrayLength];
                        if (i != 0) {
                            System.arraycopy(this.bucket, 0, newArray, 0, i);
                        }
                        for (; i < newArray.length - 1; i++) {
                            newArray[i] = new DocList.Bucket();
                            input.readMessage(newArray[i]);
                            input.readTag();
                        }
                        // Last one without readTag.
                        newArray[i] = new DocList.Bucket();
                        input.readMessage(newArray[i]);
                        this.bucket = newArray;
                        continue;
                    }
                    case 42: {
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 42);
                        int i = this.doc == null ? 0 : this.doc.length;
                        DocumentV2.DocV2[] newArray = new DocumentV2.DocV2[i + arrayLength];
                        if (i != 0) {
                            System.arraycopy(this.doc, 0, newArray, 0, i);
                        }
                        for (; i < newArray.length - 1; i++) {
                            newArray[i] = new DocumentV2.DocV2();
                            input.readMessage(newArray[i]);
                            input.readTag();
                        }
                        // Last one without readTag.
                        newArray[i] = new DocumentV2.DocV2();
                        input.readMessage(newArray[i]);
                        this.doc = newArray;
                        continue;
                    }
                    case 50: {
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(input, 50);
                        int i = this.relatedSearch == null ? 0 : this.relatedSearch.length;
                        RelatedSearch[] newArray = new RelatedSearch[i + arrayLength];
                        if (i != 0) {
                            System.arraycopy(this.relatedSearch, 0, newArray, 0, i);
                        }
                        for (; i < newArray.length - 1; i++) {
                            newArray[i] = new RelatedSearch();
                            input.readMessage(newArray[i]);
                            input.readTag();
                        }
                        // Last one without readTag.
                        newArray[i] = new RelatedSearch();
                        input.readMessage(newArray[i]);
                        this.relatedSearch = newArray;
                        continue;
                    }
                    case 58: {
                        this.serverLogsCookie = input.readBytes();
                        this.hasServerLogsCookie = true;
                        continue;
                    }
                    case 64: {
                        this.fullPageReplaced = input.readBool();
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
