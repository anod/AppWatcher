package com.google.android.finsky.api.model;

import java.util.*;
import com.android.volley.*;
import android.text.*;

public abstract class PaginatedList<T, D> extends DfeModel implements Response.Listener<T>
{
    protected final boolean mAutoLoadNextPage;
    private int mCurrentOffset;
    private Request<?> mCurrentRequest;
    private final List<D> mItems;
    private boolean mItemsRemoved;
    private int mItemsUntilEndCount;
    private int mLastPositionRequested;
    protected T mLastResponse;
    private boolean mMoreAvailable;
    protected List<UrlOffsetPair> mUrlOffsetList;
    private int mWindowDistance;
    
    protected PaginatedList(final String s) {
        this(s, true);
    }
    
    protected PaginatedList(final String s, final boolean mAutoLoadNextPage) {
        super();
        this.mWindowDistance = 12;
        this.mItems = new ArrayList<D>();
        this.mItemsUntilEndCount = 4;
        (this.mUrlOffsetList = new ArrayList<UrlOffsetPair>()).add(new UrlOffsetPair(0, s));
        this.mMoreAvailable = true;
        this.mAutoLoadNextPage = mAutoLoadNextPage;
    }
    
    protected PaginatedList(final List<UrlOffsetPair> mUrlOffsetList, final int n, final boolean b) {
        this(null, b);
        this.mUrlOffsetList = mUrlOffsetList;
        for (int i = 0; i < n; ++i) {
            this.mItems.add(null);
        }
    }
    
    private void requestMoreItemsIfNoRequestExists(final UrlOffsetPair urlOffsetPair) {
        if (!this.inErrorState()) {
            if (this.mCurrentRequest != null && !this.mCurrentRequest.isCanceled()) {
                if (this.mCurrentRequest.getUrl().endsWith(urlOffsetPair.url)) {
                    return;
                }
                this.mCurrentRequest.cancel();
            }
            this.mCurrentOffset = urlOffsetPair.offset;
            this.mCurrentRequest = this.makeRequest(urlOffsetPair.url);
        }
    }
    
    private void updateItemsUntilEndCount(final int n) {
        if (this.mItemsUntilEndCount <= 0) {
            this.mItemsUntilEndCount = 4;
            return;
        }
        this.mItemsUntilEndCount = Math.max(1, n / 4);
    }
    
    public void clearDataAndReplaceInitialUrl(final String s) {
        this.mUrlOffsetList.clear();
        this.mUrlOffsetList.add(new UrlOffsetPair(0, s));
        this.resetItems();
    }
    
    protected abstract void clearDiskCache();
    
    public void clearTransientState() {
        this.mCurrentRequest = null;
    }
    
    public void flushUnusedPages() {
        if (this.mLastPositionRequested >= 0) {
            for (int i = 0; i < this.mItems.size(); ++i) {
                if (i < -1 + (this.mLastPositionRequested - this.mWindowDistance) || i >= this.mLastPositionRequested + this.mWindowDistance) {
                    this.mItems.set(i, null);
                }
            }
        }
    }
    
    public int getCount() {
        return this.mItems.size();
    }
    
    public final D getItem(final int n) {
        return this.getItem(n, true);
    }
    
