package com.anod.appwatcher.installed;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.anod.appwatcher.R;

import java.util.List;

/**
 * @author algavris
 * @date 24/04/2016.
 */
public class ImportItemAnimator extends DefaultItemAnimator {
    private int mThemePrimaryDark;
    private int mMaterialRed;

    public ImportItemAnimator(Context context) {
        Resources res = context.getResources();
        mThemePrimaryDark = ResourcesCompat.getColor(res, R.color.theme_primary_dark, null);
        mMaterialRed = ResourcesCompat.getColor(res, R.color.material_red_800, null);
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        cancelCurrentAnimationIfExists(oldHolder);

        if (newHolder instanceof ImportAppViewHolder) {
            ImportAppViewHolder holder = (ImportAppViewHolder) newHolder;
            int status = holder.status();
            if (status == ImportDataProvider.STATUS_IMPORTING) {
                Animation anim = new AlphaAnimation(0.2f, 1.0f);
                anim.setDuration(500);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                holder.itemView.startAnimation(anim);
            } else if (status == ImportDataProvider.STATUS_DONE) {
                animateColor(holder.itemView, Color.TRANSPARENT, mThemePrimaryDark);
            } else if (status == ImportDataProvider.STATUS_ERROR) {
                animateColor(holder.itemView, Color.TRANSPARENT, mMaterialRed);
            }
        }

        return false;
    }

    private void animateColor(View view,@ColorInt int startColor, @ColorInt int endColor)
    {
        final ObjectAnimator animator = ObjectAnimator.ofObject(
                view, "backgroundColor" , new ArgbEvaluator(), startColor, endColor
        );
        animator.setDuration(300);
        animator.start();
    }

    private void cancelCurrentAnimationIfExists(RecyclerView.ViewHolder oldHolder) {
        oldHolder.itemView.clearAnimation();
    }

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
        return true;
    }
}
