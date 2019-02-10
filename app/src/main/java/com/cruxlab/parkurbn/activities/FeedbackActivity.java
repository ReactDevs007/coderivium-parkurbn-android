package com.cruxlab.parkurbn.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.api.ParkUrbnApi;
import com.cruxlab.parkurbn.api.ResponseCallback;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.consts.RequestCodes;
import com.cruxlab.parkurbn.model.request.FeedbackBody;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class FeedbackActivity extends BaseActivity implements View.OnTouchListener {

    @BindView(R.id.tv_state_title)
    TextView tvStateTitle;
    @BindView(R.id.tv_state_id)
    TextView tvStateId;
    @BindView(R.id.et_neighborhood)
    EditText etNeighborhood;
    @BindView(R.id.et_city)
    EditText etCity;
    @BindView(R.id.rating_bar)
    SimpleRatingBar ratingBar;
    @BindView(R.id.et_feedback)
    EditText etFeedback;
    @BindView(R.id.parent)
    ScrollView root;

    private ParkUrbnApi parkUrbnApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);
        root.setOnTouchListener(this);
        parkUrbnApi = ParkUrbnApplication.get().getParkUrbnApi();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.SELECT_STATE) {
            if (resultCode == Activity.RESULT_OK) {
                String title = data.getStringExtra(BundleArguments.STATE_TITLE);
                String id = data.getStringExtra(BundleArguments.STATE_ID);
                tvStateTitle.setText(title);
                tvStateId.setText(id);
            }
        }
    }

    @OnClick(R.id.ibtn_nav_btn)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.btn_submit)
    void sendFeedback() {
        if (etNeighborhood.getText().toString().isEmpty() && etCity.getText().toString().isEmpty() &&
                tvStateId.getText().equals(getString(R.string.select)) && ratingBar.getRating() == 0.0 &&
                etFeedback.getText().toString().isEmpty()) {
            Toast.makeText(FeedbackActivity.this, "Please fill fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoader();
        FeedbackBody feedbackBody = new FeedbackBody();
        if (!etNeighborhood.getText().toString().isEmpty()) {
            feedbackBody.setNeighborhood(etNeighborhood.getText().toString());
        }
        if (!etCity.getText().toString().isEmpty()) {
            feedbackBody.setCity(etCity.getText().toString());
        }
        if (!tvStateId.getText().equals(getString(R.string.select))) {
            feedbackBody.setState(tvStateId.getText().toString());
        }
        if (!etFeedback.getText().toString().isEmpty()) {
            feedbackBody.setFeedback(etFeedback.getText().toString());
        }
        if (ratingBar.getRating() > 0.0) {
            feedbackBody.setRating((int) ratingBar.getRating());
        }

        Call<JSONObject> call = parkUrbnApi.sendFeedback(feedbackBody);
        call.enqueue(new ResponseCallback<JSONObject>() {
            @Override
            public void handleResponse(Response<JSONObject> response) {
                hideLoader();
                Toast.makeText(FeedbackActivity.this, "Feedback was sent.", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                hideLoader();
                Toast.makeText(FeedbackActivity.this, "Sending of the message is failed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                hideLoader();
                Toast.makeText(FeedbackActivity.this, "Sending of the message is failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.rl_select_state)
    void selectState() {
        startActivityForResult(new Intent(FeedbackActivity.this, StateListActivity.class), RequestCodes.SELECT_STATE);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        return false;
    }
}