    public final D getItem(final int pos, final boolean b) {
        if (b) {
            this.mLastPositionRequested = pos;
        }
        if (pos < 0) {
            throw new IllegalArgumentException("Can't return an item with a negative index: " + pos);
        }
        final int count = this.getCount();
        D value = null;
        if (pos < count) {
            value = this.mItems.get(pos);
            if (this.mAutoLoadNextPage && this.mMoreAvailable && pos >= this.getCount() - this.mItemsUntilEndCount) {
                if (this.mItemsRemoved) {
                    for (int i = 0; i < this.mUrlOffsetList.size(); ++i) {
                        if (this.mUrlOffsetList.get(i).offset > this.mItems.size()) {
                            while (this.mUrlOffsetList.size() > Math.max(1, i)) {
                                this.mUrlOffsetList.remove(-1 + this.mUrlOffsetList.size());
                            }
                            final UrlOffsetPair urlOffsetPair = this.mUrlOffsetList.get(-1 + this.mUrlOffsetList.size());
                            if (b) {
                                this.requestMoreItemsIfNoRequestExists(urlOffsetPair);
                            }
                        }
                    }
                }
                else {
                    final UrlOffsetPair urlOffsetPair2 = this.mUrlOffsetList.get(-1 + this.mUrlOffsetList.size());
                    if (b) {
                        this.requestMoreItemsIfNoRequestExists(urlOffsetPair2);
                    }
                }
            }
            if (value == null) {
                UrlOffsetPair urlOffsetPair3 = null;
                for (final UrlOffsetPair urlOffsetPair4 : this.mUrlOffsetList) {
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
    
    protected abstract D[] getItemsFromResponse(final T p0);
    
    public List<String> getListPageUrls() {
        final ArrayList<String> list = new ArrayList<String>(this.mUrlOffsetList.size());
        final Iterator<UrlOffsetPair> iterator = this.mUrlOffsetList.iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next().url);
        }
        return list;
    }
    
    protected abstract String getNextPageUrl(final T p0);
    
    public boolean isMoreAvailable() {
        return this.mMoreAvailable;
    }
    
    @Override
    public boolean isReady() {
        return this.mLastResponse != null || this.mItems.size() > 0;
    }
    
    protected abstract Request<?> makeRequest(final String p0);
    
    @Override
    public void onErrorResponse(final VolleyError volleyError) {
        this.clearTransientState();
        super.onErrorResponse(volleyError);
    }
    
    @Override
    public void onResponse(final T mLastResponse) {
        this.clearErrors();
        this.mLastResponse = mLastResponse;
        final int size = this.mItems.size();
        final Object[] itemsFromResponse = this.getItemsFromResponse(mLastResponse);
        this.updateItemsUntilEndCount(itemsFromResponse.length);
        for (int i = 0; i < itemsFromResponse.length; ++i) {
            if (i + this.mCurrentOffset < this.mItems.size()) {
                this.mItems.set(i + this.mCurrentOffset, (D)itemsFromResponse[i]);
            }
            else {
                this.mItems.add((D)itemsFromResponse[i]);
            }
        }
        final String nextPageUrl = this.getNextPageUrl(mLastResponse);
        if (!TextUtils.isEmpty((CharSequence)nextPageUrl) && (this.mCurrentOffset == size || this.mItemsRemoved)) {
            this.mUrlOffsetList.add(new UrlOffsetPair(this.mItems.size(), nextPageUrl));
        }
        if (this.mItemsRemoved) {
            this.mItemsRemoved = false;
        }
        final int size2 = this.mItems.size();
        final int offset = this.mUrlOffsetList.get(-1 + this.mUrlOffsetList.size()).offset;
        boolean b = false;
        if (size2 == offset) {
            final int length = itemsFromResponse.length;
            b = false;
            if (length > 0) {
                b = true;
            }
        }
        this.mMoreAvailable = (b && this.mAutoLoadNextPage);
        this.clearTransientState();
        this.notifyDataSetChanged();
    }
    
    public void removeItem(final int n) {
        this.mItems.remove(n);
        this.mItemsRemoved = true;
        if (this.mCurrentRequest != null && !this.mCurrentRequest.isCanceled()) {
            this.mCurrentRequest.cancel();
        }
        this.clearDiskCache();
    }
    
    public void resetItems() {
        this.mMoreAvailable = true;
        this.mItems.clear();
        this.notifyDataSetChanged();
    }
    
    public void retryLoadItems() {
        if (this.inErrorState()) {
            this.clearTransientState();
            this.clearErrors();
            final int mCurrentOffset = this.mCurrentOffset;
            UrlOffsetPair urlOffsetPair = null;
            Label_0078: {
                if (mCurrentOffset != -1) {
                    final Iterator<UrlOffsetPair> iterator = this.mUrlOffsetList.iterator();
                    UrlOffsetPair urlOffsetPair2;
                    do {
                        final boolean hasNext = iterator.hasNext();
                        urlOffsetPair = null;
                        if (!hasNext) {
                            break Label_0078;
                        }
                        urlOffsetPair2 = iterator.next();
                    } while (this.mCurrentOffset != urlOffsetPair2.offset);
                    urlOffsetPair = urlOffsetPair2;
                }
            }
            if (urlOffsetPair == null) {
                urlOffsetPair = this.mUrlOffsetList.get(-1 + this.mUrlOffsetList.size());
            }
            this.requestMoreItemsIfNoRequestExists(urlOffsetPair);
        }
    }
    
    public void setWindowDistance(final int mWindowDistance) {
        this.mWindowDistance = mWindowDistance;
    }
    
    public void startLoadItems() {
        if (this.mMoreAvailable && this.getCount() == 0) {
            this.clearErrors();
            this.requestMoreItemsIfNoRequestExists(this.mUrlOffsetList.get(0));
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
