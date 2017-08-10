package com.google.android.finsky.api.model;

import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.finsky.protos.nano.Messages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class PaginatedList<T, D> extends DfeModel implements Response.Listener<Messages.Response.ResponseWrapper>
{
    private final boolean autoLoadNextPage;
    private int currentOffset;
    private Request<?> currentRequest;
    private final List<D> items;
    private boolean itemsRemoved;
    private int itemsUntilEndCount;
    private int lastPositionRequested;
    Messages.Response.ResponseWrapper lastResponse;
    private boolean moreAvailable;
    List<UrlOffsetPair> urlOffsetList;
    private int windowDistance;
    
    PaginatedList(final String url) {
        this(url, true);
    }
    
    PaginatedList(final String url, final boolean autoLoadNextPage) {
        super();
        windowDistance = 12;
        items = new ArrayList<D>();
        itemsUntilEndCount = 4;
        (urlOffsetList = new ArrayList<>()).add(new UrlOffsetPair(0, url));
        moreAvailable = true;
        this.autoLoadNextPage = autoLoadNextPage;
    }
    
    PaginatedList(final List<UrlOffsetPair> urlOffsetList, final int count, final boolean autoLoadNextPage) {
        this(null, autoLoadNextPage);
        this.urlOffsetList = urlOffsetList;
        for (int i = 0; i < count; ++i) {
            this.items.add(null);
        }
    }
    
    private void requestMoreItemsIfNoRequestExists(final UrlOffsetPair urlOffsetPair) {
        if (!this.inErrorState()) {
            if (this.currentRequest != null && !this.currentRequest.isCanceled()) {
                if (this.currentRequest.getUrl().endsWith(urlOffsetPair.url)) {
                    return;
                }
                this.currentRequest.cancel();
            }
            this.currentOffset = urlOffsetPair.offset;
            this.currentRequest = this.makeRequest(urlOffsetPair.url);
        }
    }
    
    private void updateItemsUntilEndCount(final int n) {
        if (this.itemsUntilEndCount <= 0) {
            this.itemsUntilEndCount = 4;
            return;
        }
        this.itemsUntilEndCount = Math.max(1, n / 4);
    }
    
    public void clearDataAndReplaceInitialUrl(final String s) {
        this.urlOffsetList.clear();
        this.urlOffsetList.add(new UrlOffsetPair(0, s));
        this.resetItems();
    }
    
    protected void clearDiskCache() { }
    
    public void clearTransientState() {
        this.currentRequest = null;
    }
    
    public void flushUnusedPages() {
        if (this.lastPositionRequested >= 0) {
            for (int i = 0; i < this.items.size(); ++i) {
                if (i < -1 + (this.lastPositionRequested - this.windowDistance) || i >= this.lastPositionRequested + this.windowDistance) {
                    this.items.set(i, null);
                }
            }
        }
    }
    
    public int getCount() {
        return this.items.size();
    }
    
    public final D getItem(final int n) {
        return this.getItem(n, true);
    }
    
    public final D getItem(final int pos, final boolean isLastPosition) {
        if (isLastPosition) {
            this.lastPositionRequested = pos;
        }
        if (pos < 0) {
            throw new IllegalArgumentException("Can't return an item with a negative index: " + pos);
        }
        final int count = this.getCount();
        D value = null;
        if (pos < count) {
            value = this.items.get(pos);
            if (this.autoLoadNextPage && this.moreAvailable && pos >= this.getCount() - this.itemsUntilEndCount) {
                if (this.itemsRemoved) {
                    for (int i = 0; i < this.urlOffsetList.size(); ++i) {
                        if (this.urlOffsetList.get(i).offset > this.items.size()) {
                            while (this.urlOffsetList.size() > Math.max(1, i)) {
                                this.urlOffsetList.remove(-1 + this.urlOffsetList.size());
                            }
                            final UrlOffsetPair urlOffsetPair = this.urlOffsetList.get(-1 + this.urlOffsetList.size());
                            if (isLastPosition) {
                                this.requestMoreItemsIfNoRequestExists(urlOffsetPair);
                            }
                        }
                    }
                }
                else {
                    final UrlOffsetPair urlOffsetPair2 = this.urlOffsetList.get(-1 + this.urlOffsetList.size());
                    if (isLastPosition) {
                        this.requestMoreItemsIfNoRequestExists(urlOffsetPair2);
                    }
                }
            }
            if (value == null) {
                UrlOffsetPair urlOffsetPair3 = null;
                for (final UrlOffsetPair urlOffsetPair4 : this.urlOffsetList) {
                    if (urlOffsetPair4.offset > pos) {
                        break;
                    }
                    urlOffsetPair3 = urlOffsetPair4;
                }
                this.requestMoreItemsIfNoRequestExists(urlOffsetPair3);
            }
        }
        return value;
    }
    
    protected abstract D[] getItemsFromResponse(final Messages.Response.ResponseWrapper listResponse);
    
    public List<String> getListPageUrls() {
        final ArrayList<String> list = new ArrayList<String>(this.urlOffsetList.size());
        final Iterator<UrlOffsetPair> iterator = this.urlOffsetList.iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next().url);
        }
        return list;
    }
    
    protected abstract String getNextPageUrl(final Messages.Response.ResponseWrapper listResponse);
    
    public boolean isMoreAvailable() {
        return this.moreAvailable;
    }
    
    @Override
    public boolean isReady() {
        return this.lastResponse != null || this.items.size() > 0;
    }
    
    protected abstract Request<?> makeRequest(final String url);
    
    @Override
    public void onErrorResponse(final VolleyError volleyError) {
        this.clearTransientState();
        super.onErrorResponse(volleyError);
    }

    @Override
    public void onResponse(final Messages.Response.ResponseWrapper lastResponse) {
        this.clearErrors();
        this.lastResponse = lastResponse;
        final int size = this.items.size();
        final D[] itemsFromResponse = this.getItemsFromResponse(lastResponse);
        this.updateItemsUntilEndCount(itemsFromResponse.length);
        for (int i = 0; i < itemsFromResponse.length; ++i) {
            if (i + this.currentOffset < this.items.size()) {
                this.items.set(i + this.currentOffset, (D)itemsFromResponse[i]);
            }
            else {
                this.items.add(itemsFromResponse[i]);
            }
        }
        final String nextPageUrl = this.getNextPageUrl(lastResponse);
        if (!TextUtils.isEmpty(nextPageUrl) && (this.currentOffset == size || this.itemsRemoved)) {
            this.urlOffsetList.add(new UrlOffsetPair(this.items.size(), nextPageUrl));
        }
        if (this.itemsRemoved) {
            this.itemsRemoved = false;
        }
        final int offset = this.urlOffsetList.get(-1 + this.urlOffsetList.size()).offset;
        boolean moreAvailable = false;
        if (items.size() == offset) {
            moreAvailable = (itemsFromResponse.length > 0);
        }
        this.moreAvailable = (moreAvailable && autoLoadNextPage);
        this.clearTransientState();
        this.notifyDataSetChanged();
    }
    
    public void removeItem(final int n) {
        this.items.remove(n);
        this.itemsRemoved = true;
        if (this.currentRequest != null && !this.currentRequest.isCanceled()) {
            this.currentRequest.cancel();
        }
        this.clearDiskCache();
    }
    
    public void resetItems() {
        this.moreAvailable = true;
        this.items.clear();
        this.notifyDataSetChanged();
    }
    
    public void retryLoadItems() {
        if (this.inErrorState()) {
            this.clearTransientState();
            this.clearErrors();
            final int mCurrentOffset = this.currentOffset;
            UrlOffsetPair urlOffsetPair = null;
            Label_0078: {
                if (mCurrentOffset != -1) {
                    final Iterator<UrlOffsetPair> iterator = this.urlOffsetList.iterator();
                    UrlOffsetPair urlOffsetPair2;
                    do {
                        final boolean hasNext = iterator.hasNext();
                        urlOffsetPair = null;
                        if (!hasNext) {
                            break Label_0078;
                        }
                        urlOffsetPair2 = iterator.next();
                    } while (this.currentOffset != urlOffsetPair2.offset);
                    urlOffsetPair = urlOffsetPair2;
                }
            }
            if (urlOffsetPair == null) {
                urlOffsetPair = this.urlOffsetList.get(-1 + this.urlOffsetList.size());
            }
            this.requestMoreItemsIfNoRequestExists(urlOffsetPair);
        }
    }

    public void startLoadItems() {
        if (this.moreAvailable && this.getCount() == 0) {
            this.clearErrors();
            this.requestMoreItemsIfNoRequestExists(this.urlOffsetList.get(0));
        }
    }
    
    protected static class UrlOffsetPair
    {
        public final int offset;
        public final String url;
        
        public UrlOffsetPair(final int offset, final String url) {
            super();
            this.offset = offset;
            this.url = url;
        }
    }
}
