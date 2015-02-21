package com.google.android.finsky.protos;

import com.google.protobuf.nano.*;
import java.io.*;

public interface GroupLicense
{
    public static final class GroupLicenseInfo extends MessageNano
    {
        public long gaiaGroupId;
        public Common.GroupLicenseKey groupLicenseKey;
        public boolean hasGaiaGroupId;
        public boolean hasLicensedOfferType;
        public int licensedOfferType;
        
        public GroupLicenseInfo() {
            super();
            this.clear();
        }
        
        public GroupLicenseInfo clear() {
            this.licensedOfferType = 1;
            this.hasLicensedOfferType = false;
            this.gaiaGroupId = 0L;
            this.hasGaiaGroupId = false;
            this.groupLicenseKey = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.licensedOfferType != 1 || this.hasLicensedOfferType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.licensedOfferType);
            }
            if (this.hasGaiaGroupId || this.gaiaGroupId != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeFixed64Size(2, this.gaiaGroupId);
            }
            if (this.groupLicenseKey != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, this.groupLicenseKey);
            }
            return computeSerializedSize;
        }
        
        @Override
        public GroupLicenseInfo mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                    case 8: {
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
                                this.licensedOfferType = int32;
                                this.hasLicensedOfferType = true;
                                continue;
                            }
                        }
                    }
                    case 17: {
                        this.gaiaGroupId = codedInputByteBufferNano.readFixed64();
                        this.hasGaiaGroupId = true;
                        continue;
                    }
                    case 26: {
                        if (this.groupLicenseKey == null) {
                            this.groupLicenseKey = new Common.GroupLicenseKey();
                        }
                        codedInputByteBufferNano.readMessage(this.groupLicenseKey);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.licensedOfferType != 1 || this.hasLicensedOfferType) {
                codedOutputByteBufferNano.writeInt32(1, this.licensedOfferType);
            }
            if (this.hasGaiaGroupId || this.gaiaGroupId != 0L) {
                codedOutputByteBufferNano.writeFixed64(2, this.gaiaGroupId);
            }
            if (this.groupLicenseKey != null) {
                codedOutputByteBufferNano.writeMessage(3, this.groupLicenseKey);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
