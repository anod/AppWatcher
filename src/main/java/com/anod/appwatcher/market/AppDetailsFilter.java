package com.anod.appwatcher.market;

import com.anod.appwatcher.utils.CollectionsUtils;
import com.google.android.finsky.api.model.Document;

/**
 * @author algavris
 * @date 03/03/2017.
 */

class AppDetailsFilter {

    static CollectionsUtils.Predicate<Document> createPredicate() {
        return new CollectionsUtils.Predicate<Document>() {
            @Override
            public boolean test(Document document) {
                return document.getAppDetails() == null;
            }
        };
    }
}
