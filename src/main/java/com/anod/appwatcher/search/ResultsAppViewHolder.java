package com.anod.appwatcher.search;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AddWatchAppHandler;
import com.google.android.finsky.api.model.Document;

import butterknife.BindView;
import butterknife.ButterKnife;

class ResultsAppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final AddWatchAppHandler mNewAppHandler;
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

    public ResultsAppViewHolder(View itemView, AddWatchAppHandler newAppHandler) {
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
