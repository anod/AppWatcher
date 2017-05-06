package com.anod.appwatcher;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.anod.appwatcher.accounts.AuthTokenProvider;
import com.anod.appwatcher.adapters.AppDetailsView;
import com.anod.appwatcher.adapters.AppViewHolderDataProvider;
import com.anod.appwatcher.content.TagsCursor;
import com.anod.appwatcher.fragments.RemoveDialogFragment;
import com.anod.appwatcher.market.DetailsEndpoint;
import com.anod.appwatcher.market.MarketInfo;
import com.anod.appwatcher.market.PlayStoreEndpoint;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.content.DbContentProviderClient;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.model.WatchAppList;
import com.anod.appwatcher.tags.TagSnackbar;
import com.anod.appwatcher.tags.TagsListActivity;
import com.anod.appwatcher.ui.ToolbarActivity;
import com.anod.appwatcher.utils.AppIconLoader;
import com.anod.appwatcher.utils.BackgroundTask;
import com.anod.appwatcher.utils.InstalledAppsProvider;
import com.anod.appwatcher.utils.IntentUtils;
import com.anod.appwatcher.utils.MetricsManagerEvent;
import com.anod.appwatcher.utils.PackageManagerUtils;
import com.anod.appwatcher.utils.PaletteSwatch;
import com.google.android.finsky.api.model.Document;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.anodsplace.android.anim.RevealAnimatorCompat;
import info.anodsplace.android.log.AppLog;



public class ChangelogActivity extends ToolbarActivity implements PlayStoreEndpoint.Listener, Palette.PaletteAsyncListener, View.OnClickListener, WatchAppList.Listener {

    public static final String EXTRA_APP_ID = "app_id";
    public static final String EXTRA_DETAILS_URL = "url";
    public static final String EXTRA_ROW_ID = "row_id";
    public static final String EXTRA_ADD_APP_PACKAGE = "app_add_success";
    public static final String EXTRA_UNINSTALL_APP_PACKAGE = "app_uninstall";

    @BindView(R.id.progress_bar)
    ProgressBar mLoadingView;
    @BindView(R.id.changelog)
    TextView mChangelog;
    @BindView(R.id.retry)
    Button mRetryButton;
    @BindView(android.R.id.icon)
    ImageView mAppIcon;
    @BindView(R.id.background)
    View mBackground;
    @BindView(R.id.market_btn)
    FloatingActionButton mPlayStoreButton;
    @BindView(R.id.content)
    View mContent;

    private String mDetailsUrl;
    private String mAppId;

