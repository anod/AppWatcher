package com.google.android.finsky.protos;

import java.io.*;
import com.google.protobuf.nano.*;
import java.util.*;

public interface Containers
{
    public static final class ContainerMetadata extends MessageNano
    {
        public String analyticsCookie;
        public String browseUrl;
        public ContainerView[] containerView;
        public long estimatedResults;
        public boolean hasAnalyticsCookie;
        public boolean hasBrowseUrl;
        public boolean hasEstimatedResults;
        public boolean hasNextPageUrl;
        public boolean hasOrdered;
        public boolean hasRelevance;
        public String nextPageUrl;
        public boolean ordered;
        public double relevance;
        
        public ContainerMetadata() {
            super();
            this.clear();
        }
        
        public ContainerMetadata clear() {
            this.browseUrl = "";
            this.hasBrowseUrl = false;
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
            this.containerView = ContainerView.emptyArray();
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            ContainerView containerView;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasBrowseUrl || !this.browseUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.browseUrl);
            }
            if (this.hasNextPageUrl || !this.nextPageUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.nextPageUrl);
            }
            if (this.hasRelevance || Double.doubleToLongBits(this.relevance) != Double.doubleToLongBits(0.0)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeDoubleSize(3, this.relevance);
            }
            if (this.hasEstimatedResults || this.estimatedResults != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(4, this.estimatedResults);
            }
            if (this.hasAnalyticsCookie || !this.analyticsCookie.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.analyticsCookie);
            }
            if (this.hasOrdered || this.ordered) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(6, this.ordered);
            }
            if (this.containerView != null && this.containerView.length > 0) {
                for (int i = 0; i < this.containerView.length; ++i) {
                    containerView = this.containerView[i];
                    if (containerView != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(7, containerView);
                    }
                }
            }
            return computeSerializedSize;
        }
        
        @Override
        public ContainerMetadata mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            ContainerView[] containerView;
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            continue;
                        }
                    }
                    case 0: {
                       return this;
                    }
                    case 10: {
                        this.browseUrl = codedInputByteBufferNano.readString();
                        this.hasBrowseUrl = true;
                        continue;
                    }
                    case 18: {
                        this.nextPageUrl = codedInputByteBufferNano.readString();
                        this.hasNextPageUrl = true;
                        continue;
                    }
                    case 25: {
                        this.relevance = codedInputByteBufferNano.readDouble();
                        this.hasRelevance = true;
                        continue;
                    }
                    case 32: {
                        this.estimatedResults = codedInputByteBufferNano.readInt64();
                        this.hasEstimatedResults = true;
                        continue;
                    }
                    case 42: {
                        this.analyticsCookie = codedInputByteBufferNano.readString();
                        this.hasAnalyticsCookie = true;
                        continue;
                    }
                    case 48: {
                        this.ordered = codedInputByteBufferNano.readBool();
                        this.hasOrdered = true;
                        continue;
                    }
                    case 58: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 58);
                        if (this.containerView == null) {
                            i = 0;
                        }
                        else {
                            i = this.containerView.length;
                        }
                        containerView = new ContainerView[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.containerView, 0, containerView, 0, i);
                        }
                        while (i < -1 + containerView.length) {
                            codedInputByteBufferNano.readMessage(containerView[i] = new ContainerView());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(containerView[i] = new ContainerView());
                        this.containerView = containerView;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            ContainerView containerView;
            if (this.hasBrowseUrl || !this.browseUrl.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.browseUrl);
            }
            if (this.hasNextPageUrl || !this.nextPageUrl.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.nextPageUrl);
            }
            if (this.hasRelevance || Double.doubleToLongBits(this.relevance) != Double.doubleToLongBits(0.0)) {
                codedOutputByteBufferNano.writeDouble(3, this.relevance);
            }
            if (this.hasEstimatedResults || this.estimatedResults != 0L) {
                codedOutputByteBufferNano.writeInt64(4, this.estimatedResults);
            }
            if (this.hasAnalyticsCookie || !this.analyticsCookie.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.analyticsCookie);
            }
            if (this.hasOrdered || this.ordered) {
                codedOutputByteBufferNano.writeBool(6, this.ordered);
            }
            if (this.containerView != null && this.containerView.length > 0) {
                for (int i = 0; i < this.containerView.length; ++i) {
                    containerView = this.containerView[i];
                    if (containerView != null) {
                        codedOutputByteBufferNano.writeMessage(7, containerView);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class ContainerView extends MessageNano
    {
        private static volatile ContainerView[] _emptyArray;
        public boolean hasListUrl;
        public boolean hasSelected;
        public boolean hasServerLogsCookie;
        public boolean hasTitle;
        public String listUrl;
        public boolean selected;
        public byte[] serverLogsCookie;
        public String title;
        
        public ContainerView() {
            super();
            this.clear();
        }
        
        public static ContainerView[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ContainerView[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public ContainerView clear() {
            this.selected = false;
            this.hasSelected = false;
            this.title = "";
            this.hasTitle = false;
            this.listUrl = "";
            this.hasListUrl = false;
            this.serverLogsCookie = WireFormatNano.EMPTY_BYTES;
            this.hasServerLogsCookie = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasSelected || this.selected) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(1, this.selected);
            }
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.title);
            }
            if (this.hasListUrl || !this.listUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.listUrl);
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBytesSize(4, this.serverLogsCookie);
            }
            return computeSerializedSize;
        }
        
        @Override
        public ContainerView mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
        Label_0064:
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            break Label_0064;
                        }
                        continue;
                    }
                    case 0: {
                        break Label_0064;
                    }
                    case 8: {
                        this.selected = codedInputByteBufferNano.readBool();
                        this.hasSelected = true;
                        continue;
                    }
                    case 18: {
                        this.title = codedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        continue;
                    }
                    case 26: {
                        this.listUrl = codedInputByteBufferNano.readString();
                        this.hasListUrl = true;
                        continue;
                    }
                    case 34: {
                        this.serverLogsCookie = codedInputByteBufferNano.readBytes();
                        this.hasServerLogsCookie = true;
                        continue;
                    }
                }
            }
            return this;
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasSelected || this.selected) {
                codedOutputByteBufferNano.writeBool(1, this.selected);
            }
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.title);
            }
            if (this.hasListUrl || !this.listUrl.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.listUrl);
            }
            if (this.hasServerLogsCookie || !Arrays.equals(this.serverLogsCookie, WireFormatNano.EMPTY_BYTES)) {
                codedOutputByteBufferNano.writeBytes(4, this.serverLogsCookie);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
