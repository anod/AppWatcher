package com.google.android.finsky.protos;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;

import java.io.IOException;
import java.util.Arrays;

public abstract interface VendingProtos {

    public static final class AppDataProto extends MessageNano {
        private static volatile AppDataProto[] _emptyArray;
        public boolean hasKey;
        public boolean hasValue;
        public String key;
        public String value;

        public AppDataProto() {
            clear();
        }

        public static AppDataProto[] emptyArray() {
            if (_emptyArray == null) ;
            synchronized (InternalNano.LAZY_INIT_LOCK) {
                if (_emptyArray == null)
                    _emptyArray = new AppDataProto[0];
                return _emptyArray;
            }
        }

        public AppDataProto clear() {
            this.key = "";
            this.hasKey = false;
            this.value = "";
            this.hasValue = false;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.hasKey) || (!this.key.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(1, this.key);
            if ((this.hasValue) || (!this.value.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(2, this.value);
            return i;
        }

        public AppDataProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
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
                        this.key = paramCodedInputByteBufferNano.readString();
                        this.hasKey = true;
                        break;
                    case 18:
                }
                this.value = paramCodedInputByteBufferNano.readString();
                this.hasValue = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.hasKey) || (!this.key.equals("")))
                paramCodedOutputByteBufferNano.writeString(1, this.key);
            if ((this.hasValue) || (!this.value.equals("")))
                paramCodedOutputByteBufferNano.writeString(2, this.value);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class ContentSyncRequestProto extends MessageNano {
        public AssetInstallState[] assetInstallState;
        public boolean hasIncremental;
        public boolean hasSideloadedAppCount;
        public boolean incremental;
        public int sideloadedAppCount;
        public SystemApp[] systemApp;

        public ContentSyncRequestProto() {
            clear();
        }

        public ContentSyncRequestProto clear() {
            this.incremental = false;
            this.hasIncremental = false;
            this.sideloadedAppCount = 0;
            this.hasSideloadedAppCount = false;
            this.assetInstallState = AssetInstallState.emptyArray();
            this.systemApp = SystemApp.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.hasIncremental) || (this.incremental))
                i += CodedOutputByteBufferNano.computeBoolSize(1, this.incremental);
            if ((this.assetInstallState != null) && (this.assetInstallState.length > 0))
                for (int k = 0; k < this.assetInstallState.length; k++) {
                    AssetInstallState localAssetInstallState = this.assetInstallState[k];
                    if (localAssetInstallState != null)
                        i += CodedOutputByteBufferNano.computeGroupSize(2, localAssetInstallState);
                }
            if ((this.systemApp != null) && (this.systemApp.length > 0))
                for (int j = 0; j < this.systemApp.length; j++) {
                    SystemApp localSystemApp = this.systemApp[j];
                    if (localSystemApp != null)
                        i += CodedOutputByteBufferNano.computeGroupSize(10, localSystemApp);
                }
            if ((this.hasSideloadedAppCount) || (this.sideloadedAppCount != 0))
                i += CodedOutputByteBufferNano.computeInt32Size(14, this.sideloadedAppCount);
            return i;
        }

        public ContentSyncRequestProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException {
            while (true) {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i) {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 8:
                        this.incremental = paramCodedInputByteBufferNano.readBool();
                        this.hasIncremental = true;
                        break;
                    case 19:
                        int m = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 19);
//          AssetInstallState[] arrayOfAssetInstallState;
//          for (int n = 0; ; n = this.assetInstallState.length)
//          {
//            arrayOfAssetInstallState = new AssetInstallState[n + m];
//            if (n != 0)
//              System.arraycopy(this.assetInstallState, 0, arrayOfAssetInstallState, 0, n);
//            while (n < -1 + arrayOfAssetInstallState.length)
//            {
//              arrayOfAssetInstallState[n] = new AssetInstallState();
//              paramCodedInputByteBufferNano.readGroup(arrayOfAssetInstallState[n], 2);
//              paramCodedInputByteBufferNano.readTag();
//              n++;
//            }
//          }
//            if (this.assetInstallState == null) {
//                int n = this.assetInstallState.length;
//                arrayOfAssetInstallState[n] = new AssetInstallState();
//                paramCodedInputByteBufferNano.readGroup(arrayOfAssetInstallState[n], 2);
//                this.assetInstallState = arrayOfAssetInstallState;
//            }
                        break;
                    case 83:
                        int j = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 83);
