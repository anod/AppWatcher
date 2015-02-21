package com.google.android.finsky.protos;

import com.google.protobuf.nano.*;
import java.io.*;

public interface DocAnnotations
{
    public static final class Badge extends MessageNano
    {
        private static volatile Badge[] _emptyArray;
        public String browseUrl;
        public String description;
        public boolean hasBrowseUrl;
        public boolean hasDescription;
        public boolean hasTitle;
        public Common.Image[] image;
        public String title;
        
        public Badge() {
            super();
            this.clear();
        }
        
        public static Badge[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Badge[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public Badge clear() {
            this.title = "";
            this.hasTitle = false;
            this.description = "";
            this.hasDescription = false;
            this.image = Common.Image.emptyArray();
            this.browseUrl = "";
            this.hasBrowseUrl = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            Common.Image image;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.title);
            }
            if (this.image != null && this.image.length > 0) {
                for (int i = 0; i < this.image.length; ++i) {
                    image = this.image[i];
                    if (image != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, image);
                    }
                }
            }
            if (this.hasBrowseUrl || !this.browseUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.browseUrl);
            }
            if (this.hasDescription || !this.description.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.description);
            }
            return computeSerializedSize;
        }
        
        @Override
        public Badge mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            Common.Image[] image;
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
                        this.title = codedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        continue;
                    }
                    case 18: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 18);
                        if (this.image == null) {
                            i = 0;
                        }
                        else {
                            i = this.image.length;
                        }
                        image = new Common.Image[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.image, 0, image, 0, i);
                        }
                        while (i < -1 + image.length) {
                            codedInputByteBufferNano.readMessage(image[i] = new Common.Image());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(image[i] = new Common.Image());
                        this.image = image;
                        continue;
                    }
                    case 26: {
                        this.browseUrl = codedInputByteBufferNano.readString();
                        this.hasBrowseUrl = true;
                        continue;
                    }
                    case 34: {
                        this.description = codedInputByteBufferNano.readString();
                        this.hasDescription = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            Common.Image image;
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.title);
            }
            if (this.image != null && this.image.length > 0) {
                for (int i = 0; i < this.image.length; ++i) {
                    image = this.image[i];
                    if (image != null) {
                        codedOutputByteBufferNano.writeMessage(2, image);
                    }
                }
            }
            if (this.hasBrowseUrl || !this.browseUrl.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.browseUrl);
            }
            if (this.hasDescription || !this.description.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.description);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class BadgeContainer extends MessageNano
    {
        private static volatile BadgeContainer[] _emptyArray;
        public Badge[] badge;
        public boolean hasTitle;
        public Common.Image[] image;
        public String title;
        
        public BadgeContainer() {
            super();
            this.clear();
        }
        
        public static BadgeContainer[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new BadgeContainer[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public BadgeContainer clear() {
            this.title = "";
            this.hasTitle = false;
            this.image = Common.Image.emptyArray();
            this.badge = Badge.emptyArray();
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            Common.Image image;
            Badge badge;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.title);
            }
            if (this.image != null && this.image.length > 0) {
                for (int i = 0; i < this.image.length; ++i) {
                    image = this.image[i];
                    if (image != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, image);
                    }
                }
            }
            if (this.badge != null && this.badge.length > 0) {
                for (int j = 0; j < this.badge.length; ++j) {
                    badge = this.badge[j];
                    if (badge != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, badge);
                    }
                }
            }
            return computeSerializedSize;
        }
        
        @Override
        public BadgeContainer mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            Common.Image[] image;
            int repeatedFieldArrayLength2;
            int j;
            Badge[] badge;
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
                        this.title = codedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        continue;
                    }
                    case 18: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 18);
                        if (this.image == null) {
                            i = 0;
                        }
                        else {
                            i = this.image.length;
                        }
                        image = new Common.Image[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.image, 0, image, 0, i);
                        }
                        while (i < -1 + image.length) {
                            codedInputByteBufferNano.readMessage(image[i] = new Common.Image());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(image[i] = new Common.Image());
                        this.image = image;
                        continue;
                    }
                    case 26: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 26);
                        if (this.badge == null) {
                            j = 0;
                        }
                        else {
                            j = this.badge.length;
                        }
                        badge = new Badge[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.badge, 0, badge, 0, j);
                        }
                        while (j < -1 + badge.length) {
                            codedInputByteBufferNano.readMessage(badge[j] = new Badge());
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        codedInputByteBufferNano.readMessage(badge[j] = new Badge());
                        this.badge = badge;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            Common.Image image;
            Badge badge;
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.title);
            }
            if (this.image != null && this.image.length > 0) {
                for (int i = 0; i < this.image.length; ++i) {
                    image = this.image[i];
                    if (image != null) {
                        codedOutputByteBufferNano.writeMessage(2, image);
                    }
                }
            }
            if (this.badge != null && this.badge.length > 0) {
                for (int j = 0; j < this.badge.length; ++j) {
                    badge = this.badge[j];
                    if (badge != null) {
                        codedOutputByteBufferNano.writeMessage(3, badge);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class Link extends MessageNano
    {
        private static volatile Link[] _emptyArray;
        public boolean hasUri;
        public boolean hasUriBackend;
        public ResolveLink.ResolvedLink resolvedLink;
        public String uri;
        public int uriBackend;
        
        public Link() {
            super();
            this.clear();
        }
        
        public static Link[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Link[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public Link clear() {
            this.uri = "";
            this.hasUri = false;
            this.uriBackend = 0;
            this.hasUriBackend = false;
            this.resolvedLink = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasUri || !this.uri.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.uri);
            }
            if (this.resolvedLink != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.resolvedLink);
            }
            if (this.uriBackend != 0 || this.hasUriBackend) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.uriBackend);
            }
            return computeSerializedSize;
        }
        
        @Override
        public Link mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                    case 10: {
                        this.uri = codedInputByteBufferNano.readString();
                        this.hasUri = true;
                        continue;
                    }
                    case 18: {
                        if (this.resolvedLink == null) {
                            this.resolvedLink = new ResolveLink.ResolvedLink();
                        }
                        codedInputByteBufferNano.readMessage(this.resolvedLink);
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
                                this.uriBackend = int32;
                                this.hasUriBackend = true;
                                continue;
                            }
                        }
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasUri || !this.uri.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.uri);
            }
            if (this.resolvedLink != null) {
                codedOutputByteBufferNano.writeMessage(2, this.resolvedLink);
            }
            if (this.uriBackend != 0 || this.hasUriBackend) {
                codedOutputByteBufferNano.writeInt32(3, this.uriBackend);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class PromotedDoc extends MessageNano
    {
        private static volatile PromotedDoc[] _emptyArray;
        public String descriptionHtml;
        public String detailsUrl;
        public boolean hasDescriptionHtml;
        public boolean hasDetailsUrl;
        public boolean hasSubtitle;
        public boolean hasTitle;
        public Common.Image[] image;
        public String subtitle;
        public String title;
        
        public PromotedDoc() {
            super();
            this.clear();
        }
        
        public static PromotedDoc[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new PromotedDoc[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public PromotedDoc clear() {
            this.title = "";
            this.hasTitle = false;
            this.subtitle = "";
            this.hasSubtitle = false;
            this.image = Common.Image.emptyArray();
            this.descriptionHtml = "";
            this.hasDescriptionHtml = false;
            this.detailsUrl = "";
            this.hasDetailsUrl = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            Common.Image image;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.title);
            }
            if (this.hasSubtitle || !this.subtitle.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.subtitle);
            }
            if (this.image != null && this.image.length > 0) {
                for (int i = 0; i < this.image.length; ++i) {
                    image = this.image[i];
                    if (image != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, image);
                    }
                }
            }
            if (this.hasDescriptionHtml || !this.descriptionHtml.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.descriptionHtml);
            }
            if (this.hasDetailsUrl || !this.detailsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.detailsUrl);
            }
            return computeSerializedSize;
        }
        
        @Override
        public PromotedDoc mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            Common.Image[] image;
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
                        this.title = codedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        continue;
                    }
                    case 18: {
                        this.subtitle = codedInputByteBufferNano.readString();
                        this.hasSubtitle = true;
                        continue;
                    }
                    case 26: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 26);
                        if (this.image == null) {
                            i = 0;
                        }
                        else {
                            i = this.image.length;
                        }
                        image = new Common.Image[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.image, 0, image, 0, i);
                        }
                        while (i < -1 + image.length) {
                            codedInputByteBufferNano.readMessage(image[i] = new Common.Image());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(image[i] = new Common.Image());
                        this.image = image;
                        continue;
                    }
                    case 34: {
                        this.descriptionHtml = codedInputByteBufferNano.readString();
                        this.hasDescriptionHtml = true;
                        continue;
                    }
                    case 42: {
                        this.detailsUrl = codedInputByteBufferNano.readString();
                        this.hasDetailsUrl = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            Common.Image image;
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.title);
            }
            if (this.hasSubtitle || !this.subtitle.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.subtitle);
            }
            if (this.image != null && this.image.length > 0) {
                for (int i = 0; i < this.image.length; ++i) {
                    image = this.image[i];
                    if (image != null) {
                        codedOutputByteBufferNano.writeMessage(3, image);
                    }
                }
            }
            if (this.hasDescriptionHtml || !this.descriptionHtml.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.descriptionHtml);
            }
            if (this.hasDetailsUrl || !this.detailsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.detailsUrl);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class SectionMetadata extends MessageNano
    {
        public String browseUrl;
        public String descriptionHtml;
        public boolean hasBrowseUrl;
        public boolean hasDescriptionHtml;
        public boolean hasHeader;
        public boolean hasListUrl;
        public String header;
        public String listUrl;
        
        public SectionMetadata() {
            super();
            this.clear();
        }
        
        public SectionMetadata clear() {
            this.header = "";
            this.hasHeader = false;
            this.descriptionHtml = "";
            this.hasDescriptionHtml = false;
            this.listUrl = "";
            this.hasListUrl = false;
            this.browseUrl = "";
            this.hasBrowseUrl = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasHeader || !this.header.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.header);
            }
            if (this.hasListUrl || !this.listUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.listUrl);
            }
            if (this.hasBrowseUrl || !this.browseUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.browseUrl);
            }
            if (this.hasDescriptionHtml || !this.descriptionHtml.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.descriptionHtml);
            }
            return computeSerializedSize;
        }
        
        @Override
        public SectionMetadata mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.header = codedInputByteBufferNano.readString();
                        this.hasHeader = true;
                        continue;
                    }
                    case 18: {
                        this.listUrl = codedInputByteBufferNano.readString();
                        this.hasListUrl = true;
                        continue;
                    }
                    case 26: {
                        this.browseUrl = codedInputByteBufferNano.readString();
                        this.hasBrowseUrl = true;
                        continue;
                    }
                    case 34: {
                        this.descriptionHtml = codedInputByteBufferNano.readString();
                        this.hasDescriptionHtml = true;
                        continue;
                    }
                }
            }
            return this;
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasHeader || !this.header.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.header);
            }
            if (this.hasListUrl || !this.listUrl.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.listUrl);
            }
            if (this.hasBrowseUrl || !this.browseUrl.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.browseUrl);
            }
            if (this.hasDescriptionHtml || !this.descriptionHtml.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.descriptionHtml);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class Warning extends MessageNano
    {
        private static volatile Warning[] _emptyArray;
        public boolean hasLocalizedMessage;
        public String localizedMessage;
        
        public Warning() {
            super();
            this.clear();
        }
        
        public static Warning[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Warning[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public Warning clear() {
            this.localizedMessage = "";
            this.hasLocalizedMessage = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasLocalizedMessage || !this.localizedMessage.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.localizedMessage);
            }
            return computeSerializedSize;
        }
        
        @Override
        public Warning mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.localizedMessage = codedInputByteBufferNano.readString();
                        this.hasLocalizedMessage = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasLocalizedMessage || !this.localizedMessage.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.localizedMessage);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
