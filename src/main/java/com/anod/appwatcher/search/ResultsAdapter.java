package com.anod.appwatcher.search;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AddWatchAppHandler;
import com.anod.appwatcher.utils.DocUtils;
import com.anod.appwatcher.utils.PackageManagerUtils;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.Common;
import com.google.android.finsky.protos.DocDetails;
import com.squareup.picasso.Picasso;

public abstract class ResultsAdapter extends RecyclerView.Adapter<ResultsAppViewHolder> {
    private final PackageManagerUtils mPMUtils;
    private final Context mContext;
    private final int mColorBgGray;
    private final int mColorBgWhite;
    private final AddWatchAppHandler mNewAppHandler;

    ResultsAdapter(Context context, AddWatchAppHandler newAppHandler) {
        super();
        mContext = context;
        mPMUtils = new PackageManagerUtils(context.getPackageManager());
        mNewAppHandler = newAppHandler;

        mColorBgGray = ContextCompat.getColor(context, R.color.row_inactive);
        mColorBgWhite = ContextCompat.getColor(context, R.color.white);
    }

    @Override
    public ResultsAppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_market_app, parent, false);
        return new ResultsAppViewHolder(view, mNewAppHandler);
    }

    abstract Document getDocument(int position);

    @Override
    public void onBindViewHolder(ResultsAppViewHolder holder, int position) {
        Document doc = getDocument(position);

        DocDetails.AppDetails app = doc.getAppDetails();
        String uploadDate = app == null ? "" : app.uploadDate;

        holder.doc = doc;
        holder.title.setText(doc.getTitle());
        holder.details.setText(doc.getCreator());
        holder.updated.setText(uploadDate);

        if (mNewAppHandler.isAdded(app.packageName)) {
            holder.row.setBackgroundColor(mColorBgGray);
        } else {
            holder.row.setBackgroundColor(mColorBgWhite);
        }

        String imageUrl = DocUtils.getIconUrl(doc);

        Picasso.with(mContext).load(imageUrl)
                .placeholder(R.drawable.ic_blur_on_black_48dp)
                .into(holder.icon);

        boolean isInstalled = mPMUtils.isAppInstalled(app.packageName);
        if (isInstalled) {
            holder.price.setText(R.string.installed);
        } else {
            Common.Offer offer = DocUtils.getOffer(doc);
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