//          if (this.systemApp == null);
//          SystemApp[] arrayOfSystemApp;
//          for (int k = 0; ; k = this.systemApp.length)
//          {
//            arrayOfSystemApp = new SystemApp[k + j];
//            if (k != 0)
//              System.arraycopy(this.systemApp, 0, arrayOfSystemApp, 0, k);
//            while (k < -1 + arrayOfSystemApp.length)
//            {
//              arrayOfSystemApp[k] = new SystemApp();
//              paramCodedInputByteBufferNano.readGroup(arrayOfSystemApp[k], 10);
//              paramCodedInputByteBufferNano.readTag();
//              k++;
//            }
//          }
//          arrayOfSystemApp[k] = new SystemApp();
//          paramCodedInputByteBufferNano.readGroup(arrayOfSystemApp[k], 10);
//          this.systemApp = arrayOfSystemApp;
                        break;
                    case 112:
                }
                this.sideloadedAppCount = paramCodedInputByteBufferNano.readInt32();
                this.hasSideloadedAppCount = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.hasIncremental) || (this.incremental))
                paramCodedOutputByteBufferNano.writeBool(1, this.incremental);
            if ((this.assetInstallState != null) && (this.assetInstallState.length > 0))
                for (int j = 0; j < this.assetInstallState.length; j++) {
                    AssetInstallState localAssetInstallState = this.assetInstallState[j];
                    if (localAssetInstallState != null)
                        paramCodedOutputByteBufferNano.writeGroup(2, localAssetInstallState);
                }
            if ((this.systemApp != null) && (this.systemApp.length > 0))
                for (int i = 0; i < this.systemApp.length; i++) {
                    SystemApp localSystemApp = this.systemApp[i];
                    if (localSystemApp != null)
                        paramCodedOutputByteBufferNano.writeGroup(10, localSystemApp);
                }
            if ((this.hasSideloadedAppCount) || (this.sideloadedAppCount != 0))
                paramCodedOutputByteBufferNano.writeInt32(14, this.sideloadedAppCount);
            super.writeTo(paramCodedOutputByteBufferNano);
        }

        public static final class AssetInstallState extends MessageNano {
            private static volatile AssetInstallState[] _emptyArray;
            public String assetId;
            public String assetReferrer;
            public int assetState;
            public boolean hasAssetId;
            public boolean hasAssetReferrer;
            public boolean hasAssetState;
            public boolean hasInstallTime;
            public boolean hasPackageName;
            public boolean hasUninstallTime;
            public boolean hasVersionCode;
            public long installTime;
            public String packageName;
            public long uninstallTime;
            public int versionCode;

            public AssetInstallState() {
                clear();
            }

            public static AssetInstallState[] emptyArray() {
                if (_emptyArray == null) ;
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null)
                        _emptyArray = new AssetInstallState[0];
                    return _emptyArray;
                }
            }

            public AssetInstallState clear() {
                this.assetId = "";
                this.hasAssetId = false;
                this.assetState = 1;
                this.hasAssetState = false;
                this.installTime = 0L;
                this.hasInstallTime = false;
                this.uninstallTime = 0L;
                this.hasUninstallTime = false;
                this.packageName = "";
                this.hasPackageName = false;
                this.versionCode = 0;
                this.hasVersionCode = false;
                this.assetReferrer = "";
                this.hasAssetReferrer = false;
                this.cachedSize = -1;
                return this;
            }

            protected int computeSerializedSize() {
                int i = super.computeSerializedSize();
                if ((this.hasAssetId) || (!this.assetId.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(3, this.assetId);
                if ((this.assetState != 1) || (this.hasAssetState))
                    i += CodedOutputByteBufferNano.computeInt32Size(4, this.assetState);
                if ((this.hasInstallTime) || (this.installTime != 0L))
                    i += CodedOutputByteBufferNano.computeInt64Size(5, this.installTime);
                if ((this.hasUninstallTime) || (this.uninstallTime != 0L))
                    i += CodedOutputByteBufferNano.computeInt64Size(6, this.uninstallTime);
                if ((this.hasPackageName) || (!this.packageName.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(7, this.packageName);
                if ((this.hasVersionCode) || (this.versionCode != 0))
                    i += CodedOutputByteBufferNano.computeInt32Size(8, this.versionCode);
                if ((this.hasAssetReferrer) || (!this.assetReferrer.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(9, this.assetReferrer);
                return i;
            }

            public AssetInstallState mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                    throws IOException {
                while (true) {
                    int i = paramCodedInputByteBufferNano.readTag();
                    switch (i) {
                        default:
                            if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                                continue;
                        case 0:
                            return this;
                        case 26:
                            this.assetId = paramCodedInputByteBufferNano.readString();
                            this.hasAssetId = true;
                            break;
                        case 32:
                            int j = paramCodedInputByteBufferNano.readInt32();
                            switch (j) {
                                default:
                                    break;
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
                            }
                            this.assetState = j;
                            this.hasAssetState = true;
                            break;
                        case 40:
                            this.installTime = paramCodedInputByteBufferNano.readInt64();
                            this.hasInstallTime = true;
                            break;
                        case 48:
                            this.uninstallTime = paramCodedInputByteBufferNano.readInt64();
                            this.hasUninstallTime = true;
                            break;
                        case 58:
                            this.packageName = paramCodedInputByteBufferNano.readString();
                            this.hasPackageName = true;
                            break;
                        case 64:
                            this.versionCode = paramCodedInputByteBufferNano.readInt32();
                            this.hasVersionCode = true;
                            break;
                        case 74:
                    }
                    this.assetReferrer = paramCodedInputByteBufferNano.readString();
                    this.hasAssetReferrer = true;
                }
            }

            public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                    throws IOException {
                if ((this.hasAssetId) || (!this.assetId.equals("")))
                    paramCodedOutputByteBufferNano.writeString(3, this.assetId);
                if ((this.assetState != 1) || (this.hasAssetState))
                    paramCodedOutputByteBufferNano.writeInt32(4, this.assetState);
                if ((this.hasInstallTime) || (this.installTime != 0L))
                    paramCodedOutputByteBufferNano.writeInt64(5, this.installTime);
                if ((this.hasUninstallTime) || (this.uninstallTime != 0L))
                    paramCodedOutputByteBufferNano.writeInt64(6, this.uninstallTime);
                if ((this.hasPackageName) || (!this.packageName.equals("")))
                    paramCodedOutputByteBufferNano.writeString(7, this.packageName);
                if ((this.hasVersionCode) || (this.versionCode != 0))
                    paramCodedOutputByteBufferNano.writeInt32(8, this.versionCode);
                if ((this.hasAssetReferrer) || (!this.assetReferrer.equals("")))
                    paramCodedOutputByteBufferNano.writeString(9, this.assetReferrer);
                super.writeTo(paramCodedOutputByteBufferNano);
            }
        }

        public static final class SystemApp extends MessageNano {
            private static volatile SystemApp[] _emptyArray;
            public String[] certificateHash;
            public boolean hasPackageName;
            public boolean hasVersionCode;
            public String packageName;
            public int versionCode;

            public SystemApp() {
                clear();
            }

            public static SystemApp[] emptyArray() {
                if (_emptyArray == null) ;
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null)
                        _emptyArray = new SystemApp[0];
                    return _emptyArray;
                }
            }

            public SystemApp clear() {
                this.packageName = "";
                this.hasPackageName = false;
                this.versionCode = 0;
                this.hasVersionCode = false;
                this.certificateHash = WireFormatNano.EMPTY_STRING_ARRAY;
                this.cachedSize = -1;
                return this;
            }

            protected int computeSerializedSize() {
                int i = super.computeSerializedSize();
                if ((this.hasPackageName) || (!this.packageName.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(11, this.packageName);
                if ((this.hasVersionCode) || (this.versionCode != 0))
                    i += CodedOutputByteBufferNano.computeInt32Size(12, this.versionCode);
                if ((this.certificateHash != null) && (this.certificateHash.length > 0)) {
                    int j = 0;
                    int k = 0;
                    for (int m = 0; m < this.certificateHash.length; m++) {
                        String str = this.certificateHash[m];
                        if (str != null) {
                            j++;
                            k += CodedOutputByteBufferNano.computeStringSizeNoTag(str);
                        }
                    }
                    i = i + k + j * 1;
                }
                return i;
            }

            public SystemApp mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                    throws IOException {
                while (true) {
                    int i = paramCodedInputByteBufferNano.readTag();
                    switch (i) {
                        default:
                            if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                                continue;
                        case 0:
                            return this;
                        case 90:
                            this.packageName = paramCodedInputByteBufferNano.readString();
                            this.hasPackageName = true;
                            break;
                        case 96:
                            this.versionCode = paramCodedInputByteBufferNano.readInt32();
                            this.hasVersionCode = true;
                            break;
                        case 106:
                    }
                    int j = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 106);
                    if (this.certificateHash == null) ;
                    String[] arrayOfString;
                    for (int k = 0; ; k = this.certificateHash.length) {
                        arrayOfString = new String[k + j];
                        if (k != 0)
                            System.arraycopy(this.certificateHash, 0, arrayOfString, 0, k);
                        while (k < -1 + arrayOfString.length) {
                            arrayOfString[k] = paramCodedInputByteBufferNano.readString();
                            paramCodedInputByteBufferNano.readTag();
                            k++;
                        }
                    }
//          arrayOfString[k] = paramCodedInputByteBufferNano.readString();
//          this.certificateHash = arrayOfString;
                }
            }

            public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                    throws IOException {
                if ((this.hasPackageName) || (!this.packageName.equals("")))
                    paramCodedOutputByteBufferNano.writeString(11, this.packageName);
                if ((this.hasVersionCode) || (this.versionCode != 0))
                    paramCodedOutputByteBufferNano.writeInt32(12, this.versionCode);
                if ((this.certificateHash != null) && (this.certificateHash.length > 0))
                    for (int i = 0; i < this.certificateHash.length; i++) {
                        String str = this.certificateHash[i];
                        if (str != null)
                            paramCodedOutputByteBufferNano.writeString(13, str);
                    }
                super.writeTo(paramCodedOutputByteBufferNano);
            }
        }
    }

    public static final class ContentSyncResponseProto extends MessageNano {
        public boolean hasNumUpdatesAvailable;
        public int numUpdatesAvailable;

        public ContentSyncResponseProto() {
            clear();
        }

        public ContentSyncResponseProto clear() {
            this.numUpdatesAvailable = 0;
            this.hasNumUpdatesAvailable = false;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.hasNumUpdatesAvailable) || (this.numUpdatesAvailable != 0))
                i += CodedOutputByteBufferNano.computeInt32Size(1, this.numUpdatesAvailable);
            return i;
        }

        public ContentSyncResponseProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException {
            while (true) {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i) {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 8:
                }
                this.numUpdatesAvailable = paramCodedInputByteBufferNano.readInt32();
                this.hasNumUpdatesAvailable = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.hasNumUpdatesAvailable) || (this.numUpdatesAvailable != 0))
                paramCodedOutputByteBufferNano.writeInt32(1, this.numUpdatesAvailable);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class DataMessageProto extends MessageNano {
        private static volatile DataMessageProto[] _emptyArray;
        public VendingProtos.AppDataProto[] appData;
        public String category;
        public boolean hasCategory;

        public DataMessageProto() {
            clear();
        }

        public static DataMessageProto[] emptyArray() {
            if (_emptyArray == null) ;
            synchronized (InternalNano.LAZY_INIT_LOCK) {
                if (_emptyArray == null)
                    _emptyArray = new DataMessageProto[0];
                return _emptyArray;
            }
        }

        public DataMessageProto clear() {
            this.category = "";
            this.hasCategory = false;
            this.appData = VendingProtos.AppDataProto.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.hasCategory) || (!this.category.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(1, this.category);
            if ((this.appData != null) && (this.appData.length > 0))
                for (int j = 0; j < this.appData.length; j++) {
                    VendingProtos.AppDataProto localAppDataProto = this.appData[j];
                    if (localAppDataProto != null)
                        i += CodedOutputByteBufferNano.computeMessageSize(3, localAppDataProto);
                }
            return i;
        }

        public DataMessageProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
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
                        this.category = paramCodedInputByteBufferNano.readString();
                        this.hasCategory = true;
                        break;
                    case 26:
                }
                int j = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 26);
                if (this.appData == null) ;
                VendingProtos.AppDataProto[] arrayOfAppDataProto;
                for (int k = 0; ; k = this.appData.length) {
                    arrayOfAppDataProto = new VendingProtos.AppDataProto[k + j];
                    if (k != 0)
                        System.arraycopy(this.appData, 0, arrayOfAppDataProto, 0, k);
                    while (k < -1 + arrayOfAppDataProto.length) {
                        arrayOfAppDataProto[k] = new VendingProtos.AppDataProto();
                        paramCodedInputByteBufferNano.readMessage(arrayOfAppDataProto[k]);
                        paramCodedInputByteBufferNano.readTag();
                        k++;
                    }
                }
