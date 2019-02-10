package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.activities.StartActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartFragment extends BaseFragment {

    public static StartFragment newInstance() {
        return new StartFragment();
    }

    /* LIFECYCLE */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_start, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    /* END LIFECYCLE */
    /* ON CLICK */

    @OnClick(R.id.tv_sign_in)
    void signIn() {
        getNewStartActivity().setContentFragment(LoginFragment.newInstance());
    }

    @OnClick(R.id.btn_sign_up)
    void signUp() {
        getNewStartActivity().setContentFragment(RegisterFragment.newInstance());
    }

    /* END ON CLICK */

    private StartActivity getNewStartActivity() {
        return (StartActivity) getActivity();
    }

}
