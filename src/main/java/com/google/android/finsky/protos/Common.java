package com.google.android.finsky.protos;

import java.io.*;
import com.google.protobuf.nano.*;

public interface Common
{
    public static final class Attribution extends MessageNano
    {
        public boolean hasLicenseTitle;
        public boolean hasLicenseUrl;
        public boolean hasSourceTitle;
        public boolean hasSourceUrl;
        public String licenseTitle;
        public String licenseUrl;
        public String sourceTitle;
        public String sourceUrl;

        public Attribution() {
            super();
            this.clear();
        }

        public Attribution clear() {
            this.sourceTitle = "";
            this.hasSourceTitle = false;
            this.sourceUrl = "";
            this.hasSourceUrl = false;
            this.licenseTitle = "";
            this.hasLicenseTitle = false;
            this.licenseUrl = "";
            this.hasLicenseUrl = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasSourceTitle || !this.sourceTitle.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.sourceTitle);
            }
            if (this.hasSourceUrl || !this.sourceUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.sourceUrl);
            }
            if (this.hasLicenseTitle || !this.licenseTitle.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.licenseTitle);
            }
            if (this.hasLicenseUrl || !this.licenseUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.licenseUrl);
            }
            return computeSerializedSize;
        }

        @Override
        public Attribution mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                    case 10: {
                        this.sourceTitle = codedInputByteBufferNano.readString();
                        this.hasSourceTitle = true;
                        continue;
                    }
                    case 18: {
                        this.sourceUrl = codedInputByteBufferNano.readString();
                        this.hasSourceUrl = true;
                        continue;
                    }
                    case 26: {
                        this.licenseTitle = codedInputByteBufferNano.readString();
                        this.hasLicenseTitle = true;
                        continue;
                    }
                    case 34: {
                        this.licenseUrl = codedInputByteBufferNano.readString();
                        this.hasLicenseUrl = true;
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasSourceTitle || !this.sourceTitle.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.sourceTitle);
            }
            if (this.hasSourceUrl || !this.sourceUrl.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.sourceUrl);
            }
            if (this.hasLicenseTitle || !this.licenseTitle.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.licenseTitle);
            }
            if (this.hasLicenseUrl || !this.licenseUrl.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.licenseUrl);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class Docid extends MessageNano
    {
        private static volatile Docid[] _emptyArray;
        public int backend;
        public String backendDocid;
        public boolean hasBackend;
        public boolean hasBackendDocid;
        public boolean hasType;
        public int type;

        public Docid() {
            super();
            this.clear();
        }

        public static Docid[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Docid[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Docid clear() {
            this.backendDocid = "";
            this.hasBackendDocid = false;
            this.type = 1;
            this.hasType = false;
            this.backend = 0;
            this.hasBackend = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasBackendDocid || !this.backendDocid.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.backendDocid);
            }
            if (this.type != 1 || this.hasType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.type);
            }
            if (this.backend != 0 || this.hasBackend) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.backend);
            }
            return computeSerializedSize;
        }

        @Override
        public Docid mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
            int int2;
            Label_0056:
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            break Label_0056;
                        }
                        continue;
                    }
                    case 0: {
                        break Label_0056;
                    }
                    case 10: {
                        this.backendDocid = codedInputByteBufferNano.readString();
                        this.hasBackendDocid = true;
                        continue;
                    }
                    case 16: {
                        int32 = codedInputByteBufferNano.readInt32();
                        switch (int32) {
                            default: {
                                break;
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
                                this.type = int32;
                                this.hasType = true;
                                continue;
                            }
                        }
                        break;
                    }
                    case 24: {
                        int2 = codedInputByteBufferNano.readInt32();
                        switch (int2) {
                            default: {
                                break;
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
                                this.backend = int2;
                                this.hasBackend = true;
                                continue;
                            }
                        }
                        break;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasBackendDocid || !this.backendDocid.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.backendDocid);
            }
            if (this.type != 1 || this.hasType) {
                codedOutputByteBufferNano.writeInt32(2, this.type);
            }
            if (this.backend != 0 || this.hasBackend) {
                codedOutputByteBufferNano.writeInt32(3, this.backend);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class GroupLicenseDocid extends MessageNano
    {
        public int backend;
        public String backendDocid;
        public boolean hasBackend;
        public boolean hasBackendDocid;
        public boolean hasType;
        public int type;

        public GroupLicenseDocid() {
            super();
            this.clear();
        }

        public GroupLicenseDocid clear() {
            this.backendDocid = "";
            this.hasBackendDocid = false;
            this.type = 0;
            this.hasType = false;
            this.backend = 0;
            this.hasBackend = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasBackendDocid || !this.backendDocid.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.backendDocid);
            }
            if (this.hasType || this.type != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.type);
            }
            if (this.hasBackend || this.backend != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.backend);
            }
            return computeSerializedSize;
        }

        @Override
        public GroupLicenseDocid mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            Label_0056:
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            break Label_0056;
                        }
                        continue;
                    }
                    case 0: {
                        break Label_0056;
                    }
                    case 10: {
                        this.backendDocid = codedInputByteBufferNano.readString();
                        this.hasBackendDocid = true;
                        continue;
                    }
                    case 16: {
                        this.type = codedInputByteBufferNano.readInt32();
                        this.hasType = true;
                        continue;
                    }
                    case 24: {
                        this.backend = codedInputByteBufferNano.readInt32();
                        this.hasBackend = true;
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasBackendDocid || !this.backendDocid.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.backendDocid);
            }
            if (this.hasType || this.type != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.type);
            }
            if (this.hasBackend || this.backend != 0) {
                codedOutputByteBufferNano.writeInt32(3, this.backend);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class GroupLicenseKey extends MessageNano
    {
        public long dasherCustomerId;
        public GroupLicenseDocid docid;
        public boolean hasDasherCustomerId;
        public boolean hasLicensedOfferType;
        public boolean hasRentalPeriodDays;
        public boolean hasType;
        public int licensedOfferType;
        public int rentalPeriodDays;
        public int type;

        public GroupLicenseKey() {
            super();
            this.clear();
        }

        public GroupLicenseKey clear() {
            this.dasherCustomerId = 0L;
            this.hasDasherCustomerId = false;
            this.docid = null;
            this.licensedOfferType = 0;
            this.hasLicensedOfferType = false;
            this.type = 0;
            this.hasType = false;
            this.rentalPeriodDays = 0;
            this.hasRentalPeriodDays = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasDasherCustomerId || this.dasherCustomerId != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeFixed64Size(1, this.dasherCustomerId);
            }
            if (this.docid != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.docid);
            }
            if (this.hasLicensedOfferType || this.licensedOfferType != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.licensedOfferType);
            }
            if (this.hasType || this.type != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, this.type);
            }
            if (this.hasRentalPeriodDays || this.rentalPeriodDays != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(5, this.rentalPeriodDays);
            }
            return computeSerializedSize;
        }

        @Override
        public GroupLicenseKey mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            Label_0072:
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            break Label_0072;
                        }
                        continue;
                    }
                    case 0: {
                        break Label_0072;
                    }
                    case 9: {
                        this.dasherCustomerId = codedInputByteBufferNano.readFixed64();
                        this.hasDasherCustomerId = true;
                        continue;
                    }
                    case 18: {
                        if (this.docid == null) {
                            this.docid = new GroupLicenseDocid();
                        }
                        codedInputByteBufferNano.readMessage(this.docid);
                        continue;
                    }
                    case 24: {
                        this.licensedOfferType = codedInputByteBufferNano.readInt32();
                        this.hasLicensedOfferType = true;
                        continue;
                    }
                    case 32: {
                        this.type = codedInputByteBufferNano.readInt32();
                        this.hasType = true;
                        continue;
                    }
                    case 40: {
                        this.rentalPeriodDays = codedInputByteBufferNano.readInt32();
                        this.hasRentalPeriodDays = true;
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasDasherCustomerId || this.dasherCustomerId != 0L) {
                codedOutputByteBufferNano.writeFixed64(1, this.dasherCustomerId);
            }
            if (this.docid != null) {
                codedOutputByteBufferNano.writeMessage(2, this.docid);
            }
            if (this.hasLicensedOfferType || this.licensedOfferType != 0) {
                codedOutputByteBufferNano.writeInt32(3, this.licensedOfferType);
            }
            if (this.hasType || this.type != 0) {
                codedOutputByteBufferNano.writeInt32(4, this.type);
            }
            if (this.hasRentalPeriodDays || this.rentalPeriodDays != 0) {
                codedOutputByteBufferNano.writeInt32(5, this.rentalPeriodDays);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class Image extends MessageNano
    {
        private static volatile Image[] _emptyArray;
        public String altTextLocalized;
        public Attribution attribution;
        public boolean autogen;
        public Citation citation;
        public Dimension dimension;
        public int durationSeconds;
        public String fillColorRgb;
        public boolean hasAltTextLocalized;
        public boolean hasAutogen;
        public boolean hasDurationSeconds;
        public boolean hasFillColorRgb;
        public boolean hasImageType;
        public boolean hasImageUrl;
        public boolean hasPositionInSequence;
        public boolean hasSecureUrl;
        public boolean hasSupportsFifeUrlOptions;
        public int imageType;
        public String imageUrl;
        public int positionInSequence;
        public String secureUrl;
        public boolean supportsFifeUrlOptions;

        public Image() {
            super();
            this.clear();
        }

        public static Image[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Image[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Image clear() {
            this.imageType = 0;
            this.hasImageType = false;
            this.positionInSequence = 0;
            this.hasPositionInSequence = false;
            this.dimension = null;
            this.imageUrl = "";
            this.hasImageUrl = false;
            this.secureUrl = "";
            this.hasSecureUrl = false;
            this.altTextLocalized = "";
            this.hasAltTextLocalized = false;
            this.supportsFifeUrlOptions = false;
            this.hasSupportsFifeUrlOptions = false;
            this.durationSeconds = 0;
            this.hasDurationSeconds = false;
            this.fillColorRgb = "";
            this.hasFillColorRgb = false;
            this.autogen = false;
            this.hasAutogen = false;
            this.attribution = null;
            this.citation = null;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.imageType != 0 || this.hasImageType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.imageType);
            }
            if (this.dimension != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeGroupSize(2, this.dimension);
            }
            if (this.hasImageUrl || !this.imageUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.imageUrl);
            }
            if (this.hasAltTextLocalized || !this.altTextLocalized.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(6, this.altTextLocalized);
            }
            if (this.hasSecureUrl || !this.secureUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(7, this.secureUrl);
            }
            if (this.hasPositionInSequence || this.positionInSequence != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(8, this.positionInSequence);
            }
            if (this.hasSupportsFifeUrlOptions || this.supportsFifeUrlOptions) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(9, this.supportsFifeUrlOptions);
            }
            if (this.citation != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeGroupSize(10, this.citation);
            }
            if (this.hasDurationSeconds || this.durationSeconds != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(14, this.durationSeconds);
            }
            if (this.hasFillColorRgb || !this.fillColorRgb.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(15, this.fillColorRgb);
            }
            if (this.hasAutogen || this.autogen) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(16, this.autogen);
            }
            if (this.attribution != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(17, this.attribution);
            }
            return computeSerializedSize;
        }

        @Override
        public Image mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                                break;
                            }
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9:
                            case 13:
                            case 14:
                            case 15: {
                                this.imageType = int32;
                                this.hasImageType = true;
                                continue;
                            }
                        }
                        break;
                    }
                    case 19: {
                        if (this.dimension == null) {
                            this.dimension = new Dimension();
                        }
                        codedInputByteBufferNano.readGroup(this.dimension, 2);
                        continue;
                    }
                    case 42: {
                        this.imageUrl = codedInputByteBufferNano.readString();
                        this.hasImageUrl = true;
                        continue;
                    }
                    case 50: {
                        this.altTextLocalized = codedInputByteBufferNano.readString();
                        this.hasAltTextLocalized = true;
                        continue;
                    }
                    case 58: {
                        this.secureUrl = codedInputByteBufferNano.readString();
                        this.hasSecureUrl = true;
                        continue;
                    }
                    case 64: {
                        this.positionInSequence = codedInputByteBufferNano.readInt32();
                        this.hasPositionInSequence = true;
                        continue;
                    }
                    case 72: {
                        this.supportsFifeUrlOptions = codedInputByteBufferNano.readBool();
                        this.hasSupportsFifeUrlOptions = true;
                        continue;
                    }
                    case 83: {
                        if (this.citation == null) {
                            this.citation = new Citation();
                        }
                        codedInputByteBufferNano.readGroup(this.citation, 10);
                        continue;
                    }
                    case 112: {
                        this.durationSeconds = codedInputByteBufferNano.readInt32();
                        this.hasDurationSeconds = true;
                        continue;
                    }
                    case 122: {
                        this.fillColorRgb = codedInputByteBufferNano.readString();
                        this.hasFillColorRgb = true;
                        continue;
                    }
                    case 128: {
                        this.autogen = codedInputByteBufferNano.readBool();
                        this.hasAutogen = true;
                        continue;
                    }
                    case 138: {
                        if (this.attribution == null) {
                            this.attribution = new Attribution();
                        }
                        codedInputByteBufferNano.readMessage(this.attribution);
                        continue;
                    }
                }
            }
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.imageType != 0 || this.hasImageType) {
                codedOutputByteBufferNano.writeInt32(1, this.imageType);
            }
            if (this.dimension != null) {
                codedOutputByteBufferNano.writeGroup(2, this.dimension);
            }
            if (this.hasImageUrl || !this.imageUrl.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.imageUrl);
            }
            if (this.hasAltTextLocalized || !this.altTextLocalized.equals("")) {
                codedOutputByteBufferNano.writeString(6, this.altTextLocalized);
            }
            if (this.hasSecureUrl || !this.secureUrl.equals("")) {
                codedOutputByteBufferNano.writeString(7, this.secureUrl);
            }
            if (this.hasPositionInSequence || this.positionInSequence != 0) {
                codedOutputByteBufferNano.writeInt32(8, this.positionInSequence);
            }
            if (this.hasSupportsFifeUrlOptions || this.supportsFifeUrlOptions) {
                codedOutputByteBufferNano.writeBool(9, this.supportsFifeUrlOptions);
            }
            if (this.citation != null) {
                codedOutputByteBufferNano.writeGroup(10, this.citation);
            }
            if (this.hasDurationSeconds || this.durationSeconds != 0) {
                codedOutputByteBufferNano.writeInt32(14, this.durationSeconds);
            }
            if (this.hasFillColorRgb || !this.fillColorRgb.equals("")) {
                codedOutputByteBufferNano.writeString(15, this.fillColorRgb);
            }
            if (this.hasAutogen || this.autogen) {
                codedOutputByteBufferNano.writeBool(16, this.autogen);
            }
            if (this.attribution != null) {
                codedOutputByteBufferNano.writeMessage(17, this.attribution);
            }
            super.writeTo(codedOutputByteBufferNano);
        }

        public static final class Citation extends MessageNano
        {
            public boolean hasTitleLocalized;
            public boolean hasUrl;
            public String titleLocalized;
            public String url;

            public Citation() {
                super();
                this.clear();
            }

            public Citation clear() {
                this.titleLocalized = "";
                this.hasTitleLocalized = false;
                this.url = "";
                this.hasUrl = false;
                this.cachedSize = -1;
                return this;
            }

            @Override
            protected int computeSerializedSize() {
                int computeSerializedSize;
                computeSerializedSize = super.computeSerializedSize();
                if (this.hasTitleLocalized || !this.titleLocalized.equals("")) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(11, this.titleLocalized);
                }
                if (this.hasUrl || !this.url.equals("")) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(12, this.url);
                }
                return computeSerializedSize;
            }

            @Override
            public Citation mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        case 90: {
                            this.titleLocalized = codedInputByteBufferNano.readString();
                            this.hasTitleLocalized = true;
                            continue;
                        }
                        case 98: {
                            this.url = codedInputByteBufferNano.readString();
                            this.hasUrl = true;
                            continue;
                        }
                    }
                }
                return this;
            }

            @Override
            public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
                if (this.hasTitleLocalized || !this.titleLocalized.equals("")) {
                    codedOutputByteBufferNano.writeString(11, this.titleLocalized);
                }
                if (this.hasUrl || !this.url.equals("")) {
                    codedOutputByteBufferNano.writeString(12, this.url);
                }
                super.writeTo(codedOutputByteBufferNano);
            }
        }

        public static final class Dimension extends MessageNano
        {
            public int aspectRatio;
            public boolean hasAspectRatio;
            public boolean hasHeight;
            public boolean hasWidth;
            public int height;
            public int width;

            public Dimension() {
                super();
                this.clear();
            }

            public Dimension clear() {
                this.width = 0;
                this.hasWidth = false;
                this.height = 0;
                this.hasHeight = false;
                this.aspectRatio = 0;
                this.hasAspectRatio = false;
                this.cachedSize = -1;
                return this;
            }

            @Override
            protected int computeSerializedSize() {
                int computeSerializedSize;
                computeSerializedSize = super.computeSerializedSize();
                if (this.hasWidth || this.width != 0) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.width);
                }
                if (this.hasHeight || this.height != 0) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, this.height);
                }
                if (this.aspectRatio != 0 || this.hasAspectRatio) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(18, this.aspectRatio);
                }
                return computeSerializedSize;
            }

            @Override
            public Dimension mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
                int tag;
                int int32;
                Label_0056:
                while (true) {
                    tag = codedInputByteBufferNano.readTag();
                    switch (tag) {
                        default: {
                            if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                                break Label_0056;
                            }
                            continue;
                        }
                        case 0: {
                            break Label_0056;
                        }
                        case 24: {
                            this.width = codedInputByteBufferNano.readInt32();
                            this.hasWidth = true;
                            continue;
                        }
                        case 32: {
                            this.height = codedInputByteBufferNano.readInt32();
                            this.hasHeight = true;
                            continue;
                        }
                        case 144: {
                            int32 = codedInputByteBufferNano.readInt32();
                            switch (int32) {
                                default: {
                                    break;
                                }
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5: {
                                    this.aspectRatio = int32;
                                    this.hasAspectRatio = true;
                                    continue;
                                }
                            }
                            break;
                        }
                    }
                }
                return this;
            }

            @Override
            public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
                if (this.hasWidth || this.width != 0) {
                    codedOutputByteBufferNano.writeInt32(3, this.width);
                }
                if (this.hasHeight || this.height != 0) {
                    codedOutputByteBufferNano.writeInt32(4, this.height);
                }
                if (this.aspectRatio != 0 || this.hasAspectRatio) {
                    codedOutputByteBufferNano.writeInt32(18, this.aspectRatio);
                }
                super.writeTo(codedOutputByteBufferNano);
            }
        }
    }

    public static final class Install extends MessageNano
    {
        private static volatile Install[] _emptyArray;
        public long androidId;
        public boolean bundled;
        public boolean hasAndroidId;
        public boolean hasBundled;
        public boolean hasPending;
        public boolean hasVersion;
        public boolean pending;
        public int version;

        public Install() {
            super();
            this.clear();
        }

        public static Install[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Install[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Install clear() {
            this.androidId = 0L;
            this.hasAndroidId = false;
            this.version = 0;
            this.hasVersion = false;
            this.bundled = false;
            this.hasBundled = false;
            this.pending = false;
            this.hasPending = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasAndroidId || this.androidId != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeFixed64Size(1, this.androidId);
            }
            if (this.hasVersion || this.version != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.version);
            }
            if (this.hasBundled || this.bundled) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(3, this.bundled);
            }
            if (this.hasPending || this.pending) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(4, this.pending);
            }
            return computeSerializedSize;
        }

        @Override
        public Install mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                    case 9: {
                        this.androidId = codedInputByteBufferNano.readFixed64();
                        this.hasAndroidId = true;
                        continue;
                    }
                    case 16: {
                        this.version = codedInputByteBufferNano.readInt32();
                        this.hasVersion = true;
                        continue;
                    }
                    case 24: {
                        this.bundled = codedInputByteBufferNano.readBool();
                        this.hasBundled = true;
                        continue;
                    }
                    case 32: {
                        this.pending = codedInputByteBufferNano.readBool();
                        this.hasPending = true;
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasAndroidId || this.androidId != 0L) {
                codedOutputByteBufferNano.writeFixed64(1, this.androidId);
            }
            if (this.hasVersion || this.version != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.version);
            }
            if (this.hasBundled || this.bundled) {
                codedOutputByteBufferNano.writeBool(3, this.bundled);
            }
            if (this.hasPending || this.pending) {
                codedOutputByteBufferNano.writeBool(4, this.pending);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class LicensedDocumentInfo extends MessageNano
    {
        public long assignedByGaiaId;
        public String assignmentId;
        public long[] gaiaGroupId;
        public String groupLicenseCheckoutOrderId;
        public GroupLicenseKey groupLicenseKey;
        public boolean hasAssignedByGaiaId;
        public boolean hasAssignmentId;
        public boolean hasGroupLicenseCheckoutOrderId;

        public LicensedDocumentInfo() {
            super();
            this.clear();
        }

        public LicensedDocumentInfo clear() {
            this.gaiaGroupId = WireFormatNano.EMPTY_LONG_ARRAY;
            this.groupLicenseCheckoutOrderId = "";
            this.hasGroupLicenseCheckoutOrderId = false;
            this.groupLicenseKey = null;
            this.assignedByGaiaId = 0L;
            this.hasAssignedByGaiaId = false;
            this.assignmentId = "";
            this.hasAssignmentId = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.gaiaGroupId != null && this.gaiaGroupId.length > 0) {
                computeSerializedSize = computeSerializedSize + 8 * this.gaiaGroupId.length + 1 * this.gaiaGroupId.length;
            }
            if (this.hasGroupLicenseCheckoutOrderId || !this.groupLicenseCheckoutOrderId.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.groupLicenseCheckoutOrderId);
            }
            if (this.groupLicenseKey != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, this.groupLicenseKey);
            }
            if (this.hasAssignedByGaiaId || this.assignedByGaiaId != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeFixed64Size(4, this.assignedByGaiaId);
            }
            if (this.hasAssignmentId || !this.assignmentId.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.assignmentId);
            }
            return computeSerializedSize;
        }

        @Override
        public LicensedDocumentInfo mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            long[] gaiaGroupId;
            int rawVarint32;
            int pushLimit;
            int n;
            int j;
            long[] gaiaGroupId2;
            Label_0080:
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            break Label_0080;
                        }
                        continue;
                    }
                    case 0: {
                        break Label_0080;
                    }
                    case 9: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 9);
                        if (this.gaiaGroupId == null) {
                            i = 0;
                        }
                        else {
                            i = this.gaiaGroupId.length;
                        }
                        gaiaGroupId = new long[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.gaiaGroupId, 0, gaiaGroupId, 0, i);
                        }
                        while (i < -1 + gaiaGroupId.length) {
                            gaiaGroupId[i] = codedInputByteBufferNano.readFixed64();
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        gaiaGroupId[i] = codedInputByteBufferNano.readFixed64();
                        this.gaiaGroupId = gaiaGroupId;
                        continue;
                    }
                    case 10: {
                        rawVarint32 = codedInputByteBufferNano.readRawVarint32();
                        pushLimit = codedInputByteBufferNano.pushLimit(rawVarint32);
                        n = rawVarint32 / 8;
                        if (this.gaiaGroupId == null) {
                            j = 0;
                        }
                        else {
                            j = this.gaiaGroupId.length;
                        }
                        gaiaGroupId2 = new long[j + n];
                        if (j != 0) {
                            System.arraycopy(this.gaiaGroupId, 0, gaiaGroupId2, 0, j);
                        }
                        while (j < gaiaGroupId2.length) {
                            gaiaGroupId2[j] = codedInputByteBufferNano.readFixed64();
                            ++j;
                        }
                        this.gaiaGroupId = gaiaGroupId2;
                        codedInputByteBufferNano.popLimit(pushLimit);
                        continue;
                    }
                    case 18: {
                        this.groupLicenseCheckoutOrderId = codedInputByteBufferNano.readString();
                        this.hasGroupLicenseCheckoutOrderId = true;
                        continue;
                    }
                    case 26: {
                        if (this.groupLicenseKey == null) {
                            this.groupLicenseKey = new GroupLicenseKey();
                        }
                        codedInputByteBufferNano.readMessage(this.groupLicenseKey);
                        continue;
                    }
                    case 33: {
                        this.assignedByGaiaId = codedInputByteBufferNano.readFixed64();
                        this.hasAssignedByGaiaId = true;
                        continue;
                    }
                    case 42: {
                        this.assignmentId = codedInputByteBufferNano.readString();
                        this.hasAssignmentId = true;
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.gaiaGroupId != null && this.gaiaGroupId.length > 0) {
                for (int i = 0; i < this.gaiaGroupId.length; ++i) {
                    codedOutputByteBufferNano.writeFixed64(1, this.gaiaGroupId[i]);
                }
            }
            if (this.hasGroupLicenseCheckoutOrderId || !this.groupLicenseCheckoutOrderId.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.groupLicenseCheckoutOrderId);
            }
            if (this.groupLicenseKey != null) {
                codedOutputByteBufferNano.writeMessage(3, this.groupLicenseKey);
            }
            if (this.hasAssignedByGaiaId || this.assignedByGaiaId != 0L) {
                codedOutputByteBufferNano.writeFixed64(4, this.assignedByGaiaId);
            }
            if (this.hasAssignmentId || !this.assignmentId.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.assignmentId);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class MonthAndDay extends MessageNano
    {
        public int day;
        public boolean hasDay;
        public boolean hasMonth;
        public int month;

        public MonthAndDay() {
            super();
            this.clear();
        }

        public MonthAndDay clear() {
            this.month = 0;
            this.hasMonth = false;
            this.day = 0;
            this.hasDay = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasMonth || this.month != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt32Size(1, this.month);
            }
            if (this.hasDay || this.day != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt32Size(2, this.day);
            }
            return computeSerializedSize;
        }

        @Override
        public MonthAndDay mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                    case 8: {
                        this.month = codedInputByteBufferNano.readUInt32();
                        this.hasMonth = true;
                        continue;
                    }
                    case 16: {
                        this.day = codedInputByteBufferNano.readUInt32();
                        this.hasDay = true;
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasMonth || this.month != 0) {
                codedOutputByteBufferNano.writeUInt32(1, this.month);
            }
            if (this.hasDay || this.day != 0) {
                codedOutputByteBufferNano.writeUInt32(2, this.day);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class Offer extends MessageNano
    {
        private static volatile Offer[] _emptyArray;
        public boolean checkoutFlowRequired;
        public Offer[] convertedPrice;
        public String currencyCode;
        public String formattedAmount;
        public String formattedDescription;
        public String formattedFullAmount;
        public String formattedName;
        public long fullPriceMicros;
        public boolean hasCheckoutFlowRequired;
        public boolean hasCurrencyCode;
        public boolean hasFormattedAmount;
        public boolean hasFormattedDescription;
        public boolean hasFormattedFullAmount;
        public boolean hasFormattedName;
        public boolean hasFullPriceMicros;
        public boolean hasLicensedOfferType;
        public boolean hasMicros;
        public boolean hasOfferId;
        public boolean hasOfferType;
        public boolean hasOnSaleDate;
        public boolean hasOnSaleDateDisplayTimeZoneOffsetMsec;
        public boolean hasPreorder;
        public boolean hasPreorderFulfillmentDisplayDate;
        public int licensedOfferType;
        public long micros;
        public String offerId;
        public int offerType;
        public long onSaleDate;
        public int onSaleDateDisplayTimeZoneOffsetMsec;
        public boolean preorder;
        public long preorderFulfillmentDisplayDate;
        public String[] promotionLabel;
        public RentalTerms rentalTerms;
        public SubscriptionContentTerms subscriptionContentTerms;
        public SubscriptionTerms subscriptionTerms;

        public Offer() {
            super();
            this.clear();
        }

        public static Offer[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Offer[0];
                    }
                }
            }
            return _emptyArray;
        }

        public Offer clear() {
            this.micros = 0L;
            this.hasMicros = false;
            this.currencyCode = "";
            this.hasCurrencyCode = false;
            this.formattedAmount = "";
            this.hasFormattedAmount = false;
            this.formattedName = "";
            this.hasFormattedName = false;
            this.formattedDescription = "";
            this.hasFormattedDescription = false;
            this.fullPriceMicros = 0L;
            this.hasFullPriceMicros = false;
            this.formattedFullAmount = "";
            this.hasFormattedFullAmount = false;
            this.convertedPrice = emptyArray();
            this.checkoutFlowRequired = false;
            this.hasCheckoutFlowRequired = false;
            this.offerType = 1;
            this.hasOfferType = false;
            this.licensedOfferType = 1;
            this.hasLicensedOfferType = false;
            this.rentalTerms = null;
            this.subscriptionTerms = null;
            this.subscriptionContentTerms = null;
            this.preorder = false;
            this.hasPreorder = false;
            this.preorderFulfillmentDisplayDate = 0L;
            this.hasPreorderFulfillmentDisplayDate = false;
            this.onSaleDate = 0L;
            this.hasOnSaleDate = false;
            this.onSaleDateDisplayTimeZoneOffsetMsec = 0;
            this.hasOnSaleDateDisplayTimeZoneOffsetMsec = false;
            this.promotionLabel = WireFormatNano.EMPTY_STRING_ARRAY;
            this.offerId = "";
            this.hasOfferId = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            Offer offer;
            int n;
            int n2;
            String s;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasMicros || this.micros != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(1, this.micros);
            }
            if (this.hasCurrencyCode || !this.currencyCode.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.currencyCode);
            }
            if (this.hasFormattedAmount || !this.formattedAmount.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.formattedAmount);
            }
            if (this.convertedPrice != null && this.convertedPrice.length > 0) {
                for (int i = 0; i < this.convertedPrice.length; ++i) {
                    offer = this.convertedPrice[i];
                    if (offer != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, offer);
                    }
                }
            }
            if (this.hasCheckoutFlowRequired || this.checkoutFlowRequired) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(5, this.checkoutFlowRequired);
            }
            if (this.hasFullPriceMicros || this.fullPriceMicros != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(6, this.fullPriceMicros);
            }
            if (this.hasFormattedFullAmount || !this.formattedFullAmount.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(7, this.formattedFullAmount);
            }
            if (this.offerType != 1 || this.hasOfferType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(8, this.offerType);
            }
            if (this.rentalTerms != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(9, this.rentalTerms);
            }
            if (this.hasOnSaleDate || this.onSaleDate != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(10, this.onSaleDate);
            }
            if (this.promotionLabel != null && this.promotionLabel.length > 0) {
                n = 0;
                n2 = 0;
                for (int j = 0; j < this.promotionLabel.length; ++j) {
                    s = this.promotionLabel[j];
                    if (s != null) {
                        ++n;
                        n2 += CodedOutputByteBufferNano.computeStringSizeNoTag(s);
                    }
                }
                computeSerializedSize = computeSerializedSize + n2 + n * 1;
            }
            if (this.subscriptionTerms != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(12, this.subscriptionTerms);
            }
            if (this.hasFormattedName || !this.formattedName.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(13, this.formattedName);
            }
            if (this.hasFormattedDescription || !this.formattedDescription.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(14, this.formattedDescription);
            }
            if (this.hasPreorder || this.preorder) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(15, this.preorder);
            }
            if (this.hasOnSaleDateDisplayTimeZoneOffsetMsec || this.onSaleDateDisplayTimeZoneOffsetMsec != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(16, this.onSaleDateDisplayTimeZoneOffsetMsec);
            }
            if (this.licensedOfferType != 1 || this.hasLicensedOfferType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(17, this.licensedOfferType);
            }
            if (this.subscriptionContentTerms != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(18, this.subscriptionContentTerms);
            }
            if (this.hasOfferId || !this.offerId.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(19, this.offerId);
            }
            if (this.hasPreorderFulfillmentDisplayDate || this.preorderFulfillmentDisplayDate != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(20, this.preorderFulfillmentDisplayDate);
            }
            return computeSerializedSize;
        }

        @Override
        public Offer mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            Offer[] convertedPrice;
            int int32;
            int repeatedFieldArrayLength2;
            int j;
            String[] promotionLabel;
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
                    case 8: {
                        this.micros = codedInputByteBufferNano.readInt64();
                        this.hasMicros = true;
                        continue;
                    }
                    case 18: {
                        this.currencyCode = codedInputByteBufferNano.readString();
                        this.hasCurrencyCode = true;
                        continue;
                    }
                    case 26: {
                        this.formattedAmount = codedInputByteBufferNano.readString();
                        this.hasFormattedAmount = true;
                        continue;
                    }
                    case 34: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 34);
                        if (this.convertedPrice == null) {
                            i = 0;
                        }
                        else {
                            i = this.convertedPrice.length;
                        }
                        convertedPrice = new Offer[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.convertedPrice, 0, convertedPrice, 0, i);
                        }
                        while (i < -1 + convertedPrice.length) {
                            codedInputByteBufferNano.readMessage(convertedPrice[i] = new Offer());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(convertedPrice[i] = new Offer());
                        this.convertedPrice = convertedPrice;
                        continue;
                    }
                    case 40: {
                        this.checkoutFlowRequired = codedInputByteBufferNano.readBool();
                        this.hasCheckoutFlowRequired = true;
                        continue;
                    }
                    case 48: {
                        this.fullPriceMicros = codedInputByteBufferNano.readInt64();
                        this.hasFullPriceMicros = true;
                        continue;
                    }
                    case 58: {
                        this.formattedFullAmount = codedInputByteBufferNano.readString();
                        this.hasFormattedFullAmount = true;
                        continue;
                    }
                    case 64: {
                        int32 = codedInputByteBufferNano.readInt32();
                        switch (int32) {
                            default: {
                                break;
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
                                this.offerType = int32;
                                this.hasOfferType = true;
                                continue;
                            }
                        }
                        break;
                    }
                    case 74: {
                        if (this.rentalTerms == null) {
                            this.rentalTerms = new RentalTerms();
                        }
                        codedInputByteBufferNano.readMessage(this.rentalTerms);
                        continue;
                    }
                    case 80: {
                        this.onSaleDate = codedInputByteBufferNano.readInt64();
                        this.hasOnSaleDate = true;
                        continue;
                    }
                    case 90: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 90);
                        if (this.promotionLabel == null) {
                            j = 0;
                        }
                        else {
                            j = this.promotionLabel.length;
                        }
                        promotionLabel = new String[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.promotionLabel, 0, promotionLabel, 0, j);
                        }
                        while (j < -1 + promotionLabel.length) {
                            promotionLabel[j] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        promotionLabel[j] = codedInputByteBufferNano.readString();
                        this.promotionLabel = promotionLabel;
                        continue;
                    }
                    case 98: {
                        if (this.subscriptionTerms == null) {
                            this.subscriptionTerms = new SubscriptionTerms();
                        }
                        codedInputByteBufferNano.readMessage(this.subscriptionTerms);
                        continue;
                    }
                    case 106: {
                        this.formattedName = codedInputByteBufferNano.readString();
                        this.hasFormattedName = true;
                        continue;
                    }
                    case 114: {
                        this.formattedDescription = codedInputByteBufferNano.readString();
                        this.hasFormattedDescription = true;
                        continue;
                    }
                    case 120: {
                        this.preorder = codedInputByteBufferNano.readBool();
                        this.hasPreorder = true;
                        continue;
                    }
                    case 128: {
                        this.onSaleDateDisplayTimeZoneOffsetMsec = codedInputByteBufferNano.readInt32();
                        this.hasOnSaleDateDisplayTimeZoneOffsetMsec = true;
                        continue;
                    }
                    case 136: {
                        int2 = codedInputByteBufferNano.readInt32();
                        switch (int2) {
                            default: {
                                break;
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
                                this.licensedOfferType = int2;
                                this.hasLicensedOfferType = true;
                                continue;
                            }
                        }
                        break;
                    }
                    case 146: {
                        if (this.subscriptionContentTerms == null) {
                            this.subscriptionContentTerms = new SubscriptionContentTerms();
                        }
                        codedInputByteBufferNano.readMessage(this.subscriptionContentTerms);
                        continue;
                    }
                    case 154: {
                        this.offerId = codedInputByteBufferNano.readString();
                        this.hasOfferId = true;
                        continue;
                    }
                    case 160: {
                        this.preorderFulfillmentDisplayDate = codedInputByteBufferNano.readInt64();
                        this.hasPreorderFulfillmentDisplayDate = true;
                        continue;
                    }
                }
            }
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            Offer offer;
            String s;
            if (this.hasMicros || this.micros != 0L) {
                codedOutputByteBufferNano.writeInt64(1, this.micros);
            }
            if (this.hasCurrencyCode || !this.currencyCode.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.currencyCode);
            }
            if (this.hasFormattedAmount || !this.formattedAmount.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.formattedAmount);
            }
            if (this.convertedPrice != null && this.convertedPrice.length > 0) {
                for (int i = 0; i < this.convertedPrice.length; ++i) {
                    offer = this.convertedPrice[i];
                    if (offer != null) {
                        codedOutputByteBufferNano.writeMessage(4, offer);
                    }
                }
            }
            if (this.hasCheckoutFlowRequired || this.checkoutFlowRequired) {
                codedOutputByteBufferNano.writeBool(5, this.checkoutFlowRequired);
            }
            if (this.hasFullPriceMicros || this.fullPriceMicros != 0L) {
                codedOutputByteBufferNano.writeInt64(6, this.fullPriceMicros);
            }
            if (this.hasFormattedFullAmount || !this.formattedFullAmount.equals("")) {
                codedOutputByteBufferNano.writeString(7, this.formattedFullAmount);
            }
            if (this.offerType != 1 || this.hasOfferType) {
                codedOutputByteBufferNano.writeInt32(8, this.offerType);
            }
            if (this.rentalTerms != null) {
                codedOutputByteBufferNano.writeMessage(9, this.rentalTerms);
            }
            if (this.hasOnSaleDate || this.onSaleDate != 0L) {
                codedOutputByteBufferNano.writeInt64(10, this.onSaleDate);
            }
            if (this.promotionLabel != null && this.promotionLabel.length > 0) {
                for (int j = 0; j < this.promotionLabel.length; ++j) {
                    s = this.promotionLabel[j];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(11, s);
                    }
                }
            }
            if (this.subscriptionTerms != null) {
                codedOutputByteBufferNano.writeMessage(12, this.subscriptionTerms);
            }
            if (this.hasFormattedName || !this.formattedName.equals("")) {
                codedOutputByteBufferNano.writeString(13, this.formattedName);
            }
            if (this.hasFormattedDescription || !this.formattedDescription.equals("")) {
                codedOutputByteBufferNano.writeString(14, this.formattedDescription);
            }
            if (this.hasPreorder || this.preorder) {
                codedOutputByteBufferNano.writeBool(15, this.preorder);
            }
            if (this.hasOnSaleDateDisplayTimeZoneOffsetMsec || this.onSaleDateDisplayTimeZoneOffsetMsec != 0) {
                codedOutputByteBufferNano.writeInt32(16, this.onSaleDateDisplayTimeZoneOffsetMsec);
            }
            if (this.licensedOfferType != 1 || this.hasLicensedOfferType) {
                codedOutputByteBufferNano.writeInt32(17, this.licensedOfferType);
            }
            if (this.subscriptionContentTerms != null) {
                codedOutputByteBufferNano.writeMessage(18, this.subscriptionContentTerms);
            }
            if (this.hasOfferId || !this.offerId.equals("")) {
                codedOutputByteBufferNano.writeString(19, this.offerId);
            }
            if (this.hasPreorderFulfillmentDisplayDate || this.preorderFulfillmentDisplayDate != 0L) {
                codedOutputByteBufferNano.writeInt64(20, this.preorderFulfillmentDisplayDate);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class RentalTerms extends MessageNano
    {
        public TimePeriod activatePeriod;
        public int dEPRECATEDActivatePeriodSeconds;
        public int dEPRECATEDGrantPeriodSeconds;
        public long grantEndTimeSeconds;
        public TimePeriod grantPeriod;
        public boolean hasDEPRECATEDActivatePeriodSeconds;
        public boolean hasDEPRECATEDGrantPeriodSeconds;
        public boolean hasGrantEndTimeSeconds;

        public RentalTerms() {
            super();
            this.clear();
        }

        public RentalTerms clear() {
            this.grantPeriod = null;
            this.activatePeriod = null;
            this.grantEndTimeSeconds = 0L;
            this.hasGrantEndTimeSeconds = false;
            this.dEPRECATEDGrantPeriodSeconds = 0;
            this.hasDEPRECATEDGrantPeriodSeconds = false;
            this.dEPRECATEDActivatePeriodSeconds = 0;
            this.hasDEPRECATEDActivatePeriodSeconds = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasDEPRECATEDGrantPeriodSeconds || this.dEPRECATEDGrantPeriodSeconds != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.dEPRECATEDGrantPeriodSeconds);
            }
            if (this.hasDEPRECATEDActivatePeriodSeconds || this.dEPRECATEDActivatePeriodSeconds != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.dEPRECATEDActivatePeriodSeconds);
            }
            if (this.grantPeriod != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, this.grantPeriod);
            }
            if (this.activatePeriod != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, this.activatePeriod);
            }
            if (this.hasGrantEndTimeSeconds || this.grantEndTimeSeconds != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(5, this.grantEndTimeSeconds);
            }
            return computeSerializedSize;
        }

        @Override
        public RentalTerms mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            Label_0072:
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            break Label_0072;
                        }
                        continue;
                    }
                    case 0: {
                        break Label_0072;
                    }
                    case 8: {
                        this.dEPRECATEDGrantPeriodSeconds = codedInputByteBufferNano.readInt32();
                        this.hasDEPRECATEDGrantPeriodSeconds = true;
                        continue;
                    }
                    case 16: {
                        this.dEPRECATEDActivatePeriodSeconds = codedInputByteBufferNano.readInt32();
                        this.hasDEPRECATEDActivatePeriodSeconds = true;
                        continue;
                    }
                    case 26: {
                        if (this.grantPeriod == null) {
                            this.grantPeriod = new TimePeriod();
                        }
                        codedInputByteBufferNano.readMessage(this.grantPeriod);
                        continue;
                    }
                    case 34: {
                        if (this.activatePeriod == null) {
                            this.activatePeriod = new TimePeriod();
                        }
                        codedInputByteBufferNano.readMessage(this.activatePeriod);
                        continue;
                    }
                    case 40: {
                        this.grantEndTimeSeconds = codedInputByteBufferNano.readInt64();
                        this.hasGrantEndTimeSeconds = true;
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasDEPRECATEDGrantPeriodSeconds || this.dEPRECATEDGrantPeriodSeconds != 0) {
                codedOutputByteBufferNano.writeInt32(1, this.dEPRECATEDGrantPeriodSeconds);
            }
            if (this.hasDEPRECATEDActivatePeriodSeconds || this.dEPRECATEDActivatePeriodSeconds != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.dEPRECATEDActivatePeriodSeconds);
            }
            if (this.grantPeriod != null) {
                codedOutputByteBufferNano.writeMessage(3, this.grantPeriod);
            }
            if (this.activatePeriod != null) {
                codedOutputByteBufferNano.writeMessage(4, this.activatePeriod);
            }
            if (this.hasGrantEndTimeSeconds || this.grantEndTimeSeconds != 0L) {
                codedOutputByteBufferNano.writeInt64(5, this.grantEndTimeSeconds);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class SeasonalSubscriptionInfo extends MessageNano
    {
        public MonthAndDay periodEnd;
        public MonthAndDay periodStart;

        public SeasonalSubscriptionInfo() {
            super();
            this.clear();
        }

        public SeasonalSubscriptionInfo clear() {
            this.periodStart = null;
            this.periodEnd = null;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.periodStart != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.periodStart);
            }
            if (this.periodEnd != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.periodEnd);
            }
            return computeSerializedSize;
        }

        @Override
        public SeasonalSubscriptionInfo mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        if (this.periodStart == null) {
                            this.periodStart = new MonthAndDay();
                        }
                        codedInputByteBufferNano.readMessage(this.periodStart);
                        continue;
                    }
                    case 18: {
                        if (this.periodEnd == null) {
                            this.periodEnd = new MonthAndDay();
                        }
                        codedInputByteBufferNano.readMessage(this.periodEnd);
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.periodStart != null) {
                codedOutputByteBufferNano.writeMessage(1, this.periodStart);
            }
            if (this.periodEnd != null) {
                codedOutputByteBufferNano.writeMessage(2, this.periodEnd);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class SignedData extends MessageNano
    {
        public boolean hasSignature;
        public boolean hasSignedData;
        public String signature;
        public String signedData;

        public SignedData() {
            super();
            this.clear();
        }

        public SignedData clear() {
            this.signedData = "";
            this.hasSignedData = false;
            this.signature = "";
            this.hasSignature = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasSignedData || !this.signedData.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.signedData);
            }
            if (this.hasSignature || !this.signature.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.signature);
            }
            return computeSerializedSize;
        }

        @Override
        public SignedData mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.signedData = codedInputByteBufferNano.readString();
                        this.hasSignedData = true;
                        continue;
                    }
                    case 18: {
                        this.signature = codedInputByteBufferNano.readString();
                        this.hasSignature = true;
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasSignedData || !this.signedData.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.signedData);
            }
            if (this.hasSignature || !this.signature.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.signature);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class SubscriptionContentTerms extends MessageNano
    {
        public Docid requiredSubscription;

        public SubscriptionContentTerms() {
            super();
            this.clear();
        }

        public SubscriptionContentTerms clear() {
            this.requiredSubscription = null;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.requiredSubscription != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.requiredSubscription);
            }
            return computeSerializedSize;
        }

        @Override
        public SubscriptionContentTerms mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            Label_0040:
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            break Label_0040;
                        }
                        continue;
                    }
                    case 0: {
                        break Label_0040;
                    }
                    case 10: {
                        if (this.requiredSubscription == null) {
                            this.requiredSubscription = new Docid();
                        }
                        codedInputByteBufferNano.readMessage(this.requiredSubscription);
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.requiredSubscription != null) {
                codedOutputByteBufferNano.writeMessage(1, this.requiredSubscription);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class SubscriptionTerms extends MessageNano
    {
        public String formattedPriceWithRecurrencePeriod;
        public boolean hasFormattedPriceWithRecurrencePeriod;
        public TimePeriod recurringPeriod;
        public Docid[] replaceDocid;
        public SeasonalSubscriptionInfo seasonalSubscriptionInfo;
        public TimePeriod trialPeriod;

        public SubscriptionTerms() {
            super();
            this.clear();
        }

        public SubscriptionTerms clear() {
            this.recurringPeriod = null;
            this.trialPeriod = null;
            this.formattedPriceWithRecurrencePeriod = "";
            this.hasFormattedPriceWithRecurrencePeriod = false;
            this.seasonalSubscriptionInfo = null;
            this.replaceDocid = Docid.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            Docid docid;
            computeSerializedSize = super.computeSerializedSize();
            if (this.recurringPeriod != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.recurringPeriod);
            }
            if (this.trialPeriod != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.trialPeriod);
            }
            if (this.hasFormattedPriceWithRecurrencePeriod || !this.formattedPriceWithRecurrencePeriod.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.formattedPriceWithRecurrencePeriod);
            }
            if (this.seasonalSubscriptionInfo != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, this.seasonalSubscriptionInfo);
            }
            if (this.replaceDocid != null && this.replaceDocid.length > 0) {
                for (int i = 0; i < this.replaceDocid.length; ++i) {
                    docid = this.replaceDocid[i];
                    if (docid != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(5, docid);
                    }
                }
            }
            return computeSerializedSize;
        }

        @Override
        public SubscriptionTerms mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            Docid[] replaceDocid;
            Label_0072:
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            break Label_0072;
                        }
                        continue;
                    }
                    case 0: {
                        break Label_0072;
                    }
                    case 10: {
                        if (this.recurringPeriod == null) {
                            this.recurringPeriod = new TimePeriod();
                        }
                        codedInputByteBufferNano.readMessage(this.recurringPeriod);
                        continue;
                    }
                    case 18: {
                        if (this.trialPeriod == null) {
                            this.trialPeriod = new TimePeriod();
                        }
                        codedInputByteBufferNano.readMessage(this.trialPeriod);
                        continue;
                    }
                    case 26: {
                        this.formattedPriceWithRecurrencePeriod = codedInputByteBufferNano.readString();
                        this.hasFormattedPriceWithRecurrencePeriod = true;
                        continue;
                    }
                    case 34: {
                        if (this.seasonalSubscriptionInfo == null) {
                            this.seasonalSubscriptionInfo = new SeasonalSubscriptionInfo();
                        }
                        codedInputByteBufferNano.readMessage(this.seasonalSubscriptionInfo);
                        continue;
                    }
                    case 42: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 42);
                        if (this.replaceDocid == null) {
                            i = 0;
                        }
                        else {
                            i = this.replaceDocid.length;
                        }
                        replaceDocid = new Docid[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.replaceDocid, 0, replaceDocid, 0, i);
                        }
                        while (i < -1 + replaceDocid.length) {
                            codedInputByteBufferNano.readMessage(replaceDocid[i] = new Docid());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(replaceDocid[i] = new Docid());
                        this.replaceDocid = replaceDocid;
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            Docid docid;
            if (this.recurringPeriod != null) {
                codedOutputByteBufferNano.writeMessage(1, this.recurringPeriod);
            }
            if (this.trialPeriod != null) {
                codedOutputByteBufferNano.writeMessage(2, this.trialPeriod);
            }
            if (this.hasFormattedPriceWithRecurrencePeriod || !this.formattedPriceWithRecurrencePeriod.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.formattedPriceWithRecurrencePeriod);
            }
            if (this.seasonalSubscriptionInfo != null) {
                codedOutputByteBufferNano.writeMessage(4, this.seasonalSubscriptionInfo);
            }
            if (this.replaceDocid != null && this.replaceDocid.length > 0) {
                for (int i = 0; i < this.replaceDocid.length; ++i) {
                    docid = this.replaceDocid[i];
                    if (docid != null) {
                        codedOutputByteBufferNano.writeMessage(5, docid);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class TimePeriod extends MessageNano
    {
        public int count;
        public boolean hasCount;
        public boolean hasUnit;
        public int unit;

        public TimePeriod() {
            super();
            this.clear();
        }

        public TimePeriod clear() {
            this.unit = 0;
            this.hasUnit = false;
            this.count = 0;
            this.hasCount = false;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.unit != 0 || this.hasUnit) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.unit);
            }
            if (this.hasCount || this.count != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.count);
            }
            return computeSerializedSize;
        }

        @Override
        public TimePeriod mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
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
                    case 8: {
                        int32 = codedInputByteBufferNano.readInt32();
                        switch (int32) {
                            default: {
                                break;
                            }
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                            case 6:
                            case 7: {
                                this.unit = int32;
                                this.hasUnit = true;
                                continue;
                            }
                        }
                        break;
                    }
                    case 16: {
                        this.count = codedInputByteBufferNano.readInt32();
                        this.hasCount = true;
                        continue;
                    }
                }
            }
            return this;
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.unit != 0 || this.hasUnit) {
                codedOutputByteBufferNano.writeInt32(1, this.unit);
            }
            if (this.hasCount || this.count != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.count);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
