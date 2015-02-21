package com.google.android.finsky.protos;

import com.google.protobuf.nano.*;
import java.io.*;

public interface DocList
{
    public static final class Bucket extends MessageNano
    {
        private static volatile Bucket[] _emptyArray;
        public String analyticsCookie;
        public DocumentV1.DocV1[] document;
        public long estimatedResults;
        public String fullContentsListUrl;
        public String fullContentsUrl;
        public boolean hasAnalyticsCookie;
        public boolean hasEstimatedResults;
        public boolean hasFullContentsListUrl;
        public boolean hasFullContentsUrl;
        public boolean hasIconUrl;
        public boolean hasMultiCorpus;
        public boolean hasNextPageUrl;
        public boolean hasOrdered;
        public boolean hasRelevance;
        public boolean hasTitle;
        public String iconUrl;
        public boolean multiCorpus;
        public String nextPageUrl;
        public boolean ordered;
        public double relevance;
        public String title;
        
        public Bucket() {
            super();
            this.clear();
        }
        
        public static Bucket[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Bucket[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public Bucket clear() {
            this.document = DocumentV1.DocV1.emptyArray();
            this.multiCorpus = false;
            this.hasMultiCorpus = false;
            this.title = "";
            this.hasTitle = false;
            this.iconUrl = "";
            this.hasIconUrl = false;
            this.fullContentsUrl = "";
            this.hasFullContentsUrl = false;
            this.fullContentsListUrl = "";
            this.hasFullContentsListUrl = false;
            this.nextPageUrl = "";
            this.hasNextPageUrl = false;
            this.relevance = 0.0;
            this.hasRelevance = false;
            this.estimatedResults = 0L;
            this.hasEstimatedResults = false;
            this.analyticsCookie = "";
            this.hasAnalyticsCookie = false;
            this.ordered = false;
            this.hasOrdered = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            DocumentV1.DocV1 docV1;
            computeSerializedSize = super.computeSerializedSize();
            if (this.document != null && this.document.length > 0) {
                for (int i = 0; i < this.document.length; ++i) {
                    docV1 = this.document[i];
                    if (docV1 != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, docV1);
                    }
                }
            }
            if (this.hasMultiCorpus || this.multiCorpus) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(2, this.multiCorpus);
            }
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.title);
            }
            if (this.hasIconUrl || !this.iconUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.iconUrl);
            }
            if (this.hasFullContentsUrl || !this.fullContentsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.fullContentsUrl);
            }
            if (this.hasRelevance || Double.doubleToLongBits(this.relevance) != Double.doubleToLongBits(0.0)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeDoubleSize(6, this.relevance);
            }
            if (this.hasEstimatedResults || this.estimatedResults != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(7, this.estimatedResults);
            }
            if (this.hasAnalyticsCookie || !this.analyticsCookie.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(8, this.analyticsCookie);
            }
            if (this.hasFullContentsListUrl || !this.fullContentsListUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(9, this.fullContentsListUrl);
            }
            if (this.hasNextPageUrl || !this.nextPageUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(10, this.nextPageUrl);
            }
            if (this.hasOrdered || this.ordered) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(11, this.ordered);
            }
            return computeSerializedSize;
        }
        
        @Override
        public Bucket mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            DocumentV1.DocV1[] document;
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
                        if (this.document == null) {
                            i = 0;
                        }
                        else {
                            i = this.document.length;
                        }
                        document = new DocumentV1.DocV1[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.document, 0, document, 0, i);
                        }
                        while (i < -1 + document.length) {
                            codedInputByteBufferNano.readMessage(document[i] = new DocumentV1.DocV1());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(document[i] = new DocumentV1.DocV1());
                        this.document = document;
                        continue;
                    }
                    case 16: {
                        this.multiCorpus = codedInputByteBufferNano.readBool();
                        this.hasMultiCorpus = true;
                        continue;
                    }
                    case 26: {
                        this.title = codedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        continue;
                    }
                    case 34: {
                        this.iconUrl = codedInputByteBufferNano.readString();
                        this.hasIconUrl = true;
                        continue;
                    }
                    case 42: {
                        this.fullContentsUrl = codedInputByteBufferNano.readString();
                        this.hasFullContentsUrl = true;
                        continue;
                    }
                    case 49: {
                        this.relevance = codedInputByteBufferNano.readDouble();
                        this.hasRelevance = true;
                        continue;
                    }
                    case 56: {
                        this.estimatedResults = codedInputByteBufferNano.readInt64();
                        this.hasEstimatedResults = true;
                        continue;
                    }
                    case 66: {
                        this.analyticsCookie = codedInputByteBufferNano.readString();
                        this.hasAnalyticsCookie = true;
                        continue;
                    }
                    case 74: {
                        this.fullContentsListUrl = codedInputByteBufferNano.readString();
                        this.hasFullContentsListUrl = true;
                        continue;
                    }
                    case 82: {
                        this.nextPageUrl = codedInputByteBufferNano.readString();
                        this.hasNextPageUrl = true;
                        continue;
                    }
                    case 88: {
                        this.ordered = codedInputByteBufferNano.readBool();
                        this.hasOrdered = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            DocumentV1.DocV1 docV1;
            if (this.document != null && this.document.length > 0) {
                for (int i = 0; i < this.document.length; ++i) {
                    docV1 = this.document[i];
                    if (docV1 != null) {
                        codedOutputByteBufferNano.writeMessage(1, docV1);
                    }
                }
            }
            if (this.hasMultiCorpus || this.multiCorpus) {
                codedOutputByteBufferNano.writeBool(2, this.multiCorpus);
            }
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.title);
            }
            if (this.hasIconUrl || !this.iconUrl.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.iconUrl);
            }
            if (this.hasFullContentsUrl || !this.fullContentsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.fullContentsUrl);
            }
            if (this.hasRelevance || Double.doubleToLongBits(this.relevance) != Double.doubleToLongBits(0.0)) {
                codedOutputByteBufferNano.writeDouble(6, this.relevance);
            }
            if (this.hasEstimatedResults || this.estimatedResults != 0L) {
                codedOutputByteBufferNano.writeInt64(7, this.estimatedResults);
            }
            if (this.hasAnalyticsCookie || !this.analyticsCookie.equals("")) {
                codedOutputByteBufferNano.writeString(8, this.analyticsCookie);
            }
            if (this.hasFullContentsListUrl || !this.fullContentsListUrl.equals("")) {
                codedOutputByteBufferNano.writeString(9, this.fullContentsListUrl);
            }
            if (this.hasNextPageUrl || !this.nextPageUrl.equals("")) {
                codedOutputByteBufferNano.writeString(10, this.nextPageUrl);
            }
            if (this.hasOrdered || this.ordered) {
                codedOutputByteBufferNano.writeBool(11, this.ordered);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class ListResponse extends MessageNano
    {
        public Bucket[] bucket;
        public DocumentV2.DocV2[] doc;
        
        public ListResponse() {
            super();
            this.clear();
        }
        
        public ListResponse clear() {
            this.bucket = Bucket.emptyArray();
            this.doc = DocumentV2.DocV2.emptyArray();
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            Bucket bucket;
            DocumentV2.DocV2 docV2;
            computeSerializedSize = super.computeSerializedSize();
            if (this.bucket != null && this.bucket.length > 0) {
                for (int i = 0; i < this.bucket.length; ++i) {
                    bucket = this.bucket[i];
                    if (bucket != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, bucket);
                    }
                }
            }
            if (this.doc != null && this.doc.length > 0) {
                for (int j = 0; j < this.doc.length; ++j) {
                    docV2 = this.doc[j];
                    if (docV2 != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, docV2);
                    }
                }
            }
            return computeSerializedSize;
        }
        
        @Override
        public ListResponse mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            Bucket[] bucket;
            int repeatedFieldArrayLength2;
            int j;
            DocumentV2.DocV2[] doc;
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
                        if (this.bucket == null) {
                            i = 0;
                        }
                        else {
                            i = this.bucket.length;
                        }
                        bucket = new Bucket[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.bucket, 0, bucket, 0, i);
                        }
                        while (i < -1 + bucket.length) {
                            codedInputByteBufferNano.readMessage(bucket[i] = new Bucket());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(bucket[i] = new Bucket());
                        this.bucket = bucket;
                        continue;
                    }
                    case 18: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 18);
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
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            Bucket bucket;
            DocumentV2.DocV2 docV2;
            if (this.bucket != null && this.bucket.length > 0) {
                for (int i = 0; i < this.bucket.length; ++i) {
                    bucket = this.bucket[i];
                    if (bucket != null) {
                        codedOutputByteBufferNano.writeMessage(1, bucket);
                    }
                }
            }
            if (this.doc != null && this.doc.length > 0) {
                for (int j = 0; j < this.doc.length; ++j) {
                    docV2 = this.doc[j];
                    if (docV2 != null) {
                        codedOutputByteBufferNano.writeMessage(2, docV2);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
