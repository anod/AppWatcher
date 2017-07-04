package com.google.android.finsky.api.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.google.android.finsky.protos.nano.Messages.AppDetails;
import com.google.android.finsky.protos.nano.Messages.Common;
import com.google.android.finsky.protos.nano.Messages.DocV2;

import java.util.ArrayList;
import java.util.List;

public class Document {

    private final DocV2 doc;
    private SparseArray<List<Common.Image>> mImageTypeMap;

    public Document(@NonNull DocV2 docV2) {
        this.doc = docV2;
    }

    public AppDetails getAppDetails() {
        if (this.doc.details != null)
        {
            return this.doc.details.appDetails;
        }
        return new AppDetails();
    }

    public String getTitle() {
        return this.doc.title;
    }

    public String getDocId() {
        return this.doc.docid == null ? "" : this.doc.docid;
    }

    public int getBackend() {
        return this.doc.backendId;
    }

    public String getDetailsUrl() {
        return this.doc.detailsUrl == null ? "" : this.doc.detailsUrl;
    }

    public String getCreator() {
        return this.doc.creator == null ? "" : this.doc.creator;
    }

    public Common.Offer getOffer() {
        Common.Offer offer =  this.getOffer(Common.Offer.TYPE_1); // Type 1 ?
        if (offer == null)
        {
            offer = new Common.Offer();
        }
        return offer;
    }

    public @Nullable Common.Offer getOffer(final int type) {
        for (final Common.Offer offer : this.doc.offer) {
            if (offer.offerType == type) {
                return offer;
            }
        }
        return null;
    }

    public @Nullable String getIconUrl() {
        List<Common.Image> images = this.getImageTypeMap().get(4);
        if (images == null) {
            return null;
        }
        if (images.size() > 0) {
            return images.get(0).imageUrl;
        }
        return null;
    }

    private SparseArray<List<Common.Image>> getImageTypeMap() {
        if (this.mImageTypeMap == null) {
            this.mImageTypeMap = new SparseArray<>();
            for (final Common.Image image2 : this.doc.image) {
                final int imageType = image2.imageType;
                if (this.mImageTypeMap.get(imageType) == null) {
                    this.mImageTypeMap.put(imageType, new ArrayList<Common.Image>());
                }
                this.mImageTypeMap.get(imageType).add(image2);
            }
        }
        return this.mImageTypeMap;
    }
}
