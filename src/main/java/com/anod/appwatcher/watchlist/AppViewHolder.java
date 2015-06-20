package com.anod.appwatcher.watchlist;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;

public class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public AppInfo app;
    public int position;
    public View section;
    public TextView sectionText;
    public TextView sectionCount;
    public TextView title;
    public TextView details;
    public TextView version;
    public TextView price;
    public ImageView icon;
    public View newIndicator;
    public TextView updateDate;

    public AppViewHolder(View itemView) {
        super(itemView);

        this.app = null;
        this.position = 0;
        this.section = itemView.findViewById(R.id.sec_header);
        this.sectionText = (TextView) itemView.findViewById(R.id.sec_header_title);
        this.sectionCount = (TextView) itemView.findViewById(R.id.sec_header_count);
        this.title = (TextView) itemView.findViewById(android.R.id.title);
        this.details = (TextView) itemView.findViewById(R.id.details);
        this.icon = (ImageView) itemView.findViewById(android.R.id.icon);
        this.version = (TextView) itemView.findViewById(R.id.updated);
        this.price = (TextView) itemView.findViewById(R.id.price);
        this.newIndicator = itemView.findViewById(R.id.new_indicator);
        this.updateDate = (TextView) itemView.findViewById(R.id.update_date);

        int mDefColor = this.version.getTextColors().getDefaultColor();
        itemView.setOnClickListener(this);

                /*
                                                        Viewthis this = (Viewthis)v.getTag();
                                        onChangelogClick(this.app.getAppId(), this.app.getDetailsUrl());
                 */

        this.icon.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

    }

        /*
        new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                final String tag = (String)v.getTag();
                                String[] parts = tag.split(",", -2);
                                String appId = parts[0];
                                String detailsUrl = parts[1];
                                onChangelogClick(appId, detailsUrl);
                        }

         */
}