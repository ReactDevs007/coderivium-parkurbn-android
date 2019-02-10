package com.cruxlab.parkurbn.fragments;

import android.support.v4.app.Fragment;

import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment {

    protected Unbinder mUnbinder;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        mUnbinder = null;
    }
}
