package com.cruxlab.parkurbn.custom;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.tools.Converter;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class SwipingIndicatorsLayout extends FrameLayout {

    private static final int SMALL = 0;
    private static final int MEDIUM = 1;
    private static final int LARGE = 2;
    private static final int DEFAULT_ANIMATION_TIME = 300;
    private static final int DEFAULT_SMALL_DP = 8;
    private static final int DEFAULT_MEDIUM_DP = 10;
    private static final int DEFAULT_LARGE_DP = 12;
    private static final int DEFAULT_SPACE_DP = 10;

    private float[] alphas = {0f, 0.5f, 0.75f, 1f, 0.75f, 0.5f, 0f};
    private int[] sizes = {SMALL, SMALL, MEDIUM, LARGE, MEDIUM, SMALL, SMALL};
    private int[] valuesPx = {Converter.dpToPx(DEFAULT_SMALL_DP), Converter.dpToPx(DEFAULT_MEDIUM_DP), Converter.dpToPx(DEFAULT_LARGE_DP)};
    private int[] leftMarginsPx, topMarginsPx;
    private ConcurrentLinkedDeque<ImageView> circles;
    private AtomicBoolean ready = new AtomicBoolean(true);
    private int animationTime = DEFAULT_ANIMATION_TIME;
    private int spacePx = Converter.dpToPx(DEFAULT_SPACE_DP);

    public SwipingIndicatorsLayout(Context context) {
        super(context);
        init();
    }

    public SwipingIndicatorsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipingIndicatorsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SwipingIndicatorsLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setAnimationTime(int animationTime) {
        this.animationTime = animationTime;
    }

    public void setSpaceDp(int dp) {
        spacePx = Converter.dpToPx(dp);
        calcMargins();
        drawCircles();
    }

    public void setSmallSizeDp(int dp) {
        valuesPx[SMALL] = Converter.dpToPx(dp);
        calcMargins();
        drawCircles();
    }

    public void setMediumSizeDp(int dp) {
        valuesPx[MEDIUM] = Converter.dpToPx(dp);
        calcMargins();
        drawCircles();
    }

    public void setLargeSizeDp(int dp) {
        valuesPx[LARGE] = Converter.dpToPx(dp);
        calcMargins();
        drawCircles();
    }

    public void setSizesDp(int smallDp, int mediumDp, int largeDp) {
        valuesPx[SMALL] = Converter.dpToPx(smallDp);
        valuesPx[MEDIUM] = Converter.dpToPx(mediumDp);
        valuesPx[LARGE] = Converter.dpToPx(largeDp);
        calcMargins();
        drawCircles();
    }

    private void init() {
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        circles = new ConcurrentLinkedDeque<>();
        leftMarginsPx = new int[7];
        topMarginsPx = new int[7];
        calcMargins();
        drawCircles();
    }

    private void drawCircles() {
        removeAllViews();
        circles.clear();
        for (int i = 0; i < 7; i++) circles.addLast(addCircle(sizes[i], i));
    }

    private void calcMargins() {
        leftMarginsPx[0] = spacePx;
        for (int i = 1; i < 7; i++) leftMarginsPx[i] = leftMarginsPx[i - 1] + spacePx + valuesPx[sizes[i - 1]];
        for (int i = 0; i < 7; i++) topMarginsPx[i] = (valuesPx[LARGE] - valuesPx[sizes[i]]) / 2;
    }

    private ImageView addLeft() {
        return addCircle(SMALL, 0);
    }

    private ImageView addRight() {
        return addCircle(SMALL, 6);
    }

    private void removeLeft() {
        removeView(circles.removeFirst());
    }

    private void removeRight() {
        removeView(circles.removeLast());
    }

    private ImageView addCircle(int type, int pos) {
        ImageView circle = getCircle(type);
        addView(circle);
        MarginLayoutParams marginParams = (MarginLayoutParams) circle.getLayoutParams();
        marginParams.leftMargin = leftMarginsPx[pos];
        marginParams.topMargin = topMarginsPx[pos];
        circle.setAlpha(alphas[pos]);
        return circle;
    }

    private ImageView getCircle(int type) {
        ImageView circle = new ImageView(getContext());
        circle.setLayoutParams(new ViewGroup.LayoutParams(valuesPx[type], valuesPx[type]));
        circle.setImageResource(R.drawable.indicator);
        return circle;
    }

    public void animateRight() {
        if (!ready.get()) return;
        ready.set(false);
        int i = 0;
        for (final ImageView circle : circles) {
            final int leftMargin = leftMarginsPx[i];
            final int topMargin = topMarginsPx[i];
            final int leftMarginRange = i < circles.size() - 1 ? leftMarginsPx[i + 1] - leftMarginsPx[i] : leftMarginsPx[i];
            final int topMarginRange = i < circles.size() - 1 ? topMarginsPx[i + 1] - topMarginsPx[i] : topMarginsPx[i];
            final float width = circle.getLayoutParams().width;
            final float sizeRange = i < circles.size() - 1 ? valuesPx[sizes[i + 1]] - valuesPx[sizes[i]] : 0f;
            final float height = circle.getLayoutParams().height;
            final float targetAlpha = i < circles.size() - 1 ? alphas[i + 1] : 0f;
            final int finalI = i;
            circle.animate().alpha(targetAlpha).setDuration(animationTime).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (finalI == circles.size() - 1) {
                        circles.addFirst(addLeft());
                        removeRight();
                    }
                    ready.set(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    circle.setAlpha(targetAlpha);
                    circle.getLayoutParams().width = (int) (width + sizeRange);
                    circle.getLayoutParams().height = (int) (height + sizeRange);
                    MarginLayoutParams marginParams = (MarginLayoutParams) circle.getLayoutParams();
                    marginParams.leftMargin = leftMargin + leftMarginRange;
                    marginParams.topMargin = topMargin + topMarginRange;
                    circle.requestLayout();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    circle.getLayoutParams().width = (int) (width + sizeRange * value);
                    circle.getLayoutParams().height = (int) (height + sizeRange * value);
                    MarginLayoutParams marginParams = (MarginLayoutParams) circle.getLayoutParams();
                    marginParams.leftMargin = ((int) (leftMargin + leftMarginRange * value));
                    marginParams.topMargin = ((int) (topMargin + topMarginRange * value));
                    circle.requestLayout();
                }
            });
            i++;
        }
    }

    public void animateLeft() {
        if (!ready.get()) return;
        ready.set(false);
        int i = 0;
        for (final ImageView circle : circles) {
            final int leftMargin = leftMarginsPx[i];
            final int topMargin = topMarginsPx[i];
            final int leftMarginRange = i > 0 ? leftMarginsPx[i - 1] - leftMarginsPx[i] : 0;
            final int topMarginRange = i > 0 ? topMarginsPx[i - 1] - topMarginsPx[i] : topMarginsPx[0];
            final float width = circle.getLayoutParams().width;
            final float height = circle.getLayoutParams().height;
            final float sizeRange = i > 0 ? valuesPx[sizes[i - 1]] - valuesPx[sizes[i]] : 0f;
            final float targetAlpha = i > 0 ? alphas[i - 1] : 0f;
            final int finalI = i;
            circle.animate().alpha(targetAlpha).setDuration(animationTime).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (finalI == 0) {
                        circles.addLast(addRight());
                        removeLeft();
                    }
                    ready.set(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    circle.setAlpha(targetAlpha);
                    circle.getLayoutParams().width = (int) (width + sizeRange);
                    circle.getLayoutParams().height = (int) (height + sizeRange);
                    MarginLayoutParams marginParams = (MarginLayoutParams) circle.getLayoutParams();
                    marginParams.leftMargin = leftMargin + leftMarginRange;
                    marginParams.topMargin = topMargin + topMarginRange;
                    circle.requestLayout();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    circle.getLayoutParams().width = (int) (width + sizeRange * value);
                    circle.getLayoutParams().height = (int) (height + sizeRange * value);
                    MarginLayoutParams marginParams = (MarginLayoutParams) circle.getLayoutParams();
                    marginParams.leftMargin = ((int) (leftMargin + leftMarginRange * value));
                    marginParams.topMargin = ((int) (topMargin + topMarginRange * value));
                    circle.requestLayout();
                }
            });
            i++;
        }
    }

}