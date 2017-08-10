package com.google.android.finsky.api.model;

import com.anod.appwatcher.utils.CollectionsUtils;
import com.google.android.finsky.api.DfeUtils;
import com.google.android.finsky.protos.nano.Messages;
import com.google.android.finsky.protos.nano.Messages.DocV2;

import java.util.Arrays;
import java.util.List;

public abstract class ContainerList<T> extends PaginatedList<T, Document>
{
    private final CollectionsUtils.Predicate<Document> responseFiler;

    ContainerList(final String url, final boolean autoLoadNextPage, CollectionsUtils.Predicate<Document> responseFilter) {
        super(url, autoLoadNextPage);
        this.responseFiler = responseFilter;
    }
    
    ContainerList(final List<UrlOffsetPair> list, final int count, final boolean autoLoadNextPage, CollectionsUtils.Predicate<Document> responseFilter) {
        super(list, count, autoLoadNextPage);
        this.responseFiler = responseFilter;
    }

    @Override
    protected Document[] getItemsFromResponse(Messages.Response.ResponseWrapper wrapper) {
        Messages.Response.Payload payload = payload(wrapper);
        Messages.DocV2 doc = DfeUtils.getRootDoc(payload);

        if (doc == null) {
            return new Document[0];
        }

        Document[] docs = this.getItems(doc);
        if (responseFiler == null) {
            return docs;
        }
        List<Document> list = CollectionsUtils.INSTANCE.filter(Arrays.asList(docs), responseFiler);
        return list.toArray(new Document[0]);
    }

    @Override
    protected String getNextPageUrl(Messages.Response.ResponseWrapper wrapper) {
        Messages.Response.Payload payload = payload(wrapper);
        Messages.DocV2 doc = DfeUtils.getRootDoc(payload);

        if (doc == null) {
            return null;
        }

        final Messages.Containers.ContainerMetadata containerMetadata = doc.containerMetadata;
        String nextPageUrl = null;
        if (containerMetadata != null) {
            nextPageUrl = doc.containerMetadata.nextPageUrl;
        }
        return nextPageUrl;
    }

    private Messages.Response.Payload payload(Messages.Response.ResponseWrapper wrapper) {
        Messages.Response.Payload payload = wrapper.payload;
        if (wrapper.preFetch.length > 0
                && ((payload.searchResponse != null && payload.searchResponse.doc.length == 0)
                || (payload.listResponse != null && payload.listResponse.doc.length == 0)
        )
                ) {
            return wrapper.preFetch[0].response.payload;
        }
        return payload;
    }

    private Document[] getItems(final DocV2 docV2) {
        Document[] array;
        if (docV2 != null) {
            final int length = docV2.child.length;
            array = new Document[length];
            for (int i = 0; i < length; ++i) {
                array[i] = new Document(docV2.child[i]);
            }
        }
        else {
            array = new Document[0];
        }
        return array;
    }
}
