package org.thoughtcrime.securesms.components.registration;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.thoughtcrime.securesms.R;

import java.util.ArrayList;
import java.util.List;

public final class VerificationCodeView extends FrameLayout {

    private final List<TextView> codes = new ArrayList<>(6);

    private OnCodeEnteredListener listener;
    private int index;

    public VerificationCodeView(Context context) {
        super(context);
        initialize(context);
    }

    public VerificationCodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public VerificationCodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VerificationCodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initialize(@NonNull Context context) {
        inflate(context, R.layout.verification_code_view, this);

        codes.add(findViewById(R.id.code_zero));
        codes.add(findViewById(R.id.code_one));
        codes.add(findViewById(R.id.code_two));
        codes.add(findViewById(R.id.code_three));
        codes.add(findViewById(R.id.code_four));
        codes.add(findViewById(R.id.code_five));
    }

    @MainThread
    public void setOnCompleteListener(OnCodeEnteredListener listener) {
        this.listener = listener;
    }

    @MainThread
    public void append(int value) {
        if (index >= codes.size()) return;

        setActive(codes.get(index));

        codes.get(index++).setText(String.valueOf(value));

        if (index == codes.size() && listener != null) {
            listener.onCodeComplete(Stream.of(codes).map(TextView::getText).collect(Collectors.joining()));
        }
    }

    @MainThread
    public void delete() {
        if (index <= 0) return;
        codes.get(--index).setText("");
        setActive(codes.get(index));
        setInactive(codes.get(index));
    }

    @MainThread
    public void clear() {
        if (index != 0) {
            Stream.of(codes).forEach(code -> code.setText(""));
            index = 0;
        }
        setInactive(codes);
    }

    private static void setInactive(List<TextView> views) {
        Stream.of(views).forEach(c -> c.setEnabled(false));
    }

    private static void setActive(@NonNull TextView code) {
        code.setEnabled(true);
    }
    private static void setInactive(@NonNull TextView code) {
        code.setEnabled(false);
    }

    public interface OnCodeEnteredListener {
        void onCodeComplete(@NonNull String code);
    }
}
