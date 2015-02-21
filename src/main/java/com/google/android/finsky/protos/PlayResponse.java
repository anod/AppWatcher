package com.google.android.finsky.protos;

import java.io.*;
import com.google.protobuf.nano.*;

public interface PlayResponse
{
    public static final class PlayPayload extends MessageNano
    {

        public PlayPayload() {
            super();
            this.clear();
        }
        
        public PlayPayload clear() {
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            return computeSerializedSize;
        }
        
        @Override
        public PlayPayload mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
//                        if (this.oBSOLETEPlusProfileResponse == null) {
//                            this.oBSOLETEPlusProfileResponse = new PlayPlusProfile.PlayPlusProfileResponse();
//                        }
//                        codedInputByteBufferNano.readMessage(this.oBSOLETEPlusProfileResponse);
                        continue;
                    }
                    case 18: {
//                        if (this.plusProfileResponse == null) {
//                            this.plusProfileResponse = new PlusProfile.PlusProfileResponse();
//                        }
//                        codedInputByteBufferNano.readMessage(this.plusProfileResponse);
                        continue;
                    }
                }
            }
            return this;
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class PlayResponseWrapper extends MessageNano
    {
        public ResponseMessages.ServerCommands commands;
        public PlayPayload payload;
        public ResponseMessages.PreFetch[] preFetch;
        public ResponseMessages.ServerMetadata serverMetadata;
        
        public PlayResponseWrapper() {
            super();
            this.clear();
        }
        
        public static PlayResponseWrapper parseFrom(byte[] array) throws InvalidProtocolBufferNanoException {
            return MessageNano.mergeFrom(new PlayResponseWrapper(), array);
        }
        
        public PlayResponseWrapper clear() {
            this.payload = null;
            this.commands = null;
            this.preFetch = ResponseMessages.PreFetch.emptyArray();
            this.serverMetadata = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            ResponseMessages.PreFetch preFetch;
            computeSerializedSize = super.computeSerializedSize();
            if (this.payload != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.payload);
            }
            if (this.commands != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.commands);
            }
            if (this.preFetch != null && this.preFetch.length > 0) {
                for (int i = 0; i < this.preFetch.length; ++i) {
                    preFetch = this.preFetch[i];
                    if (preFetch != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, preFetch);
                    }
                }
            }
            if (this.serverMetadata != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, this.serverMetadata);
            }
            return computeSerializedSize;
        }
        
        @Override
        public PlayResponseWrapper mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            ResponseMessages.PreFetch[] preFetch;
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
                    case 10: {
                        if (this.payload == null) {
                            this.payload = new PlayPayload();
                        }
                        codedInputByteBufferNano.readMessage(this.payload);
                        continue;
                    }
                    case 18: {
                        if (this.commands == null) {
                            this.commands = new ResponseMessages.ServerCommands();
                        }
                        codedInputByteBufferNano.readMessage(this.commands);
                        continue;
                    }
                    case 26: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 26);
                        if (this.preFetch == null) {
                            i = 0;
                        }
                        else {
                            i = this.preFetch.length;
                        }
                        preFetch = new ResponseMessages.PreFetch[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.preFetch, 0, preFetch, 0, i);
                        }
                        while (i < -1 + preFetch.length) {
                            codedInputByteBufferNano.readMessage(preFetch[i] = new ResponseMessages.PreFetch());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(preFetch[i] = new ResponseMessages.PreFetch());
                        this.preFetch = preFetch;
                        continue;
                    }
                    case 34: {
                        if (this.serverMetadata == null) {
                            this.serverMetadata = new ResponseMessages.ServerMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.serverMetadata);
                        continue;
                    }
                }
            }
            return this;
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            ResponseMessages.PreFetch preFetch;
            if (this.payload != null) {
                codedOutputByteBufferNano.writeMessage(1, this.payload);
            }
            if (this.commands != null) {
                codedOutputByteBufferNano.writeMessage(2, this.commands);
            }
            if (this.preFetch != null && this.preFetch.length > 0) {
                for (int i = 0; i < this.preFetch.length; ++i) {
                    preFetch = this.preFetch[i];
                    if (preFetch != null) {
                        codedOutputByteBufferNano.writeMessage(3, preFetch);
                    }
                }
            }
            if (this.serverMetadata != null) {
                codedOutputByteBufferNano.writeMessage(4, this.serverMetadata);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
