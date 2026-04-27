package com.example.gossipapp.Activity;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;

public class TypeWriter {

    private TextView textView;
    private CharSequence text;
    private int index;
    private long delay = 150; // milliseconds per character
    private Handler handler = new Handler();
    private Vibrator vibrator;

    public TypeWriter(TextView textView, Context context) {
        this.textView = textView;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            textView.setText(text.subSequence(0, index++));

            // Haptic feedback for each character
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(20);
                }
            }

            if (index <= text.length()) {
                handler.postDelayed(this, delay);
            }
        }
    };

    public void animateText(CharSequence txt, long characterDelay) {
        text = txt;
        index = 0;
        delay = characterDelay;
        textView.setText("");
        handler.removeCallbacks(characterAdder);
        handler.post(characterAdder);
    }
}
