package com.google.android.finsky.protos;

import java.io.*;
import com.google.protobuf.nano.*;

public interface DocDetails
{
    public static final class AlbumDetails extends MessageNano
    {
        public MusicDetails details;
        public ArtistDetails displayArtist;
        public boolean hasName;
        public String name;
        
        public AlbumDetails() {
            super();
            this.clear();
        }
        
        public AlbumDetails clear() {
            this.name = "";
            this.hasName = false;
            this.details = null;
            this.displayArtist = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasName || !this.name.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (this.details != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.details);
            }
            if (this.displayArtist != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, this.displayArtist);
            }
            return computeSerializedSize;
        }
        
        @Override
        public AlbumDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
        Label_0056:
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            return this;
                        }
                        continue;
                    }
                    case 0: {
                        return this;
                    }
                    case 10: {
                        this.name = codedInputByteBufferNano.readString();
                        this.hasName = true;
                        continue;
                    }
                    case 18: {
                        if (this.details == null) {
                            this.details = new MusicDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.details);
                        continue;
                    }
                    case 26: {
                        if (this.displayArtist == null) {
                            this.displayArtist = new ArtistDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.displayArtist);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasName || !this.name.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.name);
            }
            if (this.details != null) {
                codedOutputByteBufferNano.writeMessage(2, this.details);
            }
            if (this.displayArtist != null) {
                codedOutputByteBufferNano.writeMessage(3, this.displayArtist);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class AppDetails extends MessageNano
    {
        public String[] appCategory;
        public String appType;
        public String[] autoAcquireFreeAppIfHigherVersionAvailableTag;
        public String[] certificateHash;
        public CertificateSet[] certificateSet;
        public int contentRating;
        public boolean declaresIab;
        public String developerEmail;
        public String developerName;
        public String developerWebsite;
        public FileMetadata[] file;
        public boolean hasAppType;
        public boolean hasContentRating;
        public boolean hasDeclaresIab;
        public boolean hasDeveloperEmail;
        public boolean hasDeveloperName;
        public boolean hasDeveloperWebsite;
        public boolean hasInstallationSize;
        public boolean hasMajorVersionNumber;
        public boolean hasNumDownloads;
        public boolean hasPackageName;
        public boolean hasRecentChangesHtml;
        public boolean hasTitle;
        public boolean hasUploadDate;
        public boolean hasVariesByAccount;
        public boolean hasVersionCode;
        public boolean hasVersionString;
        public long installationSize;
        public int majorVersionNumber;
        public String numDownloads;
        public String[] oBSOLETEPermission;
        public String packageName;
        public AppPermission[] permission;
        public String recentChangesHtml;
        public String[] splitId;
        public String title;
        public String uploadDate;
        public boolean variesByAccount;
        public int versionCode;
        public String versionString;
        
        public AppDetails() {
            super();
            this.clear();
        }
        
        public AppDetails clear() {
            this.developerName = "";
            this.hasDeveloperName = false;
            this.majorVersionNumber = 0;
            this.hasMajorVersionNumber = false;
            this.versionCode = 0;
            this.hasVersionCode = false;
            this.versionString = "";
            this.hasVersionString = false;
            this.title = "";
            this.hasTitle = false;
            this.appCategory = WireFormatNano.EMPTY_STRING_ARRAY;
            this.contentRating = 0;
            this.hasContentRating = false;
            this.installationSize = 0L;
            this.hasInstallationSize = false;
            this.oBSOLETEPermission = WireFormatNano.EMPTY_STRING_ARRAY;
            this.permission = AppPermission.emptyArray();
            this.developerEmail = "";
            this.hasDeveloperEmail = false;
            this.developerWebsite = "";
            this.hasDeveloperWebsite = false;
            this.numDownloads = "";
            this.hasNumDownloads = false;
            this.packageName = "";
            this.hasPackageName = false;
            this.recentChangesHtml = "";
            this.hasRecentChangesHtml = false;
            this.uploadDate = "";
            this.hasUploadDate = false;
            this.file = FileMetadata.emptyArray();
            this.appType = "";
            this.hasAppType = false;
            this.certificateSet = CertificateSet.emptyArray();
            this.certificateHash = WireFormatNano.EMPTY_STRING_ARRAY;
            this.variesByAccount = true;
            this.hasVariesByAccount = false;
            this.autoAcquireFreeAppIfHigherVersionAvailableTag = WireFormatNano.EMPTY_STRING_ARRAY;
            this.declaresIab = false;
            this.hasDeclaresIab = false;
            this.splitId = WireFormatNano.EMPTY_STRING_ARRAY;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            int n;
            int n2;
            String s;
            int n3;
            int n4;
            String s2;
            FileMetadata fileMetadata;
            int n5;
            int n6;
            String s3;
            AppPermission appPermission;
            CertificateSet set;
            int n9;
            int n10;
            String s4;
            int n12;
            int n13;
            String s5;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasDeveloperName || !this.developerName.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.developerName);
            }
            if (this.hasMajorVersionNumber || this.majorVersionNumber != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.majorVersionNumber);
            }
            if (this.hasVersionCode || this.versionCode != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.versionCode);
            }
            if (this.hasVersionString || !this.versionString.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.versionString);
            }
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.title);
            }
            if (this.appCategory != null && this.appCategory.length > 0) {
                n = 0;
                n2 = 0;
                for (int i = 0; i < this.appCategory.length; ++i) {
                    s = this.appCategory[i];
                    if (s != null) {
                        ++n;
                        n2 += CodedOutputByteBufferNano.computeStringSizeNoTag(s);
                    }
                }
                computeSerializedSize = computeSerializedSize + n2 + n * 1;
            }
            if (this.hasContentRating || this.contentRating != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(8, this.contentRating);
            }
            if (this.hasInstallationSize || this.installationSize != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(9, this.installationSize);
            }
            if (this.oBSOLETEPermission != null && this.oBSOLETEPermission.length > 0) {
                n3 = 0;
                n4 = 0;
                for (int j = 0; j < this.oBSOLETEPermission.length; ++j) {
                    s2 = this.oBSOLETEPermission[j];
                    if (s2 != null) {
                        ++n3;
                        n4 += CodedOutputByteBufferNano.computeStringSizeNoTag(s2);
                    }
                }
                computeSerializedSize = computeSerializedSize + n4 + n3 * 1;
            }
            if (this.hasDeveloperEmail || !this.developerEmail.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(11, this.developerEmail);
            }
            if (this.hasDeveloperWebsite || !this.developerWebsite.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(12, this.developerWebsite);
            }
            if (this.hasNumDownloads || !this.numDownloads.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(13, this.numDownloads);
            }
            if (this.hasPackageName || !this.packageName.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(14, this.packageName);
            }
            if (this.hasRecentChangesHtml || !this.recentChangesHtml.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(15, this.recentChangesHtml);
            }
            if (this.hasUploadDate || !this.uploadDate.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(16, this.uploadDate);
            }
            if (this.file != null && this.file.length > 0) {
                for (int k = 0; k < this.file.length; ++k) {
                    fileMetadata = this.file[k];
                    if (fileMetadata != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(17, fileMetadata);
                    }
                }
            }
            if (this.hasAppType || !this.appType.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(18, this.appType);
            }
            if (this.certificateHash != null && this.certificateHash.length > 0) {
                n5 = 0;
                n6 = 0;
                for (int l = 0; l < this.certificateHash.length; ++l) {
                    s3 = this.certificateHash[l];
                    if (s3 != null) {
                        ++n5;
                        n6 += CodedOutputByteBufferNano.computeStringSizeNoTag(s3);
                    }
                }
                computeSerializedSize = computeSerializedSize + n6 + n5 * 2;
            }
            if (this.permission != null && this.permission.length > 0) {
                for (int n7 = 0; n7 < this.permission.length; ++n7) {
                    appPermission = this.permission[n7];
                    if (appPermission != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(20, appPermission);
                    }
                }
            }
            if (this.hasVariesByAccount || !this.variesByAccount) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(21, this.variesByAccount);
            }
            if (this.certificateSet != null && this.certificateSet.length > 0) {
                for (int n8 = 0; n8 < this.certificateSet.length; ++n8) {
                    set = this.certificateSet[n8];
                    if (set != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(22, set);
                    }
                }
            }
            if (this.autoAcquireFreeAppIfHigherVersionAvailableTag != null && this.autoAcquireFreeAppIfHigherVersionAvailableTag.length > 0) {
                n9 = 0;
                n10 = 0;
                for (int n11 = 0; n11 < this.autoAcquireFreeAppIfHigherVersionAvailableTag.length; ++n11) {
                    s4 = this.autoAcquireFreeAppIfHigherVersionAvailableTag[n11];
                    if (s4 != null) {
                        ++n9;
                        n10 += CodedOutputByteBufferNano.computeStringSizeNoTag(s4);
                    }
                }
                computeSerializedSize = computeSerializedSize + n10 + n9 * 2;
            }
            if (this.hasDeclaresIab || this.declaresIab) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(24, this.declaresIab);
            }
            if (this.splitId != null && this.splitId.length > 0) {
                n12 = 0;
                n13 = 0;
                for (int n14 = 0; n14 < this.splitId.length; ++n14) {
                    s5 = this.splitId[n14];
                    if (s5 != null) {
                        ++n12;
                        n13 += CodedOutputByteBufferNano.computeStringSizeNoTag(s5);
                    }
                }
                computeSerializedSize = computeSerializedSize + n13 + n12 * 2;
            }
            return computeSerializedSize;
        }
        
        @Override
        public AppDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            String[] appCategory;
            int repeatedFieldArrayLength2;
            int j;
            String[] obsoletePermission;
            int repeatedFieldArrayLength3;
            int k;
            FileMetadata[] file;
            int repeatedFieldArrayLength4;
            int l;
            String[] certificateHash;
            int repeatedFieldArrayLength5;
            int length;
            AppPermission[] permission;
            int repeatedFieldArrayLength6;
            int length2;
            CertificateSet[] certificateSet;
            int repeatedFieldArrayLength7;
            int length3;
            String[] autoAcquireFreeAppIfHigherVersionAvailableTag;
            int repeatedFieldArrayLength8;
            int length4;
            String[] splitId;
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
                        this.developerName = codedInputByteBufferNano.readString();
                        this.hasDeveloperName = true;
                        continue;
                    }
                    case 16: {
                        this.majorVersionNumber = codedInputByteBufferNano.readInt32();
                        this.hasMajorVersionNumber = true;
                        continue;
                    }
                    case 24: {
                        this.versionCode = codedInputByteBufferNano.readInt32();
                        this.hasVersionCode = true;
                        continue;
                    }
                    case 34: {
                        this.versionString = codedInputByteBufferNano.readString();
                        this.hasVersionString = true;
                        continue;
                    }
                    case 42: {
                        this.title = codedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        continue;
                    }
                    case 58: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 58);
                        if (this.appCategory == null) {
                            i = 0;
                        }
                        else {
                            i = this.appCategory.length;
                        }
                        appCategory = new String[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.appCategory, 0, appCategory, 0, i);
                        }
                        while (i < -1 + appCategory.length) {
                            appCategory[i] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        appCategory[i] = codedInputByteBufferNano.readString();
                        this.appCategory = appCategory;
                        continue;
                    }
                    case 64: {
                        this.contentRating = codedInputByteBufferNano.readInt32();
                        this.hasContentRating = true;
                        continue;
                    }
                    case 72: {
                        this.installationSize = codedInputByteBufferNano.readInt64();
                        this.hasInstallationSize = true;
                        continue;
                    }
                    case 82: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 82);
                        if (this.oBSOLETEPermission == null) {
                            j = 0;
                        }
                        else {
                            j = this.oBSOLETEPermission.length;
                        }
                        obsoletePermission = new String[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.oBSOLETEPermission, 0, obsoletePermission, 0, j);
                        }
                        while (j < -1 + obsoletePermission.length) {
                            obsoletePermission[j] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        obsoletePermission[j] = codedInputByteBufferNano.readString();
                        this.oBSOLETEPermission = obsoletePermission;
                        continue;
                    }
                    case 90: {
                        this.developerEmail = codedInputByteBufferNano.readString();
                        this.hasDeveloperEmail = true;
                        continue;
                    }
                    case 98: {
                        this.developerWebsite = codedInputByteBufferNano.readString();
                        this.hasDeveloperWebsite = true;
                        continue;
                    }
                    case 106: {
                        this.numDownloads = codedInputByteBufferNano.readString();
                        this.hasNumDownloads = true;
                        continue;
                    }
                    case 114: {
                        this.packageName = codedInputByteBufferNano.readString();
                        this.hasPackageName = true;
                        continue;
                    }
                    case 122: {
                        this.recentChangesHtml = codedInputByteBufferNano.readString();
                        this.hasRecentChangesHtml = true;
                        continue;
                    }
                    case 130: {
                        this.uploadDate = codedInputByteBufferNano.readString();
                        this.hasUploadDate = true;
                        continue;
                    }
                    case 138: {
                        repeatedFieldArrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 138);
                        if (this.file == null) {
                            k = 0;
                        }
                        else {
                            k = this.file.length;
                        }
                        file = new FileMetadata[k + repeatedFieldArrayLength3];
                        if (k != 0) {
                            System.arraycopy(this.file, 0, file, 0, k);
                        }
                        while (k < -1 + file.length) {
                            codedInputByteBufferNano.readMessage(file[k] = new FileMetadata());
                            codedInputByteBufferNano.readTag();
                            ++k;
                        }
                        codedInputByteBufferNano.readMessage(file[k] = new FileMetadata());
                        this.file = file;
                        continue;
                    }
                    case 146: {
                        this.appType = codedInputByteBufferNano.readString();
                        this.hasAppType = true;
                        continue;
                    }
                    case 154: {
                        repeatedFieldArrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 154);
                        if (this.certificateHash == null) {
                            l = 0;
                        }
                        else {
                            l = this.certificateHash.length;
                        }
                        certificateHash = new String[l + repeatedFieldArrayLength4];
                        if (l != 0) {
                            System.arraycopy(this.certificateHash, 0, certificateHash, 0, l);
                        }
                        while (l < -1 + certificateHash.length) {
                            certificateHash[l] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++l;
                        }
                        certificateHash[l] = codedInputByteBufferNano.readString();
                        this.certificateHash = certificateHash;
                        continue;
                    }
                    case 162: {
                        repeatedFieldArrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 162);
                        if (this.permission == null) {
                            length = 0;
                        }
                        else {
                            length = this.permission.length;
                        }
                        permission = new AppPermission[length + repeatedFieldArrayLength5];
                        if (length != 0) {
                            System.arraycopy(this.permission, 0, permission, 0, length);
                        }
                        while (length < -1 + permission.length) {
                            codedInputByteBufferNano.readMessage(permission[length] = new AppPermission());
                            codedInputByteBufferNano.readTag();
                            ++length;
                        }
                        codedInputByteBufferNano.readMessage(permission[length] = new AppPermission());
                        this.permission = permission;
                        continue;
                    }
                    case 168: {
                        this.variesByAccount = codedInputByteBufferNano.readBool();
                        this.hasVariesByAccount = true;
                        continue;
                    }
                    case 178: {
                        repeatedFieldArrayLength6 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 178);
                        if (this.certificateSet == null) {
                            length2 = 0;
                        }
                        else {
                            length2 = this.certificateSet.length;
                        }
                        certificateSet = new CertificateSet[length2 + repeatedFieldArrayLength6];
                        if (length2 != 0) {
                            System.arraycopy(this.certificateSet, 0, certificateSet, 0, length2);
                        }
                        while (length2 < -1 + certificateSet.length) {
                            codedInputByteBufferNano.readMessage(certificateSet[length2] = new CertificateSet());
                            codedInputByteBufferNano.readTag();
                            ++length2;
                        }
                        codedInputByteBufferNano.readMessage(certificateSet[length2] = new CertificateSet());
                        this.certificateSet = certificateSet;
                        continue;
                    }
                    case 186: {
                        repeatedFieldArrayLength7 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 186);
                        if (this.autoAcquireFreeAppIfHigherVersionAvailableTag == null) {
                            length3 = 0;
                        }
                        else {
                            length3 = this.autoAcquireFreeAppIfHigherVersionAvailableTag.length;
                        }
                        autoAcquireFreeAppIfHigherVersionAvailableTag = new String[length3 + repeatedFieldArrayLength7];
                        if (length3 != 0) {
                            System.arraycopy(this.autoAcquireFreeAppIfHigherVersionAvailableTag, 0, autoAcquireFreeAppIfHigherVersionAvailableTag, 0, length3);
                        }
                        while (length3 < -1 + autoAcquireFreeAppIfHigherVersionAvailableTag.length) {
                            autoAcquireFreeAppIfHigherVersionAvailableTag[length3] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++length3;
                        }
                        autoAcquireFreeAppIfHigherVersionAvailableTag[length3] = codedInputByteBufferNano.readString();
                        this.autoAcquireFreeAppIfHigherVersionAvailableTag = autoAcquireFreeAppIfHigherVersionAvailableTag;
                        continue;
                    }
                    case 192: {
                        this.declaresIab = codedInputByteBufferNano.readBool();
                        this.hasDeclaresIab = true;
                        continue;
                    }
                    case 202: {
                        repeatedFieldArrayLength8 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 202);
                        if (this.splitId == null) {
                            length4 = 0;
                        }
                        else {
                            length4 = this.splitId.length;
                        }
                        splitId = new String[length4 + repeatedFieldArrayLength8];
                        if (length4 != 0) {
                            System.arraycopy(this.splitId, 0, splitId, 0, length4);
                        }
                        while (length4 < -1 + splitId.length) {
                            splitId[length4] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++length4;
                        }
                        splitId[length4] = codedInputByteBufferNano.readString();
                        this.splitId = splitId;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            String s;
            String s2;
            FileMetadata fileMetadata;
            String s3;
            AppPermission appPermission;
            CertificateSet set;
            String s4;
            String s5;
            if (this.hasDeveloperName || !this.developerName.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.developerName);
            }
            if (this.hasMajorVersionNumber || this.majorVersionNumber != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.majorVersionNumber);
            }
            if (this.hasVersionCode || this.versionCode != 0) {
                codedOutputByteBufferNano.writeInt32(3, this.versionCode);
            }
            if (this.hasVersionString || !this.versionString.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.versionString);
            }
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.title);
            }
            if (this.appCategory != null && this.appCategory.length > 0) {
                for (int i = 0; i < this.appCategory.length; ++i) {
                    s = this.appCategory[i];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(7, s);
                    }
                }
            }
            if (this.hasContentRating || this.contentRating != 0) {
                codedOutputByteBufferNano.writeInt32(8, this.contentRating);
            }
            if (this.hasInstallationSize || this.installationSize != 0L) {
                codedOutputByteBufferNano.writeInt64(9, this.installationSize);
            }
            if (this.oBSOLETEPermission != null && this.oBSOLETEPermission.length > 0) {
                for (int j = 0; j < this.oBSOLETEPermission.length; ++j) {
                    s2 = this.oBSOLETEPermission[j];
                    if (s2 != null) {
                        codedOutputByteBufferNano.writeString(10, s2);
                    }
                }
            }
            if (this.hasDeveloperEmail || !this.developerEmail.equals("")) {
                codedOutputByteBufferNano.writeString(11, this.developerEmail);
            }
            if (this.hasDeveloperWebsite || !this.developerWebsite.equals("")) {
                codedOutputByteBufferNano.writeString(12, this.developerWebsite);
            }
            if (this.hasNumDownloads || !this.numDownloads.equals("")) {
                codedOutputByteBufferNano.writeString(13, this.numDownloads);
            }
            if (this.hasPackageName || !this.packageName.equals("")) {
                codedOutputByteBufferNano.writeString(14, this.packageName);
            }
            if (this.hasRecentChangesHtml || !this.recentChangesHtml.equals("")) {
                codedOutputByteBufferNano.writeString(15, this.recentChangesHtml);
            }
            if (this.hasUploadDate || !this.uploadDate.equals("")) {
                codedOutputByteBufferNano.writeString(16, this.uploadDate);
            }
            if (this.file != null && this.file.length > 0) {
                for (int k = 0; k < this.file.length; ++k) {
                    fileMetadata = this.file[k];
                    if (fileMetadata != null) {
                        codedOutputByteBufferNano.writeMessage(17, fileMetadata);
                    }
                }
            }
            if (this.hasAppType || !this.appType.equals("")) {
                codedOutputByteBufferNano.writeString(18, this.appType);
            }
            if (this.certificateHash != null && this.certificateHash.length > 0) {
                for (int l = 0; l < this.certificateHash.length; ++l) {
                    s3 = this.certificateHash[l];
                    if (s3 != null) {
                        codedOutputByteBufferNano.writeString(19, s3);
                    }
                }
            }
            if (this.permission != null && this.permission.length > 0) {
                for (int n = 0; n < this.permission.length; ++n) {
                    appPermission = this.permission[n];
                    if (appPermission != null) {
                        codedOutputByteBufferNano.writeMessage(20, appPermission);
                    }
                }
            }
            if (this.hasVariesByAccount || !this.variesByAccount) {
                codedOutputByteBufferNano.writeBool(21, this.variesByAccount);
            }
            if (this.certificateSet != null && this.certificateSet.length > 0) {
                for (int n2 = 0; n2 < this.certificateSet.length; ++n2) {
                    set = this.certificateSet[n2];
                    if (set != null) {
                        codedOutputByteBufferNano.writeMessage(22, set);
                    }
                }
            }
            if (this.autoAcquireFreeAppIfHigherVersionAvailableTag != null && this.autoAcquireFreeAppIfHigherVersionAvailableTag.length > 0) {
                for (int n3 = 0; n3 < this.autoAcquireFreeAppIfHigherVersionAvailableTag.length; ++n3) {
                    s4 = this.autoAcquireFreeAppIfHigherVersionAvailableTag[n3];
                    if (s4 != null) {
                        codedOutputByteBufferNano.writeString(23, s4);
                    }
                }
            }
            if (this.hasDeclaresIab || this.declaresIab) {
                codedOutputByteBufferNano.writeBool(24, this.declaresIab);
            }
            if (this.splitId != null && this.splitId.length > 0) {
                for (int n4 = 0; n4 < this.splitId.length; ++n4) {
                    s5 = this.splitId[n4];
                    if (s5 != null) {
                        codedOutputByteBufferNano.writeString(25, s5);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class AppPermission extends MessageNano
    {
        private static volatile AppPermission[] _emptyArray;
        public boolean hasKey;
        public boolean hasPermissionRequired;
        public String key;
        public boolean permissionRequired;
        
        public AppPermission() {
            super();
            this.clear();
        }
        
        public static AppPermission[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new AppPermission[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public AppPermission clear() {
            this.key = "";
            this.hasKey = false;
            this.permissionRequired = true;
            this.hasPermissionRequired = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasKey || !this.key.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.key);
            }
            if (this.hasPermissionRequired || !this.permissionRequired) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(2, this.permissionRequired);
            }
            return computeSerializedSize;
        }
        
        @Override
        public AppPermission mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
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
                        this.key = codedInputByteBufferNano.readString();
                        this.hasKey = true;
                        continue;
                    }
                    case 16: {
                        this.permissionRequired = codedInputByteBufferNano.readBool();
                        this.hasPermissionRequired = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasKey || !this.key.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.key);
            }
            if (this.hasPermissionRequired || !this.permissionRequired) {
                codedOutputByteBufferNano.writeBool(2, this.permissionRequired);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class ArtistDetails extends MessageNano
    {
        private static volatile ArtistDetails[] _emptyArray;
        public String detailsUrl;
        public ArtistExternalLinks externalLinks;
        public boolean hasDetailsUrl;
        public boolean hasName;
        public String name;
        
        public ArtistDetails() {
            super();
            this.clear();
        }
        
        public static ArtistDetails[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ArtistDetails[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public ArtistDetails clear() {
            this.detailsUrl = "";
            this.hasDetailsUrl = false;
            this.name = "";
            this.hasName = false;
            this.externalLinks = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasDetailsUrl || !this.detailsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.detailsUrl);
            }
            if (this.hasName || !this.name.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.name);
            }
            if (this.externalLinks != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, this.externalLinks);
            }
            return computeSerializedSize;
        }
        
        @Override
        public ArtistDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
        Label_0056:
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
                        this.detailsUrl = codedInputByteBufferNano.readString();
                        this.hasDetailsUrl = true;
                        continue;
                    }
                    case 18: {
                        this.name = codedInputByteBufferNano.readString();
                        this.hasName = true;
                        continue;
                    }
                    case 26: {
                        if (this.externalLinks == null) {
                            this.externalLinks = new ArtistExternalLinks();
                        }
                        codedInputByteBufferNano.readMessage(this.externalLinks);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasDetailsUrl || !this.detailsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.detailsUrl);
            }
            if (this.hasName || !this.name.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.name);
            }
            if (this.externalLinks != null) {
                codedOutputByteBufferNano.writeMessage(3, this.externalLinks);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class ArtistExternalLinks extends MessageNano
    {
        public String googlePlusProfileUrl;
        public boolean hasGooglePlusProfileUrl;
        public boolean hasYoutubeChannelUrl;
        public String[] websiteUrl;
        public String youtubeChannelUrl;
        
        public ArtistExternalLinks() {
            super();
            this.clear();
        }
        
        public ArtistExternalLinks clear() {
            this.websiteUrl = WireFormatNano.EMPTY_STRING_ARRAY;
            this.googlePlusProfileUrl = "";
            this.hasGooglePlusProfileUrl = false;
            this.youtubeChannelUrl = "";
            this.hasYoutubeChannelUrl = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            int n;
            int n2;
            String s;
            computeSerializedSize = super.computeSerializedSize();
            if (this.websiteUrl != null && this.websiteUrl.length > 0) {
                n = 0;
                n2 = 0;
                for (int i = 0; i < this.websiteUrl.length; ++i) {
                    s = this.websiteUrl[i];
                    if (s != null) {
                        ++n;
                        n2 += CodedOutputByteBufferNano.computeStringSizeNoTag(s);
                    }
                }
                computeSerializedSize = computeSerializedSize + n2 + n * 1;
            }
            if (this.hasGooglePlusProfileUrl || !this.googlePlusProfileUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.googlePlusProfileUrl);
            }
            if (this.hasYoutubeChannelUrl || !this.youtubeChannelUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.youtubeChannelUrl);
            }
            return computeSerializedSize;
        }
        
        @Override
        public ArtistExternalLinks mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            String[] websiteUrl;
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
                        if (this.websiteUrl == null) {
                            i = 0;
                        }
                        else {
                            i = this.websiteUrl.length;
                        }
                        websiteUrl = new String[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.websiteUrl, 0, websiteUrl, 0, i);
                        }
                        while (i < -1 + websiteUrl.length) {
                            websiteUrl[i] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        websiteUrl[i] = codedInputByteBufferNano.readString();
                        this.websiteUrl = websiteUrl;
                        continue;
                    }
                    case 18: {
                        this.googlePlusProfileUrl = codedInputByteBufferNano.readString();
                        this.hasGooglePlusProfileUrl = true;
                        continue;
                    }
                    case 26: {
                        this.youtubeChannelUrl = codedInputByteBufferNano.readString();
                        this.hasYoutubeChannelUrl = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            String s;
            if (this.websiteUrl != null && this.websiteUrl.length > 0) {
                for (int i = 0; i < this.websiteUrl.length; ++i) {
                    s = this.websiteUrl[i];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(1, s);
                    }
                }
            }
            if (this.hasGooglePlusProfileUrl || !this.googlePlusProfileUrl.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.googlePlusProfileUrl);
            }
            if (this.hasYoutubeChannelUrl || !this.youtubeChannelUrl.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.youtubeChannelUrl);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class BookDetails extends MessageNano
    {
        public String aboutTheAuthor;
        public boolean hasAboutTheAuthor;
        public boolean hasIsbn;
        public boolean hasNumberOfPages;
        public boolean hasPublicationDate;
        public boolean hasPublisher;
        public String isbn;
        public int numberOfPages;
        public String publicationDate;
        public String publisher;
        
        public BookDetails() {
            super();
            this.clear();
        }
        
        public BookDetails clear() {
            this.publisher = "";
            this.hasPublisher = false;
            this.publicationDate = "";
            this.hasPublicationDate = false;
            this.isbn = "";
            this.hasIsbn = false;
            this.numberOfPages = 0;
            this.hasNumberOfPages = false;
            this.aboutTheAuthor = "";
            this.hasAboutTheAuthor = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasPublisher || !this.publisher.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.publisher);
            }
            if (this.hasPublicationDate || !this.publicationDate.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.publicationDate);
            }
            if (this.hasIsbn || !this.isbn.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(6, this.isbn);
            }
            if (this.hasNumberOfPages || this.numberOfPages != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(7, this.numberOfPages);
            }
            if (this.hasAboutTheAuthor || !this.aboutTheAuthor.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(17, this.aboutTheAuthor);
            }
            return computeSerializedSize;
        }
        
        @Override
        public BookDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                    case 34: {
                        this.publisher = codedInputByteBufferNano.readString();
                        this.hasPublisher = true;
                        continue;
                    }
                    case 42: {
                        this.publicationDate = codedInputByteBufferNano.readString();
                        this.hasPublicationDate = true;
                        continue;
                    }
                    case 50: {
                        this.isbn = codedInputByteBufferNano.readString();
                        this.hasIsbn = true;
                        continue;
                    }
                    case 56: {
                        this.numberOfPages = codedInputByteBufferNano.readInt32();
                        this.hasNumberOfPages = true;
                        continue;
                    }
                    case 138: {
                        this.aboutTheAuthor = codedInputByteBufferNano.readString();
                        this.hasAboutTheAuthor = true;
                        continue;
                    }
                }
            }
            return this;
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasPublisher || !this.publisher.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.publisher);
            }
            if (this.hasPublicationDate || !this.publicationDate.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.publicationDate);
            }
            if (this.hasIsbn || !this.isbn.equals("")) {
                codedOutputByteBufferNano.writeString(6, this.isbn);
            }
            if (this.hasNumberOfPages || this.numberOfPages != 0) {
                codedOutputByteBufferNano.writeInt32(7, this.numberOfPages);
            }
            if (this.hasAboutTheAuthor || !this.aboutTheAuthor.equals("")) {
                codedOutputByteBufferNano.writeString(17, this.aboutTheAuthor);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class CertificateSet extends MessageNano
    {
        private static volatile CertificateSet[] _emptyArray;
        public String[] certificateHash;
        
        public CertificateSet() {
            super();
            this.clear();
        }
        
        public static CertificateSet[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new CertificateSet[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public CertificateSet clear() {
            this.certificateHash = WireFormatNano.EMPTY_STRING_ARRAY;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            int n;
            int n2;
            String s;
            computeSerializedSize = super.computeSerializedSize();
            if (this.certificateHash != null && this.certificateHash.length > 0) {
                n = 0;
                n2 = 0;
                for (int i = 0; i < this.certificateHash.length; ++i) {
                    s = this.certificateHash[i];
                    if (s != null) {
                        ++n;
                        n2 += CodedOutputByteBufferNano.computeStringSizeNoTag(s);
                    }
                }
                computeSerializedSize = computeSerializedSize + n2 + n * 1;
            }
            return computeSerializedSize;
        }
        
        @Override
        public CertificateSet mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            String[] certificateHash;
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
                        if (this.certificateHash == null) {
                            i = 0;
                        }
                        else {
                            i = this.certificateHash.length;
                        }
                        certificateHash = new String[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.certificateHash, 0, certificateHash, 0, i);
                        }
                        while (i < -1 + certificateHash.length) {
                            certificateHash[i] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        certificateHash[i] = codedInputByteBufferNano.readString();
                        this.certificateHash = certificateHash;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            String s;
            if (this.certificateHash != null && this.certificateHash.length > 0) {
                for (int i = 0; i < this.certificateHash.length; ++i) {
                    s = this.certificateHash[i];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(1, s);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class DocumentDetails extends MessageNano
    {
        public AlbumDetails albumDetails;
        public AppDetails appDetails;
        public ArtistDetails artistDetails;
        public BookDetails bookDetails;
        public MagazineDetails magazineDetails;
        public PersonDetails personDetails;
        public SongDetails songDetails;
        public SubscriptionDetails subscriptionDetails;
        public TalentDetails talentDetails;
        public TvEpisodeDetails tvEpisodeDetails;
        public TvSeasonDetails tvSeasonDetails;
        public TvShowDetails tvShowDetails;
        public VideoDetails videoDetails;
        
        public DocumentDetails() {
            super();
            this.clear();
        }
        
        public DocumentDetails clear() {
            this.appDetails = null;
            this.albumDetails = null;
            this.artistDetails = null;
            this.songDetails = null;
            this.bookDetails = null;
            this.videoDetails = null;
            this.subscriptionDetails = null;
            this.magazineDetails = null;
            this.tvShowDetails = null;
            this.tvSeasonDetails = null;
            this.tvEpisodeDetails = null;
            this.personDetails = null;
            this.talentDetails = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.appDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.appDetails);
            }
            if (this.albumDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.albumDetails);
            }
            if (this.artistDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, this.artistDetails);
            }
            if (this.songDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, this.songDetails);
            }
            if (this.bookDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(5, this.bookDetails);
            }
            if (this.videoDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(6, this.videoDetails);
            }
            if (this.subscriptionDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(7, this.subscriptionDetails);
            }
            if (this.magazineDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(8, this.magazineDetails);
            }
            if (this.tvShowDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(9, this.tvShowDetails);
            }
            if (this.tvSeasonDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(10, this.tvSeasonDetails);
            }
            if (this.tvEpisodeDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(11, this.tvEpisodeDetails);
            }
            if (this.personDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(12, this.personDetails);
            }
            if (this.talentDetails != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(13, this.talentDetails);
            }
            return computeSerializedSize;
        }
        
        @Override
        public DocumentDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        if (this.appDetails == null) {
                            this.appDetails = new AppDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.appDetails);
                        continue;
                    }
                    case 18: {
                        if (this.albumDetails == null) {
                            this.albumDetails = new AlbumDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.albumDetails);
                        continue;
                    }
                    case 26: {
                        if (this.artistDetails == null) {
                            this.artistDetails = new ArtistDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.artistDetails);
                        continue;
                    }
                    case 34: {
                        if (this.songDetails == null) {
                            this.songDetails = new SongDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.songDetails);
                        continue;
                    }
                    case 42: {
                        if (this.bookDetails == null) {
                            this.bookDetails = new BookDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.bookDetails);
                        continue;
                    }
                    case 50: {
                        if (this.videoDetails == null) {
                            this.videoDetails = new VideoDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.videoDetails);
                        continue;
                    }
                    case 58: {
                        if (this.subscriptionDetails == null) {
                            this.subscriptionDetails = new SubscriptionDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.subscriptionDetails);
                        continue;
                    }
                    case 66: {
                        if (this.magazineDetails == null) {
                            this.magazineDetails = new MagazineDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.magazineDetails);
                        continue;
                    }
                    case 74: {
                        if (this.tvShowDetails == null) {
                            this.tvShowDetails = new TvShowDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.tvShowDetails);
                        continue;
                    }
                    case 82: {
                        if (this.tvSeasonDetails == null) {
                            this.tvSeasonDetails = new TvSeasonDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.tvSeasonDetails);
                        continue;
                    }
                    case 90: {
                        if (this.tvEpisodeDetails == null) {
                            this.tvEpisodeDetails = new TvEpisodeDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.tvEpisodeDetails);
                        continue;
                    }
                    case 98: {
                        if (this.personDetails == null) {
                            this.personDetails = new PersonDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.personDetails);
                        continue;
                    }
                    case 106: {
                        if (this.talentDetails == null) {
                            this.talentDetails = new TalentDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.talentDetails);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.appDetails != null) {
                codedOutputByteBufferNano.writeMessage(1, this.appDetails);
            }
            if (this.albumDetails != null) {
                codedOutputByteBufferNano.writeMessage(2, this.albumDetails);
            }
            if (this.artistDetails != null) {
                codedOutputByteBufferNano.writeMessage(3, this.artistDetails);
            }
            if (this.songDetails != null) {
                codedOutputByteBufferNano.writeMessage(4, this.songDetails);
            }
            if (this.bookDetails != null) {
                codedOutputByteBufferNano.writeMessage(5, this.bookDetails);
            }
            if (this.videoDetails != null) {
                codedOutputByteBufferNano.writeMessage(6, this.videoDetails);
            }
            if (this.subscriptionDetails != null) {
                codedOutputByteBufferNano.writeMessage(7, this.subscriptionDetails);
            }
            if (this.magazineDetails != null) {
                codedOutputByteBufferNano.writeMessage(8, this.magazineDetails);
            }
            if (this.tvShowDetails != null) {
                codedOutputByteBufferNano.writeMessage(9, this.tvShowDetails);
            }
            if (this.tvSeasonDetails != null) {
                codedOutputByteBufferNano.writeMessage(10, this.tvSeasonDetails);
            }
            if (this.tvEpisodeDetails != null) {
                codedOutputByteBufferNano.writeMessage(11, this.tvEpisodeDetails);
            }
            if (this.personDetails != null) {
                codedOutputByteBufferNano.writeMessage(12, this.personDetails);
            }
            if (this.talentDetails != null) {
                codedOutputByteBufferNano.writeMessage(13, this.talentDetails);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class FileMetadata extends MessageNano
    {
        private static volatile FileMetadata[] _emptyArray;
        public int fileType;
        public boolean hasFileType;
        public boolean hasSize;
        public boolean hasSplitId;
        public boolean hasVersionCode;
        public long size;
        public String splitId;
        public int versionCode;
        
        public FileMetadata() {
            super();
            this.clear();
        }
        
        public static FileMetadata[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new FileMetadata[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public FileMetadata clear() {
            this.fileType = 0;
            this.hasFileType = false;
            this.versionCode = 0;
            this.hasVersionCode = false;
            this.size = 0L;
            this.hasSize = false;
            this.splitId = "";
            this.hasSplitId = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.fileType != 0 || this.hasFileType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.fileType);
            }
            if (this.hasVersionCode || this.versionCode != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.versionCode);
            }
            if (this.hasSize || this.size != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(3, this.size);
            }
            if (this.hasSplitId || !this.splitId.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.splitId);
            }
            return computeSerializedSize;
        }
        
        @Override
        public FileMetadata mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                            case 0:
                            case 1:
                            case 2: {
                                this.fileType = int32;
                                this.hasFileType = true;
                                continue;
                            }
                        }
                    }
                    case 16: {
                        this.versionCode = codedInputByteBufferNano.readInt32();
                        this.hasVersionCode = true;
                        continue;
                    }
                    case 24: {
                        this.size = codedInputByteBufferNano.readInt64();
                        this.hasSize = true;
                        continue;
                    }
                    case 34: {
                        this.splitId = codedInputByteBufferNano.readString();
                        this.hasSplitId = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.fileType != 0 || this.hasFileType) {
                codedOutputByteBufferNano.writeInt32(1, this.fileType);
            }
            if (this.hasVersionCode || this.versionCode != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.versionCode);
            }
            if (this.hasSize || this.size != 0L) {
                codedOutputByteBufferNano.writeInt64(3, this.size);
            }
            if (this.hasSplitId || !this.splitId.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.splitId);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class MagazineDetails extends MessageNano
    {
        public String deliveryFrequencyDescription;
        public String deviceAvailabilityDescriptionHtml;
        public boolean hasDeliveryFrequencyDescription;
        public boolean hasDeviceAvailabilityDescriptionHtml;
        public boolean hasParentDetailsUrl;
        public boolean hasPsvDescription;
        public String parentDetailsUrl;
        public String psvDescription;
        
        public MagazineDetails() {
            super();
            this.clear();
        }
        
        public MagazineDetails clear() {
            this.parentDetailsUrl = "";
            this.hasParentDetailsUrl = false;
            this.deviceAvailabilityDescriptionHtml = "";
            this.hasDeviceAvailabilityDescriptionHtml = false;
            this.psvDescription = "";
            this.hasPsvDescription = false;
            this.deliveryFrequencyDescription = "";
            this.hasDeliveryFrequencyDescription = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasParentDetailsUrl || !this.parentDetailsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.parentDetailsUrl);
            }
            if (this.hasDeviceAvailabilityDescriptionHtml || !this.deviceAvailabilityDescriptionHtml.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.deviceAvailabilityDescriptionHtml);
            }
            if (this.hasPsvDescription || !this.psvDescription.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.psvDescription);
            }
            if (this.hasDeliveryFrequencyDescription || !this.deliveryFrequencyDescription.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.deliveryFrequencyDescription);
            }
            return computeSerializedSize;
        }
        
        @Override
        public MagazineDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.parentDetailsUrl = codedInputByteBufferNano.readString();
                        this.hasParentDetailsUrl = true;
                        continue;
                    }
                    case 18: {
                        this.deviceAvailabilityDescriptionHtml = codedInputByteBufferNano.readString();
                        this.hasDeviceAvailabilityDescriptionHtml = true;
                        continue;
                    }
                    case 26: {
                        this.psvDescription = codedInputByteBufferNano.readString();
                        this.hasPsvDescription = true;
                        continue;
                    }
                    case 34: {
                        this.deliveryFrequencyDescription = codedInputByteBufferNano.readString();
                        this.hasDeliveryFrequencyDescription = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasParentDetailsUrl || !this.parentDetailsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.parentDetailsUrl);
            }
            if (this.hasDeviceAvailabilityDescriptionHtml || !this.deviceAvailabilityDescriptionHtml.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.deviceAvailabilityDescriptionHtml);
            }
            if (this.hasPsvDescription || !this.psvDescription.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.psvDescription);
            }
            if (this.hasDeliveryFrequencyDescription || !this.deliveryFrequencyDescription.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.deliveryFrequencyDescription);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class MusicDetails extends MessageNano
    {
        public ArtistDetails[] artist;
        public int censoring;
        public int durationSec;
        public String[] genre;
        public boolean hasCensoring;
        public boolean hasDurationSec;
        public boolean hasLabel;
        public boolean hasOriginalReleaseDate;
        public boolean hasReleaseDate;
        public String label;
        public String originalReleaseDate;
        public String releaseDate;
        public int[] releaseType;
        
        public MusicDetails() {
            super();
            this.clear();
        }
        
        public MusicDetails clear() {
            this.censoring = 0;
            this.hasCensoring = false;
            this.releaseType = WireFormatNano.EMPTY_INT_ARRAY;
            this.durationSec = 0;
            this.hasDurationSec = false;
            this.originalReleaseDate = "";
            this.hasOriginalReleaseDate = false;
            this.releaseDate = "";
            this.hasReleaseDate = false;
            this.label = "";
            this.hasLabel = false;
            this.artist = ArtistDetails.emptyArray();
            this.genre = WireFormatNano.EMPTY_STRING_ARRAY;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            ArtistDetails artistDetails;
            int n;
            int n2;
            String s;
            int n3;
            computeSerializedSize = super.computeSerializedSize();
            if (this.censoring != 0 || this.hasCensoring) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.censoring);
            }
            if (this.hasDurationSec || this.durationSec != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.durationSec);
            }
            if (this.hasOriginalReleaseDate || !this.originalReleaseDate.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.originalReleaseDate);
            }
            if (this.hasLabel || !this.label.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.label);
            }
            if (this.artist != null && this.artist.length > 0) {
                for (int i = 0; i < this.artist.length; ++i) {
                    artistDetails = this.artist[i];
                    if (artistDetails != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(5, artistDetails);
                    }
                }
            }
            if (this.genre != null && this.genre.length > 0) {
                n = 0;
                n2 = 0;
                for (int j = 0; j < this.genre.length; ++j) {
                    s = this.genre[j];
                    if (s != null) {
                        ++n;
                        n2 += CodedOutputByteBufferNano.computeStringSizeNoTag(s);
                    }
                }
                computeSerializedSize = computeSerializedSize + n2 + n * 1;
            }
            if (this.hasReleaseDate || !this.releaseDate.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(7, this.releaseDate);
            }
            if (this.releaseType != null && this.releaseType.length > 0) {
                n3 = 0;
                for (int k = 0; k < this.releaseType.length; ++k) {
                    n3 += CodedOutputByteBufferNano.computeInt32SizeNoTag(this.releaseType[k]);
                }
                computeSerializedSize = computeSerializedSize + n3 + 1 * this.releaseType.length;
            }
            return computeSerializedSize;
        }
        
        @Override
        public MusicDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
            int repeatedFieldArrayLength;
            int i;
            ArtistDetails[] artist;
            int repeatedFieldArrayLength2;
            int j;
            String[] genre;
            int repeatedFieldArrayLength3;
            int[] releaseType;
            int k;
            int n;
            int int2;
            int n2;
            int length;
            int[] releaseType2;
            int pushLimit;
            int n3;
            int position;
            int length2;
            int[] releaseType3;
            int int3;
            int n4;
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
                            case 0:
                            case 1:
                            case 2: {
                                this.censoring = int32;
                                this.hasCensoring = true;
                                continue;
                            }
                        }
                    }
                    case 16: {
                        this.durationSec = codedInputByteBufferNano.readInt32();
                        this.hasDurationSec = true;
                        continue;
                    }
                    case 26: {
                        this.originalReleaseDate = codedInputByteBufferNano.readString();
                        this.hasOriginalReleaseDate = true;
                        continue;
                    }
                    case 34: {
                        this.label = codedInputByteBufferNano.readString();
                        this.hasLabel = true;
                        continue;
                    }
                    case 42: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 42);
                        if (this.artist == null) {
                            i = 0;
                        }
                        else {
                            i = this.artist.length;
                        }
                        artist = new ArtistDetails[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.artist, 0, artist, 0, i);
                        }
                        while (i < -1 + artist.length) {
                            codedInputByteBufferNano.readMessage(artist[i] = new ArtistDetails());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(artist[i] = new ArtistDetails());
                        this.artist = artist;
                        continue;
                    }
                    case 50: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 50);
                        if (this.genre == null) {
                            j = 0;
                        }
                        else {
                            j = this.genre.length;
                        }
                        genre = new String[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.genre, 0, genre, 0, j);
                        }
                        while (j < -1 + genre.length) {
                            genre[j] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        genre[j] = codedInputByteBufferNano.readString();
                        this.genre = genre;
                        continue;
                    }
                    case 58: {
                        this.releaseDate = codedInputByteBufferNano.readString();
                        this.hasReleaseDate = true;
                        continue;
                    }
                    case 64: {
                        repeatedFieldArrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 64);
                        releaseType = new int[repeatedFieldArrayLength3];
                        k = 0;
                        n = 0;
                        while (k < repeatedFieldArrayLength3) {
                            if (k != 0) {
                                codedInputByteBufferNano.readTag();
                            }
                            int2 = codedInputByteBufferNano.readInt32();
                            switch (int2) {
                                default: {
                                    n2 = n;
                                    break;
                                }
                                case 0:
                                case 1:
                                case 2: {
                                    n2 = n + 1;
                                    releaseType[n] = int2;
                                    break;
                                }
                            }
                            ++k;
                            n = n2;
                        }
                        if (n == 0) {
                            continue;
                        }
                        if (this.releaseType == null) {
                            length = 0;
                        }
                        else {
                            length = this.releaseType.length;
                        }
                        if (length == 0 && n == releaseType.length) {
                            this.releaseType = releaseType;
                            continue;
                        }
                        releaseType2 = new int[length + n];
                        if (length != 0) {
                            System.arraycopy(this.releaseType, 0, releaseType2, 0, length);
                        }
                        System.arraycopy(releaseType, 0, releaseType2, length, n);
                        this.releaseType = releaseType2;
                        continue;
                    }
                    case 66: {
                        pushLimit = codedInputByteBufferNano.pushLimit(codedInputByteBufferNano.readRawVarint32());
                        n3 = 0;
                        position = codedInputByteBufferNano.getPosition();
                        while (codedInputByteBufferNano.getBytesUntilLimit() > 0) {
                            switch (codedInputByteBufferNano.readInt32()) {
                                default: {
                                    continue;
                                }
                                case 0:
                                case 1:
                                case 2: {
                                    ++n3;
                                    continue;
                                }
                            }
                        }
                        if (n3 != 0) {
                            codedInputByteBufferNano.rewindToPosition(position);
                            if (this.releaseType == null) {
                                length2 = 0;
                            }
                            else {
                                length2 = this.releaseType.length;
                            }
                            releaseType3 = new int[length2 + n3];
                            if (length2 != 0) {
                                System.arraycopy(this.releaseType, 0, releaseType3, 0, length2);
                            }
                            while (codedInputByteBufferNano.getBytesUntilLimit() > 0) {
                                int3 = codedInputByteBufferNano.readInt32();
                                switch (int3) {
                                    default: {
                                        continue;
                                    }
                                    case 0:
                                    case 1:
                                    case 2: {
                                        n4 = length2 + 1;
                                        releaseType3[length2] = int3;
                                        length2 = n4;
                                        continue;
                                    }
                                }
                            }
                            this.releaseType = releaseType3;
                        }
                        codedInputByteBufferNano.popLimit(pushLimit);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            ArtistDetails artistDetails;
            String s;
            if (this.censoring != 0 || this.hasCensoring) {
                codedOutputByteBufferNano.writeInt32(1, this.censoring);
            }
            if (this.hasDurationSec || this.durationSec != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.durationSec);
            }
            if (this.hasOriginalReleaseDate || !this.originalReleaseDate.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.originalReleaseDate);
            }
            if (this.hasLabel || !this.label.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.label);
            }
            if (this.artist != null && this.artist.length > 0) {
                for (int i = 0; i < this.artist.length; ++i) {
                    artistDetails = this.artist[i];
                    if (artistDetails != null) {
                        codedOutputByteBufferNano.writeMessage(5, artistDetails);
                    }
                }
            }
            if (this.genre != null && this.genre.length > 0) {
                for (int j = 0; j < this.genre.length; ++j) {
                    s = this.genre[j];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(6, s);
                    }
                }
            }
            if (this.hasReleaseDate || !this.releaseDate.equals("")) {
                codedOutputByteBufferNano.writeString(7, this.releaseDate);
            }
            if (this.releaseType != null && this.releaseType.length > 0) {
                for (int k = 0; k < this.releaseType.length; ++k) {
                    codedOutputByteBufferNano.writeInt32(8, this.releaseType[k]);
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class PersonDetails extends MessageNano
    {
        public boolean hasPersonIsRequester;
        public boolean personIsRequester;
        
        public PersonDetails() {
            super();
            this.clear();
        }
        
        public PersonDetails clear() {
            this.personIsRequester = false;
            this.hasPersonIsRequester = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasPersonIsRequester || this.personIsRequester) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(1, this.personIsRequester);
            }
            return computeSerializedSize;
        }
        
        @Override
        public PersonDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.personIsRequester = codedInputByteBufferNano.readBool();
                        this.hasPersonIsRequester = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasPersonIsRequester || this.personIsRequester) {
                codedOutputByteBufferNano.writeBool(1, this.personIsRequester);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class ProductDetails extends MessageNano
    {
        public boolean hasTitle;
        public ProductDetailsSection[] section;
        public String title;
        
        public ProductDetails() {
            super();
            this.clear();
        }
        
        public ProductDetails clear() {
            this.title = "";
            this.hasTitle = false;
            this.section = ProductDetailsSection.emptyArray();
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            ProductDetailsSection productDetailsSection;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.title);
            }
            if (this.section != null && this.section.length > 0) {
                for (int i = 0; i < this.section.length; ++i) {
                    productDetailsSection = this.section[i];
                    if (productDetailsSection != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, productDetailsSection);
                    }
                }
            }
            return computeSerializedSize;
        }
        
        @Override
        public ProductDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            ProductDetailsSection[] section;
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
                        if (this.section == null) {
                            i = 0;
                        }
                        else {
                            i = this.section.length;
                        }
                        section = new ProductDetailsSection[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.section, 0, section, 0, i);
                        }
                        while (i < -1 + section.length) {
                            codedInputByteBufferNano.readMessage(section[i] = new ProductDetailsSection());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(section[i] = new ProductDetailsSection());
                        this.section = section;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            ProductDetailsSection productDetailsSection;
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.title);
            }
            if (this.section != null && this.section.length > 0) {
                for (int i = 0; i < this.section.length; ++i) {
                    productDetailsSection = this.section[i];
                    if (productDetailsSection != null) {
                        codedOutputByteBufferNano.writeMessage(2, productDetailsSection);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class ProductDetailsDescription extends MessageNano
    {
        private static volatile ProductDetailsDescription[] _emptyArray;
        public String description;
        public boolean hasDescription;
        public Common.Image image;
        
        public ProductDetailsDescription() {
            super();
            this.clear();
        }
        
        public static ProductDetailsDescription[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ProductDetailsDescription[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public ProductDetailsDescription clear() {
            this.image = null;
            this.description = "";
            this.hasDescription = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.image != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.image);
            }
            if (this.hasDescription || !this.description.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.description);
            }
            return computeSerializedSize;
        }
        
        @Override
        public ProductDetailsDescription mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        if (this.image == null) {
                            this.image = new Common.Image();
                        }
                        codedInputByteBufferNano.readMessage(this.image);
                        continue;
                    }
                    case 18: {
                        this.description = codedInputByteBufferNano.readString();
                        this.hasDescription = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.image != null) {
                codedOutputByteBufferNano.writeMessage(1, this.image);
            }
            if (this.hasDescription || !this.description.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.description);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class ProductDetailsSection extends MessageNano
    {
        private static volatile ProductDetailsSection[] _emptyArray;
        public ProductDetailsDescription[] description;
        public boolean hasTitle;
        public String title;
        
        public ProductDetailsSection() {
            super();
            this.clear();
        }
        
        public static ProductDetailsSection[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new ProductDetailsSection[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public ProductDetailsSection clear() {
            this.title = "";
            this.hasTitle = false;
            this.description = ProductDetailsDescription.emptyArray();
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            ProductDetailsDescription productDetailsDescription;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.title);
            }
            if (this.description != null && this.description.length > 0) {
                for (int i = 0; i < this.description.length; ++i) {
                    productDetailsDescription = this.description[i];
                    if (productDetailsDescription != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, productDetailsDescription);
                    }
                }
            }
            return computeSerializedSize;
        }
        
        @Override
        public ProductDetailsSection mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            ProductDetailsDescription[] description;
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
                    case 26: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 26);
                        if (this.description == null) {
                            i = 0;
                        }
                        else {
                            i = this.description.length;
                        }
                        description = new ProductDetailsDescription[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.description, 0, description, 0, i);
                        }
                        while (i < -1 + description.length) {
                            codedInputByteBufferNano.readMessage(description[i] = new ProductDetailsDescription());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(description[i] = new ProductDetailsDescription());
                        this.description = description;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            ProductDetailsDescription productDetailsDescription;
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.title);
            }
            if (this.description != null && this.description.length > 0) {
                for (int i = 0; i < this.description.length; ++i) {
                    productDetailsDescription = this.description[i];
                    if (productDetailsDescription != null) {
                        codedOutputByteBufferNano.writeMessage(3, productDetailsDescription);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class SongDetails extends MessageNano
    {
        public String albumName;
        public DocAnnotations.Badge badge;
        public MusicDetails details;
        public ArtistDetails displayArtist;
        public boolean hasAlbumName;
        public boolean hasName;
        public boolean hasPreviewUrl;
        public boolean hasTrackNumber;
        public String name;
        public String previewUrl;
        public int trackNumber;
        
        public SongDetails() {
            super();
            this.clear();
        }
        
        public SongDetails clear() {
            this.name = "";
            this.hasName = false;
            this.details = null;
            this.albumName = "";
            this.hasAlbumName = false;
            this.trackNumber = 0;
            this.hasTrackNumber = false;
            this.previewUrl = "";
            this.hasPreviewUrl = false;
            this.displayArtist = null;
            this.badge = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasName || !this.name.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.name);
            }
            if (this.details != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.details);
            }
            if (this.hasAlbumName || !this.albumName.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.albumName);
            }
            if (this.hasTrackNumber || this.trackNumber != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(4, this.trackNumber);
            }
            if (this.hasPreviewUrl || !this.previewUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.previewUrl);
            }
            if (this.displayArtist != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(6, this.displayArtist);
            }
            if (this.badge != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(7, this.badge);
            }
            return computeSerializedSize;
        }
        
        @Override
        public SongDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.name = codedInputByteBufferNano.readString();
                        this.hasName = true;
                        continue;
                    }
                    case 18: {
                        if (this.details == null) {
                            this.details = new MusicDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.details);
                        continue;
                    }
                    case 26: {
                        this.albumName = codedInputByteBufferNano.readString();
                        this.hasAlbumName = true;
                        continue;
                    }
                    case 32: {
                        this.trackNumber = codedInputByteBufferNano.readInt32();
                        this.hasTrackNumber = true;
                        continue;
                    }
                    case 42: {
                        this.previewUrl = codedInputByteBufferNano.readString();
                        this.hasPreviewUrl = true;
                        continue;
                    }
                    case 50: {
                        if (this.displayArtist == null) {
                            this.displayArtist = new ArtistDetails();
                        }
                        codedInputByteBufferNano.readMessage(this.displayArtist);
                        continue;
                    }
                    case 58: {
                        if (this.badge == null) {
                            this.badge = new DocAnnotations.Badge();
                        }
                        codedInputByteBufferNano.readMessage(this.badge);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasName || !this.name.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.name);
            }
            if (this.details != null) {
                codedOutputByteBufferNano.writeMessage(2, this.details);
            }
            if (this.hasAlbumName || !this.albumName.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.albumName);
            }
            if (this.hasTrackNumber || this.trackNumber != 0) {
                codedOutputByteBufferNano.writeInt32(4, this.trackNumber);
            }
            if (this.hasPreviewUrl || !this.previewUrl.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.previewUrl);
            }
            if (this.displayArtist != null) {
                codedOutputByteBufferNano.writeMessage(6, this.displayArtist);
            }
            if (this.badge != null) {
                codedOutputByteBufferNano.writeMessage(7, this.badge);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class SubscriptionDetails extends MessageNano
    {
        public boolean hasSubscriptionPeriod;
        public int subscriptionPeriod;
        
        public SubscriptionDetails() {
            super();
            this.clear();
        }
        
        public SubscriptionDetails clear() {
            this.subscriptionPeriod = 1;
            this.hasSubscriptionPeriod = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.subscriptionPeriod != 1 || this.hasSubscriptionPeriod) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.subscriptionPeriod);
            }
            return computeSerializedSize;
        }
        
        @Override
        public SubscriptionDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                            case 5: {
                                this.subscriptionPeriod = int32;
                                this.hasSubscriptionPeriod = true;
                                continue;
                            }
                        }
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.subscriptionPeriod != 1 || this.hasSubscriptionPeriod) {
                codedOutputByteBufferNano.writeInt32(1, this.subscriptionPeriod);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class TalentDetails extends MessageNano
    {
        public TalentExternalLinks externalLinks;
        public boolean hasPrimaryRoleId;
        public int primaryRoleId;
        
        public TalentDetails() {
            super();
            this.clear();
        }
        
        public TalentDetails clear() {
            this.externalLinks = null;
            this.primaryRoleId = 0;
            this.hasPrimaryRoleId = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.externalLinks != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.externalLinks);
            }
            if (this.primaryRoleId != 0 || this.hasPrimaryRoleId) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.primaryRoleId);
            }
            return computeSerializedSize;
        }
        
        @Override
        public TalentDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        if (this.externalLinks == null) {
                            this.externalLinks = new TalentExternalLinks();
                        }
                        codedInputByteBufferNano.readMessage(this.externalLinks);
                        continue;
                    }
                    case 16: {
                        int32 = codedInputByteBufferNano.readInt32();
                        switch (int32) {
                            default: {
                                continue;
                            }
                            case 0:
                            case 1:
                            case 2:
                            case 3:
                            case 4: {
                                this.primaryRoleId = int32;
                                this.hasPrimaryRoleId = true;
                                continue;
                            }
                        }
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.externalLinks != null) {
                codedOutputByteBufferNano.writeMessage(1, this.externalLinks);
            }
            if (this.primaryRoleId != 0 || this.hasPrimaryRoleId) {
                codedOutputByteBufferNano.writeInt32(2, this.primaryRoleId);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class TalentExternalLinks extends MessageNano
    {
        public DocAnnotations.Link googlePlusProfileUrl;
        public DocAnnotations.Link[] websiteUrl;
        public DocAnnotations.Link youtubeChannelUrl;
        
        public TalentExternalLinks() {
            super();
            this.clear();
        }
        
        public TalentExternalLinks clear() {
            this.websiteUrl = DocAnnotations.Link.emptyArray();
            this.googlePlusProfileUrl = null;
            this.youtubeChannelUrl = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            DocAnnotations.Link link;
            computeSerializedSize = super.computeSerializedSize();
            if (this.websiteUrl != null && this.websiteUrl.length > 0) {
                for (int i = 0; i < this.websiteUrl.length; ++i) {
                    link = this.websiteUrl[i];
                    if (link != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, link);
                    }
                }
            }
            if (this.googlePlusProfileUrl != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.googlePlusProfileUrl);
            }
            if (this.youtubeChannelUrl != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, this.youtubeChannelUrl);
            }
            return computeSerializedSize;
        }
        
        @Override
        public TalentExternalLinks mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            DocAnnotations.Link[] websiteUrl;
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
                        if (this.websiteUrl == null) {
                            i = 0;
                        }
                        else {
                            i = this.websiteUrl.length;
                        }
                        websiteUrl = new DocAnnotations.Link[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.websiteUrl, 0, websiteUrl, 0, i);
                        }
                        while (i < -1 + websiteUrl.length) {
                            codedInputByteBufferNano.readMessage(websiteUrl[i] = new DocAnnotations.Link());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(websiteUrl[i] = new DocAnnotations.Link());
                        this.websiteUrl = websiteUrl;
                        continue;
                    }
                    case 18: {
                        if (this.googlePlusProfileUrl == null) {
                            this.googlePlusProfileUrl = new DocAnnotations.Link();
                        }
                        codedInputByteBufferNano.readMessage(this.googlePlusProfileUrl);
                        continue;
                    }
                    case 26: {
                        if (this.youtubeChannelUrl == null) {
                            this.youtubeChannelUrl = new DocAnnotations.Link();
                        }
                        codedInputByteBufferNano.readMessage(this.youtubeChannelUrl);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            DocAnnotations.Link link;
            if (this.websiteUrl != null && this.websiteUrl.length > 0) {
                for (int i = 0; i < this.websiteUrl.length; ++i) {
                    link = this.websiteUrl[i];
                    if (link != null) {
                        codedOutputByteBufferNano.writeMessage(1, link);
                    }
                }
            }
            if (this.googlePlusProfileUrl != null) {
                codedOutputByteBufferNano.writeMessage(2, this.googlePlusProfileUrl);
            }
            if (this.youtubeChannelUrl != null) {
                codedOutputByteBufferNano.writeMessage(3, this.youtubeChannelUrl);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class Trailer extends MessageNano
    {
        private static volatile Trailer[] _emptyArray;
        public String duration;
        public boolean hasDuration;
        public boolean hasThumbnailUrl;
        public boolean hasTitle;
        public boolean hasTrailerId;
        public boolean hasWatchUrl;
        public String thumbnailUrl;
        public String title;
        public String trailerId;
        public String watchUrl;
        
        public Trailer() {
            super();
            this.clear();
        }
        
        public static Trailer[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Trailer[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public Trailer clear() {
            this.trailerId = "";
            this.hasTrailerId = false;
            this.title = "";
            this.hasTitle = false;
            this.thumbnailUrl = "";
            this.hasThumbnailUrl = false;
            this.watchUrl = "";
            this.hasWatchUrl = false;
            this.duration = "";
            this.hasDuration = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasTrailerId || !this.trailerId.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.trailerId);
            }
            if (this.hasTitle || !this.title.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.title);
            }
            if (this.hasThumbnailUrl || !this.thumbnailUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.thumbnailUrl);
            }
            if (this.hasWatchUrl || !this.watchUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.watchUrl);
            }
            if (this.hasDuration || !this.duration.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.duration);
            }
            return computeSerializedSize;
        }
        
        @Override
        public Trailer mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                    case 10: {
                        this.trailerId = codedInputByteBufferNano.readString();
                        this.hasTrailerId = true;
                        continue;
                    }
                    case 18: {
                        this.title = codedInputByteBufferNano.readString();
                        this.hasTitle = true;
                        continue;
                    }
                    case 26: {
                        this.thumbnailUrl = codedInputByteBufferNano.readString();
                        this.hasThumbnailUrl = true;
                        continue;
                    }
                    case 34: {
                        this.watchUrl = codedInputByteBufferNano.readString();
                        this.hasWatchUrl = true;
                        continue;
                    }
                    case 42: {
                        this.duration = codedInputByteBufferNano.readString();
                        this.hasDuration = true;
                        continue;
                    }
                }
            }
            return this;
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasTrailerId || !this.trailerId.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.trailerId);
            }
            if (this.hasTitle || !this.title.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.title);
            }
            if (this.hasThumbnailUrl || !this.thumbnailUrl.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.thumbnailUrl);
            }
            if (this.hasWatchUrl || !this.watchUrl.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.watchUrl);
            }
            if (this.hasDuration || !this.duration.equals("")) {
                codedOutputByteBufferNano.writeString(5, this.duration);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class TvEpisodeDetails extends MessageNano
    {
        public int episodeIndex;
        public boolean hasEpisodeIndex;
        public boolean hasParentDetailsUrl;
        public boolean hasReleaseDate;
        public String parentDetailsUrl;
        public String releaseDate;
        
        public TvEpisodeDetails() {
            super();
            this.clear();
        }
        
        public TvEpisodeDetails clear() {
            this.parentDetailsUrl = "";
            this.hasParentDetailsUrl = false;
            this.episodeIndex = 0;
            this.hasEpisodeIndex = false;
            this.releaseDate = "";
            this.hasReleaseDate = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasParentDetailsUrl || !this.parentDetailsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.parentDetailsUrl);
            }
            if (this.hasEpisodeIndex || this.episodeIndex != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.episodeIndex);
            }
            if (this.hasReleaseDate || !this.releaseDate.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.releaseDate);
            }
            return computeSerializedSize;
        }
        
        @Override
        public TvEpisodeDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.parentDetailsUrl = codedInputByteBufferNano.readString();
                        this.hasParentDetailsUrl = true;
                        continue;
                    }
                    case 16: {
                        this.episodeIndex = codedInputByteBufferNano.readInt32();
                        this.hasEpisodeIndex = true;
                        continue;
                    }
                    case 26: {
                        this.releaseDate = codedInputByteBufferNano.readString();
                        this.hasReleaseDate = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasParentDetailsUrl || !this.parentDetailsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.parentDetailsUrl);
            }
            if (this.hasEpisodeIndex || this.episodeIndex != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.episodeIndex);
            }
            if (this.hasReleaseDate || !this.releaseDate.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.releaseDate);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class TvSeasonDetails extends MessageNano
    {
        public String broadcaster;
        public int episodeCount;
        public int expectedEpisodeCount;
        public boolean hasBroadcaster;
        public boolean hasEpisodeCount;
        public boolean hasExpectedEpisodeCount;
        public boolean hasParentDetailsUrl;
        public boolean hasReleaseDate;
        public boolean hasSeasonIndex;
        public String parentDetailsUrl;
        public String releaseDate;
        public int seasonIndex;
        
        public TvSeasonDetails() {
            super();
            this.clear();
        }
        
        public TvSeasonDetails clear() {
            this.parentDetailsUrl = "";
            this.hasParentDetailsUrl = false;
            this.seasonIndex = 0;
            this.hasSeasonIndex = false;
            this.releaseDate = "";
            this.hasReleaseDate = false;
            this.broadcaster = "";
            this.hasBroadcaster = false;
            this.episodeCount = 0;
            this.hasEpisodeCount = false;
            this.expectedEpisodeCount = 0;
            this.hasExpectedEpisodeCount = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasParentDetailsUrl || !this.parentDetailsUrl.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(1, this.parentDetailsUrl);
            }
            if (this.hasSeasonIndex || this.seasonIndex != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.seasonIndex);
            }
            if (this.hasReleaseDate || !this.releaseDate.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.releaseDate);
            }
            if (this.hasBroadcaster || !this.broadcaster.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.broadcaster);
            }
            if (this.hasEpisodeCount || this.episodeCount != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(5, this.episodeCount);
            }
            if (this.hasExpectedEpisodeCount || this.expectedEpisodeCount != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(6, this.expectedEpisodeCount);
            }
            return computeSerializedSize;
        }
        
        @Override
        public TvSeasonDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.parentDetailsUrl = codedInputByteBufferNano.readString();
                        this.hasParentDetailsUrl = true;
                        continue;
                    }
                    case 16: {
                        this.seasonIndex = codedInputByteBufferNano.readInt32();
                        this.hasSeasonIndex = true;
                        continue;
                    }
                    case 26: {
                        this.releaseDate = codedInputByteBufferNano.readString();
                        this.hasReleaseDate = true;
                        continue;
                    }
                    case 34: {
                        this.broadcaster = codedInputByteBufferNano.readString();
                        this.hasBroadcaster = true;
                        continue;
                    }
                    case 40: {
                        this.episodeCount = codedInputByteBufferNano.readInt32();
                        this.hasEpisodeCount = true;
                        continue;
                    }
                    case 48: {
                        this.expectedEpisodeCount = codedInputByteBufferNano.readInt32();
                        this.hasExpectedEpisodeCount = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasParentDetailsUrl || !this.parentDetailsUrl.equals("")) {
                codedOutputByteBufferNano.writeString(1, this.parentDetailsUrl);
            }
            if (this.hasSeasonIndex || this.seasonIndex != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.seasonIndex);
            }
            if (this.hasReleaseDate || !this.releaseDate.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.releaseDate);
            }
            if (this.hasBroadcaster || !this.broadcaster.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.broadcaster);
            }
            if (this.hasEpisodeCount || this.episodeCount != 0) {
                codedOutputByteBufferNano.writeInt32(5, this.episodeCount);
            }
            if (this.hasExpectedEpisodeCount || this.expectedEpisodeCount != 0) {
                codedOutputByteBufferNano.writeInt32(6, this.expectedEpisodeCount);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class TvShowDetails extends MessageNano
    {
        public String broadcaster;
        public int endYear;
        public boolean hasBroadcaster;
        public boolean hasEndYear;
        public boolean hasSeasonCount;
        public boolean hasStartYear;
        public int seasonCount;
        public int startYear;
        
        public TvShowDetails() {
            super();
            this.clear();
        }
        
        public TvShowDetails clear() {
            this.seasonCount = 0;
            this.hasSeasonCount = false;
            this.startYear = 0;
            this.hasStartYear = false;
            this.endYear = 0;
            this.hasEndYear = false;
            this.broadcaster = "";
            this.hasBroadcaster = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasSeasonCount || this.seasonCount != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.seasonCount);
            }
            if (this.hasStartYear || this.startYear != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.startYear);
            }
            if (this.hasEndYear || this.endYear != 0) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.endYear);
            }
            if (this.hasBroadcaster || !this.broadcaster.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.broadcaster);
            }
            return computeSerializedSize;
        }
        
        @Override
        public TvShowDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        this.seasonCount = codedInputByteBufferNano.readInt32();
                        this.hasSeasonCount = true;
                        continue;
                    }
                    case 16: {
                        this.startYear = codedInputByteBufferNano.readInt32();
                        this.hasStartYear = true;
                        continue;
                    }
                    case 24: {
                        this.endYear = codedInputByteBufferNano.readInt32();
                        this.hasEndYear = true;
                        continue;
                    }
                    case 34: {
                        this.broadcaster = codedInputByteBufferNano.readString();
                        this.hasBroadcaster = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.hasSeasonCount || this.seasonCount != 0) {
                codedOutputByteBufferNano.writeInt32(1, this.seasonCount);
            }
            if (this.hasStartYear || this.startYear != 0) {
                codedOutputByteBufferNano.writeInt32(2, this.startYear);
            }
            if (this.hasEndYear || this.endYear != 0) {
                codedOutputByteBufferNano.writeInt32(3, this.endYear);
            }
            if (this.hasBroadcaster || !this.broadcaster.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.broadcaster);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class VideoCredit extends MessageNano
    {
        private static volatile VideoCredit[] _emptyArray;
        public String credit;
        public int creditType;
        public boolean hasCredit;
        public boolean hasCreditType;
        public String[] name;
        
        public VideoCredit() {
            super();
            this.clear();
        }
        
        public static VideoCredit[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new VideoCredit[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public VideoCredit clear() {
            this.creditType = 0;
            this.hasCreditType = false;
            this.credit = "";
            this.hasCredit = false;
            this.name = WireFormatNano.EMPTY_STRING_ARRAY;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            int n;
            int n2;
            String s;
            computeSerializedSize = super.computeSerializedSize();
            if (this.creditType != 0 || this.hasCreditType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.creditType);
            }
            if (this.hasCredit || !this.credit.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.credit);
            }
            if (this.name != null && this.name.length > 0) {
                n = 0;
                n2 = 0;
                for (int i = 0; i < this.name.length; ++i) {
                    s = this.name[i];
                    if (s != null) {
                        ++n;
                        n2 += CodedOutputByteBufferNano.computeStringSizeNoTag(s);
                    }
                }
                computeSerializedSize = computeSerializedSize + n2 + n * 1;
            }
            return computeSerializedSize;
        }
        
        @Override
        public VideoCredit mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
            int repeatedFieldArrayLength;
            int i;
            String[] name;
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
                            case 0:
                            case 1:
                            case 2:
                            case 3: {
                                this.creditType = int32;
                                this.hasCreditType = true;
                                continue;
                            }
                        }
                    }
                    case 18: {
                        this.credit = codedInputByteBufferNano.readString();
                        this.hasCredit = true;
                        continue;
                    }
                    case 26: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 26);
                        if (this.name == null) {
                            i = 0;
                        }
                        else {
                            i = this.name.length;
                        }
                        name = new String[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.name, 0, name, 0, i);
                        }
                        while (i < -1 + name.length) {
                            name[i] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        name[i] = codedInputByteBufferNano.readString();
                        this.name = name;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            String s;
            if (this.creditType != 0 || this.hasCreditType) {
                codedOutputByteBufferNano.writeInt32(1, this.creditType);
            }
            if (this.hasCredit || !this.credit.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.credit);
            }
            if (this.name != null && this.name.length > 0) {
                for (int i = 0; i < this.name.length; ++i) {
                    s = this.name[i];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(3, s);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class VideoDetails extends MessageNano
    {
        public String[] audioLanguage;
        public String[] captionLanguage;
        public String contentRating;
        public VideoCredit[] credit;
        public long dislikes;
        public String duration;
        public String[] genre;
        public boolean hasContentRating;
        public boolean hasDislikes;
        public boolean hasDuration;
        public boolean hasLikes;
        public boolean hasReleaseDate;
        public long likes;
        public String releaseDate;
        public VideoRentalTerm[] rentalTerm;
        public Trailer[] trailer;
        
        public VideoDetails() {
            super();
            this.clear();
        }
        
        public VideoDetails clear() {
            this.credit = VideoCredit.emptyArray();
            this.duration = "";
            this.hasDuration = false;
            this.releaseDate = "";
            this.hasReleaseDate = false;
            this.contentRating = "";
            this.hasContentRating = false;
            this.likes = 0L;
            this.hasLikes = false;
            this.dislikes = 0L;
            this.hasDislikes = false;
            this.genre = WireFormatNano.EMPTY_STRING_ARRAY;
            this.trailer = Trailer.emptyArray();
            this.rentalTerm = VideoRentalTerm.emptyArray();
            this.audioLanguage = WireFormatNano.EMPTY_STRING_ARRAY;
            this.captionLanguage = WireFormatNano.EMPTY_STRING_ARRAY;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            VideoCredit videoCredit;
            int n;
            int n2;
            String s;
            Trailer trailer;
            VideoRentalTerm videoRentalTerm;
            int n3;
            int n4;
            String s2;
            int n6;
            int n7;
            String s3;
            computeSerializedSize = super.computeSerializedSize();
            if (this.credit != null && this.credit.length > 0) {
                for (int i = 0; i < this.credit.length; ++i) {
                    videoCredit = this.credit[i];
                    if (videoCredit != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, videoCredit);
                    }
                }
            }
            if (this.hasDuration || !this.duration.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.duration);
            }
            if (this.hasReleaseDate || !this.releaseDate.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.releaseDate);
            }
            if (this.hasContentRating || !this.contentRating.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(4, this.contentRating);
            }
            if (this.hasLikes || this.likes != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(5, this.likes);
            }
            if (this.hasDislikes || this.dislikes != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(6, this.dislikes);
            }
            if (this.genre != null && this.genre.length > 0) {
                n = 0;
                n2 = 0;
                for (int j = 0; j < this.genre.length; ++j) {
                    s = this.genre[j];
                    if (s != null) {
                        ++n;
                        n2 += CodedOutputByteBufferNano.computeStringSizeNoTag(s);
                    }
                }
                computeSerializedSize = computeSerializedSize + n2 + n * 1;
            }
            if (this.trailer != null && this.trailer.length > 0) {
                for (int k = 0; k < this.trailer.length; ++k) {
                    trailer = this.trailer[k];
                    if (trailer != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(8, trailer);
                    }
                }
            }
            if (this.rentalTerm != null && this.rentalTerm.length > 0) {
                for (int l = 0; l < this.rentalTerm.length; ++l) {
                    videoRentalTerm = this.rentalTerm[l];
                    if (videoRentalTerm != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(9, videoRentalTerm);
                    }
                }
            }
            if (this.audioLanguage != null && this.audioLanguage.length > 0) {
                n3 = 0;
                n4 = 0;
                for (int n5 = 0; n5 < this.audioLanguage.length; ++n5) {
                    s2 = this.audioLanguage[n5];
                    if (s2 != null) {
                        ++n3;
                        n4 += CodedOutputByteBufferNano.computeStringSizeNoTag(s2);
                    }
                }
                computeSerializedSize = computeSerializedSize + n4 + n3 * 1;
            }
            if (this.captionLanguage != null && this.captionLanguage.length > 0) {
                n6 = 0;
                n7 = 0;
                for (int n8 = 0; n8 < this.captionLanguage.length; ++n8) {
                    s3 = this.captionLanguage[n8];
                    if (s3 != null) {
                        ++n6;
                        n7 += CodedOutputByteBufferNano.computeStringSizeNoTag(s3);
                    }
                }
                computeSerializedSize = computeSerializedSize + n7 + n6 * 1;
            }
            return computeSerializedSize;
        }
        
        @Override
        public VideoDetails mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            VideoCredit[] credit;
            int repeatedFieldArrayLength2;
            int j;
            String[] genre;
            int repeatedFieldArrayLength3;
            int k;
            Trailer[] trailer;
            int repeatedFieldArrayLength4;
            int l;
            VideoRentalTerm[] rentalTerm;
            int repeatedFieldArrayLength5;
            int length;
            String[] audioLanguage;
            int repeatedFieldArrayLength6;
            int length2;
            String[] captionLanguage;
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
                        if (this.credit == null) {
                            i = 0;
                        }
                        else {
                            i = this.credit.length;
                        }
                        credit = new VideoCredit[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.credit, 0, credit, 0, i);
                        }
                        while (i < -1 + credit.length) {
                            codedInputByteBufferNano.readMessage(credit[i] = new VideoCredit());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(credit[i] = new VideoCredit());
                        this.credit = credit;
                        continue;
                    }
                    case 18: {
                        this.duration = codedInputByteBufferNano.readString();
                        this.hasDuration = true;
                        continue;
                    }
                    case 26: {
                        this.releaseDate = codedInputByteBufferNano.readString();
                        this.hasReleaseDate = true;
                        continue;
                    }
                    case 34: {
                        this.contentRating = codedInputByteBufferNano.readString();
                        this.hasContentRating = true;
                        continue;
                    }
                    case 40: {
                        this.likes = codedInputByteBufferNano.readInt64();
                        this.hasLikes = true;
                        continue;
                    }
                    case 48: {
                        this.dislikes = codedInputByteBufferNano.readInt64();
                        this.hasDislikes = true;
                        continue;
                    }
                    case 58: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 58);
                        if (this.genre == null) {
                            j = 0;
                        }
                        else {
                            j = this.genre.length;
                        }
                        genre = new String[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.genre, 0, genre, 0, j);
                        }
                        while (j < -1 + genre.length) {
                            genre[j] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        genre[j] = codedInputByteBufferNano.readString();
                        this.genre = genre;
                        continue;
                    }
                    case 66: {
                        repeatedFieldArrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 66);
                        if (this.trailer == null) {
                            k = 0;
                        }
                        else {
                            k = this.trailer.length;
                        }
                        trailer = new Trailer[k + repeatedFieldArrayLength3];
                        if (k != 0) {
                            System.arraycopy(this.trailer, 0, trailer, 0, k);
                        }
                        while (k < -1 + trailer.length) {
                            codedInputByteBufferNano.readMessage(trailer[k] = new Trailer());
                            codedInputByteBufferNano.readTag();
                            ++k;
                        }
                        codedInputByteBufferNano.readMessage(trailer[k] = new Trailer());
                        this.trailer = trailer;
                        continue;
                    }
                    case 74: {
                        repeatedFieldArrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 74);
                        if (this.rentalTerm == null) {
                            l = 0;
                        }
                        else {
                            l = this.rentalTerm.length;
                        }
                        rentalTerm = new VideoRentalTerm[l + repeatedFieldArrayLength4];
                        if (l != 0) {
                            System.arraycopy(this.rentalTerm, 0, rentalTerm, 0, l);
                        }
                        while (l < -1 + rentalTerm.length) {
                            codedInputByteBufferNano.readMessage(rentalTerm[l] = new VideoRentalTerm());
                            codedInputByteBufferNano.readTag();
                            ++l;
                        }
                        codedInputByteBufferNano.readMessage(rentalTerm[l] = new VideoRentalTerm());
                        this.rentalTerm = rentalTerm;
                        continue;
                    }
                    case 82: {
                        repeatedFieldArrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 82);
                        if (this.audioLanguage == null) {
                            length = 0;
                        }
                        else {
                            length = this.audioLanguage.length;
                        }
                        audioLanguage = new String[length + repeatedFieldArrayLength5];
                        if (length != 0) {
                            System.arraycopy(this.audioLanguage, 0, audioLanguage, 0, length);
                        }
                        while (length < -1 + audioLanguage.length) {
                            audioLanguage[length] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++length;
                        }
                        audioLanguage[length] = codedInputByteBufferNano.readString();
                        this.audioLanguage = audioLanguage;
                        continue;
                    }
                    case 90: {
                        repeatedFieldArrayLength6 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 90);
                        if (this.captionLanguage == null) {
                            length2 = 0;
                        }
                        else {
                            length2 = this.captionLanguage.length;
                        }
                        captionLanguage = new String[length2 + repeatedFieldArrayLength6];
                        if (length2 != 0) {
                            System.arraycopy(this.captionLanguage, 0, captionLanguage, 0, length2);
                        }
                        while (length2 < -1 + captionLanguage.length) {
                            captionLanguage[length2] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++length2;
                        }
                        captionLanguage[length2] = codedInputByteBufferNano.readString();
                        this.captionLanguage = captionLanguage;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            VideoCredit videoCredit;
            String s;
            Trailer trailer;
            VideoRentalTerm videoRentalTerm;
            String s2;
            String s3;
            if (this.credit != null && this.credit.length > 0) {
                for (int i = 0; i < this.credit.length; ++i) {
                    videoCredit = this.credit[i];
                    if (videoCredit != null) {
                        codedOutputByteBufferNano.writeMessage(1, videoCredit);
                    }
                }
            }
            if (this.hasDuration || !this.duration.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.duration);
            }
            if (this.hasReleaseDate || !this.releaseDate.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.releaseDate);
            }
            if (this.hasContentRating || !this.contentRating.equals("")) {
                codedOutputByteBufferNano.writeString(4, this.contentRating);
            }
            if (this.hasLikes || this.likes != 0L) {
                codedOutputByteBufferNano.writeInt64(5, this.likes);
            }
            if (this.hasDislikes || this.dislikes != 0L) {
                codedOutputByteBufferNano.writeInt64(6, this.dislikes);
            }
            if (this.genre != null && this.genre.length > 0) {
                for (int j = 0; j < this.genre.length; ++j) {
                    s = this.genre[j];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(7, s);
                    }
                }
            }
            if (this.trailer != null && this.trailer.length > 0) {
                for (int k = 0; k < this.trailer.length; ++k) {
                    trailer = this.trailer[k];
                    if (trailer != null) {
                        codedOutputByteBufferNano.writeMessage(8, trailer);
                    }
                }
            }
            if (this.rentalTerm != null && this.rentalTerm.length > 0) {
                for (int l = 0; l < this.rentalTerm.length; ++l) {
                    videoRentalTerm = this.rentalTerm[l];
                    if (videoRentalTerm != null) {
                        codedOutputByteBufferNano.writeMessage(9, videoRentalTerm);
                    }
                }
            }
            if (this.audioLanguage != null && this.audioLanguage.length > 0) {
                for (int n = 0; n < this.audioLanguage.length; ++n) {
                    s2 = this.audioLanguage[n];
                    if (s2 != null) {
                        codedOutputByteBufferNano.writeString(10, s2);
                    }
                }
            }
            if (this.captionLanguage != null && this.captionLanguage.length > 0) {
                for (int n2 = 0; n2 < this.captionLanguage.length; ++n2) {
                    s3 = this.captionLanguage[n2];
                    if (s3 != null) {
                        codedOutputByteBufferNano.writeString(11, s3);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class VideoRentalTerm extends MessageNano
    {
        private static volatile VideoRentalTerm[] _emptyArray;
        public boolean hasOfferAbbreviation;
        public boolean hasOfferType;
        public boolean hasRentalHeader;
        public String offerAbbreviation;
        public int offerType;
        public String rentalHeader;
        public Term[] term;
        
        public VideoRentalTerm() {
            super();
            this.clear();
        }
        
        public static VideoRentalTerm[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new VideoRentalTerm[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public VideoRentalTerm clear() {
            this.offerType = 1;
            this.hasOfferType = false;
            this.offerAbbreviation = "";
            this.hasOfferAbbreviation = false;
            this.rentalHeader = "";
            this.hasRentalHeader = false;
            this.term = Term.emptyArray();
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            Term term;
            computeSerializedSize = super.computeSerializedSize();
            if (this.offerType != 1 || this.hasOfferType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.offerType);
            }
            if (this.hasOfferAbbreviation || !this.offerAbbreviation.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(2, this.offerAbbreviation);
            }
            if (this.hasRentalHeader || !this.rentalHeader.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(3, this.rentalHeader);
            }
            if (this.term != null && this.term.length > 0) {
                for (int i = 0; i < this.term.length; ++i) {
                    term = this.term[i];
                    if (term != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeGroupSize(4, term);
                    }
                }
            }
            return computeSerializedSize;
        }
        
        @Override
        public VideoRentalTerm mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
            int repeatedFieldArrayLength;
            int i;
            Term[] term;
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
                                this.offerType = int32;
                                this.hasOfferType = true;
                                continue;
                            }
                        }
                    }
                    case 18: {
                        this.offerAbbreviation = codedInputByteBufferNano.readString();
                        this.hasOfferAbbreviation = true;
                        continue;
                    }
                    case 26: {
                        this.rentalHeader = codedInputByteBufferNano.readString();
                        this.hasRentalHeader = true;
                        continue;
                    }
                    case 35: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 35);
                        if (this.term == null) {
                            i = 0;
                        }
                        else {
                            i = this.term.length;
                        }
                        term = new Term[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.term, 0, term, 0, i);
                        }
                        while (i < -1 + term.length) {
                            codedInputByteBufferNano.readGroup(term[i] = new Term(), 4);
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readGroup(term[i] = new Term(), 4);
                        this.term = term;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            Term term;
            if (this.offerType != 1 || this.hasOfferType) {
                codedOutputByteBufferNano.writeInt32(1, this.offerType);
            }
            if (this.hasOfferAbbreviation || !this.offerAbbreviation.equals("")) {
                codedOutputByteBufferNano.writeString(2, this.offerAbbreviation);
            }
            if (this.hasRentalHeader || !this.rentalHeader.equals("")) {
                codedOutputByteBufferNano.writeString(3, this.rentalHeader);
            }
            if (this.term != null && this.term.length > 0) {
                for (int i = 0; i < this.term.length; ++i) {
                    term = this.term[i];
                    if (term != null) {
                        codedOutputByteBufferNano.writeGroup(4, term);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
        
        public static final class Term extends MessageNano
        {
            private static volatile Term[] _emptyArray;
            public String body;
            public boolean hasBody;
            public boolean hasHeader;
            public String header;
            
            public Term() {
                super();
                this.clear();
            }
            
            public static Term[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new Term[0];
                        }
                    }
                }
                return _emptyArray;
            }
            
            public Term clear() {
                this.header = "";
                this.hasHeader = false;
                this.body = "";
                this.hasBody = false;
                this.cachedSize = -1;
                return this;
            }
            
            @Override
            protected int computeSerializedSize() {
                int computeSerializedSize;
                computeSerializedSize = super.computeSerializedSize();
                if (this.hasHeader || !this.header.equals("")) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(5, this.header);
                }
                if (this.hasBody || !this.body.equals("")) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(6, this.body);
                }
                return computeSerializedSize;
            }
            
            @Override
            public Term mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        case 42: {
                            this.header = codedInputByteBufferNano.readString();
                            this.hasHeader = true;
                            continue;
                        }
                        case 50: {
                            this.body = codedInputByteBufferNano.readString();
                            this.hasBody = true;
                            continue;
                        }
                    }
                }
            }
            
            @Override
            public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
                if (this.hasHeader || !this.header.equals("")) {
                    codedOutputByteBufferNano.writeString(5, this.header);
                }
                if (this.hasBody || !this.body.equals("")) {
                    codedOutputByteBufferNano.writeString(6, this.body);
                }
                super.writeTo(codedOutputByteBufferNano);
            }
        }
    }
}
