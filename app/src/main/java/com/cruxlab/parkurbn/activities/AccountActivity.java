package com.cruxlab.parkurbn.activities;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.api.ParkUrbnApi;
import com.cruxlab.parkurbn.api.ResponseCallback;
import com.cruxlab.parkurbn.model.request.ChangePasswordBody;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.cruxlab.parkurbn.tools.ValidatorUtils;
import com.cruxlab.parkurbn.SharedPrefsManager;
import com.cruxlab.parkurbn.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class AccountActivity extends BaseActivity {

    @BindView(R.id.et_email)
    TextInputLayout etEmail;
    @BindView(R.id.et_old_password)
    TextInputLayout etOldPassword;
    @BindView(R.id.et_new_password)
    TextInputLayout etNewPassword;
    @BindView(R.id.ll_email_fields)
    LinearLayout llEmailFields;
    @BindView(R.id.tv_logged_in_via_fb)
    TextView tvLoggedInViaFb;
    @BindView(R.id.sw_send_receipts)
    Switch swSendReceipts;
    @BindView(R.id.btn_save)
    Button btnSave;

    private SharedPrefsManager mSharedPrefsManager;
    private ParkUrbnApi mParkUrbnApi;

    /* LIFECYCLE */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ButterKnife.bind(this);
        mSharedPrefsManager = SharedPrefsManager.get();
        mParkUrbnApi = ParkUrbnApplication.get().getParkUrbnApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUser();
    }

    /* END LIFECYCLE */
    /* ON CLICK */

    @OnClick(R.id.ibtn_nav_btn)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.btn_save)
    void save() {
        String email = etEmail.getEditText().getText().toString();
        if (!email.equals(mSharedPrefsManager.getUser().getEmail()) || swSendReceipts.isChecked() != mSharedPrefsManager.getUser().isReceiveReceipts()) {
            if (ValidatorUtils.checkEmail(AccountActivity.this, etEmail, email) || mSharedPrefsManager.getUser().isLoggedViaFb()) {
                showLoader();
                User user = new User();
                if (!mSharedPrefsManager.getUser().isLoggedViaFb()) {
                    user.setEmail(email);
                }
                user.setReceiveReceipts(swSendReceipts.isChecked());
                Call<User> call = mParkUrbnApi.modifyUser(user);
                call.enqueue(new ResponseCallback<User>() {
                    @Override
                    public void handleResponse(Response<User> response) {
                        mSharedPrefsManager.saveUser(response.body());
                        changePassword();
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        super.onFailure(call, t);
                        hideLoader();
                        Toast.makeText(AccountActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void showErrorMessage(ErrorResponse response) {
                        super.showErrorMessage(response);
                        hideLoader();
                        Toast.makeText(AccountActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            changePassword();
        }
    }

    /* END ON CLICK */

    private void changePassword() {
        String newPassword = etNewPassword.getEditText().getText().toString();
        String oldPassword = etOldPassword.getEditText().getText().toString();
        final boolean wasEmailChanged = isLoaderShown();
        if (newPassword.isEmpty() && oldPassword.isEmpty()) {
            if (isLoaderShown()) {
                Toast.makeText(AccountActivity.this, "Successfully changed.", Toast.LENGTH_SHORT).show();
                hideLoader();
            }
            return;
        }
        if (ValidatorUtils.checkPassword(AccountActivity.this, etOldPassword, oldPassword)
                && ValidatorUtils.checkPassword(AccountActivity.this, etNewPassword, newPassword)) {
            if (!isLoaderShown()) {
                showLoader();
            }
            Call<User> call = mParkUrbnApi.changePass(new ChangePasswordBody(newPassword, etOldPassword.getEditText().getText().toString()));
            call.enqueue(new ResponseCallback<User>() {
                @Override
                public void handleResponse(Response<User> response) {
                    SharedPrefsManager.get().saveUser(response.body());
                    hideLoader();
                    if (wasEmailChanged) {
                        Toast.makeText(AccountActivity.this, "Email and password successfully changed.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AccountActivity.this, "Password successfully changed.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                protected void showErrorMessage(ErrorResponse response) {
                    super.showErrorMessage(response);
                    hideLoader();
                    if (response.toString().equals("Incorrect password")) {
                        etOldPassword.setError(response.toString());
                    } else {
                        Toast.makeText(AccountActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    super.onFailure(call, t);
                    hideLoader();
                    Toast.makeText(AccountActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            if (isLoaderShown()) {
                hideLoader();
            }
        }
    }

    private void getUser() {
        showLoader();
        Call<User> call = mParkUrbnApi.getUser();
        call.enqueue(new ResponseCallback<User>() {
            @Override
            public void handleResponse(Response<User> response) {
                mSharedPrefsManager.saveUser(response.body());
                hideLoader();
                initViews();
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                Toast.makeText(AccountActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                hideLoader();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                hideLoader();
            }
        });
    }

    private void initViews() {
        swSendReceipts.setChecked(mSharedPrefsManager.getUser().isReceiveReceipts());
        if (mSharedPrefsManager.getUser().isLoggedViaFb()) {
            llEmailFields.setVisibility(View.GONE);
            tvLoggedInViaFb.setVisibility(View.VISIBLE);
            String firstName = mSharedPrefsManager.getUser().getFirstName();
            String lastName = mSharedPrefsManager.getUser().getLastName();
            if (firstName != null && lastName != null) {
                tvLoggedInViaFb.setText(getString(R.string.you_logged_in_as_via_fb, firstName, lastName));
            } else {
                tvLoggedInViaFb.setText(getString(R.string.you_logged_in_via_fb));
            }

            if (mSharedPrefsManager.getUser().getEmail() == null) {
                swSendReceipts.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
            }
        } else {
            etEmail.getEditText().setText(mSharedPrefsManager.getUser().getEmail());
            etOldPassword.requestFocus();
        }
    }
}
