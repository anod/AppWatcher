package com.google.android.finsky.protos;

import com.google.protobuf.nano.*;
import java.io.*;

public interface Ownership
{
    public static final class OwnershipInfo extends MessageNano
    {
        public boolean autoRenewing;
        public Common.SignedData developerPurchaseInfo;
        public GroupLicense.GroupLicenseInfo groupLicenseInfo;
        public boolean hasAutoRenewing;
        public boolean hasHidden;
        public boolean hasInitiationTimestampMsec;
        public boolean hasLibraryExpirationTimestampMsec;
        public boolean hasPostDeliveryRefundWindowMsec;
        public boolean hasPreordered;
        public boolean hasQuantity;
        public boolean hasRefundTimeoutTimestampMsec;
        public boolean hasValidUntilTimestampMsec;
        public boolean hidden;
        public long initiationTimestampMsec;
        public long libraryExpirationTimestampMsec;
        public Common.LicensedDocumentInfo licensedDocumentInfo;
        public long postDeliveryRefundWindowMsec;
        public boolean preordered;
        public int quantity;
        public long refundTimeoutTimestampMsec;
        public Common.RentalTerms rentalTerms;
        public long validUntilTimestampMsec;
        
        public OwnershipInfo() {
            super();
            this.clear();
        }
        
