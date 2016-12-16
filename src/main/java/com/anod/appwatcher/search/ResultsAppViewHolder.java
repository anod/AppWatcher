package com.anod.appwatcher.search;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.WatchAppList;
import com.google.android.finsky.api.model.Document;

import butterknife.BindView;
import butterknife.ButterKnife;

class ResultsAppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final WatchAppList mNewAppHandler;
    Document doc;
    @BindView(android.R.id.content)
    View row;
    @BindView(android.R.id.title)
    TextView title;
    @BindView(R.id.details)
    TextView details;
    @BindView(R.id.updated)
    TextView updated;
    @BindView(R.id.price)
    TextView price;
    @BindView(android.R.id.icon)
    ImageView icon;

    ResultsAppViewHolder(View itemView, WatchAppList newAppHandler) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.row.setOnClickListener(this);
        mNewAppHandler = newAppHandler;
    }

    @Override
    public void onClick(View v) {
        final AppInfo info = new AppInfo(doc);
        if (mNewAppHandler.isAdded(info.packageName))
        {
            mNewAppHandler.add(info);
        } else {
            mNewAppHandler.delete(info);
        }
    }
}
