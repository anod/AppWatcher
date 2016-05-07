package com.anod.appwatcher.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AddWatchAppHandler;
import com.anod.appwatcher.utils.DocUtils;
import com.google.android.finsky.api.model.Document;

import butterknife.Bind;
import butterknife.ButterKnife;

class MarketAppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final AddWatchAppHandler mNewAppHandler;
    Document doc;
    @Bind(android.R.id.content)
    View row;
    @Bind(android.R.id.title)
    TextView title;
    @Bind(R.id.details)
    TextView details;
    @Bind(R.id.updated)
    TextView updated;
    @Bind(R.id.price)
    TextView price;
    @Bind(android.R.id.icon)
    ImageView icon;

    public MarketAppViewHolder(View itemView, AddWatchAppHandler newAppHandler) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.row.setOnClickListener(this);
        mNewAppHandler = newAppHandler;
    }

    @Override
    public void onClick(View v) {
        final AppInfo info = new AppInfo(doc);
        mNewAppHandler.add(info);
    }
}