//        arrayOfAppDataProto[k] = new VendingProtos.AppDataProto();
//        paramCodedInputByteBufferNano.readMessage(arrayOfAppDataProto[k]);
//        this.appData = arrayOfAppDataProto;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.hasCategory) || (!this.category.equals("")))
                paramCodedOutputByteBufferNano.writeString(1, this.category);
            if ((this.appData != null) && (this.appData.length > 0))
                for (int i = 0; i < this.appData.length; i++) {
                    VendingProtos.AppDataProto localAppDataProto = this.appData[i];
                    if (localAppDataProto != null)
                        paramCodedOutputByteBufferNano.writeMessage(3, localAppDataProto);
                }
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class FileMetadataProto extends MessageNano {
        private static volatile FileMetadataProto[] _emptyArray;
        public String downloadUrl;
        public int fileType;
        public boolean hasDownloadUrl;
        public boolean hasFileType;
        public boolean hasSize;
        public boolean hasVersionCode;
        public long size;
        public int versionCode;

        public FileMetadataProto() {
            clear();
        }

        public static FileMetadataProto[] emptyArray() {
            if (_emptyArray == null) ;
            synchronized (InternalNano.LAZY_INIT_LOCK) {
                if (_emptyArray == null)
                    _emptyArray = new FileMetadataProto[0];
                return _emptyArray;
            }
        }

        public FileMetadataProto clear() {
            this.fileType = 0;
            this.hasFileType = false;
            this.versionCode = 0;
            this.hasVersionCode = false;
            this.size = 0L;
            this.hasSize = false;
            this.downloadUrl = "";
            this.hasDownloadUrl = false;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.fileType != 0) || (this.hasFileType))
                i += CodedOutputByteBufferNano.computeInt32Size(1, this.fileType);
            if ((this.hasVersionCode) || (this.versionCode != 0))
                i += CodedOutputByteBufferNano.computeInt32Size(2, this.versionCode);
            if ((this.hasSize) || (this.size != 0L))
                i += CodedOutputByteBufferNano.computeInt64Size(3, this.size);
            if ((this.hasDownloadUrl) || (!this.downloadUrl.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(4, this.downloadUrl);
            return i;
        }

        public FileMetadataProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException {
            while (true) {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i) {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 8:
                        int j = paramCodedInputByteBufferNano.readInt32();
                        switch (j) {
                            default:
                                break;
                            case 0:
                            case 1:
                        }
                        this.fileType = j;
                        this.hasFileType = true;
                        break;
                    case 16:
                        this.versionCode = paramCodedInputByteBufferNano.readInt32();
                        this.hasVersionCode = true;
                        break;
                    case 24:
                        this.size = paramCodedInputByteBufferNano.readInt64();
                        this.hasSize = true;
                        break;
                    case 34:
                }
                this.downloadUrl = paramCodedInputByteBufferNano.readString();
                this.hasDownloadUrl = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.fileType != 0) || (this.hasFileType))
                paramCodedOutputByteBufferNano.writeInt32(1, this.fileType);
            if ((this.hasVersionCode) || (this.versionCode != 0))
                paramCodedOutputByteBufferNano.writeInt32(2, this.versionCode);
            if ((this.hasSize) || (this.size != 0L))
                paramCodedOutputByteBufferNano.writeInt64(3, this.size);
            if ((this.hasDownloadUrl) || (!this.downloadUrl.equals("")))
                paramCodedOutputByteBufferNano.writeString(4, this.downloadUrl);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class GetAssetResponseProto extends MessageNano {
        private static volatile GetAssetResponseProto[] _emptyArray;
        public VendingProtos.FileMetadataProto[] additionalFile;
        public InstallAsset installAsset;

        public GetAssetResponseProto() {
            clear();
        }

        public static GetAssetResponseProto[] emptyArray() {
            if (_emptyArray == null) ;
            synchronized (InternalNano.LAZY_INIT_LOCK) {
                if (_emptyArray == null)
                    _emptyArray = new GetAssetResponseProto[0];
                return _emptyArray;
            }
        }

        public GetAssetResponseProto clear() {
            this.installAsset = null;
            this.additionalFile = VendingProtos.FileMetadataProto.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if (this.installAsset != null)
                i += CodedOutputByteBufferNano.computeGroupSize(1, this.installAsset);
            if ((this.additionalFile != null) && (this.additionalFile.length > 0))
                for (int j = 0; j < this.additionalFile.length; j++) {
                    VendingProtos.FileMetadataProto localFileMetadataProto = this.additionalFile[j];
                    if (localFileMetadataProto != null)
                        i += CodedOutputByteBufferNano.computeMessageSize(15, localFileMetadataProto);
                }
            return i;
        }

        public GetAssetResponseProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException {
            while (true) {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i) {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 11:
                        if (this.installAsset == null)
                            this.installAsset = new InstallAsset();
                        paramCodedInputByteBufferNano.readGroup(this.installAsset, 1);
                        break;
                    case 122:
                }
                int j = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 122);
                if (this.additionalFile == null) ;
                VendingProtos.FileMetadataProto[] arrayOfFileMetadataProto;
                for (int k = 0; ; k = this.additionalFile.length) {
                    arrayOfFileMetadataProto = new VendingProtos.FileMetadataProto[k + j];
                    if (k != 0)
                        System.arraycopy(this.additionalFile, 0, arrayOfFileMetadataProto, 0, k);
                    while (k < -1 + arrayOfFileMetadataProto.length) {
                        arrayOfFileMetadataProto[k] = new VendingProtos.FileMetadataProto();
                        paramCodedInputByteBufferNano.readMessage(arrayOfFileMetadataProto[k]);
                        paramCodedInputByteBufferNano.readTag();
                        k++;
                    }
                }
//        arrayOfFileMetadataProto[k] = new VendingProtos.FileMetadataProto();
//        paramCodedInputByteBufferNano.readMessage(arrayOfFileMetadataProto[k]);
//        this.additionalFile = arrayOfFileMetadataProto;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if (this.installAsset != null)
                paramCodedOutputByteBufferNano.writeGroup(1, this.installAsset);
            if ((this.additionalFile != null) && (this.additionalFile.length > 0))
                for (int i = 0; i < this.additionalFile.length; i++) {
                    VendingProtos.FileMetadataProto localFileMetadataProto = this.additionalFile[i];
                    if (localFileMetadataProto != null)
                        paramCodedOutputByteBufferNano.writeMessage(15, localFileMetadataProto);
                }
            super.writeTo(paramCodedOutputByteBufferNano);
        }

        public static final class InstallAsset extends MessageNano {
            public String assetId;
            public String assetName;
            public String assetPackage;
            public String assetSignature;
            public long assetSize;
            public String assetType;
            public String blobUrl;
            public String downloadAuthCookieName;
            public String downloadAuthCookieValue;
            public boolean forwardLocked;
            public boolean hasAssetId;
            public boolean hasAssetName;
            public boolean hasAssetPackage;
            public boolean hasAssetSignature;
            public boolean hasAssetSize;
            public boolean hasAssetType;
            public boolean hasBlobUrl;
            public boolean hasDownloadAuthCookieName;
            public boolean hasDownloadAuthCookieValue;
            public boolean hasForwardLocked;
            public boolean hasPostInstallRefundWindowMillis;
            public boolean hasRefundTimeoutMillis;
            public boolean hasSecured;
            public boolean hasVersionCode;
            public long postInstallRefundWindowMillis;
            public long refundTimeoutMillis;
            public boolean secured;
            public int versionCode;

            public InstallAsset() {
                clear();
            }

            public InstallAsset clear() {
                this.assetId = "";
                this.hasAssetId = false;
                this.assetName = "";
                this.hasAssetName = false;
                this.assetType = "";
                this.hasAssetType = false;
                this.assetPackage = "";
                this.hasAssetPackage = false;
                this.blobUrl = "";
                this.hasBlobUrl = false;
                this.assetSignature = "";
                this.hasAssetSignature = false;
                this.assetSize = 0L;
                this.hasAssetSize = false;
                this.refundTimeoutMillis = 0L;
                this.hasRefundTimeoutMillis = false;
                this.forwardLocked = false;
                this.hasForwardLocked = false;
                this.secured = false;
                this.hasSecured = false;
                this.versionCode = 0;
                this.hasVersionCode = false;
                this.downloadAuthCookieName = "";
                this.hasDownloadAuthCookieName = false;
                this.downloadAuthCookieValue = "";
                this.hasDownloadAuthCookieValue = false;
                this.postInstallRefundWindowMillis = 0L;
                this.hasPostInstallRefundWindowMillis = false;
                this.cachedSize = -1;
                return this;
            }

            protected int computeSerializedSize() {
                int i = super.computeSerializedSize();
                if ((this.hasAssetId) || (!this.assetId.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(2, this.assetId);
                if ((this.hasAssetName) || (!this.assetName.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(3, this.assetName);
                if ((this.hasAssetType) || (!this.assetType.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(4, this.assetType);
                if ((this.hasAssetPackage) || (!this.assetPackage.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(5, this.assetPackage);
                if ((this.hasBlobUrl) || (!this.blobUrl.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(6, this.blobUrl);
                if ((this.hasAssetSignature) || (!this.assetSignature.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(7, this.assetSignature);
                if ((this.hasAssetSize) || (this.assetSize != 0L))
                    i += CodedOutputByteBufferNano.computeInt64Size(8, this.assetSize);
                if ((this.hasRefundTimeoutMillis) || (this.refundTimeoutMillis != 0L))
                    i += CodedOutputByteBufferNano.computeInt64Size(9, this.refundTimeoutMillis);
                if ((this.hasForwardLocked) || (this.forwardLocked))
                    i += CodedOutputByteBufferNano.computeBoolSize(10, this.forwardLocked);
                if ((this.hasSecured) || (this.secured))
                    i += CodedOutputByteBufferNano.computeBoolSize(11, this.secured);
                if ((this.hasVersionCode) || (this.versionCode != 0))
                    i += CodedOutputByteBufferNano.computeInt32Size(12, this.versionCode);
                if ((this.hasDownloadAuthCookieName) || (!this.downloadAuthCookieName.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(13, this.downloadAuthCookieName);
                if ((this.hasDownloadAuthCookieValue) || (!this.downloadAuthCookieValue.equals("")))
                    i += CodedOutputByteBufferNano.computeStringSize(14, this.downloadAuthCookieValue);
                if ((this.hasPostInstallRefundWindowMillis) || (this.postInstallRefundWindowMillis != 0L))
                    i += CodedOutputByteBufferNano.computeInt64Size(16, this.postInstallRefundWindowMillis);
                return i;
            }

            public InstallAsset mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                    throws IOException {
                while (true) {
                    int i = paramCodedInputByteBufferNano.readTag();
                    switch (i) {
                        default:
                            if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                                continue;
                        case 0:
                            return this;
                        case 18:
                            this.assetId = paramCodedInputByteBufferNano.readString();
                            this.hasAssetId = true;
                            break;
                        case 26:
                            this.assetName = paramCodedInputByteBufferNano.readString();
                            this.hasAssetName = true;
                            break;
                        case 34:
                            this.assetType = paramCodedInputByteBufferNano.readString();
                            this.hasAssetType = true;
                            break;
                        case 42:
                            this.assetPackage = paramCodedInputByteBufferNano.readString();
                            this.hasAssetPackage = true;
                            break;
                        case 50:
                            this.blobUrl = paramCodedInputByteBufferNano.readString();
                            this.hasBlobUrl = true;
                            break;
                        case 58:
                            this.assetSignature = paramCodedInputByteBufferNano.readString();
                            this.hasAssetSignature = true;
                            break;
                        case 64:
                            this.assetSize = paramCodedInputByteBufferNano.readInt64();
                            this.hasAssetSize = true;
                            break;
                        case 72:
                            this.refundTimeoutMillis = paramCodedInputByteBufferNano.readInt64();
                            this.hasRefundTimeoutMillis = true;
                            break;
                        case 80:
                            this.forwardLocked = paramCodedInputByteBufferNano.readBool();
                            this.hasForwardLocked = true;
                            break;
                        case 88:
                            this.secured = paramCodedInputByteBufferNano.readBool();
                            this.hasSecured = true;
                            break;
                        case 96:
                            this.versionCode = paramCodedInputByteBufferNano.readInt32();
                            this.hasVersionCode = true;
                            break;
                        case 106:
                            this.downloadAuthCookieName = paramCodedInputByteBufferNano.readString();
                            this.hasDownloadAuthCookieName = true;
                            break;
                        case 114:
                            this.downloadAuthCookieValue = paramCodedInputByteBufferNano.readString();
                            this.hasDownloadAuthCookieValue = true;
                            break;
                        case 128:
                    }
                    this.postInstallRefundWindowMillis = paramCodedInputByteBufferNano.readInt64();
                    this.hasPostInstallRefundWindowMillis = true;
                }
            }

            public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                    throws IOException {
                if ((this.hasAssetId) || (!this.assetId.equals("")))
                    paramCodedOutputByteBufferNano.writeString(2, this.assetId);
                if ((this.hasAssetName) || (!this.assetName.equals("")))
                    paramCodedOutputByteBufferNano.writeString(3, this.assetName);
                if ((this.hasAssetType) || (!this.assetType.equals("")))
                    paramCodedOutputByteBufferNano.writeString(4, this.assetType);
                if ((this.hasAssetPackage) || (!this.assetPackage.equals("")))
                    paramCodedOutputByteBufferNano.writeString(5, this.assetPackage);
                if ((this.hasBlobUrl) || (!this.blobUrl.equals("")))
                    paramCodedOutputByteBufferNano.writeString(6, this.blobUrl);
                if ((this.hasAssetSignature) || (!this.assetSignature.equals("")))
                    paramCodedOutputByteBufferNano.writeString(7, this.assetSignature);
                if ((this.hasAssetSize) || (this.assetSize != 0L))
                    paramCodedOutputByteBufferNano.writeInt64(8, this.assetSize);
                if ((this.hasRefundTimeoutMillis) || (this.refundTimeoutMillis != 0L))
                    paramCodedOutputByteBufferNano.writeInt64(9, this.refundTimeoutMillis);
                if ((this.hasForwardLocked) || (this.forwardLocked))
                    paramCodedOutputByteBufferNano.writeBool(10, this.forwardLocked);
                if ((this.hasSecured) || (this.secured))
                    paramCodedOutputByteBufferNano.writeBool(11, this.secured);
                if ((this.hasVersionCode) || (this.versionCode != 0))
                    paramCodedOutputByteBufferNano.writeInt32(12, this.versionCode);
                if ((this.hasDownloadAuthCookieName) || (!this.downloadAuthCookieName.equals("")))
                    paramCodedOutputByteBufferNano.writeString(13, this.downloadAuthCookieName);
                if ((this.hasDownloadAuthCookieValue) || (!this.downloadAuthCookieValue.equals("")))
                    paramCodedOutputByteBufferNano.writeString(14, this.downloadAuthCookieValue);
                if ((this.hasPostInstallRefundWindowMillis) || (this.postInstallRefundWindowMillis != 0L))
                    paramCodedOutputByteBufferNano.writeInt64(16, this.postInstallRefundWindowMillis);
                super.writeTo(paramCodedOutputByteBufferNano);
            }
        }
    }

    public static final class GetMarketMetadataResponseProto extends MessageNano {
        public boolean hasLatestClientVersionCode;
        public int latestClientVersionCode;

        public GetMarketMetadataResponseProto() {
            clear();
        }

        public GetMarketMetadataResponseProto clear() {
            this.latestClientVersionCode = 0;
            this.hasLatestClientVersionCode = false;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.hasLatestClientVersionCode) || (this.latestClientVersionCode != 0))
                i += CodedOutputByteBufferNano.computeInt32Size(1, this.latestClientVersionCode);
            return i;
        }

        public GetMarketMetadataResponseProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException {
            while (true) {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i) {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 8:
                }
                this.latestClientVersionCode = paramCodedInputByteBufferNano.readInt32();
                this.hasLatestClientVersionCode = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.hasLatestClientVersionCode) || (this.latestClientVersionCode != 0))
                paramCodedOutputByteBufferNano.writeInt32(1, this.latestClientVersionCode);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class RequestPropertiesProto extends MessageNano {
        public String aid;
        public String clientId;
        public boolean hasAid;
        public boolean hasClientId;
        public boolean hasLoggingId;
        public boolean hasOperatorName;
        public boolean hasOperatorNumericName;
        public boolean hasProductNameAndVersion;
        public boolean hasSimOperatorName;
        public boolean hasSimOperatorNumericName;
        public boolean hasSoftwareVersion;
        public boolean hasUserAuthToken;
        public boolean hasUserAuthTokenSecure;
        public boolean hasUserCountry;
        public boolean hasUserLanguage;
        public String loggingId;
        public String operatorName;
        public String operatorNumericName;
        public String productNameAndVersion;
        public String simOperatorName;
        public String simOperatorNumericName;
        public int softwareVersion;
        public String userAuthToken;
        public boolean userAuthTokenSecure;
        public String userCountry;
        public String userLanguage;

        public RequestPropertiesProto() {
            clear();
        }

        public RequestPropertiesProto clear() {
            this.userAuthToken = "";
            this.hasUserAuthToken = false;
            this.userAuthTokenSecure = false;
            this.hasUserAuthTokenSecure = false;
            this.softwareVersion = 0;
            this.hasSoftwareVersion = false;
            this.aid = "";
            this.hasAid = false;
            this.productNameAndVersion = "";
            this.hasProductNameAndVersion = false;
            this.userLanguage = "";
            this.hasUserLanguage = false;
            this.userCountry = "";
            this.hasUserCountry = false;
            this.operatorName = "";
            this.hasOperatorName = false;
            this.simOperatorName = "";
            this.hasSimOperatorName = false;
            this.operatorNumericName = "";
            this.hasOperatorNumericName = false;
            this.simOperatorNumericName = "";
            this.hasSimOperatorNumericName = false;
            this.clientId = "";
            this.hasClientId = false;
            this.loggingId = "";
            this.hasLoggingId = false;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.hasUserAuthToken) || (!this.userAuthToken.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(1, this.userAuthToken);
            if ((this.hasUserAuthTokenSecure) || (this.userAuthTokenSecure))
                i += CodedOutputByteBufferNano.computeBoolSize(2, this.userAuthTokenSecure);
            if ((this.hasSoftwareVersion) || (this.softwareVersion != 0))
                i += CodedOutputByteBufferNano.computeInt32Size(3, this.softwareVersion);
            if ((this.hasAid) || (!this.aid.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(4, this.aid);
            if ((this.hasProductNameAndVersion) || (!this.productNameAndVersion.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(5, this.productNameAndVersion);
            if ((this.hasUserLanguage) || (!this.userLanguage.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(6, this.userLanguage);
            if ((this.hasUserCountry) || (!this.userCountry.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(7, this.userCountry);
            if ((this.hasOperatorName) || (!this.operatorName.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(8, this.operatorName);
            if ((this.hasSimOperatorName) || (!this.simOperatorName.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(9, this.simOperatorName);
            if ((this.hasOperatorNumericName) || (!this.operatorNumericName.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(10, this.operatorNumericName);
            if ((this.hasSimOperatorNumericName) || (!this.simOperatorNumericName.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(11, this.simOperatorNumericName);
            if ((this.hasClientId) || (!this.clientId.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(12, this.clientId);
            if ((this.hasLoggingId) || (!this.loggingId.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(13, this.loggingId);
            return i;
        }

        public RequestPropertiesProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
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
                        this.userAuthToken = paramCodedInputByteBufferNano.readString();
                        this.hasUserAuthToken = true;
                        break;
                    case 16:
                        this.userAuthTokenSecure = paramCodedInputByteBufferNano.readBool();
                        this.hasUserAuthTokenSecure = true;
                        break;
                    case 24:
                        this.softwareVersion = paramCodedInputByteBufferNano.readInt32();
                        this.hasSoftwareVersion = true;
                        break;
                    case 34:
                        this.aid = paramCodedInputByteBufferNano.readString();
                        this.hasAid = true;
                        break;
                    case 42:
                        this.productNameAndVersion = paramCodedInputByteBufferNano.readString();
                        this.hasProductNameAndVersion = true;
                        break;
                    case 50:
                        this.userLanguage = paramCodedInputByteBufferNano.readString();
                        this.hasUserLanguage = true;
                        break;
                    case 58:
                        this.userCountry = paramCodedInputByteBufferNano.readString();
                        this.hasUserCountry = true;
                        break;
                    case 66:
                        this.operatorName = paramCodedInputByteBufferNano.readString();
                        this.hasOperatorName = true;
                        break;
                    case 74:
                        this.simOperatorName = paramCodedInputByteBufferNano.readString();
                        this.hasSimOperatorName = true;
                        break;
                    case 82:
                        this.operatorNumericName = paramCodedInputByteBufferNano.readString();
                        this.hasOperatorNumericName = true;
                        break;
                    case 90:
                        this.simOperatorNumericName = paramCodedInputByteBufferNano.readString();
                        this.hasSimOperatorNumericName = true;
                        break;
                    case 98:
                        this.clientId = paramCodedInputByteBufferNano.readString();
                        this.hasClientId = true;
                        break;
                    case 106:
                }
                this.loggingId = paramCodedInputByteBufferNano.readString();
                this.hasLoggingId = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.hasUserAuthToken) || (!this.userAuthToken.equals("")))
                paramCodedOutputByteBufferNano.writeString(1, this.userAuthToken);
            if ((this.hasUserAuthTokenSecure) || (this.userAuthTokenSecure))
                paramCodedOutputByteBufferNano.writeBool(2, this.userAuthTokenSecure);
            if ((this.hasSoftwareVersion) || (this.softwareVersion != 0))
                paramCodedOutputByteBufferNano.writeInt32(3, this.softwareVersion);
            if ((this.hasAid) || (!this.aid.equals("")))
                paramCodedOutputByteBufferNano.writeString(4, this.aid);
            if ((this.hasProductNameAndVersion) || (!this.productNameAndVersion.equals("")))
                paramCodedOutputByteBufferNano.writeString(5, this.productNameAndVersion);
            if ((this.hasUserLanguage) || (!this.userLanguage.equals("")))
                paramCodedOutputByteBufferNano.writeString(6, this.userLanguage);
            if ((this.hasUserCountry) || (!this.userCountry.equals("")))
                paramCodedOutputByteBufferNano.writeString(7, this.userCountry);
            if ((this.hasOperatorName) || (!this.operatorName.equals("")))
                paramCodedOutputByteBufferNano.writeString(8, this.operatorName);
            if ((this.hasSimOperatorName) || (!this.simOperatorName.equals("")))
                paramCodedOutputByteBufferNano.writeString(9, this.simOperatorName);
            if ((this.hasOperatorNumericName) || (!this.operatorNumericName.equals("")))
                paramCodedOutputByteBufferNano.writeString(10, this.operatorNumericName);
            if ((this.hasSimOperatorNumericName) || (!this.simOperatorNumericName.equals("")))
                paramCodedOutputByteBufferNano.writeString(11, this.simOperatorNumericName);
            if ((this.hasClientId) || (!this.clientId.equals("")))
                paramCodedOutputByteBufferNano.writeString(12, this.clientId);
            if ((this.hasLoggingId) || (!this.loggingId.equals("")))
                paramCodedOutputByteBufferNano.writeString(13, this.loggingId);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class RequestProto extends MessageNano {
        public Request[] request;
        public RequestPropertiesProto requestProperties;

        public RequestProto() {
            clear();
        }

        public RequestProto clear() {
            this.requestProperties = null;
            this.request = Request.emptyArray();
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if (this.requestProperties != null)
                i += CodedOutputByteBufferNano.computeMessageSize(1, this.requestProperties);
            if ((this.request != null) && (this.request.length > 0))
                for (int j = 0; j < this.request.length; j++) {
                    Request localRequest = this.request[j];
                    if (localRequest != null)
                        i += CodedOutputByteBufferNano.computeGroupSize(2, localRequest);
                }
            return i;
        }

        public RequestProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
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
                        if (this.requestProperties == null)
                            this.requestProperties = new VendingProtos.RequestPropertiesProto();
                        paramCodedInputByteBufferNano.readMessage(this.requestProperties);
                        break;
                    case 19:
                }
                int j = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 19);
                if (this.request == null) ;
                Request[] arrayOfRequest;
                for (int k = 0; ; k = this.request.length) {
                    arrayOfRequest = new Request[k + j];
                    if (k != 0)
                        System.arraycopy(this.request, 0, arrayOfRequest, 0, k);
                    while (k < -1 + arrayOfRequest.length) {
                        arrayOfRequest[k] = new Request();
                        paramCodedInputByteBufferNano.readGroup(arrayOfRequest[k], 2);
                        paramCodedInputByteBufferNano.readTag();
                        k++;
                    }
                }
//        arrayOfRequest[k] = new Request();
//        paramCodedInputByteBufferNano.readGroup(arrayOfRequest[k], 2);
//        this.request = arrayOfRequest;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if (this.requestProperties != null)
                paramCodedOutputByteBufferNano.writeMessage(1, this.requestProperties);
            if ((this.request != null) && (this.request.length > 0))
                for (int i = 0; i < this.request.length; i++) {
                    Request localRequest = this.request[i];
                    if (localRequest != null)
                        paramCodedOutputByteBufferNano.writeGroup(2, localRequest);
                }
            super.writeTo(paramCodedOutputByteBufferNano);
        }

        public static final class Request extends MessageNano {
            private static volatile Request[] _emptyArray;
            public VendingProtos.ContentSyncRequestProto contentSyncRequest;

            public Request() {
                clear();
            }

            public static Request[] emptyArray() {
                if (_emptyArray == null) ;
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null)
                        _emptyArray = new Request[0];
                    return _emptyArray;
                }
            }

            public Request clear() {
                this.contentSyncRequest = null;
                this.cachedSize = -1;
                return this;
            }

            protected int computeSerializedSize() {
                int i = super.computeSerializedSize();
                if (this.contentSyncRequest != null)
                    i += CodedOutputByteBufferNano.computeMessageSize(9, this.contentSyncRequest);
                return i;
            }

            public Request mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                    throws IOException {
                while (true) {
                    int i = paramCodedInputByteBufferNano.readTag();
                    switch (i) {
                        default:
                            if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                                continue;
                        case 0:
                            return this;
                        case 50:
//            if (this.modifyCommentRequest == null)
//              this.modifyCommentRequest = new VendingProtos.ModifyCommentRequestProto();
//            paramCodedInputByteBufferNano.readMessage(this.modifyCommentRequest);
                            break;
                        case 74:
                            if (this.contentSyncRequest == null)
                                this.contentSyncRequest = new VendingProtos.ContentSyncRequestProto();
                            paramCodedInputByteBufferNano.readMessage(this.contentSyncRequest);
                            break;
                        case 106:
//            if (this.purchaseMetadataRequest == null)
//              this.purchaseMetadataRequest = new VendingProtos.PurchaseMetadataRequestProto();
//            paramCodedInputByteBufferNano.readMessage(this.purchaseMetadataRequest);
                            break;
                        case 146:
//            if (this.checkLicenseRequest == null)
//              this.checkLicenseRequest = new VendingProtos.CheckLicenseRequestProto();
//            paramCodedInputByteBufferNano.readMessage(this.checkLicenseRequest);
                            break;
                        case 194:
//            if (this.restoreApplicationsRequest == null)
//              this.restoreApplicationsRequest = new VendingProtos.RestoreApplicationsRequestProto();
//            paramCodedInputByteBufferNano.readMessage(this.restoreApplicationsRequest);
                            break;
                        case 210:
//            if (this.billingEventRequest == null)
//              this.billingEventRequest = new VendingProtos.BillingEventRequestProto();
//            paramCodedInputByteBufferNano.readMessage(this.billingEventRequest);
                            break;
                        case 250:
//            if (this.inAppRestoreTransactionsRequest == null)
//              this.inAppRestoreTransactionsRequest = new VendingProtos.InAppRestoreTransactionsRequestProto();
//            paramCodedInputByteBufferNano.readMessage(this.inAppRestoreTransactionsRequest);
                            break;
                        case 258:
//            if (this.inAppPurchaseInformationRequest == null)
//              this.inAppPurchaseInformationRequest = new VendingProtos.InAppPurchaseInformationRequestProto();
//            paramCodedInputByteBufferNano.readMessage(this.inAppPurchaseInformationRequest);
                            break;
                        case 266:
//            if (this.checkForNotificationsRequest == null)
//              this.checkForNotificationsRequest = new VendingProtos.CheckForNotificationsRequestProto();
//            paramCodedInputByteBufferNano.readMessage(this.checkForNotificationsRequest);
                            break;
                        case 274:
                    }
//          if (this.ackNotificationsRequest == null)
//            this.ackNotificationsRequest = new VendingProtos.AckNotificationsRequestProto();
//          paramCodedInputByteBufferNano.readMessage(this.ackNotificationsRequest);
                }
            }

            public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                    throws IOException {
                if (this.contentSyncRequest != null)
                    paramCodedOutputByteBufferNano.writeMessage(9, this.contentSyncRequest);
                super.writeTo(paramCodedOutputByteBufferNano);
            }
        }
    }

    public static final class ResponsePropertiesProto extends MessageNano {
        public boolean hasResult;
        public int result;

        public ResponsePropertiesProto() {
            clear();
        }

        public ResponsePropertiesProto clear() {
            this.result = 0;
            this.hasResult = false;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.result != 0) || (this.hasResult))
                i += CodedOutputByteBufferNano.computeInt32Size(1, this.result);
            return i;
        }

        public ResponsePropertiesProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException {
            while (true) {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i) {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 8:
                }
                int j = paramCodedInputByteBufferNano.readInt32();
                switch (j) {
                    default:
                        break;
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                }
                this.result = j;
                this.hasResult = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.result != 0) || (this.hasResult))
                paramCodedOutputByteBufferNano.writeInt32(1, this.result);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class ResponseProto extends MessageNano {
        //    public VendingProtos.PendingNotificationsProto pendingNotifications;
        public Response[] response;

        public ResponseProto() {
            clear();
        }

        public static ResponseProto parseFrom(byte[] paramArrayOfByte)
                throws InvalidProtocolBufferNanoException {
            return (ResponseProto) MessageNano.mergeFrom(new ResponseProto(), paramArrayOfByte);
        }

        public ResponseProto clear() {
            this.response = Response.emptyArray();
//      this.pendingNotifications = null;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.response != null) && (this.response.length > 0))
                for (int j = 0; j < this.response.length; j++) {
                    Response localResponse = this.response[j];
                    if (localResponse != null)
                        i += CodedOutputByteBufferNano.computeGroupSize(1, localResponse);
                }
//      if (this.pendingNotifications != null)
//        i += CodedOutputByteBufferNano.computeMessageSize(38, this.pendingNotifications);
            return i;
        }

        public ResponseProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                throws IOException {
            while (true) {
                int i = paramCodedInputByteBufferNano.readTag();
                switch (i) {
                    default:
                        if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                            continue;
                    case 0:
                        return this;
                    case 11:
                        int j = WireFormatNano.getRepeatedFieldArrayLength(paramCodedInputByteBufferNano, 11);
//          if (this.response == null);
//          Response[] arrayOfResponse;
//          for (int k = 0; ; k = this.response.length)
//          {
//            arrayOfResponse = new Response[k + j];
//            if (k != 0)
//              System.arraycopy(this.response, 0, arrayOfResponse, 0, k);
//            while (k < -1 + arrayOfResponse.length)
//            {
//              arrayOfResponse[k] = new Response();
//              paramCodedInputByteBufferNano.readGroup(arrayOfResponse[k], 1);
//              paramCodedInputByteBufferNano.readTag();
//              k++;
//            }
//          }
//          arrayOfResponse[k] = new Response();
//            paramCodedInputByteBufferNano.readGroup(arrayOfResponse[k], 1);
//            this.response = arrayOfResponse;
                        break;
                    case 306:
                }
//        if (this.pendingNotifications == null)
//          this.pendingNotifications = new VendingProtos.PendingNotificationsProto();
//        paramCodedInputByteBufferNano.readMessage(this.pendingNotifications);
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.response != null) && (this.response.length > 0))
                for (int i = 0; i < this.response.length; i++) {
                    Response localResponse = this.response[i];
                    if (localResponse != null)
                        paramCodedOutputByteBufferNano.writeGroup(1, localResponse);
                }
//      if (this.pendingNotifications != null)
//        paramCodedOutputByteBufferNano.writeMessage(38, this.pendingNotifications);
            super.writeTo(paramCodedOutputByteBufferNano);
        }

        public static final class Response extends MessageNano {
            private static volatile Response[] _emptyArray;
            //      public VendingProtos.AckNotificationsResponseProto ackNotificationsResponse;
//      public VendingProtos.BillingEventResponseProto billingEventResponse;
//      public VendingProtos.CheckLicenseResponseProto checkLicenseResponse;
            public VendingProtos.ContentSyncResponseProto contentSyncResponse;
            public VendingProtos.GetAssetResponseProto getAssetResponse;
            public VendingProtos.GetMarketMetadataResponseProto getMarketMetadataResponse;
            //      public VendingProtos.InAppPurchaseInformationResponseProto inAppPurchaseInformationResponse;
//      public VendingProtos.InAppRestoreTransactionsResponseProto inAppRestoreTransactionsResponse;
//      public VendingProtos.ModifyCommentResponseProto modifyCommentResponse;
//      public VendingProtos.PurchaseMetadataResponseProto purchaseMetadataResponse;
            public VendingProtos.ResponsePropertiesProto responseProperties;
//      public VendingProtos.RestoreApplicationsResponseProto restoreApplicationResponse;

            public Response() {
                clear();
            }

            public static Response[] emptyArray() {
                if (_emptyArray == null) ;
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null)
                        _emptyArray = new Response[0];
                    return _emptyArray;
                }
            }

            public Response clear() {
                this.responseProperties = null;
//        this.modifyCommentResponse = null;
                this.contentSyncResponse = null;
                this.getAssetResponse = null;
//        this.purchaseMetadataResponse = null;
//        this.checkLicenseResponse = null;
                this.getMarketMetadataResponse = null;
//        this.restoreApplicationResponse = null;
//        this.billingEventResponse = null;
//        this.inAppRestoreTransactionsResponse = null;
//        this.inAppPurchaseInformationResponse = null;
//        this.ackNotificationsResponse = null;
                this.cachedSize = -1;
                return this;
            }

            protected int computeSerializedSize() {
                int i = super.computeSerializedSize();
                if (this.responseProperties != null)
                    i += CodedOutputByteBufferNano.computeMessageSize(2, this.responseProperties);
//        if (this.modifyCommentResponse != null)
//          i += CodedOutputByteBufferNano.computeMessageSize(5, this.modifyCommentResponse);
                if (this.contentSyncResponse != null)
                    i += CodedOutputByteBufferNano.computeMessageSize(8, this.contentSyncResponse);
                if (this.getAssetResponse != null)
                    i += CodedOutputByteBufferNano.computeMessageSize(9, this.getAssetResponse);
//        if (this.purchaseMetadataResponse != null)
//          i += CodedOutputByteBufferNano.computeMessageSize(12, this.purchaseMetadataResponse);
//        if (this.checkLicenseResponse != null)
//          i += CodedOutputByteBufferNano.computeMessageSize(17, this.checkLicenseResponse);
                if (this.getMarketMetadataResponse != null)
                    i += CodedOutputByteBufferNano.computeMessageSize(18, this.getMarketMetadataResponse);
//        if (this.restoreApplicationResponse != null)
//          i += CodedOutputByteBufferNano.computeMessageSize(23, this.restoreApplicationResponse);
//        if (this.billingEventResponse != null)
//          i += CodedOutputByteBufferNano.computeMessageSize(25, this.billingEventResponse);
//        if (this.inAppRestoreTransactionsResponse != null)
//          i += CodedOutputByteBufferNano.computeMessageSize(30, this.inAppRestoreTransactionsResponse);
//        if (this.inAppPurchaseInformationResponse != null)
//          i += CodedOutputByteBufferNano.computeMessageSize(31, this.inAppPurchaseInformationResponse);
//        if (this.ackNotificationsResponse != null)
//          i += CodedOutputByteBufferNano.computeMessageSize(33, this.ackNotificationsResponse);
                return i;
            }

            public Response mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
                    throws IOException {
                while (true) {
                    int i = paramCodedInputByteBufferNano.readTag();
                    switch (i) {
                        default:
                            if (WireFormatNano.parseUnknownField(paramCodedInputByteBufferNano, i))
                                continue;
                        case 0:
                            return this;
                        case 18:
                            if (this.responseProperties == null)
                                this.responseProperties = new VendingProtos.ResponsePropertiesProto();
                            paramCodedInputByteBufferNano.readMessage(this.responseProperties);
                            break;
                        case 42:
//            if (this.modifyCommentResponse == null)
//              this.modifyCommentResponse = new VendingProtos.ModifyCommentResponseProto();
//            paramCodedInputByteBufferNano.readMessage(this.modifyCommentResponse);
                            break;
                        case 66:
                            if (this.contentSyncResponse == null)
                                this.contentSyncResponse = new VendingProtos.ContentSyncResponseProto();
                            paramCodedInputByteBufferNano.readMessage(this.contentSyncResponse);
                            break;
                        case 74:
                            if (this.getAssetResponse == null)
                                this.getAssetResponse = new VendingProtos.GetAssetResponseProto();
                            paramCodedInputByteBufferNano.readMessage(this.getAssetResponse);
                            break;
                        case 98:
//            if (this.purchaseMetadataResponse == null)
//              this.purchaseMetadataResponse = new VendingProtos.PurchaseMetadataResponseProto();
//            paramCodedInputByteBufferNano.readMessage(this.purchaseMetadataResponse);
                            break;
                        case 138:
//            if (this.checkLicenseResponse == null)
//              this.checkLicenseResponse = new VendingProtos.CheckLicenseResponseProto();
//            paramCodedInputByteBufferNano.readMessage(this.checkLicenseResponse);
                            break;
                        case 146:
                            if (this.getMarketMetadataResponse == null)
                                this.getMarketMetadataResponse = new VendingProtos.GetMarketMetadataResponseProto();
                            paramCodedInputByteBufferNano.readMessage(this.getMarketMetadataResponse);
                            break;
                        case 186:
//            if (this.restoreApplicationResponse == null)
//              this.restoreApplicationResponse = new VendingProtos.RestoreApplicationsResponseProto();
//            paramCodedInputByteBufferNano.readMessage(this.restoreApplicationResponse);
                            break;
                        case 202:
//            if (this.billingEventResponse == null)
//              this.billingEventResponse = new VendingProtos.BillingEventResponseProto();
//            paramCodedInputByteBufferNano.readMessage(this.billingEventResponse);
                            break;
                        case 242:
//            if (this.inAppRestoreTransactionsResponse == null)
//              this.inAppRestoreTransactionsResponse = new VendingProtos.InAppRestoreTransactionsResponseProto();
//            paramCodedInputByteBufferNano.readMessage(this.inAppRestoreTransactionsResponse);
                            break;
                        case 250:
//            if (this.inAppPurchaseInformationResponse == null)
//              this.inAppPurchaseInformationResponse = new VendingProtos.InAppPurchaseInformationResponseProto();
//            paramCodedInputByteBufferNano.readMessage(this.inAppPurchaseInformationResponse);
                            break;
                        case 266:
                    }
//          if (this.ackNotificationsResponse == null)
//            this.ackNotificationsResponse = new VendingProtos.AckNotificationsResponseProto();
//          paramCodedInputByteBufferNano.readMessage(this.ackNotificationsResponse);
                }
            }

            public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                    throws IOException {
                if (this.responseProperties != null)
                    paramCodedOutputByteBufferNano.writeMessage(2, this.responseProperties);
//        if (this.modifyCommentResponse != null)
//          paramCodedOutputByteBufferNano.writeMessage(5, this.modifyCommentResponse);
                if (this.contentSyncResponse != null)
                    paramCodedOutputByteBufferNano.writeMessage(8, this.contentSyncResponse);
                if (this.getAssetResponse != null)
                    paramCodedOutputByteBufferNano.writeMessage(9, this.getAssetResponse);
//        if (this.purchaseMetadataResponse != null)
//          paramCodedOutputByteBufferNano.writeMessage(12, this.purchaseMetadataResponse);
//        if (this.checkLicenseResponse != null)
//          paramCodedOutputByteBufferNano.writeMessage(17, this.checkLicenseResponse);
                if (this.getMarketMetadataResponse != null)
                    paramCodedOutputByteBufferNano.writeMessage(18, this.getMarketMetadataResponse);
//        if (this.restoreApplicationResponse != null)
//          paramCodedOutputByteBufferNano.writeMessage(23, this.restoreApplicationResponse);
//        if (this.billingEventResponse != null)
//          paramCodedOutputByteBufferNano.writeMessage(25, this.billingEventResponse);
//        if (this.inAppRestoreTransactionsResponse != null)
//          paramCodedOutputByteBufferNano.writeMessage(30, this.inAppRestoreTransactionsResponse);
//        if (this.inAppPurchaseInformationResponse != null)
//          paramCodedOutputByteBufferNano.writeMessage(31, this.inAppPurchaseInformationResponse);
//        if (this.ackNotificationsResponse != null)
//          paramCodedOutputByteBufferNano.writeMessage(33, this.ackNotificationsResponse);
                super.writeTo(paramCodedOutputByteBufferNano);
            }
        }
    }

    public static final class SignatureHashProto extends MessageNano {
        public boolean hasHash;
        public boolean hasPackageName;
        public boolean hasVersionCode;
        public byte[] hash;
        public String packageName;
        public int versionCode;

        public SignatureHashProto() {
            clear();
        }

        public SignatureHashProto clear() {
            this.packageName = "";
            this.hasPackageName = false;
            this.versionCode = 0;
            this.hasVersionCode = false;
            this.hash = WireFormatNano.EMPTY_BYTES;
            this.hasHash = false;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.hasPackageName) || (!this.packageName.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(1, this.packageName);
            if ((this.hasVersionCode) || (this.versionCode != 0))
                i += CodedOutputByteBufferNano.computeInt32Size(2, this.versionCode);
            if ((this.hasHash) || (!Arrays.equals(this.hash, WireFormatNano.EMPTY_BYTES)))
                i += CodedOutputByteBufferNano.computeBytesSize(3, this.hash);
            return i;
        }

        public SignatureHashProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
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
                        this.packageName = paramCodedInputByteBufferNano.readString();
                        this.hasPackageName = true;
                        break;
                    case 16:
                        this.versionCode = paramCodedInputByteBufferNano.readInt32();
                        this.hasVersionCode = true;
                        break;
                    case 26:
                }
                this.hash = paramCodedInputByteBufferNano.readBytes();
                this.hasHash = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.hasPackageName) || (!this.packageName.equals("")))
                paramCodedOutputByteBufferNano.writeString(1, this.packageName);
            if ((this.hasVersionCode) || (this.versionCode != 0))
                paramCodedOutputByteBufferNano.writeInt32(2, this.versionCode);
            if ((this.hasHash) || (!Arrays.equals(this.hash, WireFormatNano.EMPTY_BYTES)))
                paramCodedOutputByteBufferNano.writeBytes(3, this.hash);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

    public static final class SignedDataProto extends MessageNano {
        public boolean hasSignature;
        public boolean hasSignedData;
        public String signature;
        public String signedData;

        public SignedDataProto() {
            clear();
        }

        public SignedDataProto clear() {
            this.signedData = "";
            this.hasSignedData = false;
            this.signature = "";
            this.hasSignature = false;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if ((this.hasSignedData) || (!this.signedData.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(1, this.signedData);
            if ((this.hasSignature) || (!this.signature.equals("")))
                i += CodedOutputByteBufferNano.computeStringSize(2, this.signature);
            return i;
        }

        public SignedDataProto mergeFrom(CodedInputByteBufferNano paramCodedInputByteBufferNano)
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
                        this.signedData = paramCodedInputByteBufferNano.readString();
                        this.hasSignedData = true;
                        break;
                    case 18:
                }
                this.signature = paramCodedInputByteBufferNano.readString();
                this.hasSignature = true;
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if ((this.hasSignedData) || (!this.signedData.equals("")))
                paramCodedOutputByteBufferNano.writeString(1, this.signedData);
            if ((this.hasSignature) || (!this.signature.equals("")))
                paramCodedOutputByteBufferNano.writeString(2, this.signature);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }

}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.android.finsky.protos.VendingProtos
 * JD-Core Version:    0.6.2
 */