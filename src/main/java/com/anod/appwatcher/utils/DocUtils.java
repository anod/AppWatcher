package com.anod.appwatcher.utils;

import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.Common;

import java.util.List;

/**
 * @author alex
 * @date 2015-02-23
 */
public class DocUtils {
    private static final int OFFER_TYPE = 1;

    public static String getIconUrl(Document doc) {
        List<Common.Image> images = doc.getImages(4);
        if (images == null) {
            return null;
        }
        if (images.size() > 0) {
            return images.get(0).imageUrl;
        }
        return null;
    }

    public static Common.Offer getOffer(Document doc) {
        return doc.getOffer(OFFER_TYPE); // Type 1 ?
    }

    public static String getUrl(String packageName)
    {
        return "details?doc="+packageName;
    }
}
