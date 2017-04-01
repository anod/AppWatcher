package com.anod.appwatcher.tags;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anod.appwatcher.R;
import com.anod.appwatcher.fragments.AppWatcherListFragment;
import com.anod.appwatcher.model.Tag;

/**
 * @author algavris
 * @date 01/04/2017.
 */

public class AppsTagListFragment extends AppWatcherListFragment {

    public static AppsTagListFragment newInstance(int filterId, int sortId, SectionProvider sectionProvider, @Nullable Tag tag) {
        AppsTagListFragment frag = new AppsTagListFragment();
        frag.setArguments(createArguments(filterId, sortId, sectionProvider, tag));
        return frag;
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appstag_list, container, false);
    }

}
