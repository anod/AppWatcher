package com.google.android.finsky.protos;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;

import java.io.IOException;

public abstract interface Response {
    public static final class Payload extends MessageNano {
        public DocList.ListResponse listResponse;
        public Search.SearchResponse searchResponse;

        public Payload() {
            clear();
        }

        public Payload clear() {
            this.listResponse = null;
            this.searchResponse = null;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if (this.listResponse != null)
                i += CodedOutputByteBufferNano.computeMessageSize(1, this.listResponse);
            if (this.searchResponse != null)
                i += CodedOutputByteBufferNano.computeMessageSize(5, this.searchResponse);
            return i;
        }

        public Payload mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException {
            while (true) {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i) {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 10:
                        if (this.listResponse == null)
                            this.listResponse = new DocList.ListResponse();
                        paramCodedInputByteBufferNano.readMessage(this.listResponse);
                        break;
                    case 42:
                        if (this.searchResponse == null)
                            this.searchResponse = new Search.SearchResponse();
                        paramCodedInputByteBufferNano.readMessage(this.searchResponse);
                        break;
                }
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if (this.listResponse != null)
                paramCodedOutputByteBufferNano.writeMessage(1, this.listResponse);
            if (this.searchResponse != null)
                paramCodedOutputByteBufferNano.writeMessage(5, this.searchResponse);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class ResponseWrapper extends MessageNano {
        public ResponseMessages.ServerCommands commands;
        public Response.Payload payload;
        public ResponseMessages.PreFetch[] preFetch;
        public ResponseMessages.ServerMetadata serverMetadata;

        public ResponseWrapper() {
            clear();
        }

        public static ResponseWrapper parseFrom(byte[] paramArrayOfByte)
                throws InvalidProtocolBufferNanoException {
            return (ResponseWrapper) MessageNano.mergeFrom(new ResponseWrapper(), paramArrayOfByte);
        }

        public ResponseWrapper clear() {
            this.payload = null;
            this.commands = null;
            this.preFetch = ResponseMessages.PreFetch.emptyArray();
            this.serverMetadata = null;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if (this.payload != null)
                i += CodedOutputByteBufferNano.computeMessageSize(1, this.payload);
            if (this.commands != null)
                i += CodedOutputByteBufferNano.computeMessageSize(2, this.commands);
            if ((this.preFetch != null) && (this.preFetch.length > 0))
                for (int k = 0; k < this.preFetch.length; k++) {
                    ResponseMessages.PreFetch localPreFetch = this.preFetch[k];
                    if (localPreFetch != null)
                        i += CodedOutputByteBufferNano.computeMessageSize(3, localPreFetch);
                }
            if (this.serverMetadata != null)
                i += CodedOutputByteBufferNano.computeMessageSize(5, this.serverMetadata);
            return i;
        }

        public ResponseWrapper mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException {
            while (true) {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i) {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i)) {
                            continue;
                        }
                    case 0:
                        return this;
                    case 10:
                        if (this.payload == null)
                            this.payload = new Response.Payload();
                        paramCodedInputByteBufferNano.readMessage(this.payload);
                        break;
                    case 18:
                        if (this.commands == null)
                            this.commands = new ResponseMessages.ServerCommands();
                        paramCodedInputByteBufferNano.readMessage(this.commands);
                        break;
                    case 26:
                        int arrayLength = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 26);
                        int n = this.preFetch == null ? 0 : this.preFetch.length;
                        ResponseMessages.PreFetch[] newArray = new ResponseMessages.PreFetch[i + arrayLength];
                        if (n != 0) {
                            System.arraycopy(this.preFetch, 0, newArray, 0, i);
                        }
                        for (; n < newArray.length - 1; n++) {
                            newArray[n] = new ResponseMessages.PreFetch();
                            paramCodedInputByteBufferNano.readMessage(newArray[n]);
                            paramCodedInputByteBufferNano.readTag();
                        }
                        // Last one without readTag.
                        newArray[n] = new ResponseMessages.PreFetch();
                        paramCodedInputByteBufferNano.readMessage(newArray[n]);
                        this.preFetch = newArray;
                        break;
                    case 42:
                        if (this.serverMetadata == null)
                            this.serverMetadata = new ResponseMessages.ServerMetadata();
                        paramCodedInputByteBufferNano.readMessage(this.serverMetadata);
                        break;
                    case 50:
                }
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if (this.payload != null)
                paramCodedOutputByteBufferNano.writeMessage(1, this.payload);
            if (this.commands != null)
                paramCodedOutputByteBufferNano.writeMessage(2, this.commands);
            if ((this.preFetch != null) && (this.preFetch.length > 0))
                for (int j = 0; j < this.preFetch.length; j++) {
                    ResponseMessages.PreFetch localPreFetch = this.preFetch[j];
                    if (localPreFetch != null)
                        paramCodedOutputByteBufferNano.writeMessage(3, localPreFetch);
                }
            if (this.serverMetadata != null)
                paramCodedOutputByteBufferNano.writeMessage(5, this.serverMetadata);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }
}