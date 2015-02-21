package com.google.android.finsky.protos;

import java.util.*;
import com.google.protobuf.nano.*;
import java.io.*;

public interface ResponseMessages
{
    public static final class PreFetch extends MessageNano
    {
        private static volatile PreFetch[] _emptyArray;
        public String etag;
        public boolean hasEtag;
        public boolean hasResponse;
        public boolean hasSoftTtl;
        public boolean hasTtl;
        public boolean hasUrl;
        public byte[] response;
        public long softTtl;
        public long ttl;
        public String url;
        
        public PreFetch() {
            super();
            this.clear();
        }
        
        public static PreFetch[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new PreFetch[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public PreFetch clear() {
            this.url = "";
            this.hasUrl = false;
            this.response = WireFormatNano.EMPTY_BYTES;
            this.hasResponse = false;
            this.etag = "";
            this.hasEtag = false;
            this.ttl = 0L;
            this.hasTtl = false;
            this.softTtl = 0L;
            this.hasSoftTtl = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasUrl || !this.url.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.url);
            }
            if (this.hasResponse || !Arrays.equals(this.response, WireFormatNano.EMPTY_BYTES)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBytesSize(2, this.response);
            }
            if (this.hasEtag || !this.etag.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.etag);
            }
            if (this.hasTtl || this.ttl != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(4, this.ttl);
            }
            if (this.hasSoftTtl || this.softTtl != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(5, this.softTtl);
            }
            return computeSerializedSize;
        }
        
        @Override
        public PreFetch mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.url = codedInputByteBufferNano.readString();
                        this.hasUrl = true;
                        continue;
                    }
                    case 18: {
                        this.response = codedInputByteBufferNano.readBytes();
                        this.hasResponse = true;
                        continue;
                    }
                    case 26: {
                        this.etag = codedInputByteBufferNano.readString();
                        this.hasEtag = true;
                        continue;
                    }
                    case 32: {
                        this.ttl = codedInputByteBufferNano.readInt64();
                        this.hasTtl = true;
                        continue;
                    }
                    case 40: {
                        this.softTtl = codedInputByteBufferNano.readInt64();
                        this.hasSoftTtl = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasUrl || !this.url.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.url);
            }
            if (this.hasResponse || !Arrays.equals(this.response, WireFormatNano.EMPTY_BYTES)) {
                codedOutputByteBufferNano.writeBytes(2, this.response);
            }
            if (this.hasEtag || !this.etag.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.etag);
            }
            if (this.hasTtl || this.ttl != 0L) {
                codedOutputByteBufferNano.writeInt64(4, this.ttl);
            }
            if (this.hasSoftTtl || this.softTtl != 0L) {
                codedOutputByteBufferNano.writeInt64(5, this.softTtl);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class ServerCommands extends MessageNano
    {
        public boolean clearCache;
        public String displayErrorMessage;
        public boolean hasClearCache;
        public boolean hasDisplayErrorMessage;
        public boolean hasLogErrorStacktrace;
        public String logErrorStacktrace;
        
        public ServerCommands() {
            super();
            this.clear();
        }
        
        public ServerCommands clear() {
            this.clearCache = false;
            this.hasClearCache = false;
            this.displayErrorMessage = "";
            this.hasDisplayErrorMessage = false;
            this.logErrorStacktrace = "";
            this.hasLogErrorStacktrace = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasClearCache || this.clearCache) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(1, this.clearCache);
            }
            if (this.hasDisplayErrorMessage || !this.displayErrorMessage.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.displayErrorMessage);
            }
            if (this.hasLogErrorStacktrace || !this.logErrorStacktrace.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.logErrorStacktrace);
            }
            return computeSerializedSize;
        }
        
        @Override
        public ServerCommands mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                    case 8: {
                        this.clearCache = codedInputByteBufferNano.readBool();
                        this.hasClearCache = true;
                        continue;
                    }
                    case 18: {
                        this.displayErrorMessage = codedInputByteBufferNano.readString();
                        this.hasDisplayErrorMessage = true;
                        continue;
                    }
                    case 26: {
                        this.logErrorStacktrace = codedInputByteBufferNano.readString();
                        this.hasLogErrorStacktrace = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasClearCache || this.clearCache) {
                codedOutputByteBufferNano.writeBool(1, this.clearCache);
            }
            if (this.hasDisplayErrorMessage || !this.displayErrorMessage.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.displayErrorMessage);
            }
            if (this.hasLogErrorStacktrace || !this.logErrorStacktrace.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.logErrorStacktrace);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class ServerMetadata extends MessageNano
    {
        public boolean hasLatencyMillis;
        public long latencyMillis;
        
        public ServerMetadata() {
            super();
            this.clear();
        }
        
        public ServerMetadata clear() {
            this.latencyMillis = 0L;
            this.hasLatencyMillis = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasLatencyMillis || this.latencyMillis != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(1, this.latencyMillis);
            }
            return computeSerializedSize;
        }
        
        @Override
        public ServerMetadata mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                    case 8: {
                        this.latencyMillis = codedInputByteBufferNano.readInt64();
                        this.hasLatencyMillis = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasLatencyMillis || this.latencyMillis != 0L) {
                codedOutputByteBufferNano.writeInt64(1, this.latencyMillis);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
