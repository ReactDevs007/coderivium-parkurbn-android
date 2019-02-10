package com.cruxlab.parkurbn.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.cruxlab.parkurbn.BuildConfig;
import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.SharedPrefsManager;
import com.cruxlab.parkurbn.api.ParkUrbnApi;
import com.cruxlab.parkurbn.api.ResponseCallback;
import com.cruxlab.parkurbn.fragments.StartFragment;
import com.cruxlab.parkurbn.model.User;
import com.cruxlab.parkurbn.model.request.LoginBody;
import com.cruxlab.parkurbn.model.request.LoginFBBody;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.UpdateManager;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class StartActivity extends BaseActivity implements FacebookCallback<LoginResult>, View.OnTouchListener {

    protected final List<String> permissions = Collections.singletonList("email");
    protected CallbackManager mCallbackManager;
    protected ParkUrbnApi mParkUrbnApi;

    /* LIFECYCLE */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, this);
        mParkUrbnApi = ParkUrbnApplication.get().getParkUrbnApi();
        setContentFragment(StartFragment.newInstance());
        checkForUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }

    /* END LIFECYCLE */
    /* HOCKEYAPP */

    private void checkForCrashes() {
        if (BuildConfig.BUILD_TYPE.equals("release")) {
            CrashManager.register(this, getString(R.string.hockeyapp_app_id), new CrashManagerListener() {
                @Override
                public boolean shouldAutoUploadCrashes() {
                    return true;
                }
            });
        }
    }

    private void checkForUpdates() {
        UpdateManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }

    /* END HOCKEYAPP */
    /* EVENTS */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        showLoader();
        Call<JsonObject> call = mParkUrbnApi.loginFBUser(new LoginFBBody(loginResult.getAccessToken().getToken()));
        call.enqueue(new ResponseCallback<JsonObject>() {
            @Override
            public void handleResponse(Response<JsonObject> response) {
                handleUserInfo(response, this);
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                Toast.makeText(StartActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                hideLoader();
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                super.onFailure(call, t);
                hideLoader();
                LoginManager.getInstance().logOut();
                Toast.makeText(StartActivity.this, "Failed to login with this FB account.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onError(FacebookException error) {
        error.printStackTrace();
        Toast.makeText(StartActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        return false;
    }

    /* END EVENTS */

    public void setContentFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (getSupportFragmentManager().findFragmentById(R.id.content_start) == null) {
            transaction.add(R.id.content_start, fragment);
        } else {
            transaction.replace(R.id.content_start, fragment);
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(StartActivity.this, permissions);
    }

    /* QUERIES */

    public void login(String email, String password) {
        showLoader();
        LoginBody body = new LoginBody("test@coderivium.com", "qwerty");
        Call<JsonObject> call = mParkUrbnApi.loginUser(body);
        call.enqueue(new ResponseCallback<JsonObject>() {
            @Override
            public void handleResponse(Response<JsonObject> response) {
                handleUserInfo(response, this);
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                Toast.makeText(StartActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                hideLoader();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                super.onFailure(call, t);
                Toast.makeText(StartActivity.this, "Login or password is incorrect.", Toast.LENGTH_SHORT).show();
                hideLoader();
            }
        });
    }

    public void register(final String email, final String password) {
        showLoader();
        LoginBody body = new LoginBody(email, password);
        Call<JsonObject> call = mParkUrbnApi.registerUser(body);
        call.enqueue(new ResponseCallback<JsonObject>() {
            @Override
            public void handleResponse(Response<JsonObject> response) {
                handleUserInfo(response, this);
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                super.showErrorMessage(response);
                hideLoader();
                Toast.makeText(StartActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                super.onFailure(call, t);
                Toast.makeText(StartActivity.this, "Sign up failed.", Toast.LENGTH_SHORT).show();
                hideLoader();
            }
        });
    }

    public void forgetPassword(String email) {
        showLoader();
        mParkUrbnApi.forgotPassword(new LoginBody(email)).enqueue(new ResponseCallback<JSONObject>() {
            @Override
            public void handleResponse(Response<JSONObject> response) {
                hideLoader();
                onBackPressed();
                Toast.makeText(StartActivity.this, "Reset password link was sent successfully.", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                super.showErrorMessage(response);
                hideLoader();
                Toast.makeText(StartActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                super.onFailure(call, t);
                hideLoader();
                Toast.makeText(StartActivity.this, "Error occurred during password restoring.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* END QUERIES */

    private void handleUserInfo(Response<JsonObject> response, ResponseCallback<JsonObject> callback) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            User user = gson.fromJson(response.body().toString(), User.class);
            SharedPrefsManager.get().saveUser(user);
        } catch (JsonSyntaxException e) {
            callback.onFailure(null, e);
        }

        ParkUrbnApplication.get().getLoadHistoryManager().loadHistory(null);
        hideLoader();
        goToMap();
    }

    private void goToMap() {
        startActivity(new Intent(this, TutorialActivity.class));
        finish();
    }

}
