package com.anod.appwatcher.wishlist;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.anod.appwatcher.R;
import com.anod.appwatcher.market.PlayStoreEndpoint;
import com.anod.appwatcher.market.WishlistEndpoint;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppInfoMetadata;
import com.anod.appwatcher.model.WatchAppList;
import com.anod.appwatcher.tags.TagSnackbar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author algavris
 * @date 16/12/2016.
 */

public class WishlistFragment extends Fragment implements WatchAppList.Listener, PlayStoreEndpoint.Listener {
    public static final String TAG = "wishlist";
    public static final String EXTRA_ACCOUNT = "extra_account";
    public static final String EXTRA_AUTH_TOKEN = "extra_auth_token";

    @BindView(R.id.loading)
    LinearLayout mLoading;
    @BindView(android.R.id.list)
    RecyclerView mListView;
    @BindView(android.R.id.empty)
    TextView mEmptyView;
    @BindView(R.id.retry_box)
    LinearLayout mRetryView;
    @BindView(R.id.retry)
    Button mRetryButton;

    private WishlistEndpoint mEndpoint;
    private WatchAppList mWatchAppList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (mEndpoint == null) {
            mEndpoint = new WishlistEndpoint(context, true);
        }

        if (mWatchAppList == null)
        {
            mWatchAppList = new WatchAppList(this);
        }

        mWatchAppList.attach(context);
        mEndpoint.setListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mEndpoint.setListener(null);
        mWatchAppList.detach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);
        ButterKnife.bind(this, view);

        Context context = getContext();

        mListView.setLayoutManager(new LinearLayoutManager(context));
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEndpoint.startAsync();
            }
        });

        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
        mRetryView.setVisibility(View.GONE);

        getActivity().setTitle(R.string.wishlist);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Account account = getArguments().getParcelable(EXTRA_ACCOUNT);
        String authToken = getArguments().getString(EXTRA_AUTH_TOKEN);

        if (account == null || TextUtils.isEmpty(authToken)) {
            Toast.makeText(getContext(), R.string.choose_an_account, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        } else {
            startLoadingList(account, authToken);
        }
    }

    @Override
    public void onWatchListChangeSuccess(AppInfo info, int newStatus) {
        if (newStatus == AppInfoMetadata.STATUS_NORMAL) {
            TagSnackbar.make(getActivity(), info, false).show();
        }
        mListView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onWatchListChangeError(AppInfo info, int error) {
        if (WatchAppList.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(getContext(), R.string.app_already_added, Toast.LENGTH_SHORT).show();
            mListView.getAdapter().notifyDataSetChanged();
        } else if (error == WatchAppList.ERROR_INSERT) {
            Toast.makeText(getContext(), R.string.error_insert_app, Toast.LENGTH_SHORT).show();
        }
    }


    private void startLoadingList(@NonNull Account account,@NonNull String authSubToken) {
        mEndpoint.setAccount(account, authSubToken);

        Context context = getContext();

        ResultsAdapterWishlist adapter = new ResultsAdapterWishlist(context, mEndpoint , mWatchAppList);
        mListView.setAdapter(adapter);

        mEndpoint.startAsync();
    }


    private void showRetryButton() {
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mRetryView.setVisibility(View.VISIBLE);
    }

    private void showListView() {
        mListView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mRetryView.setVisibility(View.GONE);
    }

    private void showNoResults() {
        mLoading.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mRetryView.setVisibility(View.GONE);
        mEmptyView.setText(R.string.no_result_found);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDataChanged() {
        if (mEndpoint.getData().getCount() == 0) {
            showNoResults();
        } else {
            showListView();
            mListView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mLoading.setVisibility(View.GONE);
        showRetryButton();
    }
}
