package com.cruxlab.parkurbn.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.cruxlab.parkurbn.DurationPickerManager;
import com.cruxlab.parkurbn.NotificationPublisher;
import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.SharedPrefsManager;
import com.cruxlab.parkurbn.api.ParkUrbnApi;
import com.cruxlab.parkurbn.api.ResponseCallback;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.consts.RequestCodes;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.model.request.PaymentRequest;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.cruxlab.parkurbn.model.response.PaymentResponse;
import com.cruxlab.parkurbn.model.response.TokenResponse;
import com.cruxlab.parkurbn.tools.Converter;
import com.cruxlab.parkurbn.tools.DialogUtils;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;
import com.wx.wheelview.widget.WheelView;

import java.util.Locale;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ParkingInfoActivity extends BaseActivity implements DurationPickerManager.DurationPickerCallback {

    @BindView(R.id.ts_title)
    TextSwitcher tsTitle;
    @BindView(R.id.v_timer_info_bg)
    View vTimerInfoBg;
    @BindView(R.id.tv_time_remaining)
    TextView tvTimeRemaining;
    @BindView(R.id.tv_refill_text)
    TextView tvRefillText;
    @BindView(R.id.tv_info_text)
    TextView tvInfoText;
    @BindView(R.id.ll_bottom_state_info)
    LinearLayout llBottomStateInfo;
    @BindView(R.id.ll_bottom_state_refill)
    LinearLayout llBottomStateRefill;
    @BindView(R.id.wv_hours)
    WheelView<String> wvHours;
    @BindView(R.id.wv_mins)
    WheelView<String> wvMins;
    @BindView(R.id.btn_pay)
    Button btnPay;
    @BindView(R.id.tv_ends_at_info)
    TextView tvEndsAtInfo;
    @BindView(R.id.tv_ends_at_refill)
    TextView tvEndsAtRefill;
    @BindView(R.id.tv_remind_before_time)
    TextView tvRemindBeforeTime;
    @BindView(R.id.rl_top_content)
    RelativeLayout rlTopContent;

    @BindColor(R.color.color_gray)
    int colorGray;
    @BindColor(R.color.bt_error_red)
    int colorRed;
    @BindString(R.string.parking_time_remaining)
    String sParkingTimeRemaining;
    @BindString(R.string.add_time_refill)
    String sAddTimeRefill;

    public static final int FROM_ORDER = 1001;
    public static final int FROM_FRAGMENT = 1002;
    public static final int FROM_NOTIFICATION = 1003;
    public static final int FROM_FRAGMENT_NOTIFICATION = 1004;
    private int from;

    private static final long TIMER_STEP = 1000L;
    private final static int STATE_ANIM = 0;
    private final static int STATE_INFO = 1;
    private final static int STATE_REFILL = 2;

    private ParkUrbnApi mParkUrbnApi;
    private DurationPickerManager mPickerManager;
    private AlertDialog mDurationPickerDialog;
    private CountDownTimer mCountDownTimer;
    private Spot mSpot;

    private long refillPriceCent;
    private boolean isRefill;
    private int curState = STATE_INFO;

    /* LIFECYCLE */

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_info);
        ButterKnife.bind(this);
        from = getIntent().getIntExtra(BundleArguments.FROM, FROM_ORDER);
        mSpot = SharedPrefsManager.get().getParkedSpot();
        mParkUrbnApi = ParkUrbnApplication.get().getParkUrbnApi();
        initTitle();
        initViews();
        ParkUrbnApplication.get().getLoadHistoryManager().loadHistory(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    /* END LIFECYCLE */
    /* EVENTS */

    @Override
    public void onBackPressed() {
        if (isRefill) {
            closeRefill();
            return;
        }
        switch (from) {
            case FROM_FRAGMENT_NOTIFICATION:
            case FROM_FRAGMENT:
                super.onBackPressed();
                break;
            case FROM_NOTIFICATION:
            case FROM_ORDER:
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public void onDurationPicked(int hours, int mins) {
        if (isRefill) {
            int timeMins = hours * 60 + mins;
            updateTimerLayout(Math.max(0, mSpot.getTimeRemainingSec() + timeMins * 60));
            initTimer(Math.max(0, mSpot.getTimeRemainingMillis() + timeMins * 60 * 1000L));
            int freeMins = Math.min(timeMins, mSpot.getFreeMinsRemaining());
            int paidMins = timeMins - freeMins;
            refillPriceCent = Math.round(paidMins * mSpot.getPricePerMin());
            btnPay.setText(paidMins == 0 ? getString(R.string.next) : "Pay " + Converter.getPriceStr(refillPriceCent / 100f));
        }
    }

    @Override
    protected void onNotificationReceived() {
        if (mSpot.getMaxParkingTimeMins() > 0) {
            DialogUtils.showParkingMeterExpiresSoonDialog(this, new DialogUtils.ConfirmDialogCallback() {
                @Override
                public void okButtonPressed() {
                    if (mSpot.getMaxParkingTimeMins() > 0) {
                        openRefill();
                    }
                }

                @Override
                public void cancelButtonPressed() {

                }
            });
        } else {
            DialogUtils.showParkingMeterExpiresSoonDialog(this, (DialogUtils.InfoDialogCallback) null);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.BRAINTREE_DROPIN) {
            if (resultCode == Activity.RESULT_OK) {
                showLoader();
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                mParkUrbnApi.pay(new PaymentRequest(refillPriceCent, result.getPaymentMethodNonce().getNonce(),
                        mSpot.getLicencePlateNumber(), mSpot.getId(), mSpot.getExpiration(), mSpot.getArrivedDate())).enqueue(new ResponseCallback<PaymentResponse>() {
                    @Override
                    protected void showErrorMessage(ErrorResponse response) {
                        super.showErrorMessage(response);
                        Toast.makeText(ParkingInfoActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                        hideLoader();
                    }

                    @Override
                    public void onFailure(Call<PaymentResponse> call, Throwable t) {
                        super.onFailure(call, t);
                        Toast.makeText(ParkingInfoActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        hideLoader();
                    }

                    @Override
                    public void handleResponse(Response<PaymentResponse> response) {
                        if (response.body().isSuccessful()) {
                            hideLoader();
                            updateExpiration(response.body().getExpiration());
                        } else {
                            hideLoader();
                            Toast.makeText(ParkingInfoActivity.this, response.body().errors.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (resultCode != Activity.RESULT_CANCELED) {
                Exception e = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (from == FROM_NOTIFICATION || from == FROM_FRAGMENT_NOTIFICATION) {
            if (mSpot.getMaxParkingTimeMins() > 0) {
                openRefill();
            }
            from = (from == FROM_NOTIFICATION) ? FROM_ORDER : FROM_FRAGMENT;
        }
    }

    /* END EVENTS */
    /* INITIALIZATION */

    private void initTitle() {
        tsTitle.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView view = new TextView(ParkingInfoActivity.this);
                view.setTextColor(Color.BLACK);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                view.setGravity(Gravity.CENTER_HORIZONTAL);
                return view;
            }
        });
        Animation inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        tsTitle.setInAnimation(inAnimation);
        tsTitle.setOutAnimation(outAnimation);
    }

    private void initViews() {
        int maxTimeMins = mSpot.getMaxParkingTimeMins();
        if (maxTimeMins <= 0) {
            tvRefillText.setVisibility(View.INVISIBLE);
        } else {
            mPickerManager = new DurationPickerManager(this, wvHours, wvMins, maxTimeMins, this);
        }
        updateTimerLayout(mSpot.getTimeRemainingSec());
        initTimer(Math.max(0, mSpot.getTimeRemainingMillis()));
    }

    private void initTimer(long timerMilliseconds) {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer(timerMilliseconds, TIMER_STEP) {

            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerLayout((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                updateTimerLayout(0);
            }
        }.start();
    }

    /* END INITIALIZATION */
    /* ON CLICK */

    @OnClick(R.id.btn_pay)
    void pay() {
        if (curState == STATE_REFILL) {
            if (refillPriceCent == 0) {
                updateExpiration(mSpot.getExpiration() + mPickerManager.getDuration());
            } else {
                showLoader();
                mParkUrbnApi.getClientToken().enqueue(new ResponseCallback<TokenResponse>() {
                    @Override
                    public void onFailure(Call<TokenResponse> call, final Throwable t) {
                        super.onFailure(call, t);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ParkingInfoActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
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
                                .setTotalPrice(String.valueOf(refillPriceCent / 100f))
                                .addLineItem(LineItem.newBuilder()
                                        .setCurrencyCode("USD")
                                        .setDescription("Description")
                                        .setQuantity(String.valueOf(refillPriceCent / 100f))
                                        .setUnitPrice(String.valueOf(refillPriceCent / 100f))
                                        .setTotalPrice(String.valueOf(refillPriceCent / 100f))
                                        .build())
                                .build();
                        DropInRequest dropInRequest = new DropInRequest()
                                .clientToken(response.body().getClientToken())
                                .androidPayCart(cart);
                        startActivityForResult(dropInRequest.getIntent(ParkingInfoActivity.this), RequestCodes.BRAINTREE_DROPIN);
                    }

                    @Override
                    protected void showErrorMessage(ErrorResponse response) {
                        hideLoader();
                    }
                });
            }
        }
    }

    @OnClick(R.id.btn_show_car)
    void showCar() {
        if (curState == STATE_INFO) {
            onBackPressed();
        }
    }

    @OnClick(R.id.btn_leave)
    void leftTheSpot() {
        if (curState == STATE_INFO) {
            DialogUtils.showLeaveParkingSpotDialog(this, new DialogUtils.ConfirmDialogCallback() {
                @Override
                public void okButtonPressed() {
                    Intent notificationIntent = new Intent(ParkingInfoActivity.this, NotificationPublisher.class);
                    notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_CANCEL, 1);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(ParkingInfoActivity.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, pendingIntent);
                    SharedPrefsManager.get().clearRemindBeforeTimeMins();
                    SharedPrefsManager.get().clearParkedSpot();
                    SharedPrefsManager.get().clearCapturedSpot();
                    SharedPrefsManager.get().clearCapturedSegment();
                    Intent intent = new Intent(ParkingInfoActivity.this, MapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @Override
                public void cancelButtonPressed() {

                }
            });
        }
    }

    @OnClick(R.id.btn_cancel)
    void closeRefill() {
        if (curState == STATE_REFILL) {
            isRefill = false;
            updateTimerLayout(mSpot.getTimeRemainingSec());
            initTimer(Math.max(0, mSpot.getTimeRemainingMillis()));
            setInfoState();
        }
    }

    @OnClick(R.id.tv_refill_text)
    void openRefill() {
        if (curState == STATE_INFO) {
            isRefill = true;
            setRefillState();
        }
    }

    @OnClick(R.id.rl_remind)
    void selectRemindBefore() {
        if (curState == STATE_INFO) {
            int timeRemMins = mSpot.getTimeRemainingMins();
            if (timeRemMins > 0) {
                showDurationPickerDialog(timeRemMins);
            }
        }
    }

    /* END ON CLICK */

    private void setRefillState() {
        curState = STATE_ANIM;
        AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.view_flip_top_out);
        flipOut.setTarget(llBottomStateInfo);
        AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.view_flip_top_in);
        flipIn.setTarget(llBottomStateRefill);
        ObjectAnimator hideRefillBtn = ObjectAnimator.ofFloat(tvRefillText, "alpha", 1f, 0f);
        ObjectAnimator hideTimerBg = ObjectAnimator.ofFloat(vTimerInfoBg, "alpha", 1f, 0f);
        ObjectAnimator hideInfoText = ObjectAnimator.ofFloat(tvInfoText, "alpha", 1f, 0f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(flipIn, flipOut, hideRefillBtn, hideInfoText, hideTimerBg);
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                llBottomStateRefill.setVisibility(View.VISIBLE);
                tsTitle.setText(sAddTimeRefill);
                onDurationPicked(mPickerManager.getDuration() / 60, mPickerManager.getDuration() % 60);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                llBottomStateInfo.setVisibility(View.INVISIBLE);
                tvRefillText.setVisibility(View.INVISIBLE);
                curState = STATE_REFILL;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSet.start();
    }

    private void setInfoState() {
        curState = STATE_ANIM;
        AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.view_flip_bottom_out);
        flipOut.setTarget(llBottomStateRefill);
        AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.view_flip_bottom_in);
        flipIn.setTarget(llBottomStateInfo);
        ObjectAnimator showRefillBtn = ObjectAnimator.ofFloat(tvRefillText, "alpha", 0f, 1f);
        ObjectAnimator showTimerBg = ObjectAnimator.ofFloat(vTimerInfoBg, "alpha", 0f, 1f);
        ObjectAnimator showInfoText = ObjectAnimator.ofFloat(tvInfoText, "alpha", 0f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(flipIn, flipOut, showRefillBtn, showInfoText, showTimerBg);
        animSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                llBottomStateInfo.setVisibility(View.VISIBLE);
                tsTitle.setText(sParkingTimeRemaining);
                if (mSpot.getMaxParkingTimeMins() > 0) {
                    tvRefillText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                llBottomStateRefill.setVisibility(View.INVISIBLE);
                curState = STATE_INFO;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSet.start();
    }

    private void scheduleNotification(Notification notification, long beforeMillis) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + mSpot.getTimeRemainingMillis() - beforeMillis, pendingIntent);
    }

    private Notification getNotification() {
        Intent resultIntent = new Intent(this, ParkingInfoActivity.class);
        resultIntent.putExtra(BundleArguments.FROM, FROM_NOTIFICATION);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.click_to_refill))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        return builder.build();
    }

    private void showDurationPickerDialog(int maxTimeMins) {
        if (mDurationPickerDialog != null) {
            mDurationPickerDialog.dismiss();
        }
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_duration_picker, null);
        final WheelView dialogWvHours = (WheelView) dialogView.findViewById(R.id.wv_hours);
        final WheelView dialogWvMins = (WheelView) dialogView.findViewById(R.id.wv_mins);
        new DurationPickerManager(this, dialogWvHours, dialogWvMins, maxTimeMins, null);
        mDurationPickerDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.remind_me_before))
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int hours = Integer.parseInt((String) dialogWvHours.getSelectionItem());
                        int mins = Integer.parseInt((String) dialogWvMins.getSelectionItem());
                        int remindBeforeTimeMins = hours * 60 + mins;
                        SharedPrefsManager.get().saveRemindBeforeTimeMins(remindBeforeTimeMins);
                        tvRemindBeforeTime.setText(Converter.minsToTime(remindBeforeTimeMins));
                        scheduleNotification(getNotification(), remindBeforeTimeMins * 60 * 1000);
                    }
                })
                .show();
    }

    private void updateExpiration(int newExpiration) {
        mSpot.setExpiration(newExpiration);
        int maxTimeMins = mSpot.getMaxParkingTimeMins();
        if (maxTimeMins <= 0) {
            tvRefillText.setVisibility(View.INVISIBLE);
        } else {
            mPickerManager.setMaxTime(maxTimeMins);
        }
        SharedPrefsManager.get().saveParkedSpot(mSpot);
        Intent notificationIntent = new Intent(ParkingInfoActivity.this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_CANCEL, 1);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ParkingInfoActivity.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, pendingIntent);
        int remindBeforeTimeMins = SharedPrefsManager.get().getRemindBeforeTimeMins();
        if (remindBeforeTimeMins != -1) {
            scheduleNotification(getNotification(), remindBeforeTimeMins * 60 * 1000);
        }
        closeRefill();
    }

    private void updateTimerLayout(int timeRemSec) {
        if (timeRemSec <= 0 && !isRefill) {
            tvRemindBeforeTime.setText(R.string.unavailable);
            tvInfoText.setVisibility(View.VISIBLE);
            tvInfoText.setTextColor(Color.BLACK);
            tvInfoText.setText(R.string.time_has_expired_please_refill);
            vTimerInfoBg.getBackground().setColorFilter(colorGray, PorterDuff.Mode.SRC_IN);
            tvTimeRemaining.setTextColor(colorGray);
            tvTimeRemaining.setText(R.string.time_00_00_00);
            String expiredAt = getString(R.string.time_expired_at, Converter.minsToTimeAMPM(mSpot.getClientExpiration()));
            tvEndsAtInfo.setText(expiredAt);
            tvEndsAtRefill.setText(expiredAt);
        } else {
            if ((timeRemSec / 60) < 1 && !isRefill) {
                tvRemindBeforeTime.setText(R.string.unavailable);
                tvInfoText.setVisibility(View.VISIBLE);
                tvInfoText.setTextColor(Color.WHITE);
                tvInfoText.setText(R.string.time_expires_soon);
                vTimerInfoBg.getBackground().setColorFilter(colorRed, PorterDuff.Mode.SRC_IN);
                tvTimeRemaining.setTextColor(colorRed);
            } else {
                int remindBeforeTimeMins = SharedPrefsManager.get().getRemindBeforeTimeMins();
                tvRemindBeforeTime.setText(remindBeforeTimeMins == -1 ? getString(R.string.select) : Converter.minsToTime(remindBeforeTimeMins));
                if (curState != STATE_ANIM) {
                    tvInfoText.setVisibility(View.INVISIBLE);
                    vTimerInfoBg.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
                }
                tvTimeRemaining.setTextColor(Color.BLACK);
            }
            tvTimeRemaining.setText(String.format(Locale.US, "%02d:%02d:%02d", timeRemSec / 3600, (timeRemSec / 60) % 60, timeRemSec % 60));
            String endsAt = getString(R.string.parking_spot_ends_at, Converter.minsToTimeAMPM(mSpot.getClientExpiration() + (isRefill ? mPickerManager.getDuration() : 0)));
            tvEndsAtInfo.setText(endsAt);
            tvEndsAtRefill.setText(endsAt);
        }
    }

}