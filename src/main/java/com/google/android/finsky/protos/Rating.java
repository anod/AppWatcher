package com.google.android.finsky.protos;

import com.google.protobuf.nano.*;
import java.io.*;

public interface Rating
{
    public static final class AggregateRating extends MessageNano
    {
        public double bayesianMeanRating;
        public long commentCount;
        public long fiveStarRatings;
        public long fourStarRatings;
        public boolean hasBayesianMeanRating;
        public boolean hasCommentCount;
        public boolean hasFiveStarRatings;
        public boolean hasFourStarRatings;
        public boolean hasOneStarRatings;
        public boolean hasRatingsCount;
        public boolean hasStarRating;
        public boolean hasThreeStarRatings;
        public boolean hasThumbsDownCount;
        public boolean hasThumbsUpCount;
        public boolean hasTwoStarRatings;
        public boolean hasType;
        public long oneStarRatings;
        public long ratingsCount;
        public float starRating;
        public long threeStarRatings;
        public long thumbsDownCount;
        public long thumbsUpCount;
        public long twoStarRatings;
        public int type;
        
        public AggregateRating() {
            super();
            this.clear();
        }
        
        public AggregateRating clear() {
            this.type = 1;
            this.hasType = false;
            this.starRating = 0.0f;
            this.hasStarRating = false;
            this.ratingsCount = 0L;
            this.hasRatingsCount = false;
            this.commentCount = 0L;
            this.hasCommentCount = false;
            this.oneStarRatings = 0L;
            this.hasOneStarRatings = false;
            this.twoStarRatings = 0L;
            this.hasTwoStarRatings = false;
            this.threeStarRatings = 0L;
            this.hasThreeStarRatings = false;
            this.fourStarRatings = 0L;
            this.hasFourStarRatings = false;
            this.fiveStarRatings = 0L;
            this.hasFiveStarRatings = false;
            this.bayesianMeanRating = 0.0;
            this.hasBayesianMeanRating = false;
            this.thumbsUpCount = 0L;
            this.hasThumbsUpCount = false;
            this.thumbsDownCount = 0L;
            this.hasThumbsDownCount = false;
            this.cachedSize = -1;
            return this;
        }
        
        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.type != 1 || this.hasType) {
                computeSerializedSize += CodedOutputByteBufferNano.computeInt32Size(1, this.type);
            }
            if (this.hasStarRating || Float.floatToIntBits(this.starRating) != Float.floatToIntBits(0.0f)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeFloatSize(2, this.starRating);
            }
            if (this.hasRatingsCount || this.ratingsCount != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(3, this.ratingsCount);
            }
            if (this.hasOneStarRatings || this.oneStarRatings != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(4, this.oneStarRatings);
            }
            if (this.hasTwoStarRatings || this.twoStarRatings != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(5, this.twoStarRatings);
            }
            if (this.hasThreeStarRatings || this.threeStarRatings != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(6, this.threeStarRatings);
            }
            if (this.hasFourStarRatings || this.fourStarRatings != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(7, this.fourStarRatings);
            }
            if (this.hasFiveStarRatings || this.fiveStarRatings != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(8, this.fiveStarRatings);
            }
            if (this.hasThumbsUpCount || this.thumbsUpCount != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(9, this.thumbsUpCount);
            }
            if (this.hasThumbsDownCount || this.thumbsDownCount != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(10, this.thumbsDownCount);
            }
            if (this.hasCommentCount || this.commentCount != 0L) {
                computeSerializedSize += CodedOutputByteBufferNano.computeUInt64Size(11, this.commentCount);
            }
            if (this.hasBayesianMeanRating || Double.doubleToLongBits(this.bayesianMeanRating) != Double.doubleToLongBits(0.0)) {
                computeSerializedSize += CodedOutputByteBufferNano.computeDoubleSize(12, this.bayesianMeanRating);
            }
            return computeSerializedSize;
        }
        
        @Override
        public AggregateRating mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
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
                            case 3: {
                                this.type = int32;
                                this.hasType = true;
                                continue;
                            }
                        }
                    }
                    case 21: {
                        this.starRating = codedInputByteBufferNano.readFloat();
                        this.hasStarRating = true;
                        continue;
                    }
                    case 24: {
                        this.ratingsCount = codedInputByteBufferNano.readUInt64();
                        this.hasRatingsCount = true;
                        continue;
                    }
                    case 32: {
                        this.oneStarRatings = codedInputByteBufferNano.readUInt64();
                        this.hasOneStarRatings = true;
                        continue;
                    }
                    case 40: {
                        this.twoStarRatings = codedInputByteBufferNano.readUInt64();
                        this.hasTwoStarRatings = true;
                        continue;
                    }
                    case 48: {
                        this.threeStarRatings = codedInputByteBufferNano.readUInt64();
                        this.hasThreeStarRatings = true;
                        continue;
                    }
                    case 56: {
                        this.fourStarRatings = codedInputByteBufferNano.readUInt64();
                        this.hasFourStarRatings = true;
                        continue;
                    }
                    case 64: {
                        this.fiveStarRatings = codedInputByteBufferNano.readUInt64();
                        this.hasFiveStarRatings = true;
                        continue;
                    }
                    case 72: {
                        this.thumbsUpCount = codedInputByteBufferNano.readUInt64();
                        this.hasThumbsUpCount = true;
                        continue;
                    }
                    case 80: {
                        this.thumbsDownCount = codedInputByteBufferNano.readUInt64();
                        this.hasThumbsDownCount = true;
                        continue;
                    }
                    case 88: {
                        this.commentCount = codedInputByteBufferNano.readUInt64();
                        this.hasCommentCount = true;
                        continue;
                    }
                    case 97: {
                        this.bayesianMeanRating = codedInputByteBufferNano.readDouble();
                        this.hasBayesianMeanRating = true;
                        continue;
                    }
                }
            }
        }
        
        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.type != 1 || this.hasType) {
                codedOutputByteBufferNano.writeInt32(1, this.type);
            }
            if (this.hasStarRating || Float.floatToIntBits(this.starRating) != Float.floatToIntBits(0.0f)) {
                codedOutputByteBufferNano.writeFloat(2, this.starRating);
            }
            if (this.hasRatingsCount || this.ratingsCount != 0L) {
                codedOutputByteBufferNano.writeUInt64(3, this.ratingsCount);
            }
            if (this.hasOneStarRatings || this.oneStarRatings != 0L) {
                codedOutputByteBufferNano.writeUInt64(4, this.oneStarRatings);
            }
            if (this.hasTwoStarRatings || this.twoStarRatings != 0L) {
                codedOutputByteBufferNano.writeUInt64(5, this.twoStarRatings);
            }
            if (this.hasThreeStarRatings || this.threeStarRatings != 0L) {
                codedOutputByteBufferNano.writeUInt64(6, this.threeStarRatings);
            }
            if (this.hasFourStarRatings || this.fourStarRatings != 0L) {
                codedOutputByteBufferNano.writeUInt64(7, this.fourStarRatings);
            }
            if (this.hasFiveStarRatings || this.fiveStarRatings != 0L) {
                codedOutputByteBufferNano.writeUInt64(8, this.fiveStarRatings);
            }
            if (this.hasThumbsUpCount || this.thumbsUpCount != 0L) {
                codedOutputByteBufferNano.writeUInt64(9, this.thumbsUpCount);
            }
            if (this.hasThumbsDownCount || this.thumbsDownCount != 0L) {
                codedOutputByteBufferNano.writeUInt64(10, this.thumbsDownCount);
            }
            if (this.hasCommentCount || this.commentCount != 0L) {
                codedOutputByteBufferNano.writeUInt64(11, this.commentCount);
            }
            if (this.hasBayesianMeanRating || Double.doubleToLongBits(this.bayesianMeanRating) != Double.doubleToLongBits(0.0)) {
                codedOutputByteBufferNano.writeDouble(12, this.bayesianMeanRating);
            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }
}