        public OwnershipInfo clear() {
            this.initiationTimestampMsec = 0L;
            this.hasInitiationTimestampMsec = false;
            this.validUntilTimestampMsec = 0L;
            this.hasValidUntilTimestampMsec = false;
            this.autoRenewing = false;
            this.hasAutoRenewing = false;
            this.libraryExpirationTimestampMsec = 0L;
            this.hasLibraryExpirationTimestampMsec = false;
            this.refundTimeoutTimestampMsec = 0L;
            this.hasRefundTimeoutTimestampMsec = false;
            this.postDeliveryRefundWindowMsec = 0L;
            this.hasPostDeliveryRefundWindowMsec = false;
            this.developerPurchaseInfo = null;
            this.preordered = false;
            this.hasPreordered = false;
            this.hidden = false;
            this.hasHidden = false;
            this.rentalTerms = null;
            this.groupLicenseInfo = null;
            this.licensedDocumentInfo = null;
            this.quantity = 1;
            this.hasQuantity = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasInitiationTimestampMsec || this.initiationTimestampMsec != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(1, this.initiationTimestampMsec);
            }
            if (this.hasValidUntilTimestampMsec || this.validUntilTimestampMsec != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(2, this.validUntilTimestampMsec);
            }
            if (this.hasAutoRenewing || this.autoRenewing) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(3, this.autoRenewing);
            }
            if (this.hasRefundTimeoutTimestampMsec || this.refundTimeoutTimestampMsec != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(4, this.refundTimeoutTimestampMsec);
            }
            if (this.hasPostDeliveryRefundWindowMsec || this.postDeliveryRefundWindowMsec != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(5, this.postDeliveryRefundWindowMsec);
            }
            if (this.developerPurchaseInfo != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(6, this.developerPurchaseInfo);
            }
            if (this.hasPreordered || this.preordered) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(7, this.preordered);
            }
            if (this.hasHidden || this.hidden) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(8, this.hidden);
            }
            if (this.rentalTerms != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(9, this.rentalTerms);
            }
            if (this.groupLicenseInfo != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(10, this.groupLicenseInfo);
            }
            if (this.licensedDocumentInfo != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(11, this.licensedDocumentInfo);
            }
            if (this.hasQuantity || this.quantity != 1) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(12, this.quantity);
            }
            if (this.hasLibraryExpirationTimestampMsec || this.libraryExpirationTimestampMsec != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(14, this.libraryExpirationTimestampMsec);
            }
            return computeSerializedSize;
        }
        
        @Override
        public OwnershipInfo mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.initiationTimestampMsec = codedInputByteBufferNano.readInt64();
                        this.hasInitiationTimestampMsec = true;
                        continue;
                    }
                    case 16: {
                        this.validUntilTimestampMsec = codedInputByteBufferNano.readInt64();
                        this.hasValidUntilTimestampMsec = true;
                        continue;
                    }
                    case 24: {
                        this.autoRenewing = codedInputByteBufferNano.readBool();
                        this.hasAutoRenewing = true;
                        continue;
                    }
                    case 32: {
                        this.refundTimeoutTimestampMsec = codedInputByteBufferNano.readInt64();
                        this.hasRefundTimeoutTimestampMsec = true;
                        continue;
                    }
                    case 40: {
                        this.postDeliveryRefundWindowMsec = codedInputByteBufferNano.readInt64();
                        this.hasPostDeliveryRefundWindowMsec = true;
                        continue;
                    }
                    case 50: {
                        if (this.developerPurchaseInfo == null) {
                            this.developerPurchaseInfo = new Common.SignedData();
                        }
                        codedInputByteBufferNano.readMessage(this.developerPurchaseInfo);
                        continue;
                    }
                    case 56: {
                        this.preordered = codedInputByteBufferNano.readBool();
                        this.hasPreordered = true;
                        continue;
                    }
                    case 64: {
                        this.hidden = codedInputByteBufferNano.readBool();
                        this.hasHidden = true;
                        continue;
                    }
                    case 74: {
                        if (this.rentalTerms == null) {
                            this.rentalTerms = new Common.RentalTerms();
                        }
                        codedInputByteBufferNano.readMessage(this.rentalTerms);
                        continue;
                    }
                    case 82: {
                        if (this.groupLicenseInfo == null) {
                            this.groupLicenseInfo = new GroupLicense.GroupLicenseInfo();
                        }
                        codedInputByteBufferNano.readMessage(this.groupLicenseInfo);
                        continue;
                    }
                    case 90: {
                        if (this.licensedDocumentInfo == null) {
                            this.licensedDocumentInfo = new Common.LicensedDocumentInfo();
                        }
                        codedInputByteBufferNano.readMessage(this.licensedDocumentInfo);
                        continue;
                    }
                    case 96: {
                        this.quantity = codedInputByteBufferNano.readInt32();
                        this.hasQuantity = true;
                        continue;
                    }
                    case 112: {
                        this.libraryExpirationTimestampMsec = codedInputByteBufferNano.readInt64();
                        this.hasLibraryExpirationTimestampMsec = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasInitiationTimestampMsec || this.initiationTimestampMsec != 0L) {
                codedOutputByteBufferNano.writeInt64(1, this.initiationTimestampMsec);
            }
            if (this.hasValidUntilTimestampMsec || this.validUntilTimestampMsec != 0L) {
                codedOutputByteBufferNano.writeInt64(2, this.validUntilTimestampMsec);
            }
            if (this.hasAutoRenewing || this.autoRenewing) {
                codedOutputByteBufferNano.writeBool(3, this.autoRenewing);
            }
            if (this.hasRefundTimeoutTimestampMsec || this.refundTimeoutTimestampMsec != 0L) {
                codedOutputByteBufferNano.writeInt64(4, this.refundTimeoutTimestampMsec);
            }
            if (this.hasPostDeliveryRefundWindowMsec || this.postDeliveryRefundWindowMsec != 0L) {
                codedOutputByteBufferNano.writeInt64(5, this.postDeliveryRefundWindowMsec);
            }
            if (this.developerPurchaseInfo != null) {
                codedOutputByteBufferNano.writeMessage(6, this.developerPurchaseInfo);
            }
            if (this.hasPreordered || this.preordered) {
                codedOutputByteBufferNano.writeBool(7, this.preordered);
            }
            if (this.hasHidden || this.hidden) {
                codedOutputByteBufferNano.writeBool(8, this.hidden);
            }
            if (this.rentalTerms != null) {
                codedOutputByteBufferNano.writeMessage(9, this.rentalTerms);
            }
            if (this.groupLicenseInfo != null) {
                codedOutputByteBufferNano.writeMessage(10, this.groupLicenseInfo);
            }
            if (this.licensedDocumentInfo != null) {
                codedOutputByteBufferNano.writeMessage(11, this.licensedDocumentInfo);
            }
            if (this.hasQuantity || this.quantity != 1) {
                codedOutputByteBufferNano.writeInt32(12, this.quantity);
            }
            if (this.hasLibraryExpirationTimestampMsec || this.libraryExpirationTimestampMsec != 0L) {
                codedOutputByteBufferNano.writeInt64(14, this.libraryExpirationTimestampMsec);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
