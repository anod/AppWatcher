package com.anod.appwatcher;

import android.accounts.Account;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Target;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.anod.appwatcher.accounts.AuthTokenProvider;
import com.anod.appwatcher.fragments.RemoveDialogFragment;
import com.anod.appwatcher.market.DetailsEndpoint;
import com.anod.appwatcher.market.MarketInfo;
import com.anod.appwatcher.market.PlayStoreEndpoint;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.NewWatchAppHandler;
import com.anod.appwatcher.ui.ToolbarActivity;
import com.anod.appwatcher.utils.DocUtils;
import com.anod.appwatcher.utils.IntentUtils;
import com.anod.appwatcher.utils.PackageManagerUtils;
import com.google.android.finsky.api.model.Document;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.anodsplace.android.anim.RevealAnimatorCompat;
import info.anodsplace.android.log.AppLog;



public class ChangelogActivity extends ToolbarActivity implements PlayStoreEndpoint.Listener, Palette.PaletteAsyncListener, View.OnClickListener, NewWatchAppHandler.Listener {

    public static final String EXTRA_APP_ID = "app_id";
    public static final String EXTRA_DETAILS_URL = "url";
    public static final String EXTRA_ROW_ID = "row_id";
    public static final String EXTRA_ADD_APP_PACKAGE = "app_add_success";

    @Bind(R.id.progress_bar)
    ProgressBar mLoadingView;
    @Bind(R.id.changelog)
    TextView mChangelog;
    @Bind(R.id.retry)
    Button mRetryButton;
    @Bind(android.R.id.icon)
    ImageView mAppIcon;
    @Bind(android.R.id.title)
    TextView mAppTitle;
    @Bind(R.id.background)
    View mBackground;
    @Bind(R.id.market_btn)
    FloatingActionButton mPlayStoreButton;

    private static Target[] sTargets = new Target[] {
            Target.DARK_VIBRANT,
            Target.DARK_MUTED,
            Target.MUTED,
            Target.VIBRANT,
    };

    private String mDetailsUrl;
    private String mAppId;

