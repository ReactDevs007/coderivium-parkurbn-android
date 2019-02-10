package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.activities.StartActivity;
import com.cruxlab.parkurbn.tools.ValidatorUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ForgotPasswordFragment extends BaseFragment {

    @BindView(R.id.parent)
    ScrollView root;
    @BindView(R.id.et_email)
    TextInputLayout etEmail;

    public static ForgotPasswordFragment newInstance() {
        return new ForgotPasswordFragment();
    }

    /* LIFECYCLE */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        root.setOnTouchListener(getNewStartActivity());
        return rootView;
    }

    /* END LIFECYCLE */
    /* ON CLICK */

    @OnClick(R.id.ibtn_nav_btn)
    void back() {
        getActivity().onBackPressed();
    }

    @OnClick(R.id.btn_submit)
    void submit() {
        String email = etEmail.getEditText().getText().toString();
        if (ValidatorUtils.checkEmail(getActivity(), etEmail, email)) {
            getNewStartActivity().forgetPassword(email);
        }
    }

    /* END ON CLICK */

    private StartActivity getNewStartActivity() {
        return (StartActivity) getActivity();
    }
}
