package com.anod.appwatcher.adapters;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppIconLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppViewHolder extends AppViewHolderBase implements View.OnClickListener {
    public AppInfo app;
    public int position;

    @BindView(R.id.sec_header) public View section;
    @BindView(R.id.sec_header_title) public TextView sectionText;
    @BindView(R.id.sec_header_count) public TextView sectionCount;
    @BindView(android.R.id.icon) ImageView icon;
    @BindView(R.id.new_indicator) View newIndicator;
    @BindView(R.id.sec_action_button) public Button actionButton;

    private final OnClickListener mListener;
    final AppDetailsView detailsView;

    protected boolean mIsLocalApp = false;

    public interface OnClickListener {
        void onItemClick(AppInfo app);
        void onActionButton();
    }

    public AppViewHolder(View itemView, DataProvider dataProvider, AppIconLoader iconLoader, OnClickListener listener) {
        super(itemView, dataProvider, iconLoader);

        mListener = listener;

        this.app = null;
        this.position = 0;
        ButterKnife.bind(this, itemView);

        this.detailsView = new AppDetailsView(itemView, dataProvider);

        itemView.findViewById(android.R.id.content).setOnClickListener(this);
    }

    @OnClick(R.id.sec_action_button)
    public void onAction()
    {
        mListener.onActionButton();
    }

    @Override
    public void onClick(View v) {
        mListener.onItemClick(this.app);
    }

    public void bindView(int position, AppInfo app) {
        this.position = position;
        this.app = app;

        this.detailsView.fillDetails(app, mIsLocalApp);

        if (app.getStatus() == AppInfo.STATUS_UPDATED) {
            newIndicator.setVisibility(View.VISIBLE);
        } else {
            newIndicator.setVisibility(View.INVISIBLE);
        }

        bindIcon(app, this.icon);
        bindSectionView();
    }

    protected void bindSectionView() {
        if (position == mDataProvider.getNewAppsCount()) {
            sectionText.setText(R.string.watching);
            sectionCount.setText(String.valueOf(mDataProvider.getTotalAppsCount() - mDataProvider.getNewAppsCount()));
            section.setVisibility(View.VISIBLE);
            actionButton.setVisibility(View.GONE);
        } else if (position == 0 && mDataProvider.getNewAppsCount() > 0) {
            sectionText.setText(R.string.recently_updated);
            section.setVisibility(View.VISIBLE);
            if (mDataProvider.getUpdatableAppsCount() > 0) {
                actionButton.setVisibility(View.VISIBLE);
                sectionCount.setVisibility(View.GONE);
            } else {
                actionButton.setVisibility(View.GONE);
                sectionCount.setText(String.valueOf(mDataProvider.getNewAppsCount()));
                sectionCount.setVisibility(View.VISIBLE);
            }
        } else {
            section.setVisibility(View.GONE);
        }
    }
}