package com.google.android.finsky.protos;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InternalNano;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;

import java.io.IOException;

public interface FilterRules
{
    public static final class Availability extends MessageNano
    {
        public AvailabilityProblem[] availabilityProblem;
        public boolean availableIfOwned;
        public FilterEvaluationInfo filterInfo;
        public boolean hasAvailableIfOwned;
        public boolean hasHidden;
        public boolean hasOfferType;
        public boolean hasRestriction;
        public boolean hidden;
        public Common.Install[] install;
        public int offerType;
        public Ownership.OwnershipInfo ownershipInfo;
        public PerDeviceAvailabilityRestriction[] perDeviceAvailabilityRestriction;
        public int restriction;
        public Rule rule;
        
        public Availability() {
            super();
            this.clear();
        }
        
        public Availability clear() {
            this.restriction = 1;
            this.hasRestriction = false;
            this.availabilityProblem = AvailabilityProblem.emptyArray();
            this.availableIfOwned = false;
            this.hasAvailableIfOwned = false;
            this.offerType = 1;
            this.hasOfferType = false;
            this.ownershipInfo = null;
            this.hidden = false;
            this.hasHidden = false;
            this.install = Common.Install.emptyArray();
            this.rule = null;
            this.perDeviceAvailabilityRestriction = PerDeviceAvailabilityRestriction.emptyArray();
            this.filterInfo = null;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            PerDeviceAvailabilityRestriction perDeviceAvailabilityRestriction;
            Common.Install install;
            AvailabilityProblem availabilityProblem;
            computeSerializedSize = super.computeSerializedSize();
            if (this.restriction != 1 || this.hasRestriction) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(5, this.restriction);
            }
            if (this.offerType != 1 || this.hasOfferType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(6, this.offerType);
            }
            if (this.rule != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(7, this.rule);
            }
            if (this.perDeviceAvailabilityRestriction != null && this.perDeviceAvailabilityRestriction.length > 0) {
                for (int i = 0; i < this.perDeviceAvailabilityRestriction.length; ++i) {
                    perDeviceAvailabilityRestriction = this.perDeviceAvailabilityRestriction[i];
                    if (perDeviceAvailabilityRestriction != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeGroupSize(9, perDeviceAvailabilityRestriction);
                    }
                }
            }
            if (this.hasAvailableIfOwned || this.availableIfOwned) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(13, this.availableIfOwned);
            }
            if (this.install != null && this.install.length > 0) {
                for (int j = 0; j < this.install.length; ++j) {
                    install = this.install[j];
                    if (install != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(14, install);
                    }
                }
            }
            if (this.filterInfo != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(16, this.filterInfo);
            }
            if (this.ownershipInfo != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(17, this.ownershipInfo);
            }
            if (this.availabilityProblem != null && this.availabilityProblem.length > 0) {
                for (int k = 0; k < this.availabilityProblem.length; ++k) {
                    availabilityProblem = this.availabilityProblem[k];
                    if (availabilityProblem != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(18, availabilityProblem);
                    }
                }
            }
            if (this.hasHidden || this.hidden) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(21, this.hidden);
            }
            return computeSerializedSize;
        }
        
        @Override
        public Availability mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
            int int2;
            int repeatedFieldArrayLength;
            int i;
            PerDeviceAvailabilityRestriction[] perDeviceAvailabilityRestriction;
            int repeatedFieldArrayLength2;
            int j;
            Common.Install[] install;
            int repeatedFieldArrayLength3;
            int k;
            AvailabilityProblem[] availabilityProblem;
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
                    case 40: {
                        int32 = codedInputByteBufferNano.readInt32();
                        switch (int32) {
                            default: {
                                continue;
                            }
                            case 1:
                            case 2:
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
                            case 20:
                            case 21: {
                                this.restriction = int32;
                                this.hasRestriction = true;
                                continue;
                            }
                        }
                    }
                    case 48: {
                        int2 = codedInputByteBufferNano.readInt32();
                        switch (int2) {
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
                                this.offerType = int2;
                                this.hasOfferType = true;
                                continue;
                            }
                        }
                    }
                    case 58: {
                        if (this.rule == null) {
                            this.rule = new Rule();
                        }
                        codedInputByteBufferNano.readMessage(this.rule);
                        continue;
                    }
                    case 75: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 75);
                        if (this.perDeviceAvailabilityRestriction == null) {
                            i = 0;
                        }
                        else {
                            i = this.perDeviceAvailabilityRestriction.length;
                        }
                        perDeviceAvailabilityRestriction = new PerDeviceAvailabilityRestriction[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.perDeviceAvailabilityRestriction, 0, perDeviceAvailabilityRestriction, 0, i);
                        }
                        while (i < -1 + perDeviceAvailabilityRestriction.length) {
                            codedInputByteBufferNano.readGroup(perDeviceAvailabilityRestriction[i] = new PerDeviceAvailabilityRestriction(), 9);
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readGroup(perDeviceAvailabilityRestriction[i] = new PerDeviceAvailabilityRestriction(), 9);
                        this.perDeviceAvailabilityRestriction = perDeviceAvailabilityRestriction;
                        continue;
                    }
                    case 104: {
                        this.availableIfOwned = codedInputByteBufferNano.readBool();
                        this.hasAvailableIfOwned = true;
                        continue;
                    }
                    case 114: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 114);
                        if (this.install == null) {
                            j = 0;
                        }
                        else {
                            j = this.install.length;
                        }
                        install = new Common.Install[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.install, 0, install, 0, j);
                        }
                        while (j < -1 + install.length) {
                            codedInputByteBufferNano.readMessage(install[j] = new Common.Install());
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        codedInputByteBufferNano.readMessage(install[j] = new Common.Install());
                        this.install = install;
                        continue;
                    }
                    case 130: {
                        if (this.filterInfo == null) {
                            this.filterInfo = new FilterEvaluationInfo();
                        }
                        codedInputByteBufferNano.readMessage(this.filterInfo);
                        continue;
                    }
                    case 138: {
                        if (this.ownershipInfo == null) {
                            this.ownershipInfo = new Ownership.OwnershipInfo();
                        }
                        codedInputByteBufferNano.readMessage(this.ownershipInfo);
                        continue;
                    }
                    case 146: {
                        repeatedFieldArrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 146);
                        if (this.availabilityProblem == null) {
                            k = 0;
                        }
                        else {
                            k = this.availabilityProblem.length;
                        }
                        availabilityProblem = new AvailabilityProblem[k + repeatedFieldArrayLength3];
                        if (k != 0) {
                            System.arraycopy(this.availabilityProblem, 0, availabilityProblem, 0, k);
                        }
                        while (k < -1 + availabilityProblem.length) {
                            codedInputByteBufferNano.readMessage(availabilityProblem[k] = new AvailabilityProblem());
                            codedInputByteBufferNano.readTag();
                            ++k;
                        }
                        codedInputByteBufferNano.readMessage(availabilityProblem[k] = new AvailabilityProblem());
                        this.availabilityProblem = availabilityProblem;
                        continue;
                    }
                    case 168: {
                        this.hidden = codedInputByteBufferNano.readBool();
                        this.hasHidden = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            PerDeviceAvailabilityRestriction perDeviceAvailabilityRestriction;
            Common.Install install;
            AvailabilityProblem availabilityProblem;
            if (this.restriction != 1 || this.hasRestriction) {
                codedOutputByteBufferNano.writeInt32(5, this.restriction);
            }
            if (this.offerType != 1 || this.hasOfferType) {
                codedOutputByteBufferNano.writeInt32(6, this.offerType);
            }
            if (this.rule != null) {
                codedOutputByteBufferNano.writeMessage(7, this.rule);
            }
            if (this.perDeviceAvailabilityRestriction != null && this.perDeviceAvailabilityRestriction.length > 0) {
                for (int i = 0; i < this.perDeviceAvailabilityRestriction.length; ++i) {
                    perDeviceAvailabilityRestriction = this.perDeviceAvailabilityRestriction[i];
                    if (perDeviceAvailabilityRestriction != null) {
                        codedOutputByteBufferNano.writeGroup(9, perDeviceAvailabilityRestriction);
                    }
                }
            }
            if (this.hasAvailableIfOwned || this.availableIfOwned) {
                codedOutputByteBufferNano.writeBool(13, this.availableIfOwned);
            }
            if (this.install != null && this.install.length > 0) {
                for (int j = 0; j < this.install.length; ++j) {
                    install = this.install[j];
                    if (install != null) {
                        codedOutputByteBufferNano.writeMessage(14, install);
                    }
                }
            }
            if (this.filterInfo != null) {
                codedOutputByteBufferNano.writeMessage(16, this.filterInfo);
            }
            if (this.ownershipInfo != null) {
                codedOutputByteBufferNano.writeMessage(17, this.ownershipInfo);
            }
            if (this.availabilityProblem != null && this.availabilityProblem.length > 0) {
                for (int k = 0; k < this.availabilityProblem.length; ++k) {
                    availabilityProblem = this.availabilityProblem[k];
                    if (availabilityProblem != null) {
                        codedOutputByteBufferNano.writeMessage(18, availabilityProblem);
                    }
                }
            }
            if (this.hasHidden || this.hidden) {
                codedOutputByteBufferNano.writeBool(21, this.hidden);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
        
        public static final class PerDeviceAvailabilityRestriction extends MessageNano
        {
            private static volatile PerDeviceAvailabilityRestriction[] _emptyArray;
            public long androidId;
            public long channelId;
            public int deviceRestriction;
            public FilterEvaluationInfo filterInfo;
            public boolean hasAndroidId;
            public boolean hasChannelId;
            public boolean hasDeviceRestriction;
            
            public PerDeviceAvailabilityRestriction() {
                super();
                this.clear();
            }
            
            public static PerDeviceAvailabilityRestriction[] emptyArray() {
                if (_emptyArray == null) {
                    synchronized (InternalNano.LAZY_INIT_LOCK) {
                        if (_emptyArray == null) {
                            _emptyArray = new PerDeviceAvailabilityRestriction[0];
                        }
                    }
                }
                return _emptyArray;
            }
            
            public PerDeviceAvailabilityRestriction clear() {
                this.androidId = 0L;
                this.hasAndroidId = false;
                this.deviceRestriction = 1;
                this.hasDeviceRestriction = false;
                this.channelId = 0L;
                this.hasChannelId = false;
                this.filterInfo = null;
                this.cachedSize = -1;
                return this;
            }
            
            @Override
            protected int computeSerializedSize() {
                int computeSerializedSize;
                computeSerializedSize = super.computeSerializedSize();
                if (this.hasAndroidId || this.androidId != 0L) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeFixed64Size(10, this.androidId);
                }
                if (this.deviceRestriction != 1 || this.hasDeviceRestriction) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(11, this.deviceRestriction);
                }
                if (this.hasChannelId || this.channelId != 0L) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeInt64Size(12, this.channelId);
                }
                if (this.filterInfo != null) {
                    computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(15, this.filterInfo);
                }
                return computeSerializedSize;
            }
            
            @Override
            public PerDeviceAvailabilityRestriction mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                        case 81: {
                            this.androidId = codedInputByteBufferNano.readFixed64();
                            this.hasAndroidId = true;
                            continue;
                        }
                        case 88: {
                            int32 = codedInputByteBufferNano.readInt32();
                            switch (int32) {
                                default: {
                                    continue;
                                }
                                case 1:
                                case 2:
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
                                case 20:
                                case 21: {
                                    this.deviceRestriction = int32;
                                    this.hasDeviceRestriction = true;
                                    continue;
                                }
                            }
                        }
                        case 96: {
                            this.channelId = codedInputByteBufferNano.readInt64();
                            this.hasChannelId = true;
                            continue;
                        }
                        case 122: {
                            if (this.filterInfo == null) {
                                this.filterInfo = new FilterEvaluationInfo();
                            }
                            codedInputByteBufferNano.readMessage(this.filterInfo);
                            continue;
                        }
                    }
                }
            }
            
            @Override
            public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
                if (this.hasAndroidId || this.androidId != 0L) {
                    codedOutputByteBufferNano.writeFixed64(10, this.androidId);
                }
                if (this.deviceRestriction != 1 || this.hasDeviceRestriction) {
                    codedOutputByteBufferNano.writeInt32(11, this.deviceRestriction);
                }
                if (this.hasChannelId || this.channelId != 0L) {
                    codedOutputByteBufferNano.writeInt64(12, this.channelId);
                }
                if (this.filterInfo != null) {
                    codedOutputByteBufferNano.writeMessage(15, this.filterInfo);
                }
                super.writeTo(codedOutputByteBufferNano);
            }
        }
    }
    
    public static final class AvailabilityProblem extends MessageNano
    {
        private static volatile AvailabilityProblem[] _emptyArray;
        public boolean hasProblemType;
        public String[] missingValue;
        public int problemType;
        
        public AvailabilityProblem() {
            super();
            this.clear();
        }
        
        public static AvailabilityProblem[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new AvailabilityProblem[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public AvailabilityProblem clear() {
            this.problemType = 1;
            this.hasProblemType = false;
            this.missingValue = WireFormatNano.EMPTY_STRING_ARRAY;
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
            if (this.problemType != 1 || this.hasProblemType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.problemType);
            }
            if (this.missingValue != null && this.missingValue.length > 0) {
                n = 0;
                n2 = 0;
                for (int i = 0; i < this.missingValue.length; ++i) {
                    s = this.missingValue[i];
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
        public AvailabilityProblem mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
            int repeatedFieldArrayLength;
            int i;
            String[] missingValue;
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
                            case 8: {
                                this.problemType = int32;
                                this.hasProblemType = true;
                                continue;
                            }
                        }
                    }
                    case 18: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 18);
                        if (this.missingValue == null) {
                            i = 0;
                        }
                        else {
                            i = this.missingValue.length;
                        }
                        missingValue = new String[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.missingValue, 0, missingValue, 0, i);
                        }
                        while (i < -1 + missingValue.length) {
                            missingValue[i] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        missingValue[i] = codedInputByteBufferNano.readString();
                        this.missingValue = missingValue;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            String s;
            if (this.problemType != 1 || this.hasProblemType) {
                codedOutputByteBufferNano.writeInt32(1, this.problemType);
            }
            if (this.missingValue != null && this.missingValue.length > 0) {
                for (int i = 0; i < this.missingValue.length; ++i) {
                    s = this.missingValue[i];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(2, s);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class FilterEvaluationInfo extends MessageNano
    {
        public RuleEvaluation[] ruleEvaluation;
        
        public FilterEvaluationInfo() {
            super();
            this.clear();
        }
        
        public FilterEvaluationInfo clear() {
            this.ruleEvaluation = RuleEvaluation.emptyArray();
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            RuleEvaluation ruleEvaluation;
            computeSerializedSize = super.computeSerializedSize();
            if (this.ruleEvaluation != null && this.ruleEvaluation.length > 0) {
                for (int i = 0; i < this.ruleEvaluation.length; ++i) {
                    ruleEvaluation = this.ruleEvaluation[i];
                    if (ruleEvaluation != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, ruleEvaluation);
                    }
                }
            }
            return computeSerializedSize;
        }
        
        @Override
        public FilterEvaluationInfo mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            RuleEvaluation[] ruleEvaluation;
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
                        if (this.ruleEvaluation == null) {
                            i = 0;
                        }
                        else {
                            i = this.ruleEvaluation.length;
                        }
                        ruleEvaluation = new RuleEvaluation[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.ruleEvaluation, 0, ruleEvaluation, 0, i);
                        }
                        while (i < -1 + ruleEvaluation.length) {
                            codedInputByteBufferNano.readMessage(ruleEvaluation[i] = new RuleEvaluation());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(ruleEvaluation[i] = new RuleEvaluation());
                        this.ruleEvaluation = ruleEvaluation;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            RuleEvaluation ruleEvaluation;
            if (this.ruleEvaluation != null && this.ruleEvaluation.length > 0) {
                for (int i = 0; i < this.ruleEvaluation.length; ++i) {
                    ruleEvaluation = this.ruleEvaluation[i];
                    if (ruleEvaluation != null) {
                        codedOutputByteBufferNano.writeMessage(1, ruleEvaluation);
                    }
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class Rule extends MessageNano
    {
        private static volatile Rule[] _emptyArray;
        public int availabilityProblemType;
        public String comment;
        public int[] constArg;
        public double[] doubleArg;
        public boolean hasAvailabilityProblemType;
        public boolean hasComment;
        public boolean hasIncludeMissingValues;
        public boolean hasKey;
        public boolean hasNegate;
        public boolean hasOperator;
        public boolean hasResponseCode;
        public boolean includeMissingValues;
        public int key;
        public long[] longArg;
        public boolean negate;
        public int operator;
        public int responseCode;
        public String[] stringArg;
        public long[] stringArgHash;
        public Rule[] subrule;
        
        public Rule() {
            super();
            this.clear();
        }
        
        public static Rule[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new Rule[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public Rule clear() {
            this.negate = false;
            this.hasNegate = false;
            this.operator = 1;
            this.hasOperator = false;
            this.key = 1;
            this.hasKey = false;
            this.stringArg = WireFormatNano.EMPTY_STRING_ARRAY;
            this.stringArgHash = WireFormatNano.EMPTY_LONG_ARRAY;
            this.longArg = WireFormatNano.EMPTY_LONG_ARRAY;
            this.doubleArg = WireFormatNano.EMPTY_DOUBLE_ARRAY;
            this.constArg = WireFormatNano.EMPTY_INT_ARRAY;
            this.subrule = emptyArray();
            this.responseCode = 1;
            this.hasResponseCode = false;
            this.availabilityProblemType = 1;
            this.hasAvailabilityProblemType = false;
            this.includeMissingValues = false;
            this.hasIncludeMissingValues = false;
            this.comment = "";
            this.hasComment = false;
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
            Rule rule;
            int n4;
            computeSerializedSize = super.computeSerializedSize();
            if (this.hasNegate || this.negate) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(1, this.negate);
            }
            if (this.operator != 1 || this.hasOperator) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(2, this.operator);
            }
            if (this.key != 1 || this.hasKey) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(3, this.key);
            }
            if (this.stringArg != null && this.stringArg.length > 0) {
                n = 0;
                n2 = 0;
                for (int i = 0; i < this.stringArg.length; ++i) {
                    s = this.stringArg[i];
                    if (s != null) {
                        ++n;
                        n2 += CodedOutputByteBufferNano.computeStringSizeNoTag(s);
                    }
                }
                computeSerializedSize = computeSerializedSize + n2 + n * 1;
            }
            if (this.longArg != null && this.longArg.length > 0) {
                n3 = 0;
                for (int j = 0; j < this.longArg.length; ++j) {
                    n3 += CodedOutputByteBufferNano.computeInt64SizeNoTag(this.longArg[j]);
                }
                computeSerializedSize = computeSerializedSize + n3 + 1 * this.longArg.length;
            }
            if (this.doubleArg != null && this.doubleArg.length > 0) {
                computeSerializedSize = computeSerializedSize + 8 * this.doubleArg.length + 1 * this.doubleArg.length;
            }
            if (this.subrule != null && this.subrule.length > 0) {
                for (int k = 0; k < this.subrule.length; ++k) {
                    rule = this.subrule[k];
                    if (rule != null) {
                        computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(7, rule);
                    }
                }
            }
            if (this.responseCode != 1 || this.hasResponseCode) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(8, this.responseCode);
            }
            if (this.hasComment || !this.comment.equals("")) {
                computeSerializedSize += CodedOutputByteBufferNano.computeStringSize(9, this.comment);
            }
            if (this.stringArgHash != null && this.stringArgHash.length > 0) {
                computeSerializedSize = computeSerializedSize + 8 * this.stringArgHash.length + 1 * this.stringArgHash.length;
            }
            if (this.constArg != null && this.constArg.length > 0) {
                n4 = 0;
                for (int l = 0; l < this.constArg.length; ++l) {
                    n4 += CodedOutputByteBufferNano.computeInt32SizeNoTag(this.constArg[l]);
                }
                computeSerializedSize = computeSerializedSize + n4 + 1 * this.constArg.length;
            }
            if (this.availabilityProblemType != 1 || this.hasAvailabilityProblemType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(12, this.availabilityProblemType);
            }
            if (this.hasIncludeMissingValues || this.includeMissingValues) {
                computeSerializedSize += CodedOutputByteBufferNano.computeBoolSize(13, this.includeMissingValues);
            }
            return computeSerializedSize;
        }
        
        @Override
        public Rule mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int int32;
            int int2;
            int repeatedFieldArrayLength;
            int i;
            String[] stringArg;
            int repeatedFieldArrayLength2;
            int j;
            long[] longArg;
            int pushLimit;
            int n;
            int position;
            int k;
            long[] longArg2;
            int repeatedFieldArrayLength3;
            int l;
            double[] doubleArg;
            int rawVarint32;
            int pushLimit2;
            int n2;
            int length;
            double[] doubleArg2;
            int repeatedFieldArrayLength4;
            int length2;
            Rule[] subrule;
            int int3;
            int repeatedFieldArrayLength5;
            int length3;
            long[] stringArgHash;
            int rawVarint2;
            int pushLimit3;
            int n3;
            int length4;
            long[] stringArgHash2;
            int repeatedFieldArrayLength6;
            int[] constArg;
            int n4;
            int n5;
            int int4;
            int n6;
            int length5;
            int[] constArg2;
            int pushLimit4;
            int n7;
            int position2;
            int length6;
            int[] constArg3;
            int int5;
            int n8;
            int int6;
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
                        this.negate = codedInputByteBufferNano.readBool();
                        this.hasNegate = true;
                        continue;
                    }
                    case 16: {
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
                            case 12:
                            case 13: {
                                this.operator = int32;
                                this.hasOperator = true;
                                continue;
                            }
                        }
                    }
                    case 24: {
                        int2 = codedInputByteBufferNano.readInt32();
                        switch (int2) {
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
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                            case 29:
                            case 30:
                            case 31:
                            case 32:
                            case 33: {
                                this.key = int2;
                                this.hasKey = true;
                                continue;
                            }
                        }
                    }
                    case 34: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 34);
                        if (this.stringArg == null) {
                            i = 0;
                        }
                        else {
                            i = this.stringArg.length;
                        }
                        stringArg = new String[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.stringArg, 0, stringArg, 0, i);
                        }
                        while (i < -1 + stringArg.length) {
                            stringArg[i] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        stringArg[i] = codedInputByteBufferNano.readString();
                        this.stringArg = stringArg;
                        continue;
                    }
                    case 40: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 40);
                        if (this.longArg == null) {
                            j = 0;
                        }
                        else {
                            j = this.longArg.length;
                        }
                        longArg = new long[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.longArg, 0, longArg, 0, j);
                        }
                        while (j < -1 + longArg.length) {
                            longArg[j] = codedInputByteBufferNano.readInt64();
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        longArg[j] = codedInputByteBufferNano.readInt64();
                        this.longArg = longArg;
                        continue;
                    }
                    case 42: {
                        pushLimit = codedInputByteBufferNano.pushLimit(codedInputByteBufferNano.readRawVarint32());
                        n = 0;
                        position = codedInputByteBufferNano.getPosition();
                        while (codedInputByteBufferNano.getBytesUntilLimit() > 0) {
                            codedInputByteBufferNano.readInt64();
                            ++n;
                        }
                        codedInputByteBufferNano.rewindToPosition(position);
                        if (this.longArg == null) {
                            k = 0;
                        }
                        else {
                            k = this.longArg.length;
                        }
                        longArg2 = new long[k + n];
                        if (k != 0) {
                            System.arraycopy(this.longArg, 0, longArg2, 0, k);
                        }
                        while (k < longArg2.length) {
                            longArg2[k] = codedInputByteBufferNano.readInt64();
                            ++k;
                        }
                        this.longArg = longArg2;
                        codedInputByteBufferNano.popLimit(pushLimit);
                        continue;
                    }
                    case 49: {
                        repeatedFieldArrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 49);
                        if (this.doubleArg == null) {
                            l = 0;
                        }
                        else {
                            l = this.doubleArg.length;
                        }
                        doubleArg = new double[l + repeatedFieldArrayLength3];
                        if (l != 0) {
                            System.arraycopy(this.doubleArg, 0, doubleArg, 0, l);
                        }
                        while (l < -1 + doubleArg.length) {
                            doubleArg[l] = codedInputByteBufferNano.readDouble();
                            codedInputByteBufferNano.readTag();
                            ++l;
                        }
                        doubleArg[l] = codedInputByteBufferNano.readDouble();
                        this.doubleArg = doubleArg;
                        continue;
                    }
                    case 50: {
                        rawVarint32 = codedInputByteBufferNano.readRawVarint32();
                        pushLimit2 = codedInputByteBufferNano.pushLimit(rawVarint32);
                        n2 = rawVarint32 / 8;
                        if (this.doubleArg == null) {
                            length = 0;
                        }
                        else {
                            length = this.doubleArg.length;
                        }
                        doubleArg2 = new double[length + n2];
                        if (length != 0) {
                            System.arraycopy(this.doubleArg, 0, doubleArg2, 0, length);
                        }
                        while (length < doubleArg2.length) {
                            doubleArg2[length] = codedInputByteBufferNano.readDouble();
                            ++length;
                        }
                        this.doubleArg = doubleArg2;
                        codedInputByteBufferNano.popLimit(pushLimit2);
                        continue;
                    }
                    case 58: {
                        repeatedFieldArrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 58);
                        if (this.subrule == null) {
                            length2 = 0;
                        }
                        else {
                            length2 = this.subrule.length;
                        }
                        subrule = new Rule[length2 + repeatedFieldArrayLength4];
                        if (length2 != 0) {
                            System.arraycopy(this.subrule, 0, subrule, 0, length2);
                        }
                        while (length2 < -1 + subrule.length) {
                            codedInputByteBufferNano.readMessage(subrule[length2] = new Rule());
                            codedInputByteBufferNano.readTag();
                            ++length2;
                        }
                        codedInputByteBufferNano.readMessage(subrule[length2] = new Rule());
                        this.subrule = subrule;
                        continue;
                    }
                    case 64: {
                        int3 = codedInputByteBufferNano.readInt32();
                        switch (int3) {
                            default: {
                                continue;
                            }
                            case 1:
                            case 2:
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
                            case 20:
                            case 21: {
                                this.responseCode = int3;
                                this.hasResponseCode = true;
                                continue;
                            }
                        }
                    }
                    case 74: {
                        this.comment = codedInputByteBufferNano.readString();
                        this.hasComment = true;
                        continue;
                    }
                    case 81: {
                        repeatedFieldArrayLength5 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 81);
                        if (this.stringArgHash == null) {
                            length3 = 0;
                        }
                        else {
                            length3 = this.stringArgHash.length;
                        }
                        stringArgHash = new long[length3 + repeatedFieldArrayLength5];
                        if (length3 != 0) {
                            System.arraycopy(this.stringArgHash, 0, stringArgHash, 0, length3);
                        }
                        while (length3 < -1 + stringArgHash.length) {
                            stringArgHash[length3] = codedInputByteBufferNano.readFixed64();
                            codedInputByteBufferNano.readTag();
                            ++length3;
                        }
                        stringArgHash[length3] = codedInputByteBufferNano.readFixed64();
                        this.stringArgHash = stringArgHash;
                        continue;
                    }
                    case 82: {
                        rawVarint2 = codedInputByteBufferNano.readRawVarint32();
                        pushLimit3 = codedInputByteBufferNano.pushLimit(rawVarint2);
                        n3 = rawVarint2 / 8;
                        if (this.stringArgHash == null) {
                            length4 = 0;
                        }
                        else {
                            length4 = this.stringArgHash.length;
                        }
                        stringArgHash2 = new long[length4 + n3];
                        if (length4 != 0) {
                            System.arraycopy(this.stringArgHash, 0, stringArgHash2, 0, length4);
                        }
                        while (length4 < stringArgHash2.length) {
                            stringArgHash2[length4] = codedInputByteBufferNano.readFixed64();
                            ++length4;
                        }
                        this.stringArgHash = stringArgHash2;
                        codedInputByteBufferNano.popLimit(pushLimit3);
                        continue;
                    }
                    case 88: {
                        repeatedFieldArrayLength6 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 88);
                        constArg = new int[repeatedFieldArrayLength6];
                        n4 = 0;
                        n5 = 0;
                        while (n4 < repeatedFieldArrayLength6) {
                            if (n4 != 0) {
                                codedInputByteBufferNano.readTag();
                            }
                            int4 = codedInputByteBufferNano.readInt32();
                            switch (int4) {
                                default: {
                                    n6 = n5;
                                    break;
                                }
                                case 1:
                                case 2:
                                case 3:
                                case 4: {
                                    n6 = n5 + 1;
                                    constArg[n5] = int4;
                                    break;
                                }
                            }
                            ++n4;
                            n5 = n6;
                        }
                        if (n5 == 0) {
                            continue;
                        }
                        if (this.constArg == null) {
                            length5 = 0;
                        }
                        else {
                            length5 = this.constArg.length;
                        }
                        if (length5 == 0 && n5 == constArg.length) {
                            this.constArg = constArg;
                            continue;
                        }
                        constArg2 = new int[length5 + n5];
                        if (length5 != 0) {
                            System.arraycopy(this.constArg, 0, constArg2, 0, length5);
                        }
                        System.arraycopy(constArg, 0, constArg2, length5, n5);
                        this.constArg = constArg2;
                        continue;
                    }
                    case 90: {
                        pushLimit4 = codedInputByteBufferNano.pushLimit(codedInputByteBufferNano.readRawVarint32());
                        n7 = 0;
                        position2 = codedInputByteBufferNano.getPosition();
                        while (codedInputByteBufferNano.getBytesUntilLimit() > 0) {
                            switch (codedInputByteBufferNano.readInt32()) {
                                default: {
                                    continue;
                                }
                                case 1:
                                case 2:
                                case 3:
                                case 4: {
                                    ++n7;
                                    continue;
                                }
                            }
                        }
                        if (n7 != 0) {
                            codedInputByteBufferNano.rewindToPosition(position2);
                            if (this.constArg == null) {
                                length6 = 0;
                            }
                            else {
                                length6 = this.constArg.length;
                            }
                            constArg3 = new int[length6 + n7];
                            if (length6 != 0) {
                                System.arraycopy(this.constArg, 0, constArg3, 0, length6);
                            }
                            while (codedInputByteBufferNano.getBytesUntilLimit() > 0) {
                                int5 = codedInputByteBufferNano.readInt32();
                                switch (int5) {
                                    default: {
                                        continue;
                                    }
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4: {
                                        n8 = length6 + 1;
                                        constArg3[length6] = int5;
                                        length6 = n8;
                                        continue;
                                    }
                                }
                            }
                            this.constArg = constArg3;
                        }
                        codedInputByteBufferNano.popLimit(pushLimit4);
                        continue;
                    }
                    case 96: {
                        int6 = codedInputByteBufferNano.readInt32();
                        switch (int6) {
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
                            case 8: {
                                this.availabilityProblemType = int6;
                                this.hasAvailabilityProblemType = true;
                                continue;
                            }
                        }
                    }
                    case 104: {
                        this.includeMissingValues = codedInputByteBufferNano.readBool();
                        this.hasIncludeMissingValues = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            String s;
            Rule rule;
            if (this.hasNegate || this.negate) {
                codedOutputByteBufferNano.writeBool(1, this.negate);
            }
            if (this.operator != 1 || this.hasOperator) {
                codedOutputByteBufferNano.writeInt32(2, this.operator);
            }
            if (this.key != 1 || this.hasKey) {
                codedOutputByteBufferNano.writeInt32(3, this.key);
            }
            if (this.stringArg != null && this.stringArg.length > 0) {
                for (int i = 0; i < this.stringArg.length; ++i) {
                    s = this.stringArg[i];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(4, s);
                    }
                }
            }
            if (this.longArg != null && this.longArg.length > 0) {
                for (int j = 0; j < this.longArg.length; ++j) {
                    codedOutputByteBufferNano.writeInt64(5, this.longArg[j]);
                }
            }
            if (this.doubleArg != null && this.doubleArg.length > 0) {
                for (int k = 0; k < this.doubleArg.length; ++k) {
                    codedOutputByteBufferNano.writeDouble(6, this.doubleArg[k]);
                }
            }
            if (this.subrule != null && this.subrule.length > 0) {
                for (int l = 0; l < this.subrule.length; ++l) {
                    rule = this.subrule[l];
                    if (rule != null) {
                        codedOutputByteBufferNano.writeMessage(7, rule);
                    }
                }
            }
            if (this.responseCode != 1 || this.hasResponseCode) {
                codedOutputByteBufferNano.writeInt32(8, this.responseCode);
            }
            if (this.hasComment || !this.comment.equals("")) {
                codedOutputByteBufferNano.writeString(9, this.comment);
            }
            if (this.stringArgHash != null && this.stringArgHash.length > 0) {
                for (int n = 0; n < this.stringArgHash.length; ++n) {
                    codedOutputByteBufferNano.writeFixed64(10, this.stringArgHash[n]);
                }
            }
            if (this.constArg != null && this.constArg.length > 0) {
                for (int n2 = 0; n2 < this.constArg.length; ++n2) {
                    codedOutputByteBufferNano.writeInt32(11, this.constArg[n2]);
                }
            }
            if (this.availabilityProblemType != 1 || this.hasAvailabilityProblemType) {
                codedOutputByteBufferNano.writeInt32(12, this.availabilityProblemType);
            }
            if (this.hasIncludeMissingValues || this.includeMissingValues) {
                codedOutputByteBufferNano.writeBool(13, this.includeMissingValues);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
    
    public static final class RuleEvaluation extends MessageNano
    {
        private static volatile RuleEvaluation[] _emptyArray;
        public boolean[] actualBoolValue;
        public double[] actualDoubleValue;
        public long[] actualLongValue;
        public String[] actualStringValue;
        public Rule rule;
        
        public RuleEvaluation() {
            super();
            this.clear();
        }
        
        public static RuleEvaluation[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new RuleEvaluation[0];
                    }
                }
            }
            return _emptyArray;
        }
        
        public RuleEvaluation clear() {
            this.rule = null;
            this.actualStringValue = WireFormatNano.EMPTY_STRING_ARRAY;
            this.actualLongValue = WireFormatNano.EMPTY_LONG_ARRAY;
            this.actualBoolValue = WireFormatNano.EMPTY_BOOLEAN_ARRAY;
            this.actualDoubleValue = WireFormatNano.EMPTY_DOUBLE_ARRAY;
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
            computeSerializedSize = super.computeSerializedSize();
            if (this.rule != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.rule);
            }
            if (this.actualStringValue != null && this.actualStringValue.length > 0) {
                n = 0;
                n2 = 0;
                for (int i = 0; i < this.actualStringValue.length; ++i) {
                    s = this.actualStringValue[i];
                    if (s != null) {
                        ++n;
                        n2 += CodedOutputByteBufferNano.computeStringSizeNoTag(s);
                    }
                }
                computeSerializedSize = computeSerializedSize + n2 + n * 1;
            }
            if (this.actualLongValue != null && this.actualLongValue.length > 0) {
                n3 = 0;
                for (int j = 0; j < this.actualLongValue.length; ++j) {
                    n3 += CodedOutputByteBufferNano.computeInt64SizeNoTag(this.actualLongValue[j]);
                }
                computeSerializedSize = computeSerializedSize + n3 + 1 * this.actualLongValue.length;
            }
            if (this.actualBoolValue != null && this.actualBoolValue.length > 0) {
                computeSerializedSize = computeSerializedSize + 1 * this.actualBoolValue.length + 1 * this.actualBoolValue.length;
            }
            if (this.actualDoubleValue != null && this.actualDoubleValue.length > 0) {
                computeSerializedSize = computeSerializedSize + 8 * this.actualDoubleValue.length + 1 * this.actualDoubleValue.length;
            }
            return computeSerializedSize;
        }
        
        @Override
        public RuleEvaluation mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            String[] actualStringValue;
            int repeatedFieldArrayLength2;
            int j;
            long[] actualLongValue;
            int pushLimit;
            int n;
            int position;
            int k;
            long[] actualLongValue2;
            int repeatedFieldArrayLength3;
            int l;
            boolean[] actualBoolValue;
            int pushLimit2;
            int n2;
            int position2;
            int length;
            boolean[] actualBoolValue2;
            int repeatedFieldArrayLength4;
            int length2;
            double[] actualDoubleValue;
            int rawVarint32;
            int pushLimit3;
            int n3;
            int length3;
            double[] actualDoubleValue2;
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
                        if (this.rule == null) {
                            this.rule = new Rule();
                        }
                        codedInputByteBufferNano.readMessage(this.rule);
                        continue;
                    }
                    case 18: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 18);
                        if (this.actualStringValue == null) {
                            i = 0;
                        }
                        else {
                            i = this.actualStringValue.length;
                        }
                        actualStringValue = new String[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.actualStringValue, 0, actualStringValue, 0, i);
                        }
                        while (i < -1 + actualStringValue.length) {
                            actualStringValue[i] = codedInputByteBufferNano.readString();
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        actualStringValue[i] = codedInputByteBufferNano.readString();
                        this.actualStringValue = actualStringValue;
                        continue;
                    }
                    case 24: {
                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 24);
                        if (this.actualLongValue == null) {
                            j = 0;
                        }
                        else {
                            j = this.actualLongValue.length;
                        }
                        actualLongValue = new long[j + repeatedFieldArrayLength2];
                        if (j != 0) {
                            System.arraycopy(this.actualLongValue, 0, actualLongValue, 0, j);
                        }
                        while (j < -1 + actualLongValue.length) {
                            actualLongValue[j] = codedInputByteBufferNano.readInt64();
                            codedInputByteBufferNano.readTag();
                            ++j;
                        }
                        actualLongValue[j] = codedInputByteBufferNano.readInt64();
                        this.actualLongValue = actualLongValue;
                        continue;
                    }
                    case 26: {
                        pushLimit = codedInputByteBufferNano.pushLimit(codedInputByteBufferNano.readRawVarint32());
                        n = 0;
                        position = codedInputByteBufferNano.getPosition();
                        while (codedInputByteBufferNano.getBytesUntilLimit() > 0) {
                            codedInputByteBufferNano.readInt64();
                            ++n;
                        }
                        codedInputByteBufferNano.rewindToPosition(position);
                        if (this.actualLongValue == null) {
                            k = 0;
                        }
                        else {
                            k = this.actualLongValue.length;
                        }
                        actualLongValue2 = new long[k + n];
                        if (k != 0) {
                            System.arraycopy(this.actualLongValue, 0, actualLongValue2, 0, k);
                        }
                        while (k < actualLongValue2.length) {
                            actualLongValue2[k] = codedInputByteBufferNano.readInt64();
                            ++k;
                        }
                        this.actualLongValue = actualLongValue2;
                        codedInputByteBufferNano.popLimit(pushLimit);
                        continue;
                    }
                    case 32: {
                        repeatedFieldArrayLength3 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 32);
                        if (this.actualBoolValue == null) {
                            l = 0;
                        }
                        else {
                            l = this.actualBoolValue.length;
                        }
                        actualBoolValue = new boolean[l + repeatedFieldArrayLength3];
                        if (l != 0) {
                            System.arraycopy(this.actualBoolValue, 0, actualBoolValue, 0, l);
                        }
                        while (l < -1 + actualBoolValue.length) {
                            actualBoolValue[l] = codedInputByteBufferNano.readBool();
                            codedInputByteBufferNano.readTag();
                            ++l;
                        }
                        actualBoolValue[l] = codedInputByteBufferNano.readBool();
                        this.actualBoolValue = actualBoolValue;
                        continue;
                    }
                    case 34: {
                        pushLimit2 = codedInputByteBufferNano.pushLimit(codedInputByteBufferNano.readRawVarint32());
                        n2 = 0;
                        position2 = codedInputByteBufferNano.getPosition();
                        while (codedInputByteBufferNano.getBytesUntilLimit() > 0) {
                            codedInputByteBufferNano.readBool();
                            ++n2;
                        }
                        codedInputByteBufferNano.rewindToPosition(position2);
                        if (this.actualBoolValue == null) {
                            length = 0;
                        }
                        else {
                            length = this.actualBoolValue.length;
                        }
                        actualBoolValue2 = new boolean[length + n2];
                        if (length != 0) {
                            System.arraycopy(this.actualBoolValue, 0, actualBoolValue2, 0, length);
                        }
                        while (length < actualBoolValue2.length) {
                            actualBoolValue2[length] = codedInputByteBufferNano.readBool();
                            ++length;
                        }
                        this.actualBoolValue = actualBoolValue2;
                        codedInputByteBufferNano.popLimit(pushLimit2);
                        continue;
                    }
                    case 41: {
                        repeatedFieldArrayLength4 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 41);
                        if (this.actualDoubleValue == null) {
                            length2 = 0;
                        }
                        else {
                            length2 = this.actualDoubleValue.length;
                        }
                        actualDoubleValue = new double[length2 + repeatedFieldArrayLength4];
                        if (length2 != 0) {
                            System.arraycopy(this.actualDoubleValue, 0, actualDoubleValue, 0, length2);
                        }
                        while (length2 < -1 + actualDoubleValue.length) {
                            actualDoubleValue[length2] = codedInputByteBufferNano.readDouble();
                            codedInputByteBufferNano.readTag();
                            ++length2;
                        }
                        actualDoubleValue[length2] = codedInputByteBufferNano.readDouble();
                        this.actualDoubleValue = actualDoubleValue;
                        continue;
                    }
                    case 42: {
                        rawVarint32 = codedInputByteBufferNano.readRawVarint32();
                        pushLimit3 = codedInputByteBufferNano.pushLimit(rawVarint32);
                        n3 = rawVarint32 / 8;
                        if (this.actualDoubleValue == null) {
                            length3 = 0;
                        }
                        else {
                            length3 = this.actualDoubleValue.length;
                        }
                        actualDoubleValue2 = new double[length3 + n3];
                        if (length3 != 0) {
                            System.arraycopy(this.actualDoubleValue, 0, actualDoubleValue2, 0, length3);
                        }
                        while (length3 < actualDoubleValue2.length) {
                            actualDoubleValue2[length3] = codedInputByteBufferNano.readDouble();
                            ++length3;
                        }
                        this.actualDoubleValue = actualDoubleValue2;
                        codedInputByteBufferNano.popLimit(pushLimit3);
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            String s;
            if (this.rule != null) {
                codedOutputByteBufferNano.writeMessage(1, this.rule);
            }
            if (this.actualStringValue != null && this.actualStringValue.length > 0) {
                for (int i = 0; i < this.actualStringValue.length; ++i) {
                    s = this.actualStringValue[i];
                    if (s != null) {
                        codedOutputByteBufferNano.writeString(2, s);
                    }
                }
            }
            if (this.actualLongValue != null && this.actualLongValue.length > 0) {
                for (int j = 0; j < this.actualLongValue.length; ++j) {
                    codedOutputByteBufferNano.writeInt64(3, this.actualLongValue[j]);
                }
            }
            if (this.actualBoolValue != null && this.actualBoolValue.length > 0) {
                for (int k = 0; k < this.actualBoolValue.length; ++k) {
                    codedOutputByteBufferNano.writeBool(4, this.actualBoolValue[k]);
                }
            }
            if (this.actualDoubleValue != null && this.actualDoubleValue.length > 0) {
                for (int l = 0; l < this.actualDoubleValue.length; ++l) {
                    codedOutputByteBufferNano.writeDouble(5, this.actualDoubleValue[l]);
                }
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
