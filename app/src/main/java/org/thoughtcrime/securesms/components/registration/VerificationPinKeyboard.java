package org.thoughtcrime.securesms.components.registration;


import android.content.Context;
import android.graphics.PorterDuff;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ViewUtil;
import org.thoughtcrime.securesms.util.concurrent.ListenableFuture;
import org.thoughtcrime.securesms.util.concurrent.SettableFuture;

import java.util.ArrayList;
import java.util.List;

public class VerificationPinKeyboard extends FrameLayout {

    private KeyboardView keyboardView;
    private ImageView successView;
    private View llProgressArea;
    private ImageView failureView;
    private ImageView lockedView;
    private CountDownTimer timer;
    private int progressStep = 0;

    private OnKeyPressListener listener;
    private List<AppCompatImageView> progressViews = new ArrayList<>();

    public VerificationPinKeyboard(@NonNull Context context) {
        super(context);
        initialize();
    }

    public VerificationPinKeyboard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public VerificationPinKeyboard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VerificationPinKeyboard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initializeProgressViews() {
        progressViews.clear();
        progressViews.add(findViewById(R.id.ivProgressPart1));
        progressViews.add(findViewById(R.id.ivProgressPart2));
        progressViews.add(findViewById(R.id.ivProgressPart3));
        progressViews.add(findViewById(R.id.ivProgressPart4));
        progressViews.add(findViewById(R.id.ivProgressPart5));
        progressViews.add(findViewById(R.id.ivProgressPart6));
        progressViews.add(findViewById(R.id.ivProgressPart7));
    }

    private void initialize() {
        inflate(getContext(), R.layout.verification_pin_keyboard_view, this);

        this.keyboardView = findViewById(R.id.keyboard_view);
        this.llProgressArea = findViewById(R.id.llProgressArea);
        this.successView = findViewById(R.id.success);
        this.failureView = findViewById(R.id.failure);
        this.lockedView = findViewById(R.id.locked);

        initializeProgressViews();

        keyboardView.setPreviewEnabled(false);
        keyboardView.setKeyboard(new Keyboard(getContext(), R.xml.pin_keyboard));
        keyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {
                if (listener != null) listener.onKeyPress(primaryCode);
            }

            @Override
            public void onRelease(int primaryCode) {
            }

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
            }

            @Override
            public void onText(CharSequence text) {
            }

            @Override
            public void swipeLeft() {
            }

            @Override
            public void swipeRight() {
            }

            @Override
            public void swipeDown() {
            }

            @Override
            public void swipeUp() {
            }
        });

        displayKeyboard();
    }

    public void setOnKeyPressListener(@Nullable OnKeyPressListener listener) {
        this.listener = listener;
    }

    public void displayKeyboard() {
        this.keyboardView.setVisibility(View.VISIBLE);
        hideProgress();
        this.successView.setVisibility(View.GONE);
        this.failureView.setVisibility(View.GONE);
        this.lockedView.setVisibility(View.GONE);
    }

    public void displayProgress() {
        this.keyboardView.setVisibility(View.INVISIBLE);
        showProgress();
        this.successView.setVisibility(View.GONE);
        this.failureView.setVisibility(View.GONE);
        this.lockedView.setVisibility(View.GONE);
    }

    private void clearProgressViewsBackgrounds() {
        for (AppCompatImageView view : progressViews) {
            view.setImageResource(R.drawable.ic_loading_inactive_part);
        }
    }

    private void hideProgress() {
        if (timer != null) {
            timer.cancel();
        }
        progressStep = 0;
        this.llProgressArea.setVisibility(View.GONE);
    }

    private void showProgress() {
        this.llProgressArea.setVisibility(View.VISIBLE);
        progressStep = 0;
        timer = new CountDownTimer(8 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (progressStep<progressViews.size()){
                    progressViews.get(progressStep).setImageResource(R.drawable.ic_loading_active_part);
                    progressStep++;
                }
            }

            @Override
            public void onFinish() {
                clearProgressViewsBackgrounds();
                timer.start();
            }
        };
        timer.start();
    }

    public ListenableFuture<Boolean> displaySuccess() {
        SettableFuture<Boolean> result = new SettableFuture<>();

        this.keyboardView.setVisibility(View.INVISIBLE);
        hideProgress();
        this.failureView.setVisibility(View.GONE);
        this.lockedView.setVisibility(View.GONE);

        // this.successView.getBackground().setColorFilter(getResources().getColor(R.color.green_500), PorterDuff.Mode.SRC_IN);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setInterpolator(new OvershootInterpolator());
        scaleAnimation.setDuration(800);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                result.set(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        ViewUtil.animateIn(this.successView, scaleAnimation);
        return result;
    }

    public ListenableFuture<Boolean> displayFailure() {
        SettableFuture<Boolean> result = new SettableFuture<>();

        this.keyboardView.setVisibility(View.INVISIBLE);
        hideProgress();
        this.failureView.setVisibility(View.GONE);
        this.lockedView.setVisibility(View.GONE);

//    this.failureView.getBackground().setColorFilter(getResources().getColor(R.color.red_500), PorterDuff.Mode.SRC_IN);
        this.failureView.setVisibility(View.VISIBLE);

        TranslateAnimation shake = new TranslateAnimation(0, 30, 0, 0);
        shake.setDuration(50);
        shake.setRepeatCount(7);
        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                result.set(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        this.failureView.startAnimation(shake);

        return result;
    }

    public ListenableFuture<Boolean> displayLocked() {
        SettableFuture<Boolean> result = new SettableFuture<>();

        this.keyboardView.setVisibility(View.INVISIBLE);
        hideProgress();
        this.failureView.setVisibility(View.GONE);
        this.lockedView.setVisibility(View.GONE);

        this.lockedView.getBackground().setColorFilter(getResources().getColor(R.color.green_500), PorterDuff.Mode.SRC_IN);

        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setInterpolator(new OvershootInterpolator());
        scaleAnimation.setDuration(800);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                result.set(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        ViewUtil.animateIn(this.lockedView, scaleAnimation);
        return result;
    }

    public interface OnKeyPressListener {
        void onKeyPress(int keyCode);
    }
}