    private DetailsEndpoint mDetailsEndpoint;
    private AppInfo mApp;
    private boolean mNewApp;
    private MenuItem mAddMenu;
    private AppIconLoader mIconLoader;
    private AppViewHolderDataProvider mDataProvider;
    private AppDetailsView mAppDetailsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_changelog);
        ButterKnife.bind(this);
        setupToolbar();

        Intent data = getIntent();

        mIconLoader = App.provide(this).iconLoader();
        mAppId = data.getStringExtra(EXTRA_APP_ID);
        mDetailsUrl = data.getStringExtra(EXTRA_DETAILS_URL);
        int rowId = data.getIntExtra(EXTRA_ROW_ID, -1);
        MetricsManagerEvent.track(this, "open_changelog", "DETAILS_APP_ID", mAppId, "DETAILS_ROW_ID", String.valueOf(rowId));

        mDataProvider = new AppViewHolderDataProvider(this, new InstalledAppsProvider.PackageManager(getPackageManager()));
        mAppDetailsView = new AppDetailsView(findViewById(R.id.container), mDataProvider);

        mDetailsEndpoint = new DetailsEndpoint(this);
        mDetailsEndpoint.setUrl(mDetailsUrl);

        mContent.setVisibility(View.INVISIBLE);
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
            DbContentProviderClient cr = new DbContentProviderClient(this);
            mApp = cr.queryAppRow(rowId);
            cr.close();
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
        return PackageManagerUtils.packageToApp(mAppId, getPackageManager());
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
        mPlayStoreButton.setOnClickListener(this);

        mAppDetailsView.fillDetails(app, mApp.getRowId() == -1);

        if (TextUtils.isEmpty(app.iconUrl)) {
            if (app.getRowId() > 0)
            {
                Uri dbImageUri = AppListContentProvider.ICONS_CONTENT_URI.buildUpon().appendPath(String.valueOf(app.getRowId())).build();
                mIconLoader.retrieve(dbImageUri).into(mIconLoadTarget);
            } else {
                setDefaultIcon();
            }
        } else {
            mIconLoader.retrieve(app.iconUrl).into(mIconLoadTarget);
        }
    }

    private com.squareup.picasso.Target mIconLoadTarget = new com.squareup.picasso.Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Palette.from(bitmap).generate(ChangelogActivity.this);
            mAppIcon.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            AppLog.e("mIconLoadTarget::onBitmapFailed", e);
            setDefaultIcon();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private void setDefaultIcon() {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notifications_black_24dp);
        mBackground.setVisibility(View.VISIBLE);
        applyColor(ContextCompat.getColor(this, R.color.theme_primary));
        mAppIcon.setImageBitmap(icon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.changelog, menu);
        mAddMenu = menu.findItem(R.id.menu_add);
        mAddMenu.setEnabled(false);
        if (mNewApp) {
            menu.findItem(R.id.menu_remove).setVisible(false);
            menu.findItem(R.id.menu_tag_app).setVisible(false);
        } else {
            menu.findItem(R.id.menu_add).setVisible(false);
            MenuItem tagMenu = menu.findItem(R.id.menu_tag_app);
            loadTagSubmenu(tagMenu);
        }
        if (!mDataProvider.getInstalledAppsProvider().getInfo(mAppId).isInstalled()) {
            menu.findItem(R.id.menu_uninstall).setVisible(false);
        }

        return true;
    }

    private static class TagMenuItem {
        final Tag tag;
        final boolean selected;

        private TagMenuItem(Tag tag, boolean selected) {
            this.tag = tag;
            this.selected = selected;
        }
    }

    private void loadTagSubmenu(final MenuItem tagMenu) {

        BackgroundTask.execute(new BackgroundTask.Worker<Void, List<TagMenuItem>>(null, this) {

            @Override
            public List<TagMenuItem> run(Void param, Context context) {
                DbContentProviderClient cr = new DbContentProviderClient(context);
                TagsCursor tags = cr.queryTags();
                List<Integer> appTags = cr.queryAppTags(mApp.getRowId());

                tags.moveToPosition(-1);
                ArrayList<TagMenuItem> result = new ArrayList<>();
                while (tags.moveToNext()) {
                    Tag tag = tags.getTag();
                    result.add(new TagMenuItem(tag, appTags.contains(tag.id)));
                }
                cr.close();
                return result;
            }

            @Override
            public void finished(List<TagMenuItem> result, Context context) {
                SubMenu tagSubMenu = tagMenu.getSubMenu();
                for (TagMenuItem item: result) {
                    tagSubMenu.add(R.id.menu_group_tags, item.tag.id, 0, item.tag.name)
                        .setChecked(item.selected);
                }
                tagSubMenu.setGroupCheckable(R.id.menu_group_tags, true, false);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_remove:
                RemoveDialogFragment removeDialog = RemoveDialogFragment.newInstance(
                        mApp.title, mApp.getRowId()
                );
                removeDialog.show(getSupportFragmentManager(), "removeDialog");
                return true;
            case R.id.menu_add:
                Document doc = mDetailsEndpoint.getDocument();
                if (doc != null) {
                    final AppInfo info = new AppInfo(doc);
                    WatchAppList appList = new WatchAppList(this);
                    appList.attach(this);
                    appList.add(info);
                    appList.detach();
                }
                return true;
            case R.id.menu_uninstall:
                Intent data = new Intent();
                data.putExtra(EXTRA_UNINSTALL_APP_PACKAGE, mAppId);
                setResult(RESULT_OK, data);
                Intent uninstallIntent = IntentUtils.createUninstallIntent(mAppId);
                startActivity(uninstallIntent);
                return true;
            case R.id.menu_share:
                shareApp();
                return true;
        }
        if (item.getGroupId() == R.id.menu_group_tags) {
            if (changeTag(item.getItemId(), item.isChecked())) {
                item.setChecked(!item.isChecked());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean changeTag(int tagId, boolean checked) {
        DbContentProviderClient cr = new DbContentProviderClient(this);
        if (checked) {
            return cr.removeAppFromTag(mApp.getAppId(), tagId);
        }
        return cr.addAppToTag(mApp.getAppId(), tagId);
    }

    private void shareApp() {
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(this);
        if (mApp.getStatus() == AppInfo.STATUS_UPDATED) {
            builder.setSubject(getString(R.string.share_subject_updated, mApp.title));
        } else {
            builder.setSubject(getString(R.string.share_subject_normal, mApp.title));
        }
        builder.setText(String.format(MarketInfo.URL_WEB_PLAY_STORE, mApp.packageName));
        builder.setType("text/plain");
        builder.startChooser();
    }

    @Override
    public void onDataChanged() {
        mLoadingView.setVisibility(View.GONE);
        mContent.setVisibility(View.VISIBLE);
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
        mContent.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.GONE);
        mChangelog.setVisibility(View.VISIBLE);
        mChangelog.setAutoLinkMask(Linkify.ALL);

        mChangelog.setText(getString(R.string.error_fetching_info));
        mRetryButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onGenerated(Palette palette) {
        Palette.Swatch darkSwatch = PaletteSwatch.getDark(palette, ContextCompat.getColor(this, R.color.theme_primary));
        applyColor(darkSwatch.getRgb());
        animateBackground();

        if (App.with(this).isNightTheme())
        {
            mAppDetailsView.updateAccentColor(ContextCompat.getColor(this, R.color.primary_text_dark), mApp);
        } else {
            mAppDetailsView.updateAccentColor(darkSwatch.getRgb(), mApp);
        }
    }

    private void applyColor(@ColorInt int color) {
        Drawable drawable = DrawableCompat.wrap(mPlayStoreButton.getDrawable());
        DrawableCompat.setTint(drawable, color);
        mPlayStoreButton.setImageDrawable(drawable);
        mBackground.setBackgroundColor(color);
        mLoadingView.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void animateBackground() {
        mBackground.post(new Runnable() {
            @Override
            public void run() {
                int[] location = new int[2];
                mAppIcon.getLocationOnScreen(location);
                if (ViewCompat.isAttachedToWindow(mBackground)) {
                    RevealAnimatorCompat.show(mBackground, location[0], location[1], 0).start();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.market_btn) {
            Intent intent = IntentUtils.createPlayStoreIntent(mApp.packageName);
            startActivity(intent);
        }
    }

    @Override
    public void onWatchListChangeSuccess(AppInfo info, int newStatus) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ADD_APP_PACKAGE, info.packageName);
        setResult(RESULT_OK, data);

        TagSnackbar.make(this, info, true).show();
    }

    @Override
    public void onWatchListChangeError(AppInfo info, int error) {
        if (WatchAppList.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(this, R.string.app_already_added, Toast.LENGTH_SHORT).show();
        } else if (error == WatchAppList.ERROR_INSERT) {
            Toast.makeText(this, R.string.error_insert_app, Toast.LENGTH_SHORT).show();
        }
    }
}
