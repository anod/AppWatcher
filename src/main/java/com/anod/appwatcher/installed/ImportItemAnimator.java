package com.anod.appwatcher.installed;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.util.List;

/**
 * @author algavris
 * @date 24/04/2016.
 */
class ImportItemAnimator extends DefaultItemAnimator {

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        oldHolder.itemView.clearAnimation();
        newHolder.itemView.clearAnimation();

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
                animateColor(holder.itemView, Color.TRANSPARENT, holder.themeAccent);
            } else if (status == ImportDataProvider.STATUS_ERROR) {
                animateColor(holder.itemView, Color.TRANSPARENT, holder.materialRed);
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

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
        return true;
    }
}
