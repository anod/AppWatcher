package com.anod.appwatcher.search;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.WatchAppList;
import com.anod.appwatcher.utils.InstalledAppsProvider;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.nano.Messages.Common;
import com.google.android.finsky.protos.nano.Messages.AppDetails;
import com.squareup.picasso.Picasso;

public abstract class ResultsAdapter extends RecyclerView.Adapter<ResultsAppViewHolder> {
    private final Context mContext;
    private final int mColorBgDisabled;
    private final int mColorBgNormal;
    private final WatchAppList mNewAppHandler;
    private final InstalledAppsProvider mInstalledAppsProvider;

    protected ResultsAdapter(Context context, WatchAppList newAppHandler) {
        super();
        mContext = context;
        mNewAppHandler = newAppHandler;

        mColorBgDisabled = ContextCompat.getColor(context, R.color.row_inactive);
        mColorBgNormal = ContextCompat.getColor(context, R.color.item_background);

        mInstalledAppsProvider = new InstalledAppsProvider.MemoryCache(new InstalledAppsProvider.PackageManager(context.getPackageManager()));
    }

    @Override
    public ResultsAppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_market_app, parent, false);
        return new ResultsAppViewHolder(view, mNewAppHandler);
    }

    public abstract Document getDocument(int position);

    @Override
    public void onBindViewHolder(ResultsAppViewHolder holder, int position) {
        Document doc = getDocument(position);

        AppDetails app = doc.getAppDetails();
        String uploadDate = app == null ? "" : app.uploadDate;
        String packageName =  app == null ? "" : app.packageName;

        holder.doc = doc;
        holder.title.setText(doc.getTitle());
        holder.details.setText(doc.getCreator());
        holder.updated.setText(uploadDate);


        if (mNewAppHandler.contains(packageName)) {
            holder.row.setBackgroundColor(mColorBgDisabled);
        } else {
            holder.row.setBackgroundColor(mColorBgNormal);
        }

        String imageUrl = doc.getIconUrl();

        Picasso.with(mContext).load(imageUrl)
                .placeholder(R.drawable.ic_blur_on_black_48dp)
                .into(holder.icon);

        boolean isInstalled = mInstalledAppsProvider.getInfo(packageName).isInstalled();
        if (isInstalled) {
            holder.price.setText(R.string.installed);
        } else {
            Common.Offer offer = doc.getOffer(Common.Offer.TYPE_1);
            if (offer == null)
            {
                holder.price.setText("");
            } else if (offer.micros == 0) {
                holder.price.setText(R.string.free);
            } else {
                holder.price.setText(offer.formattedAmount);
            }
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }
}