    private DetailsEndpoint mDetailsEndpoint;
    private AppInfo mApp;
    private boolean mNewApp;
    private MenuItem mAddMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_changelog);
        ButterKnife.bind(this);
        setupToolbar();

        Intent data = getIntent();

        mAppId = data.getStringExtra(EXTRA_APP_ID);
        mDetailsUrl = data.getStringExtra(EXTRA_DETAILS_URL);
        int rowId = data.getIntExtra(EXTRA_ROW_ID, -1);

        mDetailsEndpoint = new DetailsEndpoint(this);
        mDetailsEndpoint.setUrl(mDetailsUrl);

        mLoadingView.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mChangelog.setVisibility(View.GONE);
        mBackground.setVisibility(View.INVISIBLE);

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoadingView.setVisibility(View.VISIBLE);
                mRetryButton.setVisibility(View.GONE);
                mChangelog.setVisibility(View.GONE);
                mRetryButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDetailsEndpoint.startAsync();
                    }
                }, 500);
            }
        });

        if (rowId == -1) {
            mApp = loadInstalledApp();
            mNewApp = true;
        } else {
            AppListContentProviderClient cr = new AppListContentProviderClient(this);
            mApp = cr.queryAppId(mAppId);
            cr.release();
            mNewApp = false;
        }

        if (mApp == null)
        {
            Toast.makeText(this, getString(R.string.cannot_load_app, mAppId), Toast.LENGTH_LONG).show();
            AppLog.e("Cannot load app details: '"+mAppId+"'");
            finish();
            return;
        }
        setupAppView(mApp);
    }

    private AppInfo loadInstalledApp()
    {
        PackageManagerUtils utils = new PackageManagerUtils(getPackageManager());
        PackageInfo pkgInfo = utils.getPackageInfo(mAppId);
        if (pkgInfo == null)
        {
            return null;
        }

        AppInfo appInfo = utils.packageToApp(pkgInfo);

        Bitmap icon = utils.loadIcon(utils.getLaunchComponent(pkgInfo), getResources().getDisplayMetrics());
        appInfo.setIcon(icon);
        return appInfo;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDetailsEndpoint.setListener(this);

        AuthTokenProvider accHelper = new AuthTokenProvider(this);
        final Preferences prefs = new Preferences(this);
        final Account account = prefs.getAccount();
        mLoadingView.setVisibility(View.VISIBLE);
        accHelper.requestToken(this, account, new AuthTokenProvider.AuthenticateCallback() {
            @Override
            public void onAuthTokenAvailable(String token) {
                mDetailsEndpoint.setAccount(account, token);
                mDetailsEndpoint.startAsync();
            }

            @Override
            public void onUnRecoverableException(String errorMessage) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDetailsEndpoint.setListener(null);
    }

    private void setupAppView(AppInfo app) {
        Bitmap icon = app.getIcon();
        if (icon == null) {
            icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_android_black_48dp);
            mBackground.setVisibility(View.VISIBLE);
            applyColor(ContextCompat.getColor(this, R.color.theme_primary));
        } else {
            Palette.from(icon).generate(this);
        }
        mAppIcon.setImageBitmap(icon);
        mAppTitle.setText(app.getTitle());

        mPlayStoreButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.changelog, menu);
        mAddMenu = menu.findItem(R.id.menu_add);
        mAddMenu.setEnabled(false);
        if (mNewApp) {
            menu.findItem(R.id.menu_remove).setVisible(false);
        } else {
            menu.findItem(R.id.menu_add).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_remove:
                RemoveDialogFragment removeDialog = RemoveDialogFragment.newInstance(
                        mApp.getTitle(), mApp.getRowId()
                );
                removeDialog.show(getSupportFragmentManager(), "removeDialog");
                return true;
            case R.id.menu_add:
                Document doc = mDetailsEndpoint.getDocument();
                if (doc != null) {
                    String imageUrl = DocUtils.getIconUrl(doc);
                    final AppInfo info = new AppInfo(doc, null);
                    AppListContentProviderClient client = new AppListContentProviderClient(this);
                    NewWatchAppHandler appHandler = new NewWatchAppHandler(this, this);
                    appHandler.setContentProvider(client);
                    appHandler.add(info, imageUrl);
                    client.release();
                }
                return true;
            case R.id.menu_share:
                shareApp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareApp() {
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(this);
        if (mApp.getStatus() == AppInfo.STATUS_UPDATED) {
            builder.setSubject(getString(R.string.share_subject_updated, mApp.getTitle()));
        } else {
            builder.setSubject(getString(R.string.share_subject_normal, mApp.getTitle()));
        }
        builder.setText(String.format(MarketInfo.URL_WEB_PLAY_STORE, mApp.getPackageName()));
        builder.setType("text/plain");
        builder.startChooser();
    }

    @Override
    public void onDataChanged() {
        mLoadingView.setVisibility(View.GONE);
        mChangelog.setVisibility(View.VISIBLE);
        mChangelog.setAutoLinkMask(Linkify.ALL);

        mRetryButton.setVisibility(View.GONE);
        String changes = mDetailsEndpoint.getRecentChanges();
        if (changes == null || changes.equals("")) {
            mChangelog.setText(R.string.no_recent_changes);
        } else {
            mChangelog.setText(Html.fromHtml(changes));
        }
        if (mDetailsEndpoint.getDocument() != null) {
            mAddMenu.setEnabled(true);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mLoadingView.setVisibility(View.GONE);
        mChangelog.setVisibility(View.VISIBLE);
        mChangelog.setAutoLinkMask(Linkify.ALL);

        mChangelog.setText(getString(R.string.error_fetching_info));
        mRetryButton.setVisibility(View.VISIBLE);
    }


    @Override
    public void onGenerated(Palette palette) {
        for(Target target : sTargets) {
            Palette.Swatch swatch = palette.getSwatchForTarget(target);
            if (swatch != null) {
                applyColor(swatch.getRgb());
                animateBackground();
                return;
            }
        }
        mBackground.setVisibility(View.VISIBLE);
        applyColor(ContextCompat.getColor(this, R.color.theme_primary));
    }

    private void applyColor(@ColorInt int color) {
        Drawable drawable = DrawableCompat.wrap(mPlayStoreButton.getDrawable());
        DrawableCompat.setTint(drawable, color);
        mPlayStoreButton.setImageDrawable(drawable);
        mBackground.setBackgroundColor(color);
        mLoadingView.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void animateBackground() {
        int[] location = new int[2];
        mAppIcon.getLocationOnScreen(location);
        RevealAnimatorCompat.show(mBackground, location[0], location[1], 0).start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.market_btn) {
            Intent intent = IntentUtils.createPlayStoreIntent(mApp.getPackageName());
            startActivity(intent);
        }
    }

    @Override
    public void onAppAddSuccess(AppInfo info) {
        String msg = getString(R.string.app_stored, info.getTitle());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Intent data = new Intent();
        data.putExtra(EXTRA_ADD_APP_PACKAGE, info.getPackageName());
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onAppAddError(int error) {
        if (NewWatchAppHandler.ERROR_ALEREADY_ADDED == error) {
            Toast.makeText(this, R.string.app_already_added, Toast.LENGTH_SHORT).show();
        } else if (error == NewWatchAppHandler.ERROR_INSERT) {
            Toast.makeText(this, R.string.error_insert_app, Toast.LENGTH_SHORT).show();
        }
    }
}
