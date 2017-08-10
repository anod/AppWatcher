package com.google.android.finsky.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.volley.Request;
import com.anod.appwatcher.utils.CollectionsUtils;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.api.DfeUtils;
import com.google.android.finsky.protos.nano.Messages;
import com.google.android.finsky.protos.nano.Messages.Containers;
import com.google.android.finsky.protos.nano.Messages.DocV2;
import com.google.android.finsky.protos.nano.Messages.Search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DfeSearch extends ContainerList<Search.SearchResponse> implements Parcelable
{
    public static Parcelable.Creator<DfeSearch> CREATOR;
    private DfeApi mDfeApi;
    public final String query;

    static {
        DfeSearch.CREATOR = new Parcelable.Creator<DfeSearch>() {
            public DfeSearch createFromParcel(final Parcel parcel) {
                final int int1 = parcel.readInt();
                final ArrayList<UrlOffsetPair> list = new ArrayList<>();
                for (int i = 0; i < int1; ++i) {
                    list.add(new UrlOffsetPair(parcel.readInt(), parcel.readString()));
                }
                final int count = parcel.readInt();
                final String query = parcel.readString();
                return new DfeSearch(list, count, query, null);
            }
            
            public DfeSearch[] newArray(final int n) {
                return new DfeSearch[n];
            }
        };
    }

    public DfeSearch(final DfeApi dfeApi, final String query, final String initialUrl, boolean autoLoadNextPage, CollectionsUtils.Predicate<Document> responseFilter) {
        super(initialUrl,autoLoadNextPage, responseFilter);
        mDfeApi = dfeApi;
        this.query = query;
    }
    
    private DfeSearch(final List<UrlOffsetPair> list, final int count, final String query, CollectionsUtils.Predicate<Document> responseFilter) {
        super(list, count, true, responseFilter);
        this.query = query;
    }

    public int describeContents() {
        return 0;
    }

    protected Request<?> makeRequest(final String url) {
        return this.mDfeApi.search(url, this, this);
    }
    
    public void writeToParcel(final Parcel parcel, final int n) {
        parcel.writeInt(this.urlOffsetList.size());
        for (final UrlOffsetPair urlOffsetPair : this.urlOffsetList) {
            parcel.writeInt(urlOffsetPair.offset);
            parcel.writeString(urlOffsetPair.url);
        }
        parcel.writeInt(this.getCount());
        parcel.writeString(this.query);
    }
}
