package com.cruxlab.parkurbn.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.cruxlab.parkurbn.DurationPickerManager;
import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.SharedPrefsManager;
import com.cruxlab.parkurbn.api.ParkUrbnApi;
import com.cruxlab.parkurbn.api.ResponseCallback;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.consts.RequestCodes;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.model.Vehicle;
import com.cruxlab.parkurbn.model.request.PaymentRequest;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.cruxlab.parkurbn.model.response.PaymentResponse;
import com.cruxlab.parkurbn.model.response.TokenResponse;
import com.cruxlab.parkurbn.tools.Converter;
import com.cruxlab.parkurbn.tools.DialogUtils;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;
import com.wx.wheelview.widget.WheelView;

import java.io.FileNotFoundException;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class OrderActivity extends BaseActivity implements DurationPickerManager.DurationPickerCallback {

    @BindView(R.id.tv_time_duration)
    TextView tvDuration;
    @BindView(R.id.tv_time_end)
    TextView tvTimeEnd;
    @BindView(R.id.tv_vehicle_arrow_text)
    TextView tvVehicleArrowText;
    @BindView(R.id.tv_vehicle_title)
    TextView tvVehicleTitle;
    @BindView(R.id.iv_map_screenshot)
    ImageView ivMapScreenshot;
    @BindView(R.id.tv_price_per_hour)
    TextView tvPricePerHour;
    @BindView(R.id.ll_bottom_state_pay)
    LinearLayout llBottomStatePay;
    @BindView(R.id.ll_bottom_state_pick)
    LinearLayout llBottomStatePick;
    @BindView(R.id.wv_hours)
    WheelView<String> wvHours;
    @BindView(R.id.wv_mins)
    WheelView<String> wvMins;
    @BindView(R.id.tv_total_price)
    TextView tvTotalPrice;
    @BindView(R.id.ll_vehicle)
    LinearLayout llSelectVehicle;
    @BindView(R.id.btn_pay)
    Button btnPay;
    @BindView(R.id.iv_time_arrow)
    ImageView ivTimeArrow;
    @BindView(R.id.ll_time_and_price)
    LinearLayout llTimeAndPrice;
    @BindView(R.id.tv_paid_price)
    TextView tvPaidPrice;
    @BindView(R.id.tv_free_time)
    TextView tvFreeTime;
    @BindView(R.id.tv_paid_time)
    TextView tvPaidTime;
    @BindView(R.id.ll_free_time)
    LinearLayout llFreeTime;
    @BindView(R.id.ll_paid_time)
    LinearLayout llPaidTime;
    @BindView(R.id.ll_total_price)
    LinearLayout llTotalPrice;
    @BindView(R.id.ll_selected_time)
    LinearLayout llSelectedTime;
    @BindColor(R.color.color_orange)
    int colorOrange;
    @BindColor(R.color.color_orange_light)
    int colorOrangeLight;

    private ParkUrbnApi mParkUrbnApi;
    private DurationPickerManager mPickerManager;
    private Spot mSpot;
    private Vehicle mVehicle;
    private long priceCent;

    private static final int ANIM_DURATION = 400;
    private final static int STATE_ANIM = 0;
    private final static int STATE_PAY = 1;
    private final static int STATE_PICK = 2;

    private int curState = STATE_PAY;

    /* LIFECYCLE */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        ButterKnife.bind(this);
        mParkUrbnApi = ParkUrbnApplication.get().getParkUrbnApi();
        mSpot = getIntent().getParcelableExtra(BundleArguments.SPOT);
        if (mVehicle != null) {
            setSelectedVehicle(mVehicle);
        } else {
            getVehicles();
        }
        mPickerManager = new DurationPickerManager(this, wvHours, wvMins, mSpot.getMaxParkingTimeMins(), this);
        setMapBackground();
        initTimeAndPriceMargin();
    }

    /* END LIFECYCLE */
    /* INITIALIZATION */

    private void initTimeAndPriceMargin() {
        llSelectVehicle.post(new Runnable() {
            @Override
            public void run() {
                int margin = llSelectVehicle.getHeight();
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) llTimeAndPrice.getLayoutParams();
                marginLayoutParams.topMargin = margin;
                llTimeAndPrice.requestLayout();
            }
        });
    }

    /* END INITIALIZATION */
    /* EVENTS */

    @Override
    public void onDurationPicked(int hours, int mins) {
        tvDuration.setText((hours < 10 ? "0" : "") + hours + ":" + (mins < 10 ? "0" : "") + mins);
        int timeMins = hours * 60 + mins;
        tvTimeEnd.setText(getString(R.string.ends_at, Converter.minsToTimeAMPM(mSpot.getClientArrivedTime() + timeMins)));
        int freeMins = Math.min(timeMins, mSpot.getFreeMinsRemaining());
        int paidMins = timeMins - freeMins;
        llFreeTime.setVisibility((freeMins == 0 || paidMins == 0) ? View.GONE : View.VISIBLE);
        llPaidTime.setVisibility((freeMins == 0 || paidMins == 0) ? View.GONE : View.VISIBLE);
        tvFreeTime.setText(Converter.minsToDuration(freeMins));
        tvPaidTime.setText(Converter.minsToDuration(paidMins));

        priceCent = Math.round(mSpot.getPricePerMin() * paidMins);
        tvPaidPrice.setText(Converter.getPriceStr(priceCent / 100f));

        llTotalPrice.setVisibility(paidMins == 0 ? View.GONE : View.VISIBLE);
        tvTotalPrice.setText(Converter.getPriceStr(priceCent / 100f));
        btnPay.setText(getString(paidMins == 0 ? R.string.next : R.string.pay));
        tvPricePerHour.setText(paidMins == 0 ? "FREE" : Converter.getPriceStr(mSpot.getPricePerMin() * 60 / 100) + "/h");
        btnPay.setBackgroundResource(timeMins != 0 && mVehicle != null ? R.drawable.bg_app_button : R.drawable.bg_app_button_disabled);
        btnPay.setTextColor(timeMins != 0 && mVehicle != null ? colorOrange : colorOrangeLight);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, Intent data) {
        if (requestCode == RequestCodes.SELECT_VEHICLE) {
            if (resultCode == Activity.RESULT_OK) {
                Vehicle vehicle = data.getParcelableExtra(BundleArguments.VEHICLE);
                setSelectedVehicle(vehicle);
            }
        } else if (requestCode == RequestCodes.BRAINTREE_DROPIN) {
            if (resultCode == Activity.RESULT_OK) {
                showLoader();
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                mParkUrbnApi.pay(new PaymentRequest(priceCent, result.getPaymentMethodNonce().getNonce(),
                        mVehicle.getLicensePlateNumber(), mSpot.getId(), mSpot.getArrivedTime(), mSpot.getArrivedDate())).enqueue(new ResponseCallback<PaymentResponse>() {
                    @Override
                    protected void showErrorMessage(ErrorResponse response) {
                        super.showErrorMessage(response);
                        Toast.makeText(OrderActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                        hideLoader();
                    }

                    @Override
                    public void onFailure(Call<PaymentResponse> call, Throwable t) {
                        super.onFailure(call, t);
                        Toast.makeText(OrderActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        hideLoader();
                    }

                    @Override
                    public void handleResponse(Response<PaymentResponse> response) {
                        if (response.body().isSuccessful()) {
                            hideLoader();
                            mSpot.setExpiration(response.body().getExpiration());
                            mSpot.setLicencePlateNumber(mVehicle.getLicensePlateNumber());
                            SharedPrefsManager.get().saveParkedSpot(mSpot);
                            Intent parkedIntent = new Intent(OrderActivity.this, ParkingInfoActivity.class);
                            parkedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(parkedIntent);
                        } else {
                            hideLoader();
                            Toast.makeText(OrderActivity.this, response.body().errors.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (resultCode != Activity.RESULT_CANCELED) {
                Exception e = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                e.printStackTrace();
            }
        }
    }

    /* END EVENTS */
    /* ON CLICK */

    @OnClick(R.id.tv_done)
    void done() {
        if (curState == STATE_PICK) {
            setPayState();
        }
    }

    @OnClick(R.id.ibtn_nav_btn)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.ll_time)
    void selectTime() {
        if (curState == STATE_PAY) {
            setPickState();
        }
    }

    @OnClick(R.id.ll_select_vehicle)
    void selectVehicle() {
        if (curState == STATE_PAY) {
            Intent vehicleIntent = new Intent(OrderActivity.this, VehicleActivity.class);
            if (mVehicle == null) {
                vehicleIntent.putExtra(BundleArguments.SETUP_MODE, true);
            } else {
                vehicleIntent.putExtra(BundleArguments.VEHICLE, mVehicle);
            }
            vehicleIntent.putExtra(BundleArguments.SELECT_MODE, true);
            startActivityForResult(vehicleIntent, RequestCodes.SELECT_VEHICLE);
        }
    }

    @OnClick(R.id.btn_pay)
    void pay() {
        if (mPickerManager.getDuration() == 0 || mVehicle == null) {
            DialogUtils.showSelectVehicleAndTimeDialog(this, null);
            return;
        }
        if (btnPay.getText().equals("Pay")) {
            showLoader();
            mParkUrbnApi.getClientToken().enqueue(new ResponseCallback<TokenResponse>() {
                @Override
                public void onFailure(Call<TokenResponse> call, final Throwable t) {
                    super.onFailure(call, t);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OrderActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            hideLoader();
                        }
                    });
                }

                @Override
                public void handleResponse(Response<TokenResponse> response) {
                    hideLoader();
                    //TODO: Test with existing Android Pay card
                    Cart cart = Cart.newBuilder()
                            .setCurrencyCode("USD")
                            .setTotalPrice(String.valueOf(priceCent / 100f))
                            .addLineItem(LineItem.newBuilder()
                                    .setCurrencyCode("USD")
                                    .setDescription("Description")
                                    .setQuantity(String.valueOf(priceCent / 100f))
                                    .setUnitPrice(String.valueOf(priceCent / 100f))
                                    .setTotalPrice(String.valueOf(priceCent / 100f))
                                    .build())
                            .build();
                    DropInRequest dropInRequest = new DropInRequest()
                            .clientToken(response.body().getClientToken())
                            .androidPayCart(cart);
                    startActivityForResult(dropInRequest.getIntent(OrderActivity.this), RequestCodes.BRAINTREE_DROPIN);
                }

                @Override
                protected void showErrorMessage(ErrorResponse response) {
                    hideLoader();
                    Log.d("myLog", "error: " + response);
                }
            });
        } else {
            mSpot.setExpiration(mSpot.getArrivedTime() + mPickerManager.getDuration());
            mSpot.setLicencePlateNumber(mVehicle.getLicensePlateNumber());
            SharedPrefsManager.get().saveParkedSpot(mSpot);
            Intent parkedIntent = new Intent(OrderActivity.this, ParkingInfoActivity.class);
            parkedIntent.putExtra(BundleArguments.FROM, ParkingInfoActivity.FROM_ORDER);
            parkedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(parkedIntent);
        }
    }

    /* END ON CLICK */
    /* QUERIES */

    private void getVehicles() {
        showLoader();
        mParkUrbnApi.getVehicleList().enqueue(new ResponseCallback<List<Vehicle>>() {
            @Override
            public void onFailure(Call<List<Vehicle>> call, Throwable t) {
                super.onFailure(call, t);
                hideLoader();
                Toast.makeText(OrderActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                super.showErrorMessage(response);
                Toast.makeText(OrderActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                hideLoader();
            }

            @Override
            public void handleResponse(Response<List<Vehicle>> response) {
                hideLoader();
                List<Vehicle> vehicles = response.body();
                if (vehicles.size() > 0) {
                    for (Vehicle vehicle : vehicles) {
                        if (vehicle.isDefault()) {
                            setSelectedVehicle(vehicle);
                            break;
                        }
                    }
                }
            }
        });
    }

    /* END QUERIES */

    public ParkUrbnApi getParkUrbnApi() {
        return mParkUrbnApi;
    }

    private void setSelectedVehicle(Vehicle vehicle) {
        mVehicle = vehicle;
        if (mVehicle != null) {
            boolean hasNickname = mVehicle.getNickname() != null && !mVehicle.getNickname().isEmpty();
            tvVehicleTitle.setText(hasNickname ? mVehicle.getNickname() : mVehicle.getLicensePlateNumber());
            tvVehicleArrowText.setText(hasNickname ? mVehicle.getLicensePlateNumber() : "");
        } else {
            tvVehicleTitle.setText(getString(R.string.select_your_vehicle));
            tvVehicleArrowText.setText(getString(R.string.select));
        }
        btnPay.setBackgroundResource(mPickerManager.getDuration() != 0 && mVehicle != null ? R.drawable.bg_app_button : R.drawable.bg_app_button_disabled);
        btnPay.setTextColor(mPickerManager.getDuration() != 0 && mVehicle != null ? colorOrange : colorOrangeLight);
    }

    private void setMapBackground() {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(openFileInput(MapActivity.SCREENSHOT_NAME));
            ivMapScreenshot.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setPickState() {
        curState = STATE_ANIM;
        btnPay.setClickable(false);
        AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.view_flip_top_out);
        flipOut.setTarget(llBottomStatePay);
        AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.view_flip_top_in);
        flipIn.setTarget(llBottomStatePick);
        ObjectAnimator hideSelectVehicle = ObjectAnimator.ofFloat(llSelectVehicle, "alpha", 1f, 0f);
        ObjectAnimator hideSelectTimeArrow = ObjectAnimator.ofFloat(ivTimeArrow, "alpha", 1f, 0f);
        final AnimatorSet flipSet = new AnimatorSet();
        flipSet.playTogether(flipIn, flipOut);
        flipSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                llBottomStatePick.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                llBottomStatePay.setVisibility(View.INVISIBLE);
                llBottomStatePick.setElevation(Converter.dpToPx(6));
                curState = STATE_PICK;
                btnPay.setClickable(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        ValueAnimator marginAnim = ValueAnimator.ofInt(llSelectVehicle.getHeight(), 0);
        marginAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                llSelectVehicle.setVisibility(View.GONE);
                flipSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        final ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) llTimeAndPrice.getLayoutParams();
        marginAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                marginLayoutParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
                llTimeAndPrice.requestLayout();
            }
        });
        marginAnim.setDuration(ANIM_DURATION);
        marginAnim.start();
        hideSelectVehicle.setDuration(ANIM_DURATION).start();
        hideSelectTimeArrow.setDuration(ANIM_DURATION).start();
        llSelectedTime.animate().translationX(ivTimeArrow.getWidth() + Converter.dpToPx(16)).setDuration(ANIM_DURATION).start();
    }

    private void setPayState() {
        curState = STATE_ANIM;
        btnPay.setClickable(false);
        AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.view_flip_bottom_out);
        flipOut.setTarget(llBottomStatePick);
        AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.view_flip_bottom_in);
        flipIn.setTarget(llBottomStatePay);
        final ObjectAnimator showSelectVehicle = ObjectAnimator.ofFloat(llSelectVehicle, "alpha", 0f, 1f);
        final ObjectAnimator showSelectTimeArrow = ObjectAnimator.ofFloat(ivTimeArrow, "alpha", 0f, 1f);
        final ValueAnimator marginAnim = ValueAnimator.ofInt(0, llSelectVehicle.getHeight());
        marginAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                llSelectVehicle.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                curState = STATE_PAY;
                btnPay.setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        final ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) llTimeAndPrice.getLayoutParams();
        marginAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                marginLayoutParams.topMargin = (Integer) valueAnimator.getAnimatedValue();
                llTimeAndPrice.requestLayout();
            }
        });
        marginAnim.setDuration(ANIM_DURATION);
        AnimatorSet flipSet = new AnimatorSet();
        flipSet.playTogether(flipIn, flipOut);
        flipSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                llBottomStatePay.setVisibility(View.VISIBLE);
                llBottomStatePick.setElevation(0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                llBottomStatePick.setVisibility(View.INVISIBLE);
                marginAnim.start();
                showSelectVehicle.setDuration(ANIM_DURATION).start();
                showSelectTimeArrow.setDuration(ANIM_DURATION).start();
                llSelectedTime.animate().translationX(0).setDuration(ANIM_DURATION).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        flipSet.start();
    }

}