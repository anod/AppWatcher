package com.anod.appwatcher.tags;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;

/**
 * @author algavris
 * @date 02/05/2017.
 */

public class TagSnackbar {
    private static final String GREEN_BOOK = "📗";

    public static Snackbar make(Activity activity, AppInfo info, boolean finishActivity) {
        String msg = activity.getString(R.string.app_stored, info.title);
        String tagText = activity.getString(R.string.action_tag, GREEN_BOOK);

        return Snackbar.make(activity.findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
                .setAction(tagText, new TagAction(activity, info))
                .addCallback(new TagCallback(activity, finishActivity));
    }

    static class TagAction implements View.OnClickListener {
        private final Activity activity;
        private final AppInfo app;

        TagAction(Activity activity, AppInfo info) {
            this.activity = activity;
            app = info;
        }

        @Override
        public void onClick(View v) {
            this.activity.startActivity(TagsListActivity.intent(activity, app));
        }
    }


    private static class TagCallback extends Snackbar.Callback {
        private final Activity activity;
        private final boolean finishActivity;

        TagCallback(Activity activity, boolean finishActivity) {
            this.finishActivity = finishActivity;
            this.activity = activity;
        }

        @Override
        public void onDismissed(Snackbar transientBottomBar, int event) {
            if (this.finishActivity) {
                this.activity.finish();
            }
        }
    }
}
