package com.cruxlab.parkurbn.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.activities.StartActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginFragment extends BaseFragment {

    @BindView(R.id.et_email)
    TextInputLayout etEmail;
    @BindView(R.id.et_password)
    TextInputLayout etPassword;
    @BindView(R.id.parent)
    ScrollView root;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        root.setOnTouchListener(getNewStartActivity());
        etPassword.getEditText().setOnEditorActionListener(new DoneOnEditorActionListener());
        return rootView;
    }

    private StartActivity getNewStartActivity() {
        return (StartActivity) getActivity();
    }

    @OnClick(R.id.btn_login)
    void login() {
//        TODO: DEBUG
//        if (ValidatorUtils.checkEmail(getActivity(), etEmail, etEmail.getEditText().getText().toString()) &&
//                ValidatorUtils.checkPassword(getActivity(), etPassword, etPassword.getEditText().getText().toString())) {
        getNewStartActivity().login(etEmail.getEditText().getText().toString(), etPassword.getEditText().getText().toString());
//        }
    }

    @OnClick(R.id.btn_forgot_password)
    void restorePassword() {
        getNewStartActivity().setContentFragment(ForgotPasswordFragment.newInstance());
    }

    @OnClick(R.id.ibtn_nav_btn)
    void back() {
        getActivity().onBackPressed();
    }

    @OnClick(R.id.fl_login_with_fb)
    void loginWithFacebook() {
        getNewStartActivity().loginWithFacebook();
    }

    private class DoneOnEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                login();
                return true;
            }
            return false;
        }
    }
}
